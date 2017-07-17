#!/bin/bash
set -e

# find the java heap size as 50% of container memory using sysfs, or 512m whichever is less
max_heap=`echo "512 * 1024 * 1024" | bc`
if [ -r "/sys/fs/cgroup/memory/memory.limit_in_bytes" ]; then
    mem_limit=`cat /sys/fs/cgroup/memory/memory.limit_in_bytes`
    if [ ${mem_limit} -lt ${max_heap} ]; then
        max_heap=${mem_limit}
    fi
fi
max_heap=`echo "(${max_heap} / 1024 / 1024) / 2" | bc`
export JAVA_OPTS="${JAVA_OPTS} -Xmx${max_heap}m"

# Set basic java options
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

# open the secrets
if [ ! -z "${HS256_KEY}" ]; then
  hs256_key=${HS256_KEY}
else
  hs256_key=`cat /var/run/secrets/hs256-key/key`
fi

if [ -z "${couchdb}" ]; then
    echo "Error: couchdb is not defined in environment!"
    exit 1
fi

cloudant_username=`echo ${couchdb} | base64 -d | jq -r '.username'`
cloudant_password=`echo ${couchdb} | base64 -d | jq -r '.password'`
cloudant_host=`echo ${couchdb} | base64 -d | jq -r  '.host'`
cloudant_port=`echo ${couchdb} | base64 -d | jq -r '.port'`
cloudant_proto=`echo ${couchdb} | base64 -d | jq -r '.url' | sed -e 's|://.*||'`

JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.username=${cloudant_username} -Dspring.application.cloudant.password=${cloudant_password} -Dspring.application.cloudant.host=${cloudant_host} -Dspring.application.cloudant.port=${cloudant_port} -Dspring.application.cloudant.protocol=${cloudant_proto}"
JAVA_OPTS="${JAVA_OPTS} -Djwt.sharedSecret=${hs256_key}"

# disable eureka
JAVA_OPTS="${JAVA_OPTS} -Deureka.client.enabled=false -Deureka.client.registerWithEureka=false -Deureka.fetchRegistry=false"

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

