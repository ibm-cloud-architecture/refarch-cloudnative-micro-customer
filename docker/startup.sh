#!/bin/bash
set -e

# Set basic java options
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

