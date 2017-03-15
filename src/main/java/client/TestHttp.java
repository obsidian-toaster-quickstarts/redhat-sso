package client;

import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.util.JsonSerialization;

public class TestHttp {
    private static class HttpsTrustManager implements X509TrustManager {

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

    public static void main(String[] args) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new HttpsTrustManager[]{new HttpsTrustManager()}, null);

        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (configStream == null) {
            throw new RuntimeException("Could not find any keycloak.json file in classpath.");
        }
        // Set the sso.auth.server.url used in the keycloak.json file
        System.setProperty("sso.auth.server.url", "https://secure-sso-sso.e8ca.engint.openshiftapps.com/auth");
        Configuration configuration = JsonSerialization.readValue(configStream, Configuration.class, true);
        Http http = new Http(configuration);
        String configurationUrl = configuration.getAuthServerUrl();

        if (configurationUrl == null) {
            throw new IllegalArgumentException("Configuration URL can not be null.");
        }

        // https://secure-sso-sso.e8ca.engint.openshiftapps.com/realms/master/.well-known/openid-configuration
        // https://secure-sso-sso.e8ca.engint.openshiftapps.com/auth/realms/master/.well-known/openid-configuration
        // https://secure-sso-sso.e8ca.engint.openshiftapps.com/auth/realms/master/.well-known/openid-configuration
        configurationUrl += "/realms/" + configuration.getRealm() + "/.well-known/uma-configuration";

        try {
            ServerConfiguration json = http.<ServerConfiguration>get(URI.create(configurationUrl))
                    .response().json(ServerConfiguration.class)
                    .execute();
            System.out.printf("json=%s\n", json);
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain configuration from server [" + configurationUrl + "].", e);
        }
    }
}
