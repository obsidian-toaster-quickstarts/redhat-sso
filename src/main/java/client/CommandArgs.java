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

import com.beust.jcommander.Parameter;

/**
 * The command line arguments used by the client
 * @see GreetingAuthzClient
 */
public class CommandArgs {
    @Parameter(names = "--app", description = "The name of the rest endpoint to access")
    public String app;
    @Parameter(names = "--user", description = "The SSO Realm username to authenticate as")
    public String user = "alice";
    @Parameter(names = "--password", description = "The SSO Realm password to authenticate with")
    public String password = "password";
    @Parameter(names = "--from", description = "The name to pass to the greeting endpoint")
    public String from = "World";
    @Parameter(names = "--debug", description = "The debugging level, > 0 means more verbosity")
    public int debugLevel;
    @Parameter(names = "--displaySSOURL", description = "Obtain and display the RH SSO server auth URL and then exit")
    public boolean displaySSOAuthURL;
    @Parameter(names = "--outputCurlScript", description = "Generate a bash script to the given filename that performs the query using cURL")
    public String outputCurlScript;
}
