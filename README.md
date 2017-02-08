# Customer Microservice
*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative*

## Introduction

This project demonstrates how to build a Microservices application implemented as a Spring Boot application and deployed in a docker container.  It provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://console.ng.bluemix.net/docs/services/Cloudant/index.html#Cloudant) NoSQL database as part of the Customer Profile function of BlueCompute.  Additionally the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) calls this microservice to perform Customer username/password authentication.  This project covers the following technical areas:

- Build a microservice as a Spring Boot Java application
- Deploy the Customer microservice as a container on the [IBM Bluemix Container Service](https://console.ng.bluemix.net/docs/containers/container_index.html).
- Register the container with Eureka service registry (part of [Spring Cloud Netflix project](http://cloud.spring.io/spring-cloud-netflix/)
- Persist Customer data in an [IBM Cloudant](https://console.ng.bluemix.net/docs/services/Cloudant/index.html#Cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).

## Pre-requisites

### Install Docker

Install [Docker](https://www.docker.com)

### Install Cloud Foundry CLI and IBM Containers plugin

Install the [Cloud Foundry CLI](https://console.ng.bluemix.net/docs/starters/install_cli.html) and the [IBM Containers Plugin](https://console.ng.bluemix.net/docs/cli/plugins/containers/index.html)

### Provision Cloudant Database in Bluemix

1. Login to your Bluemix console  
2. Open browser to create Cloudant Service using this link [https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db](https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db)  
3. Name your Cloudant service name like `refarch-cloudantdb`  
4. For testing, you can select the "Lite" plan, then click "Create"  
5. Once the service has been created, note the service credentials under `Service Credentials`.  In particular, the Customer microservice requires the `url` property.

## Deploy to BlueMix

You can use the following button to deploy the Customer microservice to Bluemix, or you can follow the instructions manually below.

[![Create BlueCompute Deployment Toolchain](https://console.ng.bluemix.net/devops/graphics/create_toolchain_button.png)](https://console.ng.bluemix.net/devops/setup/deploy?repository=https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git)

## Build the Docker container.

1. Build the application:

   ```
   # ./gradlew build
   ```

2. Copy the binaries to the docker container:
   
   ```
   # ./gradlew docker
   ```

3. Build the docker container
   ```
   # cd docker
   # docker build -t customer-microservice .
   ```

## Run the Docker container locally (optional)

Execute the following to run the Docker container locally.  Make sure to update the `<Eureka URL>`, and the `<Cloudant username>`, `<Cloudant password>`, `<Cloudant host>`, and `<Cloudant port>` retrieved from the `Service Credentials` tab in the Bluemix portal.

```
# docker run --name customer-service --publish 8080 -e eureka.client.fetchRegistry=true -e eureka.client.registerWithEureka=true -e spring.application.name=customer-microservice -e server.context-path=/micro -e eureka.client.serviceUrl.defaultZone=<Eureka URL> -e spring.application.cloudant.username=<Cloudant username> -e spring.application.cloudant.password=<Cloudant password> -e spring.application.cloudant.host=<Cloudant host> -e spring.application.cloudant.port=<Cloudant port> -e spring.application.cloudant.database=customers customer-microservice
```

## Run the Docker container on Bluemix
1. Log into the Cloud Foundry CLI
   ```
   # cf login
   ```
   
   Be sure to set the correct target space where Cloudant instance was provisioned.
   
2. Initialize the Bluemix Containers plugin
   
   ```
   # cf ic init
   ```
   
   Ensure that the container namespace is set:
   ```
   # cf ic namespace get
   ```
   
   If it is not set, use the following command to set it:
   ```
   # cf ic namespace set <namespace>
   ```
   
3. Tag and push the docker image to the Bluemix private registry:

   ```
   # docker tag customer-microservice registry.ng.bluemix.net/$(cf ic namespace get)/customer-microservice
   # docker push registry.ng.bluemix.net/$(cf ic namespace get)/customer-microservice
   ```

4. Execute the following to run the container in a group on Bluemix Container Service.  Make sure to update the `<Eureka URL>`, and the `<Cloudant username>`, `<Cloudant password>`, `<Cloudant host>`, and `<Cloudant port>` retrieved from the `Service Credentials` tab in the Bluemix portal.
   ```
   cf ic group create --name customer-microservice --publish 8080 -m 256 -e eureka.client.fetchRegistry=true -e eureka.client.registerWithEureka=true -e spring.application.name=customer-microservice -e server.context-path=/micro -e eureka.client.serviceUrl.defaultZone=<Eureka URL> -e spring.application.cloudant.username=<Cloudant username> -e spring.application.cloudant.password=<Cloudant password> -e spring.application.cloudant.host=<Cloudant host> -e spring.application.cloudant.port=<Cloudant port> -e spring.application.cloudant.database=customers --desired 2 --min 1 --max 3 registry.ng.bluemix.net/$(cf ic namespace get)/customer-microservice
   ```

## Validate the Customer microservice

Retrieve the Zuul URL associated with the Spring Cloud Framework.

### Create an Customer

Create a customer profile for the user `foo` with the password `bar`.  Make sure that you replace `<Zuul Hostname>` with your Zuul proxy's hostname or IP address.

```
# curl -X POST -H "Content-Type: application/json" -d '{"username": "foo", "password": "bar", "firstName": "foo", "lastName": "bar"}' -i https://<Zuul Hostname>/customer-microservice/micro/customer
HTTP/1.1 201 Created
X-Backside-Transport: OK OK
Connection: Keep-Alive
Transfer-Encoding: chunked
Date: Wed, 08 Feb 2017 21:41:31 GMT
Location: http://<Zuul Hostname>/customer-microservice/micro/customer/bff5631f24c849e8897645be8b66af16
X-Application-Context: zuul-proxy:8080
X-Global-Transaction-ID: 1311769839
```

Note the `Location` header returned, which contains the ID of the created customer.

### Get Customer

Verify the customer.  The caller must pass a header, `IBM-App-User`, to the API, to identify itself.  This header is passed by API Connect whan an authenticated user passes a valid Bearer token.  For testing, pass the header manually.

```
# curl -H "IBM-App-User: bff5631f24c849e8897645be8b66af16"  http://<Zuul IP>/customer-microservice/micro/customer
{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","imageUrl":null,"customerId":"bff5631f24c849e8897645be8b66af16"}
```

Note that *only* the customer object identified by `IBM-App-User` is returned to the caller.


### Search Customer by username

Call the `search` API to find the customer by username.  This API is used by the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) to authenticate users.

```
# curl -H "IBM-App-User: bff5631f24c849e8897645be8b66af16"  http://<Zuul IP>/customer-microservice/micro/customer/search?username=foo
[{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","imageUrl":null,"customerId":"bff5631f24c849e8897645be8b66af16"}]
```
