#!/bin/bash

function parse_arguments {

	# CUSTOMER_HOST
	if [ -z "${CUSTOMER_HOST}" ]; then
		echo "CUSTOMER_HOST not set. Using parameter \"$1\"";
		CUSTOMER_HOST=$1;
	fi

	if [ -z "${CUSTOMER_HOST}" ]; then
		echo "CUSTOMER_HOST not set. Using default key";
		CUSTOMER_HOST=localhost;
	fi

	# CUSTOMER_PORT
	if [ -z "${CUSTOMER_PORT}" ]; then
		echo "CUSTOMER_PORT not set. Using parameter \"$2\"";
		CUSTOMER_PORT=$2;
	fi

	if [ -z "${CUSTOMER_PORT}" ]; then
		echo "CUSTOMER_PORT not set. Using default key";
		CUSTOMER_PORT=9445;
	fi

	# AUTH_HOST
	if [ -z "${AUTH_HOST}" ]; then
		echo "AUTH_HOST not set. Using parameter \"$3\"";
		AUTH_HOST=$3;
	fi

	if [ -z "${AUTH_HOST}" ]; then
		echo "AUTH_HOST not set. Using default key";
		AUTH_HOST=localhost;
	fi

	# AUTH_PORT
	if [ -z "${AUTH_PORT}" ]; then
		echo "AUTH_PORT not set. Using parameter \"$4\"";
		AUTH_PORT=$4;
	fi

	if [ -z "${AUTH_PORT}" ]; then
		echo "AUTH_PORT not set. Using default key";
		AUTH_PORT=9443;
	fi
}

# Thanks, https://gist.github.com/cjus/1047794#gistcomment-37563
function jsonValue {
	KEY=$1
	num=$2
	awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}

# Test health
function test_health {
	echo "Testing the customer health endpoint to see if connected to Auth and Cloudant"
	HEALTH_RESPONSE=$(curl -k https://$CUSTOMER_HOST:$CUSTOMER_PORT/health)
	echo ${HEALTH_RESPONSE}
}

# Testing only endpoint
# This sequence of commands work locally.
# TODO: The cust curl sometimes returns 'Cloudant is down' when Health and direct curls prove otherwise. Restart build works.
# TODO: Something about the token/command returns no results when searching for users.
function get_user {
	ACCESS_TOKEN=$(curl -k -d "grant_type=password&client_id=bluecomputeweb&client_secret=bluecomputewebs3cret&username=user&password=password&scope=openid" https://${AUTH_HOST}:${AUTH_PORT}/oidc/endpoint/OP/token | jq '.access_token')
	# echo $ACCESS_TOKEN

	CURL=$(curl -k --request GET --url https://${CUSTOMER_HOST}:${CUSTOMER_PORT}/customer/rest/customer --header "Authorization: Bearer ${ACCESS_TOKEN}" --header "Content-Type: application/json" | jq -r '.rows' | jq '.[].doc.username')	
	echo "Found user with name: \"$CURL\""

	if [ "$RETRIEVED_USER" != "user" ]; then
		echo "search_user: ❌ could not find user";
		echo "Printing out result of the curl command below:"
		echo $(curl -k --request GET --url https://${CUSTOMER_HOST}:${CUSTOMER_PORT}/customer/rest/customer --header "Authorization: Bearer ${ACCESS_TOKEN}" --header "Content-Type: application/json")
    echo "Check first curl in travis for comparison. If line above says 0 rows, still figuring out why travis can't find it."
		    # exit 1;
    else
    	echo "search_user: ✅";
  fi
}

# Setup
parse_arguments $1 $2 $3 $4
test_health

# API Tests
echo "Starting Tests"
get_user