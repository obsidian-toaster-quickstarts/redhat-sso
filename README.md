# Shared RH SSO Deployment
This repository contains the RH SSO deployment that is shared by the secured versions of the quick starts. 

## Realm Model
TODO: describe the demoapp, secured-swarm-endpoint, secured-vertx-endpoint, secured-springboot-endpoint clients, their
associated swarm-admin, vertx-admin, springboot-admin roles, and the role mappings for the admin and alice users.

# Prerequisites

To get started with these quickstarts you'll need the following prerequisites:

Name | Description | Version
--- | --- | ---
[java][1] | Java JDK | 8
[maven][2] | Apache Maven | 3.2.x
[oc][3] | OpenShift Client | v3.3.x
[git][4] | Git version management | 2.x

[1]: http://www.oracle.com/technetwork/java/javase/downloads/
[2]: https://maven.apache.org/download.cgi?Preferred=ftp://mirror.reverse.net/pub/apache/
[3]: https://docs.openshift.com/enterprise/3.2/cli_reference/get_started_cli.html
[4]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

In order to build and deploy this project, you must have an account on an OpenShift Online (OSO): https://console.dev-preview-int.openshift.com/ instance.

# OpenShift Online

1. Using OpenShift Online or Dedicated, log on to the OpenShift Server.

    ```bash
    oc login https://<OPENSHIFT_ADDRESS> --token=MYTOKEN
    ```

1. Create a new project on OpenShift if you don't already have one. You can check this via the `oc status` command.

    ```bash
    oc new-project some_project_name
    ```

1. Deploy the SSO server.

    ```
    mvn fabric8:deploy
    ```

# Deploy the Secured Endpoints

Now you can deploy one or more of the secured versions of the quick starts by cloning them and following the OSO deployment
instructions. Current secured quick starts include:

* [secured_rest_swarm](https://github.com/obsidian-toaster-quickstarts/secured_rest_swarm)
* [secured_rest-springboot](https://github.com/obsidian-toaster-quickstarts/secured_rest-springboot)
* [secured_rest-vertx](https://github.com/obsidian-toaster-quickstarts/secured_rest-vertx)

# Access the Secured Endpoints
TODO: describe using the ./scripts/token_req.sh to test access to the secured endpoints.

```bash
[redhat-sso 573]$ ./scripts/token_req.sh secured-springboot-rest
Using APP=secured-springboot-rest
SSH_HOST=https://secure-sso-sso.e8ca.engint.openshiftapps.com
using APP_URL=http://secured-springboot-rest-sso.e8ca.engint.openshiftapps.com
wget --no-check-certificate -O - --post-data=grant_type=password&username=alice&password=password&client_id=demoapp&client_secret=1daa57a2-b60e-468b-a3ac-25bd2dc2eadc https://secure-sso-sso.e8ca.engint.openshiftapps.com/auth/realms/master/protocol/openid-connect/token
--2017-03-12 21:25:50--  https://secure-sso-sso.e8ca.engint.openshiftapps.com/auth/realms/master/protocol/openid-connect/token
Resolving secure-sso-sso.e8ca.engint.openshiftapps.com... 52.20.43.181, 107.21.11.121
Connecting to secure-sso-sso.e8ca.engint.openshiftapps.com|52.20.43.181|:443... connected.
WARNING: cannot verify secure-sso-sso.e8ca.engint.openshiftapps.com's certificate, issued by 'CN=Bill,OU=CE,O=RH,L=SD,ST=CA,C=US':
  Self-signed certificate encountered.
    WARNING: certificate common name 'Bill' doesn't match requested host name 'secure-sso-sso.e8ca.engint.openshiftapps.com'.
HTTP request sent, awaiting response... 200 OK
Length: 4480 (4.4K) [application/json]
Saving to: 'STDOUT'

-                   100%[=====================>]   4.38K  --.-KB/s   in 0s     

2017-03-12 21:25:51 (610 MB/s) - written to stdout [4480/4480]

>>> TOKEN Received
eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0NjU0YTM2MS0zNGJjLTRkM2EtODc3Yi04OWZiMWVmNmQxYjUiLCJleHAiOjE0ODkzNzkyMTEsIm5iZiI6MCwiaWF0IjoxNDg5Mzc5MTUxLCJpc3MiOiJodHRwczovL3NlY3VyZS1zc28tc3NvLmU4Y2EuZW5naW50Lm9wZW5zaGlmdGFwcHMuY29tL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImRlbW9hcHAiLCJzdWIiOiJjMDE3NWNjYi0wODkyLTRiMzEtODI5Zi1kZGE4NzM4MTVmZTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJkZW1vYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjQ5ZmVlZjIxLWUzZDMtNDFhMC05YmQ5LTU5MTEzOGZjZGRmNyIsImNsaWVudF9zZXNzaW9uIjoiZjk3OTJlMWUtOWQ0Yi00MmY5LTgyYTMtNzZkN2Q2NGE3YTAyIiwiYWxsb3dlZC1vcmlnaW5zIjpbXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyZWF0ZS1yZWFsbSIsInZlcnR4LWFkbWluIiwiYWRtaW4iXX0sInJlc291cmNlX2FjY2VzcyI6eyJzZWN1cmVkLXN3YXJtLWVuZHBvaW50Ijp7InJvbGVzIjpbInN3YXJtLWFkbWluIl19LCJzZWN1cmVkLXZlcnR4LWVuZHBvaW50Ijp7InJvbGVzIjpbInZlcnR4LWFkbWluIl19LCJzZWN1cmVkLXNwcmluZ2Jvb3QtZW5kcG9pbnQiOnsicm9sZXMiOlsic3ByaW5nYm9vdC1hZG1pbiJdfSwibWFzdGVyLXJlYWxtIjp7InJvbGVzIjpbIm1hbmFnZS1ldmVudHMiLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInZpZXctZXZlbnRzIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJBbGljZSBJbkNoYWlucyIsInByZWZlcnJlZF91c2VybmFtZSI6ImFsaWNlIiwiZ2l2ZW5fbmFtZSI6IkFsaWNlIiwiZmFtaWx5X25hbWUiOiJJbkNoYWlucyIsImVtYWlsIjoiYWxpY2VAa2V5Y2xvYWsub3JnIn0.fManSb6ZlOqvZDWZHkM04_PdZD1H4Tp44iaZl--8jcYXm22V6xhFWbLJs7Gc0KSEJGW258QZst-sTQHU17lI438uXFxQFcHmwdE342igXYdtfOvYaek0iPmOAJmrBqGWgUWWo0eEmK8uOGr2jeZIByuUGYHfh2OMNHkszclvcaqJgZ9ryl8LTciJwRh4pZEceXofDAgaXxPv2FSgF3XyPvzDe2PHuj0VX2sAwtQpKwD3Tf5S7UUl1FB-I56rsraccBAH7JSgww24gQptFrARrYgVsm8qPjCvGkQA-Cg-j5X1WKflySgdcOyBiybjAqMGDYQER9viR22Is3OgiNtAIA
>>> Greeting
curl -kv http://secured-springboot-rest-sso.e8ca.engint.openshiftapps.com/greeting -H Authorization:Bearer eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0NjU0YTM2MS0zNGJjLTRkM2EtODc3Yi04OWZiMWVmNmQxYjUiLCJleHAiOjE0ODkzNzkyMTEsIm5iZiI6MCwiaWF0IjoxNDg5Mzc5MTUxLCJpc3MiOiJodHRwczovL3NlY3VyZS1zc28tc3NvLmU4Y2EuZW5naW50Lm9wZW5zaGlmdGFwcHMuY29tL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImRlbW9hcHAiLCJzdWIiOiJjMDE3NWNjYi0wODkyLTRiMzEtODI5Zi1kZGE4NzM4MTVmZTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJkZW1vYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjQ5ZmVlZjIxLWUzZDMtNDFhMC05YmQ5LTU5MTEzOGZjZGRmNyIsImNsaWVudF9zZXNzaW9uIjoiZjk3OTJlMWUtOWQ0Yi00MmY5LTgyYTMtNzZkN2Q2NGE3YTAyIiwiYWxsb3dlZC1vcmlnaW5zIjpbXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyZWF0ZS1yZWFsbSIsInZlcnR4LWFkbWluIiwiYWRtaW4iXX0sInJlc291cmNlX2FjY2VzcyI6eyJzZWN1cmVkLXN3YXJtLWVuZHBvaW50Ijp7InJvbGVzIjpbInN3YXJtLWFkbWluIl19LCJzZWN1cmVkLXZlcnR4LWVuZHBvaW50Ijp7InJvbGVzIjpbInZlcnR4LWFkbWluIl19LCJzZWN1cmVkLXNwcmluZ2Jvb3QtZW5kcG9pbnQiOnsicm9sZXMiOlsic3ByaW5nYm9vdC1hZG1pbiJdfSwibWFzdGVyLXJlYWxtIjp7InJvbGVzIjpbIm1hbmFnZS1ldmVudHMiLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInZpZXctZXZlbnRzIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJBbGljZSBJbkNoYWlucyIsInByZWZlcnJlZF91c2VybmFtZSI6ImFsaWNlIiwiZ2l2ZW5fbmFtZSI6IkFsaWNlIiwiZmFtaWx5X25hbWUiOiJJbkNoYWlucyIsImVtYWlsIjoiYWxpY2VAa2V5Y2xvYWsub3JnIn0.fManSb6ZlOqvZDWZHkM04_PdZD1H4Tp44iaZl--8jcYXm22V6xhFWbLJs7Gc0KSEJGW258QZst-sTQHU17lI438uXFxQFcHmwdE342igXYdtfOvYaek0iPmOAJmrBqGWgUWWo0eEmK8uOGr2jeZIByuUGYHfh2OMNHkszclvcaqJgZ9ryl8LTciJwRh4pZEceXofDAgaXxPv2FSgF3XyPvzDe2PHuj0VX2sAwtQpKwD3Tf5S7UUl1FB-I56rsraccBAH7JSgww24gQptFrARrYgVsm8qPjCvGkQA-Cg-j5X1WKflySgdcOyBiybjAqMGDYQER9viR22Is3OgiNtAIA
*   Trying 52.20.43.181...
* Connected to secured-springboot-rest-sso.e8ca.engint.openshiftapps.com (52.20.43.181) port 80 (#0)
> GET /greeting HTTP/1.1
> Host: secured-springboot-rest-sso.e8ca.engint.openshiftapps.com
> User-Agent: curl/7.43.0
> Accept: */*
> Authorization:Bearer eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0NjU0YTM2MS0zNGJjLTRkM2EtODc3Yi04OWZiMWVmNmQxYjUiLCJleHAiOjE0ODkzNzkyMTEsIm5iZiI6MCwiaWF0IjoxNDg5Mzc5MTUxLCJpc3MiOiJodHRwczovL3NlY3VyZS1zc28tc3NvLmU4Y2EuZW5naW50Lm9wZW5zaGlmdGFwcHMuY29tL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImRlbW9hcHAiLCJzdWIiOiJjMDE3NWNjYi0wODkyLTRiMzEtODI5Zi1kZGE4NzM4MTVmZTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJkZW1vYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjQ5ZmVlZjIxLWUzZDMtNDFhMC05YmQ5LTU5MTEzOGZjZGRmNyIsImNsaWVudF9zZXNzaW9uIjoiZjk3OTJlMWUtOWQ0Yi00MmY5LTgyYTMtNzZkN2Q2NGE3YTAyIiwiYWxsb3dlZC1vcmlnaW5zIjpbXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyZWF0ZS1yZWFsbSIsInZlcnR4LWFkbWluIiwiYWRtaW4iXX0sInJlc291cmNlX2FjY2VzcyI6eyJzZWN1cmVkLXN3YXJtLWVuZHBvaW50Ijp7InJvbGVzIjpbInN3YXJtLWFkbWluIl19LCJzZWN1cmVkLXZlcnR4LWVuZHBvaW50Ijp7InJvbGVzIjpbInZlcnR4LWFkbWluIl19LCJzZWN1cmVkLXNwcmluZ2Jvb3QtZW5kcG9pbnQiOnsicm9sZXMiOlsic3ByaW5nYm9vdC1hZG1pbiJdfSwibWFzdGVyLXJlYWxtIjp7InJvbGVzIjpbIm1hbmFnZS1ldmVudHMiLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInZpZXctZXZlbnRzIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJBbGljZSBJbkNoYWlucyIsInByZWZlcnJlZF91c2VybmFtZSI6ImFsaWNlIiwiZ2l2ZW5fbmFtZSI6IkFsaWNlIiwiZmFtaWx5X25hbWUiOiJJbkNoYWlucyIsImVtYWlsIjoiYWxpY2VAa2V5Y2xvYWsub3JnIn0.fManSb6ZlOqvZDWZHkM04_PdZD1H4Tp44iaZl--8jcYXm22V6xhFWbLJs7Gc0KSEJGW258QZst-sTQHU17lI438uXFxQFcHmwdE342igXYdtfOvYaek0iPmOAJmrBqGWgUWWo0eEmK8uOGr2jeZIByuUGYHfh2OMNHkszclvcaqJgZ9ryl8LTciJwRh4pZEceXofDAgaXxPv2FSgF3XyPvzDe2PHuj0VX2sAwtQpKwD3Tf5S7UUl1FB-I56rsraccBAH7JSgww24gQptFrARrYgVsm8qPjCvGkQA-Cg-j5X1WKflySgdcOyBiybjAqMGDYQER9viR22Is3OgiNtAIA
> 
< HTTP/1.1 200 
< Cache-Control: private
< Expires: Thu, 01 Jan 1970 00:00:00 UTC
< X-Application-Context: application
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Mon, 13 Mar 2017 04:25:51 GMT
< Set-Cookie: 65b4255df185535d64372d9bc21b7991=226ed2c83d4fb043af84eaf25ddd1147; path=/; HttpOnly
< 
* Connection #0 to host secured-springboot-rest-sso.e8ca.engint.openshiftapps.com left intact
{"id":2,"content":"Hello, World!"}

```