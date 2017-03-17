#!/usr/bin/env python

# A python script to

import argparse
import json
import subprocess
import ssl
import urllib
import urllib2

# Setup --user, --password, --app and --debug options
parser = argparse.ArgumentParser()
parser.add_argument("--app", type=str, help="The application endpoint name")
parser.add_argument("--user", type=str, help="The application username to use for authentication")
parser.add_argument("--password", type=str, help="The application password to use for authentication")
parser.add_argument("--debug", type=int, choices=[0, 1, 2], help="increase output verbosity")
args = parser.parse_args()

print "app={}, debug={}".format(args.app, args.debug)

APP=args.app
USER="alice" if args.user is None else args.user
PASSWORD="password" if args.password is None else args.password
CLIENT="demoapp"
SECRET="1daa57a2-b60e-468b-a3ac-25bd2dc2eadc"
REALM="master"
APP_URL=None
SSO_HOST=None

# We require the app endpoint name
if APP is None:
    print "You must specify the application endpoint name using: --app app-endpoint"
    parser.print_help()
    exit(1)

# Query for the deployed routes
cmd = ["oc", "get", "routes"]
oc = subprocess.Popen(cmd, stdout=subprocess.PIPE)
routeNames = []
for line in oc.stdout.readlines():
    routeInfo = line.split()
    #print "fields:%d, route: %s, host: %s" % (len(routeInfo), routeInfo[0], routeInfo[1])
    # Skip the header line
    if routeInfo[0] == "NAME":
        continue
    # Only add the non-sso endpoints
    if "sso" not in routeInfo[0]:
        routeNames.append(routeInfo[0])
    # Save the app host
    if routeInfo[0] == APP:
        APP_URL = "http://" + routeInfo[1]
    # Save the sso secure host
    if routeInfo[0] == "secure-sso":
        SSO_HOST = "https://" + routeInfo[1]

# Error if we did not find a matching application endpoint
if APP_URL == None:
    print "Failed to locate application endpoint for: %s" % (APP)
    print "Valid application endpoints: %s" % routeNames
    exit(2)

print "Using APP=%s" % APP
print "Using USER=%s" % USER
print "Using SSO_HOST=%s" % SSO_HOST
print "Using APP_URL=%s" % APP_URL

# Request the user's access token
values = {"grant_type": "password",
          "username": str(USER),
          "client_secret": str(SECRET),
          "password": str(PASSWORD),
          "client_id": str(CLIENT)}
data = urllib.urlencode(values)
tokenURL = "%s/auth/realms/%s/protocol/openid-connect/token" % (SSO_HOST, REALM)
req = urllib2.Request(tokenURL, data)
# This ignores validation of the server certificate
ignoreCert = ssl._create_unverified_context()
handler=urllib2.HTTPSHandler(debuglevel=args.debug, context=ignoreCert)
opener = urllib2.build_opener(handler)
try:
    response = opener.open(req)
except urllib2.HTTPError, e:
    print "Failure, %s" % e
    exit(3)

tokenJson = response.read()
token = json.loads(tokenJson)
# Display the access token if debug > 0
if args.debug > 0:
    headers = response.info()
    print 'HEADERS :'
    print '---------'
    print headers
    print "Access.token: %s" % token['access_token']

# Access the application endpoint using the access token
print ">>> Greeting"
appURL = "%s/greeting" % APP_URL
req = urllib2.Request(appURL)
req.add_header("Authorization", "Bearer %s" % token['access_token'])
handler=urllib2.HTTPHandler(debuglevel=args.debug)
opener = urllib2.build_opener(handler)
try:
    response = opener.open(req)
    headers = response.info()
    reply = response.read()
    if args.debug > 0:
        print 'HEADERS :'
        print '---------'
        print headers
    print reply
except urllib2.HTTPError, e:
    print "Failure, %s" % e
