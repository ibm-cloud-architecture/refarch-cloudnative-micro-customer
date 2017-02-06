FROM openjdk:8
VOLUME /tmp
ADD app.jar app.jar
RUN bash -c 'touch /app.jar'

# IBM vulnerability advisor policies
RUN sed -i -e '/^PASS_MIN_DAYS/ s/0/1/' /etc/login.defs
RUN sed -i -e '/^PASS_MAX_DAYS/ s/99999/90/' /etc/login.defs
RUN sed -i -e '/^password.*pam_unix.so/ s/$/ minlen=8/' /etc/pam.d/common-password

# Install prereqs
RUN apt-get update && apt-get install -y jq

# Copy agent files
COPY agents/ /agents/

# Add the new relic agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip -O /tmp/newrelic.zip \
  && unzip -u -d /agents /tmp/newrelic.zip newrelic/newrelic.jar \
  && rm /tmp/newrelic.zip

COPY startup.sh startup.sh
EXPOSE 8080
ENTRYPOINT ["./startup.sh"]
