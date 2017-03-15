package client;

import com.beust.jcommander.Parameter;

/**
 * Created by starksm on 3/14/17.
 */
public class CommandArgs {
    @Parameter(names = "--user", description = "The SSO Realm username to authenticate as")
    public String user = "alice";
    @Parameter(names = "--password", description = "The SSO Realm password to authenticate with")
    public String password = "password";
    @Parameter(names = "--app", description = "The name of the rest endpoint to access")
    public String app;
    @Parameter(names = "--from", description = "The name to pass to the greeting endpoint")
    public String from = "World";
}
