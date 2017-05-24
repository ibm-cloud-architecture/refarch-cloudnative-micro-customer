FROM openjdk:8-alpine
VOLUME /tmp
ADD app.jar app.jar
RUN sh -c 'touch /app.jar'

# Install prereqs
RUN apk --no-cache update \
 && apk add jq wget bash ca-certificates \
 && update-ca-certificates

# Copy agent files
COPY agents/ /agents/

# Add the new relic agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip -O /tmp/newrelic.zip \
  && unzip -d /agents /tmp/newrelic.zip newrelic/newrelic.jar \
  && rm /tmp/newrelic.zip

COPY startup.sh startup.sh
EXPOSE 8080
ENTRYPOINT ["./startup.sh"]
