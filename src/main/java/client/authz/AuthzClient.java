/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package client.authz;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import client.LoggingFilter;
import client.json.JsonSerialization;

/**
 * A partial facade for the org.keycloak.authorization.client.AuthzClient
 */
public class AuthzClient {
    private Configuration deployment;

    public AuthzClient(Configuration deployment) {
        this.deployment = deployment;
    }

    public String obtainAccessToken(String username, String password) {
        Form form = new Form();
        form.param("grant_type", "password");
        form.param("username", username);
        form.param("password", password);
        form.param("client_id", deployment.getClientId());
        String secret = deployment.getClientCredentials().get("secret").toString();
        form.param("client_secret", secret);
        Client client = null;
        try {
            ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            SSLContext sslcontext = deployment.getSSLContext();
            if(sslcontext != null) {
                client = clientBuilder.sslContext(sslcontext).hostnameVerifier(new AnyHostnameVerifier()).build();
            } else {
                client = clientBuilder.build();
            }

            String tokenURL = String.format("%s/auth/realms/%s/protocol/openid-connect/token",
                    deployment.getAuthServerUrl(), deployment.getRealm());
            WebTarget target = client.target(tokenURL);
            if(deployment.getDebug() > 0)
                target.register(new LoggingFilter());
            String json = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
            AccessToken accessToken = JsonSerialization.readValue(json, AccessToken.class);
            return accessToken.getToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to request token", e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public static AuthzClient create(Configuration configuration) {
        return new AuthzClient(configuration);
    }

    private static class AnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
