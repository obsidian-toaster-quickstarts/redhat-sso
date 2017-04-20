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

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by starksm on 3/15/17.
 */
public class Configuration {

    @JsonProperty("auth-server-url")
    protected String authServerUrl;

    @JsonProperty("no-check-certificate")
    protected Boolean noCertCheck;

    @JsonProperty("realm")
    protected String realm;

    @JsonProperty("resource")
    protected String clientId;

    @JsonProperty("credentials")
    protected Map<String, Object> clientCredentials = new HashMap<>();

    @JsonProperty("debug")
    protected int debug;

    public Configuration() {

    }

    public Boolean getNoCertCheck() {
        return noCertCheck;
    }

    /**
     * If this is a https authServerUrl and noCertCheck is true, create an SSLContext that uses
     * an X509TrustManager that allows any certificate.
     * @return SSLContext with all trusting TrustManager if noCertCheck is true, null otherwise
     */
    public SSLContext getSSLContext() {
        SSLContext sslContext = null;
        if(authServerUrl.startsWith("https") && noCertCheck) {
            try {
                // Install a TrustManager that ignores certificate checks
                sslContext = SSLContext.getInstance("TLS");
                TrustManager[] trustManagers = {new TrustAllManager()};
                sslContext.init(null, trustManagers, new SecureRandom());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create HttpsClient", e);
            }
        }

        return sslContext;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public Map<String, Object> getClientCredentials() {
        return clientCredentials;
    }

    public String getRealm() {
        return realm;
    }

    public int getDebug() {
        return debug;
    }
    public void setDebug(int debug) {
        this.debug = debug;
    }

    private static class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            //ignore
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            //ignore
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
