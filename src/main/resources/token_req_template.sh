#!/bin/bash
# A script template for performing the query via cURL
if [ "$#" -lt 1 ]
then
     echo "Usage: $0 ENDPOINT_NAME"
     exit
fi

REALM=master
USER=alice
PASSWORD=password
CLIENT_ID=demoapp
SECRET=1daa57a2-b60e-468b-a3ac-25bd2dc2eadc
APP=$1

echo "Using APP=${APP}"

declare -a routes
routes=($(oc get routes))
count=${#routes[@]}

# Read in the oc get routes information to determine SSO_HOST and endpoint routes
while read -r line
do
    #echo "$line"
    routeInfo=($line[@])
    #echo "  + ${routeInfo[0]}"
    if [ "${routeInfo[0]}" == "secure-sso" ]
    then
      SSO_HOST="https://${routeInfo[1]}"
      echo "SSH_HOST=${SSO_HOST}"
    fi
    if [ "${routeInfo[0]}" == "${APP}" ]
    then
      APP_URL="http://${routeInfo[1]}"
      echo "using APP_URL=${APP_URL}"
    fi
done < <(oc get routes)

GET="curl -sk -X POST $SSO_HOST/auth/realms/$REALM/protocol/openid-connect/token -d grant_type=password -d username=$USER -d client_secret=$SECRET -d password=$PASSWORD -d client_id=$CLIENT_ID"
#GET="wget --no-check-certificate -O - --post-data=grant_type=password&username=${USER}&password=${PASSWORD}&client_id=${CLIENT_ID}&client_secret=${SECRET} ${SSO_HOST}/auth/realms/${REALM}/protocol/openid-connect/token"
#echo ">>> HTTP Token query"
echo $GET

auth_result=$($GET)
access_token=$(echo -e "$auth_result" | awk -F"," '{print $1}' | awk -F":" '{print $2}' | sed s/\"//g | tr -d ' ')

echo "<<< TOKEN Received"
echo $access_token

echo ">>> Greeting"
curl -kv $APP_URL/greeting -H "Authorization:Bearer $access_token"
