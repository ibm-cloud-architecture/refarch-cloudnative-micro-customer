###### refarch-cloudnative-micro-customer

## Microprofile based Microservice Apps Integration with IBM Cloudant

This repository contains the **MicroProfile** implementation of the **Customer Service** which is a part of the 'IBM Cloud Native Reference Architecture' suite, available at https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes

<p align="center">
  <a href="https://microprofile.io/">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-wfd/blob/microprofile/static/imgs/microprofile_small.png" width="300" height="100">
  </a>
</p>

1. [Introduction](#introduction)
2. [How it works](#how-it-works)
3. [API Endpoints](#api-endpoints)
4. [Implementation](#implementation)
    1. [Liberty app accelerator](#liberty-app-accelerator)
    2. [Microprofile](#microprofile)
5. [Features and App details](#features)
6. [Building the app](#building-the-app)
7. [Running the app and stopping it](#running-the-app-and-stopping-it)
    1. [Pre-requisites](#pre-requisites)
    2. [Locally in JVM](#locally-in-jvm)
    3. [Locally in Containers](#locally-in-containers)
    4. [Locally in Minikube](#locally-in-minikube)
    5. [Remotely in ICP](#remotely-in-icp)
8. [DevOps Strategy](#devops-strategy)
9. [References](#references)

### Introduction

This project is built to demonstrate how to build Customer Microservices applications using Microprofile. This application provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database as part of the Customer Profile function of BlueCompute. Additionally the Auth Microservice calls this microservice to perform Customer username/password authentication.

- Based on [MicroProfile](https://microprofile.io/).
- OAuth protect the microservice REST API using JWT token signed with a HS256 shared secret.
- Persist Customer data in an [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).
- Devops - TBD
- Deployment options for local, Docker Container-based runtimes, Minikube environment and ICP/BMX.

### How it works

Customer Microservice serves 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes. Though it is a part of a bigger application, Order service is itself an application in turn that performs basic operations of creating and querying customer profiles.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/customer_microservice.png">
</p>

### API Endpoints

```
GET     /customer/rest/customer 
```

- Returns all customers. The caller of this API must pass a valid OAuth token with the scope `blue`. The OAuth token is a JWT signed and is verified using a HS256 shared key. A JSON object array is returned consisting of only users that match the customer ID embedded in the JWT claim `user_name`, either length 0 or 1.

```
GET     /customer/rest/customer/{id} 
```

- Return customer by ID. The caller of this API must pass a valid OAuth token with the scope `blue`. The OAuth token is a JWT signed and is verified using a HS256 shared key. If the `id` matches the customer ID passed in the `user_name` claim in the JWT, it is returned as a JSON object in the response; otherwise `HTTP 401` is returned.

```
GET    /customer/rest/customer/search
```

- Return customer by username. The caller of this API must pass a valid OAuth token with the scope `admin`. This API is called by the Auth Microservice when authenticating a user. A JSON object array is returned consisting of only users that match the customer username (either length 0 or 1).

```
POST   /customer/rest/customer
```

- Create a customer. - Return customer by username. The caller of this API must pass a valid OAuth token with the scope `admin`. The Customer object must be passed as JSON object in the request body with the following format:

```
{
  "username": <username>,
  "password": <password>,
  "email": <email address>,
  "firstName": <first name>,
  "lastName": <last name>,
  "imageUrl": <image URL>
}
```

On success, `HTTP 201` is returned with the ID of the created user in the `Location` response header. This API is currently not called as it is not a function of the BlueCompute application.

```
PUT /micro/customer/{id}
```

- Update a customer record. The caller of this API must pass a valid OAuth token with the scope `blue`. The full Customer object must be passed in the request body. If the `id` matches the customer ID passed in the `user_name` claim in the JWT, the customer object is updated; otherwise `HTTP 401` is returned. This API is currently not called as it is not a function of the BlueCompute application.

```
DELETE /micro/customer/{id} 
```
- Delete a customer record. The caller of this API must pass a valid OAuth token with the scope `blue`. If the `id` matches the customer ID passed in the `user_name` claim in the JWT, the customer object is deleted; otherwise `HTTP 401` is returned. This API is currently not called as it is not a function of the BlueCompute application.

You can use cURL or Chrome POSTMAN to send get/post/put/delete requests to the application.

### Implementation

#### [Liberty app accelerator](https://liberty-app-accelerator.wasdev.developer.ibm.com/start/)

For Liberty, there is nice tool called [Liberty Accelerator](https://liberty-app-accelerator.wasdev.developer.ibm.com/start/) that generates a simple project based upon your configuration. Using this, you can build and deploy to Liberty either using the Maven or Gradle build.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/LibertyAcc_Home.png">
</p>

Just check the options of your choice and click Generate project. You can either Download it as a zip or you can create git project.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/LibertyAcc_PrjGen.png">
</p>

Once you are done with this, you will have a sample microprofile based application that you can deploy on Liberty.

Using Liberty Accelerator is your choice. You can also create the entire project manually, but using Liberty Accelerator will make things easier.

#### [MicroProfile](https://microprofile.io/)

MicroProfile is an open platform that optimizes the Enterprise Java for microservices architecture. In this application, we are using [**MicroProfile 1.2**](https://github.com/eclipse/microprofile-bom). This includes

- MicroProfile 1.0 ([JAX-RS 2.0](https://jcp.org/en/jsr/detail?id=339), [CDI 1.2](https://jcp.org/en/jsr/detail?id=346), and [JSON-P 1.0](https://jcp.org/en/jsr/detail?id=353))
- MicroProfile 1.1 (MicroProfile 1.0, [MicroProfile Config 1.0.](https://github.com/eclipse/microprofile-config))
- [MicroProfile Config 1.1](https://github.com/eclipse/microprofile-config) (supercedes MicroProfile Config 1.0), [MicroProfile Fault Tolerance 1.0](https://github.com/eclipse/microprofile-fault-tolerance), [MicroProfile Health Check 1.0](https://github.com/eclipse/microprofile-health), [MicroProfile Metrics 1.0](https://github.com/eclipse/microprofile-metrics), [MicroProfile JWT Authentication 1.0](https://github.com/eclipse/microprofile-jwt-auth).

You can make use of this feature by including this dependency in Maven.

```
<dependency>
<groupId>org.eclipse.microprofile</groupId>
<artifactId>microprofile</artifactId>
<version>1.2</version>
<type>pom</type>
<scope>provided</scope>
</dependency>
```

You should also include a feature in [server.xml](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/blob/microprofile/src/main/liberty/config/server.xml).

```
<server description="Sample Liberty server">

  <featureManager>
      <feature>microprofile-1.2</feature>
  </featureManager>

  <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
      id="defaultHttpEndpoint" host="*" />

</server>
```

### Features

1. Java SE 8 - Used Java Programming language

2. CDI 1.2 - Used CDI for typesafe dependency injection

3. JAX-RS 2.0.1 - JAX-RS is used for providing both standard client and server APIs for RESTful communication by MicroProfile applications.

4. Eclipse MicroProfile Config 1.1 - Configuration data comes from different sources like system properties, system environment variables, .properties etc. These values may change dynamically. Using this feature, helps us to pick up configured values immediately after they got changed.

The config values are sorted according to their ordinal. We can override the lower importance values from outside. The config sources by default, below is the order of importance.

- System.getProperties()
- System.getenv()
- all META-INF/microprofile-config.properties files on the ClassPath.

In our sample application, we obtained the configuration programatically.

### Building the app

To build the application, we used maven build. Maven is a project management tool that is based on the Project Object Model (POM). Typically, people use Maven for project builds, dependencies, and documentation. Maven simplifies the project build. In this task, you use Maven to build the project.

1. Clone this repository.

   `git clone https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git`
   
2. `cd refarch-cloudnative-micro-customer/`

3. Checkout MicroProfile branch.

   `git checkout microprofile`

4. Run this command. This command builds the project and installs it.

   `mvn install`
   
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
[INFO] Total time: 35.841 s
[INFO] Finished at: 2018-02-26T16:21:33-05:00
[INFO] Final Memory: 22M/306M
[INFO] ------------------------------------------------------------------------
```

### Running the app and stopping it

### Pre-requisites

As Customer service needs an Oauth token, make sure the auth service is up and running before running the Customer.

1. Locally in JVM

To run the Customer microservice locally in JVM, please complete the [Building the app](#building-the-app) section.

**Set Up Cloudant Database**

1. Login to your Bluemix console  
2. Open browser to create Cloudant Service using this link [https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db](https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db)  
3. Name your Cloudant service name like `refarch-cloudantdb`  
4. For testing, you can select the "Lite" plan, then click "Create"  
5. Once the service has been created, note the service credentials under `Service Credentials`.  In particular, the Customer microservice requires the `url` property.

```
export protocol=http
export host=<Host>
export port=<Port>

export user=<User>
export password=<Password>

expose database=customers
```

**Set Up Cloudant on Docker locally**

1. Pull the official docker image

`docker pull ibmcom/cloudant-developer`

2. Run the container.

`docker run \
       --detach \
       --volume cloudant:/srv \
       --name cloudant \
       --publish 8081:80 \
       ibmcom/cloudant-developer`

In this case, your jdbcURL will be 

```
export protocol=http
export host=localhost
export port=8081

export user=admin
export password=pass

expose database=customers
```

### Locally in JVM

1. Set the environment variables before you start your application. The host and port depends on the service you use. You can run the Cloudant locally on your system using the Cloudant docker container or use the [IBM Cloudant](https://www.ibm.com/cloud/compose/mysql) available in [IBM Cloud](https://www.ibm.com/cloud/).

```
export protocol=http
export host=<HOST>
export port=<PORT>

export user=<USER>
export password=<PASSWORD>

expose database=<DATABASE_NAME>

   ``` 
 2. Start your server.

   `mvn liberty:start-server -DtestServerHttpPort=9083`

   You will see the below.
   
 ```
 [INFO] CWWKM2001I: Invoke command is [/Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/bin/server, start, defaultServer].
[INFO] objc[14799]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java (0x1018654c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x10195f4e0). One of the two will be used. Which one is undefined.
[INFO] Starting server defaultServer.
[INFO] Server defaultServer started with process ID 14798.
[INFO] Waiting up to 30 seconds for server confirmation:  CWWKF0011I to be found in /Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log
[INFO] CWWKM2010I: Searching for CWWKF0011I in /Users/Hemankita.Perabathini@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log. This search will timeout after 30 seconds.
[INFO] CWWKM2015I: Match number: 1 is [26/2/18 16:54:19:707 EST] 00000019 com.ibm.ws.kernel.feature.internal.FeatureManager            A CWWKF0011I: The server defaultServer is ready to run a smarter planet..
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 22.423 s
[INFO] Finished at: 2018-02-26T16:54:19-05:00
[INFO] Final Memory: 13M/309M
[INFO] ------------------------------------------------------------------------
 ```
 3. If you are done accessing the application, you can stop your server using the following command.

   `mvn liberty:stop-server -DtestServerHttpPort=9083`

Once you do this, you see the below messages.

```
[INFO] CWWKM2001I: Invoke command is [/Users/user@ibm.com/BlueCompute/refarch-cloudnative-micro-customer/target/liberty/wlp/bin/server, stop, defaultServer].
[INFO] objc[14840]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java (0x1015024c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x1035bc4e0). One of the two will be used. Which one is undefined.
[INFO] Stopping server defaultServer.
[INFO] Server defaultServer stopped.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.125 s
[INFO] Finished at: 2018-02-26T16:55:23-05:00
[INFO] Final Memory: 13M/309M
[INFO] ------------------------------------------------------------------------
```
