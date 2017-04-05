#!/bin/bash
set -e

# Set basic java options
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

# open the secrets
cloudant_username=`cat /opt/binding-refarch-cloudantdb/binding | jq '.username' | sed -e 's/"//g'`
cloudant_password=`cat /opt/binding-refarch-cloudantdb/binding | jq '.password' | sed -e 's/"//g'`
cloudant_host=`cat /opt/binding-refarch-cloudantdb/binding | jq '.host' | sed -e 's/"//g'`

JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.username=${cloudant_username} -Dspring.application.cloudant.password=${cloudant_password} -Dspring.application.cloudant.host=${cloudant_host}"

# disable eureka
JAVA_OPTS="${JAVA_OPTS} -Deureka.client.enabled=false -Deureka.client.registerWithEureka=false -Deureka.fetchRegistry=false"

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

