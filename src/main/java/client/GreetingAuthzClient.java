package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import com.beust.jcommander.JCommander;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

/**
 * Created by starksm on 3/14/17.
 */
public class GreetingAuthzClient {
    private static final String SECURE_SSO = "secure-sso";
    private AuthzClient authzClient;
    private String token;
    private Map<String, String> endpoints = new HashMap<>();

    public static void main(String[] args) throws Exception {
        CommandArgs cmdArgs = new CommandArgs();
        new JCommander(cmdArgs, args);
        GreetingAuthzClient client = new GreetingAuthzClient();
        client.init(cmdArgs.user, cmdArgs.password);
        Greeting greeting = client.getGreeting(cmdArgs.app, cmdArgs.from);
        System.out.printf("%s\n", greeting);
    }

    private void init(String username, String password) throws IOException, InterruptedException {
        // Get the endpoints routes
        loadEndpoints();
        String authServerURL = endpoints.get(SECURE_SSO);
        if(authServerURL == null)
            throw new IllegalStateException("Failed to load %s from routes. Check 'oc status' for login expiration");
        System.out.printf("Using auth server URL: %s\n", authServerURL);

        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (configStream == null) {
            throw new RuntimeException("Could not find any keycloak.json file in classpath.");
        }
        // Set the sso.auth.server.url used in the keycloak.json file
        System.setProperty("sso.auth.server.url", authServerURL+"/auth");
        Configuration config = JsonSerialization.readValue(configStream, Configuration.class, true);
        // create a new instance based on the configuration defined in keycloak.json
        authzClient = AuthzClient.create(config);
        AccessTokenResponse tokenResponse = authzClient.obtainAccessToken(username, username);
        token = tokenResponse.getToken();
    }

    private void loadEndpoints() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("oc", "get", "routes");
        Process process = pb.start();
        int errCode = process.waitFor();
        System.out.println("Successful oc get routes: " + (errCode == 0 ? "No" : "Yes"));
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\s+");
                if(fields[0].equals("NAME"))
                    continue;
                String name = fields[0];
                String host = fields[1];
                if(name.equals(SECURE_SSO))
                    endpoints.put(name, "https://"+host);
                else
                    endpoints.put(name, "http://"+host);
            }
        }
    }

    private Greeting getGreeting(String endpoint, String name) {
        String endpointURL = endpoints.get(endpoint);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpointURL);
        // Provide the authorization information
        target.register((ClientRequestFilter) requestContext -> {
            //System.err.printf("SEND(%s)\n", requestContext.getUri());
            requestContext.getHeaders().add("Authorization", "Bearer "+token);
        });
        IGreeting greetingClient = ((ResteasyWebTarget)target).proxy(IGreeting.class);
        Greeting greeting = greetingClient.greeting(name);
        return greeting;
    }

}
