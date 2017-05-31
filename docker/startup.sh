#!/bin/bash
set -e

# find the java heap size as 50% of container memory using sysfs
max_heap=`cat /sys/fs/cgroup/memory/memory.limit_in_bytes | xargs -I{} echo "({} / 1024 / 1024) / 2" | bc`
export JAVA_OPTS="${JAVA_OPTS} -Xmx${max_heap}m"

# Set basic java options
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

# open the secrets
hs256_key=`cat /var/run/secrets/hs256-key/key`
cloudant_username=`cat /var/run/secrets/binding-refarch-cloudantdb/binding | jq '.username' | sed -e 's/"//g'`
cloudant_password=`cat /var/run/secrets/binding-refarch-cloudantdb/binding | jq '.password' | sed -e 's/"//g'`
cloudant_host=`cat /var/run/secrets/binding-refarch-cloudantdb/binding | jq '.host' | sed -e 's/"//g'`
cloudant_port=`cat /var/run/secrets/binding-refarch-cloudantdb/binding | jq '.port' | sed -e 's/"//g'`
cloudant_proto=`cat /var/run/secrets/binding-refarch-cloudantdb/binding | jq '.url' | sed -e 's/"//g' | sed -e 's|://.*||'`

JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.username=${cloudant_username} -Dspring.application.cloudant.password=${cloudant_password} -Dspring.application.cloudant.host=${cloudant_host} -Dspring.application.cloudant.port=${cloudant_port} -Dspring.application.cloudant.protocol=${cloudant_proto}"
JAVA_OPTS="${JAVA_OPTS} -Djwt.sharedSecret=${hs256_key}"

# disable eureka
JAVA_OPTS="${JAVA_OPTS} -Deureka.client.enabled=false -Deureka.client.registerWithEureka=false -Deureka.fetchRegistry=false"

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

