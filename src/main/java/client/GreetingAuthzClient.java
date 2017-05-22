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
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import client.authz.AuthzClient;
import client.authz.Configuration;
import client.json.JsonSerialization;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * The JAX-RS client example that illustrates obtaining a bearer token using a
 */
public class GreetingAuthzClient {
    private static final String SECURE_SSO = "secure-sso";
    private AuthzClient authzClient;
    private String token;
    private Map<String, String> endpoints = new HashMap<>();
    private CommandArgs cmdArgs;

    public static void main(String[] args) throws Exception {
        CommandArgs cmdArgs = new CommandArgs();
        JCommander cmdParser = new JCommander(cmdArgs);
        try {
            cmdParser.setProgramName(GreetingAuthzClient.class.getName());
            cmdParser.parse(args);
        } catch (ParameterException e) {
            StringBuilder info = new StringBuilder("Specifiy the name of the application endpoint with --app\n");
            cmdParser.usage(info);
            System.err.printf(info.toString());
            System.exit(1);
        }
        // Handle outputCurlScript
        if(cmdArgs.outputCurlScript != null) {
            InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("token_req_template.sh");
            Path path = FileSystems.getDefault().getPath(cmdArgs.outputCurlScript);
            Files.copy(scriptStream, path, StandardCopyOption.REPLACE_EXISTING);
            scriptStream.close();
            System.out.printf("Wrote script to: %s\n", path.toFile().getAbsolutePath());
            System.exit(0);
        }

        GreetingAuthzClient client = new GreetingAuthzClient();
        client.init(cmdArgs);
        System.out.printf("\nRequesting greeting...\n");
        Greeting greeting = client.getGreeting(cmdArgs.app, cmdArgs.from);
        System.out.printf("%s\n", greeting);
    }

    /**
     * Loads the current service endpoint routes from openshift, and obtains the access token for
     * making a request to the secured endpoint.
     *
     * @param cmdArgs
     * @throws IOException
     * @throws InterruptedException
     */
    private void init(CommandArgs cmdArgs) throws IOException, InterruptedException {
        this.cmdArgs = cmdArgs;
        // Get the endpoints routes
        loadEndpoints();
        String authServerURL = endpoints.get(SECURE_SSO);
        if(authServerURL == null) {
            String msg = String.format("Failed to load %s from routes. Check 'oc status' for login expiration", SECURE_SSO);
            throw new IllegalStateException(msg);
        }
        System.out.printf("Using auth server URL: %s/auth\n", authServerURL);
        System.out.printf("Available application endpoint names: %s\n", endpoints.keySet().stream().filter(name -> !name.contains("sso")).collect(Collectors.toList()));
        if(cmdArgs.displaySSOAuthURL) {
            // We are done
            System.exit(0);
        }

        // Validate the app
        if(!endpoints.containsKey(cmdArgs.app)) {
            System.out.flush();
            System.err.printf("No application endpoints match: %s\nSee available applications above\n", cmdArgs.app);
            System.exit(1);
        }

        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (configStream == null) {
            throw new RuntimeException("Could not find any keycloak.json file in classpath.");
        }
        // Set the sso.auth.server.url used in the keycloak.json file
        System.setProperty("sso.auth.server.url", authServerURL);
        Configuration config = JsonSerialization.readValue(configStream, Configuration.class, true);
        config.setDebug(cmdArgs.debugLevel);
        // create a new instance based on the configuration defined in keycloak.json
        authzClient = AuthzClient.create(config);
        token = authzClient.obtainAccessToken(cmdArgs.user, cmdArgs.password);
        if(cmdArgs.debugLevel > 0)
            System.out.printf("\nToken: %s\n", token);
    }

    private void loadEndpoints() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("oc", "get", "routes");
        Process process = pb.start();
        int errCode = process.waitFor();
        System.out.println("Successful oc get routes: " + (errCode == 0 ? "Yes" : "No"));
        if(errCode != 0) {
            StringBuilder errorMsg = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line);
                }
            }
            throw new IOException(String.format("oc get routes failed, errno=%d, msg=%s", errCode, errorMsg.toString()));
        }
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
        try {
            WebTarget target = client.target(endpointURL);
            // Provide the authorization information
            target.register((ClientRequestFilter) requestContext -> {
				requestContext.getHeaders().add("Authorization", "Bearer "+token);
			});
            if(cmdArgs.debugLevel > 0)
				target.register(new LoggingFilter());
            IGreeting greetingClient = ((ResteasyWebTarget)target).proxy(IGreeting.class);
            Greeting greeting = greetingClient.greeting(name);
            return greeting;
        } finally {
            client.close();
        }
    }

}
