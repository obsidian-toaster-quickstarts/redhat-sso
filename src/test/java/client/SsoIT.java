package client;

import client.authz.AuthzClient;
import client.authz.Configuration;
import client.json.JsonSerialization;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SsoIT {
    private static AuthzClient authzClient;
    private static List<String> applicationUrls;

    @BeforeClass
    public static void setup() throws IOException {
        OpenShiftClient oc = new DefaultOpenShiftClient();
        List<Route> routes = oc.routes().inNamespace(oc.getNamespace()).list().getItems();

        String ssoAuthUrl = routes.stream()
                .filter(r -> "secure-sso".equals(r.getMetadata().getName()))
                .findFirst()
                .map(r -> "https://" + r.getSpec().getHost())
                .orElseThrow(() -> new IllegalStateException("Couldn't find secure-sso route"));

        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (configStream == null) {
            throw new IllegalStateException("Could not find any keycloak.json file in classpath.");
        }
        System.setProperty("sso.auth.server.url", ssoAuthUrl);
        Configuration config = JsonSerialization.readValue(configStream, Configuration.class, true);
        authzClient = AuthzClient.create(config);

        applicationUrls = routes.stream()
                .filter(r -> r.getMetadata().getName().contains("secured"))
                .map(r -> "http://" + r.getSpec().getHost())
                .collect(toList());

    }

    private static Greeting getGreeting(String url, String token, String from) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(url);
            target.register((ClientRequestFilter) requestContext -> {
                requestContext.getHeaders().add("Authorization", "Bearer " + token);
            });
            IGreeting greetingClient = ((ResteasyWebTarget) target).proxy(IGreeting.class);
            return greetingClient.greeting(from);
        } finally {
            client.close();
        }
    }

    @Test
    public void defaultUser_defaultFrom() {
        String token = authzClient.obtainAccessToken("alice", "password");
        for (String url : applicationUrls) {
            Greeting greeting = getGreeting(url, token, null);

            assertThat(greeting).isNotNull();
            assertThat(greeting.getContent()).contains("Hello, World!");
        }
    }

    @Test
    public void defaultUser_customFrom() {
        String token = authzClient.obtainAccessToken("alice", "password");
        for (String url : applicationUrls) {
            Greeting greeting = getGreeting(url, token, "Scott");

            assertThat(greeting).isNotNull();
            assertThat(greeting.getContent()).contains("Hello, Scott!");
        }
    }

    @Test
    public void adminUser() {
        String token = authzClient.obtainAccessToken("admin", "admin");
        for (String url : applicationUrls) {
            try {
                getGreeting(url, token, null);
                fail("ForbiddenException expected");
            } catch (ForbiddenException e) {
                // expected
            }
        }
    }

    @Test
    public void badPassword() {
        try {
            authzClient.obtainAccessToken("alice", "bad");
            fail("NotAuthorizedException expected");
        } catch (RuntimeException e) {
            // it's the AuthzClient in this project who wraps it into a RuntimeException
            if (e.getCause() instanceof NotAuthorizedException) {
                // expected
            } else {
                throw e;
            }
        }
    }
}
