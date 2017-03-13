REALM=master
USER=alice
PASSWORD=password
CLIENT_ID=demoapp
SECRET=1daa57a2-b60e-468b-a3ac-25bd2dc2eadc
#SSO_HOST=${1:-https://secure-sso-obsidian.e8ca.engint.openshiftapps.com}
APP=$1
echo "Using APP=${APP}"

declare -a routes
routes=($(oc get routes))
count=${#routes[@]}

x=0
while read -r line
do
    #echo "${x}:$line"
    let x=$x+1
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


function jsonValue() {
  KEY=$1
  num=$2
  awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}
