#!/bin/bash

SCRIPT_DIR="$(dirname "$0")"

. $SCRIPT_DIR/common.sh $*

GET="curl -sk -X POST $SSO_HOST/auth/realms/$REALM/protocol/openid-connect/token -d grant_type=password -d username=$USER -d client_secret=$SECRET -d password=$PASSWORD -d client_id=$CLIENT_ID"
GET="wget --no-check-certificate -O - --post-data=grant_type=password&username=${USER}&password=${PASSWORD}&client_id=${CLIENT_ID}&client_secret=${SECRET} ${SSO_HOST}/auth/realms/${REALM}/protocol/openid-connect/token"
#echo ">>> HTTP Token query"
echo $GET

auth_result=$($GET)
access_token=$(echo -e "$auth_result" | awk -F"," '{print $1}' | awk -F":" '{print $2}' | sed s/\"//g | tr -d ' ')

echo ">>> TOKEN Received"
echo $access_token

echo ">>> Greeting"
echo curl -kv $APP_URL/greeting -H "Authorization:Bearer $access_token"
curl -kv $APP_URL/greeting -H "Authorization:Bearer $access_token"

