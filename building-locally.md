# Run Customer Service locally

## Table of Contents

* [Building the app](#building-the-app)
* [Setting up Cloudant](#setting-up-cloudant)
* [Setting up Zipkin](#setting-up-zipkin) (Optional)

## Building the app

To build the application, we used maven build. Maven is a project management tool that is based on the Project Object Model (POM). Typically, people use Maven for project builds, dependencies, and documentation. Maven simplifies the project build. In this task, you use Maven to build the project.

1. Clone this repository.

   `git clone https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git`
   
   `cd refarch-cloudnative-micro-customer/`

2. Checkout MicroProfile branch.

   `git checkout microprofile`

3. Run this command. This command builds the project and installs it.

   `mvn install`
   
   If this runs successfully, you will be able to see the below messages.

```
[INFO] --- maven-failsafe-plugin:2.18.1:verify (verify-results) @ customer ---
[INFO] Failsafe report directory: /Users/user@ibm.com/Desktop/BlueCompute/refarch-cloudnative-micro-customer/target/test-reports/it
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ customer ---
[INFO] Installing /Users/user@ibm.com/Desktop/BlueCompute/refarch-cloudnative-micro-customer/target/customer-1.0-SNAPSHOT.war to /Users/user@ibm.com/.m2/repository/projects/customer/1.0-SNAPSHOT/customer-1.0-SNAPSHOT.war
[INFO] Installing /Users/user@ibm.com/Desktop/BlueCompute/refarch-cloudnative-micro-customer/pom.xml to /Users/user@ibm.com/.m2/repository/projects/customer/1.0-SNAPSHOT/customer-1.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15.788 s
[INFO] Finished at: 2018-07-23T16:38:19-05:00
[INFO] Final Memory: 26M/266M
[INFO] ------------------------------------------------------------------------
```

4. The Auth and Keystore services are required to be ran since a JWT is required to access the Customer endpoints.
Follow the instructions below to run these services.
https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth/tree/microprofile
https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/Keystore/README.md


## Setting up Cloudant

To set up the Cloudant database locally, we are running it as a docker container. You need [Docker](https://www.docker.com/) as a prerequisite.
To run cloudant on docker locally, run the commands below.

1.
```
docker pull ibmcom/cloudant-developer
```


2. In this case, you will need to set this property in microprofile-config.properties
https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/blob/microprofile/src/main/resources/META-INF/microprofile-config.properties

```
application.rest.client.CloudantClientService/mp-rest/url=http://localhost:8080
```

## Setting up Zipkin

This is an optional step.

In our sample application, we used Zipkin as our distributed tracing system.

If you want to access the traces for inventory service, run Zipkin as a docker container locally. You can find the instructions and more details [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/Zipkin/README.md)

### Running the app and stopping it

1. The host and port depends on the service you use.
Set the Zipkin host and port to defaults.

```
export zipkinHost=localhost
export zipkinPort=9411
```

2. Run the docker image.

```
docker run \
       --detach \
       --volume cloudant:/srv \
       --name cloudant-developer \
       --publish 8080:80 \
       --hostname cloudant.dev \
       ibmcom/cloudant-developer
```

3. Start your server.
```
mvn liberty:start-server -DhttpPort=9084 -DhttpsPort=9445
```
You will see something similar to the below messages.

```
[INFO] Starting server defaultServer.
[INFO] Server defaultServer started with process ID 48582.
[INFO] Waiting up to 30 seconds for server confirmation:  CWWKF0011I to be found in /Users/user@ibm.com/Desktop/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log
[INFO] CWWKM2010I: Searching for CWWKF0011I in /Users/user@ibm.com/Desktop/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log. This search will timeout after 30 seconds.
[INFO] CWWKM2015I: Match number: 1 is [7/24/18 8:03:43:394 CDT] 0000001a com.ibm.ws.kernel.feature.internal.FeatureManager            A CWWKF0011I: The server defaultServer is ready to run a smarter planet..
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.524 s
[INFO] Finished at: 2018-07-24T08:03:43-05:00
[INFO] Final Memory: 12M/309M
[INFO] ------------------------------------------------------------------------
```
4. Validate the inventory service. You should get a list of all inventory items.  You should get user information from this call.
```
curl -H "Authorization: Bearer <JWT>" http://localhost:9084/customer/rest/customer
```

5. If you are done accessing the application, you can stop your server using the following command.

`mvn liberty:stop-server`

Once you do this, you see the below messages
```
[INFO] Stopping server defaultServer.
[INFO] Server defaultServer stopped.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.952 s
[INFO] Finished at: 2018-07-24T08:03:53-05:00
[INFO] Final Memory: 11M/309M
[INFO] ------------------------------------------------------------------------
```
