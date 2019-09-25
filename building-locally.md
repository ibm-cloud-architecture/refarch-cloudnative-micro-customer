# Run Customer Service locally

## Table of Contents

* [Building the app](#building-the-app)
* [Setting up CouchDB](#setting-up-couchdb)
* [Setting up Zipkin](#setting-up-zipkin) (Optional)
* [Running the app and stopping it](#running-the-app-and-stopping-it)

## Building the app

To build the application, we used maven build. Maven is a project management tool that is based on the Project Object Model (POM). Typically, people use Maven for project builds, dependencies, and documentation. Maven simplifies the project build. In this task, you use Maven to build the project.

1. Clone this repository.

   `git clone https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git`
   
   `cd refarch-cloudnative-micro-customer/`

2. Checkout MicroProfile branch.

   `git checkout microprofile`
   
3. The Auth and Keystore services are required to be run because a JWT is required to access the Customer endpoints.
Follow the instructions below to run these services.
https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth/tree/microprofile/building-locally.md
   
4. Create the [auth keystore and truststore](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/microprofile/Keystore)
, and then correct their locations in your server.xml
    ```
    <ssl id="defaultSSLConfig" keyStoreRef="KeystorebcKeyStore" trustStoreRef="bcTrustStore"/>
    <keyStore id="bcKeyStore" location="<Path to BCKeyStoreFile.jks>" type="JKS" password="password"/>
    <keyStore id="bcTrustStore" location="<Path to truststore.jks>" type="JKS" password="password"/>
    ```

5. Run this command. This command builds the project and installs it.

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
By default, the application runs on [WebSphere Liberty with Web Profile](https://developer.ibm.com/wasdev/websphere-liberty/). You can also run it on [Open Liberty](https://openliberty.io/) as follows.

`mvn clean install -Popenliberty`

 If this runs successfully, you will be able to see the below messages.
 
 ```
[INFO] --- maven-failsafe-plugin:2.18.1:verify (verify-results) @ customer ---
[INFO] Failsafe report directory: /Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/test-reports/it
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ customer ---
[INFO] Installing /Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/customer-1.0-SNAPSHOT.war to /Users/user@ibm.com/.m2/repository/projects/customer/1.0-SNAPSHOT/customer-1.0-SNAPSHOT.war
[INFO] Installing /Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/pom.xml to /Users/user@ibm.com/.m2/repository/projects/customer/1.0-SNAPSHOT/customer-1.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 26.498 s
[INFO] Finished at: 2018-10-26T23:05:51-05:00
[INFO] Final Memory: 34M/474M
[INFO] ------------------------------------------------------------------------
 ```

## Setting up CouchDB

We use the official CouchDB image to set up the CouchDB database locally. You need [Docker](https://www.docker.com/) as a prerequisite.
To run CouchDB on docker locally, run the commands below.

1. Pull the official CouchDB docker image:

	```
	docker pull couchdb
	```

2. Run the docker image:

	```
	docker run -d -e COUCHDB_USER='admin' -e COUCHDB_PASSWORD='passw0rd' -p 5984:5984 couchdb
	```

* If your database isn't populated already, you can run a script to populate the database with default users. By doing so, you will have one admin user named `user` and one basic user named `foo` created in your CouchDB database. There are three approaches:

	1. Run the `populate.py` script directly. This will require you to have the Cloudant Python library installed, which you can obtain with `pip install cloudant`:  
	
		```
		cd populate
		python3 populate.py localhost 5984
		```
		
	2. 	1. You can access the newly created an populated `customers` database at `http://localhost:5984/customers`.
		

In this case, you will need to set this property in [microprofile-config.properties](./src/main/resources/META-INF/microprofile-config.properties)

```
application.rest.client.CouchDBClientService/mp-rest/url=http://localhost:5984
```

This requires a rebuild, run `mvn clean install`.

## Setting up Zipkin

This is an optional step.

In our sample application, we used Zipkin as our distributed tracing system.

If you want to access the traces for inventory service, run Zipkin as a docker container locally. You can find the instructions and more details [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/Zipkin/README.md).

## Running the app and stopping it

1. Set some environment variables as following.

We must set the Zipkin host and port to defaults to suppress some missing config messages.
    
    ```
    export zipkinHost=localhost
    export zipkinPort=9411
    ``` 
    
    If hitting the secure REST endpoints, such as `orders/rest/orders`, we must set some JWKS vars.
    ```
    export jwksUri=https://localhost:9443/oidc/endpoint/OP/jwk
    export jwksIssuer=https://localhost:9443/oidc/endpoint/OP
    export administratorRealm=https://localhost:9443/oidc/endpoint/OP
    ```
    
    And lastly here are optional exports if Auth or Inventory are running.
    ```
    export host=localhost
    export port=8080
    export auth_health=https://localhost:9443/health
    ```
2. To enable authentication, the [Auth MicroService](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth/tree/microprofile) must be running and the keystore must be set up. Please refer to the link for further instructions.

3. Start your server.
```
mvn liberty:start-server -DtestServerHttpPort=9084 -DtestServerHttpsPort=9445
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
4. Retrieve the JWT from the Auth Service to authorize secure REST calls:
    ```
    curl -k -d 'grant_type=password&client_id=bluecomputeweb&client_secret=bluecomputewebs3cret&username=user&password=password&scope=openid' https://localhost:9443/oidc/endpoint/OP/token
    ```

5. Validate the Customer service. You should get the user information for the customer that the JWT belongs to.
```
curl -k --request GET \
  --url https://localhost:8445/customer/rest/customer \
  --header 'Authorization: Bearer <Insert Token Here>' --header 'Content-Type: application/json'
```

6. If you are done accessing the application, you can stop your server using the following command.

`mvn liberty:stop-server -DtestServerHttpPort=9084 -DtestServerHttpsPort=9445`

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
