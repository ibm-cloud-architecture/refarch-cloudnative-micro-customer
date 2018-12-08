# refarch-cloudnative-micro-customer: Spring Boot Microservice with CouchDB Database
[![Build Status](https://travis-ci.org/ibm-cloud-architecture/refarch-cloudnative-micro-customer.svg?branch=master)](https://travis-ci.org/ibm-cloud-architecture/refarch-cloudnative-micro-customer)

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring*

## Table of Contents
  * [Introduction](#introduction)
    + [APIs](#apis)
  * [Pre-requisites:](#pre-requisites)
  * [Deploy Customer Application to Kubernetes Cluster](#deploy-customer-application-to-kubernetes-cluster)
  * [Validate the Customer Microservice API](#validate-the-customer-microservice-api)
    + [Setup](#setup)
      - [1. Create a temporary HS256 shared secret](#1-create-a-temporary-hs256-shared-secret)
      - [2. Generate a JWT Token with `admin` Scope](#2-generate-a-jwt-token-with-admin-scope)
    + [Create a Customer](#create-a-customer)
    + [Search the Customer](#search-the-customer)
    + [Get the Customer](#get-the-customer)
      - [Generate a JWT Token with `blue` Scope for New User](#generate-a-jwt-token-with-blue-scope-for-new-user)
      - [Use `blue` Scoped JWT Token to Get the Customer information](#use-blue-scoped-jwt-token-to-get-the-customer-information)
    + [Delete the User](#delete-the-user)
  * [Deploy Customer Application on Docker](#deploy-customer-application-on-docker)
    + [Deploy the CouchDB Docker Container](#deploy-the-couchdb-docker-container)
    + [Deploy the Customer Docker Container](#deploy-the-customer-docker-container)
  * [Run Customer Service application on localhost](#run-customer-service-application-on-localhost)
  * [Deploy Customer Application on Open Liberty](#deploy-customer-application-on-openliberty)
  * [Optional: Setup CI/CD Pipeline](#optional-setup-cicd-pipeline)
  * [Conclusion](#conclusion)
  * [Contributing](#contributing)
    + [GOTCHAs](#gotchas)
    + [Contributing a New Chart Package to Microservices Reference Architecture Helm Repository](#contributing-a-new-chart-package-to-microservices-reference-architecture-helm-repository)

## Introduction
This project will demonstrate how to deploy a Spring Boot Application with a CouchDB database onto a Kubernetes Cluster.

![Application Architecture](static/customer.png?raw=true)

Here is an overview of the project's features:
- Leverage [`Spring Boot`](https://projects.spring.io/spring-boot/) framework to build a Microservices application.
- Uses [`Spring Data JPA`](http://projects.spring.io/spring-data-jpa/) to persist data to CouchDB database.
- Uses [`CouchDB`](http://couchdb.apache.org/) as the customer database.
- Uses [`Docker`](https://docs.docker.com/) to package application binary and its dependencies.
- Uses [`Helm`](https://helm.sh/) to package application and CouchDB deployment configuration and deploy to a [`Kubernetes`](https://kubernetes.io/) cluster.

## APIs
The Customer Microservice REST API is OAuth protected.
- `POST /micro/customer`
  - Create a customer. - Return customer by username.  The caller of this API must pass a valid OAuth token with the scope `admin`.  The Customer object must be passed as JSON object in the request body with the following format:
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

    On success, `HTTP 201` is returned with the ID of the created user in the `Location` response header.  This API is currently not called as it is not a function of the BlueCompute application.

- `PUT /micro/customer/{id}`
  - Update a customer record.  The caller of this API must pass a valid OAuth token with the scope `blue`.  The full Customer object must be passed in the request body.  If the `id` matches the customer ID passed in the `user_name` claim in the JWT, the customer object is updated; otherwise `HTTP 401` is returned.  This API is currently not called as it is not a function of the BlueCompute application.

- `GET /micro/customer`
  - Returns all customers.  The caller of this API must pass a valid OAuth token with the scope `blue`.  The OAuth token is a JWT signed and is verified using a HS256 shared key.  A JSON object array is returned consisting of only users that match the customer ID embedded in the JWT claim `user_name`, either length 0 or 1.

- `GET /micro/customer/{id}`
  - Return customer by ID.  The caller of this API must pass a valid OAuth token with the scope `blue`.  The OAuth token is a JWT signed and is verified using a HS256 shared key.  If the `id` matches the customer ID passed in the `user_name` claim in the JWT, it is returned as a JSON object in the response; otherwise `HTTP 401` is returned.

- `GET /micro/customer/search`
  - Return customer by username.  The caller of this API must pass a valid OAuth token with the scope `admin`.  This API is called by the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) when authenticating a user.  A JSON object array is returned consisting of only users that match the customer username (either length 0 or 1).

- `DELETE /micro/customer/{id}`
  - Delete a customer record.  The caller of this API must pass a valid OAuth token with the scope `blue`.  If the `id` matches the customer ID passed in the `user_name` claim in the JWT, the customer object is deleted; otherwise `HTTP 401` is returned.  This API is currently not called as it is not a function of the BlueCompute application.

## Pre-requisites:
* Create a Kubernetes Cluster by following the steps [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes#create-a-kubernetes-cluster).
* Install the following CLI's on your laptop/workstation:
    + [`docker`](https://docs.docker.com/install/)
    + [`kubectl`](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
    + [`helm`](https://docs.helm.sh/using_helm/#installing-helm)
* Clone customer repository:
```bash
git clone -b spring --single-branch https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git
cd refarch-cloudnative-micro-customer
```

## Deploy Customer Application to Kubernetes Cluster
In this section, we are going to deploy the Customer Application, along with a CouchDB service, to a Kubernetes cluster using Helm. To do so, follow the instructions below:
```bash
# Add helm repos for CouchDB Chart
helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator

# Install CouchDB Chart
helm upgrade --install couchdb \
  --version 0.2.2 \
  --set fullnameOverride=customer-couchdb \
  --set service.externalPort=5985 \
  --set createAdminSecret=true \
  --set adminUsername=user \
  --set adminPassword=passw0rd \
  --set clusterSize=1 \
  --set persistentVolume.enabled=false \
  incubator/couchdb

# Go to Chart Directory
cd chart/customer

# Deploy Customer to Kubernetes cluster
helm upgrade --install customer --set service.type=NodePort .
```

The last command will give you instructions on how to access/test the Customer application. Please note that before the Customer application starts, the CouchDB deployment must be fully up and running, which normally takes a couple of minutes. With Kubernetes [Init Containers](https://kubernetes.io/docs/concepts/workloads/pods/init-containers/), the Customer Deployment polls for CouchDB readiness status so that Customer can start once CouchDB is ready, or error out if CouchDB fails to start.

Also, once CouchDB is fully up and running, a [`Kubernetes Job`](https://kubernetes.io/docs/concepts/workloads/controllers/jobs-run-to-completion/) will run to populate the CouchDB database with a new customer record so that it can be served by the application. This is done for convenience to be used by the Bluecompute Web Application.

To check and wait for the deployment status, you can run the following command:
```bash
kubectl get deployments -w
NAME                  DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
customer-customer     1         1         1            1           10h
```

The `-w` flag is so that the command above not only retrieves the deployment but also listens for changes. If you a 1 under the `CURRENT` column, that means that the customer app deployment is ready.

## Validate the Customer Microservice API
Now that we have the customer service up and running, let's go ahead and test that the API works properly.

### Setup
#### a. Setup Customer Service Hostname and Port
To make going through this document easier, we recommend you create environment variables for the customer service hostname/IP and port. To do so, run the following commands:
```bash
export CUSTOMER_HOST=localhost
export CUSTOMER_PORT=8082
```

Where:
* `CUSTOMER_HOST` is the hostname or IP address for the customer service.
  + If using `IBM Cloud Private`, use the IP address of one of the proxy nodes.
  + If using `IBM Cloud Kubernetes Service`, use the IP address of one of the worker nodes.
* `CUSTOMER_PORT` is the port for the customer service.
  + If using `IBM Cloud Private` or `IBM Cloud Kubernetes Service`, enter the value of the NodePort.

#### b. Create a temporary HS256 shared secret
As the APIs in this microservice as OAuth protected, the HS256 shared secret used to sign the JWT generated by the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is needed to validate the access token provided by the caller.

To make things easier for you, we pasted below the 2048-bit secret that's included in the customer chart [here](chart/customer/values.yaml#L28), which you can export to your environment as follows:
```bash
export HS256_KEY="E6526VJkKYhyTFRFMC0pTECpHcZ7TGcq8pKsVVgz9KtESVpheEO284qKzfzg8HpWNBPeHOxNGlyudUHi6i8tFQJXC8PiI48RUpMh23vPDLGD35pCM0417gf58z5xlmRNii56fwRCmIhhV7hDsm3KO2jRv4EBVz7HrYbzFeqI45CaStkMYNipzSm2duuer7zRdMjEKIdqsby0JfpQpykHmC5L6hxkX0BT7XWqztTr6xHCwqst26O0g8r7bXSYjp4a"
```

However, if you must create your own 2048-bit secret, one can be generated using the following command:
```bash
cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 1 | xargs echo -n
```

Note that if the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is also deployed, it must use the *same* HS256 shared secret.

#### c. Generate a JWT Token with `admin` Scope
To generate a JWT Token with an `admin` scope, which will let you create/get/delete users, run the commands below:
```bash
# JWT Header
jwt1=$(echo -n '{"alg":"HS256","typ":"JWT"}' | openssl enc -base64);
# JWT Payload
jwt2=$(echo -n "{\"scope\":[\"admin\"],\"user_name\":\"${TEST_USER}\"}" | openssl enc -base64);
# JWT Signature: Header and Payload
jwt3=$(echo -n "${jwt1}.${jwt2}" | tr '+\/' '-_' | tr -d '=' | tr -d '\r\n');
# JWT Signature: Create signed hash with secret key
jwt4=$(echo -n "${jwt3}" | openssl dgst -binary -sha256 -hmac "${HS256_KEY}" | openssl enc -base64 | tr '+\/' '-_' | tr -d '=' | tr -d '\r\n');
# Complete JWT
jwt=$(echo -n "${jwt3}.${jwt4}");
```

Where:
* `admin` is the scope needed to create the user.
* `${TEST_USER}` is the user to create, i.e. `foo`.
* `${HS256_KEY}` is the 2048-bit secret from the previous step.

### 1. Create a Customer
Let's create a new customer with username `foo` and password `bar` and its respective profile with the following command:
```bash
curl -X POST -i "http://${CUSTOMER_HOST}:${CUSTOMER_PORT}/micro/customer" -H "Content-Type: application/json" -H "Authorization: Bearer ${jwt}" -d "{\"username\": \"${TEST_USER}\", \"password\": \"bar\", \"firstName\": \"foo\", \"lastName\": \"bar\", \"email\": \"foo@bar.com\"}"

HTTP/1.1 201 Created
Date: Mon, 20 Aug 2018 21:43:51 GMT
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
X-Application-Context: customer-microservice:8082
Location: http://localhost:8082/micro/customer/41757d0170344f9ea47a2d9634bc9ba7
Content-Length: 0
Server: Jetty(9.2.13.v20150730)
```

Where:
* `${CUSTOMER_HOST}` is the hostname/ip address for the customer microservice.
* `${CUSTOMER_PORT}` is the port for the customer microservice.
* `${jwt}` is the JWT token created in the previous step.
* `${TEST_USER}` is the user to create, i.e. `foo`.

Note the `Location` header returned, which contains the `CUSTOMER_ID` of the created customer.  For the GET calls below, copy the ID in the `Location` header (e.g. in the above, `41757d0170344f9ea47a2d9634bc9ba7`). This id will be used later when deleting the user. To save it in your environment, run the following command using the the id returned above:
```bash
# In this case, we are using the id that was returned in our sample command above, which will differ for you
CUSTOMER_ID=41757d0170344f9ea47a2d9634bc9ba7
```

### 2. Search the Customer
To search users with a particular username, i.e. `foo`, run the command below:
```bash
curl -s -X GET "http://${CUSTOMER_HOST}:${CUSTOMER_PORT}/micro/customer/search?username=${TEST_USER}" -H 'Content-type: application/json' -H "Authorization: Bearer ${jwt}"

[{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","email":"foo@bar.com","imageUrl":null,"customerId":"7145e43859764b3e8abc76784f1eb36a"}]
```

Where:
* `${CUSTOMER_HOST}` is the hostname/ip address for the customer microservice.
* `${CUSTOMER_PORT}` is the port for the customer microservice.
* `${jwt}` is the JWT token created in the previous step.
* `${TEST_USER}` is the user to create, i.e. `foo`.

### 3. Get the Customer
To use the customer service as a non-admin user and still be able to retrieve a user's own record, you must create a JWT token with the `blue` scope and pass the customer id as the value for the `user_name` payload. By doing this, we guarantee that only the identied user can retrieve/update/delete it's own record.

#### Generate a JWT Token with `blue` Scope for New Customer
In order for the newly created user to retrieve its own record, and only its own record, you will need to create a new JWT token with the scope `blue` and a payload that has the `CUSTOMER_ID` as the value for `user_name`. To generate the new JWT token, run the following commands:
```bash
# JWT Header
jwt1=$(echo -n '{"alg":"HS256","typ":"JWT"}' | openssl enc -base64);
# JWT Payload
jwt2=$(echo -n "{\"scope\":[\"blue\"],\"user_name\":\"${CUSTOMER_ID}\"}" | openssl enc -base64);
# JWT Signature: Header and Payload
jwt3=$(echo -n "${jwt1}.${jwt2}" | tr '+\/' '-_' | tr -d '=' | tr -d '\r\n');
# JWT Signature: Create signed hash with secret key
jwt4=$(echo -n "${jwt3}" | openssl dgst -binary -sha256 -hmac "${HS256_KEY}" | openssl enc -base64 | tr '+\/' '-_' | tr -d '=' | tr -d '\r\n');
# Complete JWT
jwt_blue=$(echo -n "${jwt3}.${jwt4}");
```

Where:
* `blue` is the scope needed to create the user.
* `${CUSTOMER_ID}` is the id of the customer user crated earlier, i.e. `41757d0170344f9ea47a2d9634bc9ba7`.
* `${HS256_KEY}` is the 2048-bit secret from the previous step.

#### Use `blue` Scoped JWT Token to Retrieve the Customer Record
To retrieve the customer record using the `blue` scoped JWT token, run the command below:
```bash
curl -s -X GET "http://${CUSTOMER_HOST}:${CUSTOMER_PORT}/micro/customer" -H "Authorization: Bearer ${jwt_blue}"

[{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","email":"foo@bar.com","imageUrl":null,"customerId":"7145e43859764b3e8abc76784f1eb36a"}]
```

Note that *only* the customer object identified by the encoded `user_name` is returned to the caller.

### 4. Delete the Customer
Using either the `admin` or the `blue` scoped JWT token, you can delete the customer record. If using the `blue` scoped JWT token, *only* the customer object identified by the encoded `user_name` can be deleted. To run with the `blue` scoped JWT token to delete the user, run the command below:
```bash
curl -X DELETE -i "http://${CUSTOMER_HOST}:${CUSTOMER_PORT}/micro/customer/${CUSTOMER_ID}" -H "Content-type: application/json" -H "Authorization: Bearer ${jwt_blue}"

HTTP/1.1 200 OK
Date: Mon, 20 Aug 2018 22:20:00 GMT
X-Application-Context: customer-microservice:8082
Content-Length: 0
Server: Jetty(9.2.13.v20150730)
```

Where:
* `${CUSTOMER_HOST}` is the hostname/ip address for the customer microservice.
* `${CUSTOMER_PORT}` is the port for the customer microservice.
* `${CUSTOMER_ID}` is the id of the customer user crated earlier, i.e. `41757d0170344f9ea47a2d9634bc9ba7`.
* `${jwt_blue}` is the JWT token created in the previous step.

If successful, you should get a `200 OK` status code as shown in the command above.

## Deploy Customer Application on Docker
You can also run the Customer Application locally on Docker. Before we show you how to do so, you will need to have a running CouchDB deployment running somewhere.

### Deploy the CouchDB Docker Container
The easiest way to get CouchDB running is via a Docker container. To do so, run the following commands:
```bash
# Start a CouchDB Container with a database user, a password, and create a new database
docker run --name customercouchdb -p 5985:5984 -e COUCHDB_USER=admin -e COUCHDB_PASSWORD=passw0rd -d couchdb:2.1.2

# Get the CouchDB Container's IP Address
docker inspect customercouchdb | grep "IPAddress"
            "SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.2",
                    "IPAddress": "172.17.0.2",
```
Make sure to select the IP Address in the `IPAddress` field. You will use this IP address when deploying the Customer container.

### Deploy the Customer Docker Container
To deploy the Customer container, run the following commands:
```bash
# Build the Docker Image
docker build -t customer .

# Start the Customer Container
docker run --name customer \
    -e COUCHDB_PROTOCOL=http \
    -e COUCHDB_USER=admin \
    -e COUCHDB_PASSWORD=passw0rd \
    -e COUCHDB_HOST=${COUCHDB_IP_ADDRESS} \
    -e COUCHDB_PORT=5984 \
    -e HS256_KEY=${HS256_KEY} \
    -p 8082:8082 \
    -d customer
```

Where `${COUCHDB_IP_ADDRESS}` is the IP address of the CouchDB container, which is only accessible from the Docker container network.

If everything works successfully, you should be able to get some data when you run the following command:
```bash
curl http://localhost:8082/micro/customer
```

## Run Customer Service application on localhost
In this section you will run the Spring Boot application on your local workstation. Before we show you how to do so, you will need to deploy a CouchDB Docker container as shown in the [Deploy a CouchDB Docker Container](#deploy-a-couchdb-docker-container).

Once CouchDB is ready, we can run the Spring Boot Customer application locally as follows:

1. Open [`src/main/resources/application.yml`](src/main/resources/application.yml) file, enter the following values for the fields under `spring.application.cloudant`, and save the file:
    * **protocol:** http
    * **username:** admin
    * **password:** passw0rd
    * **host:** 127.0.0.1
    * **port:** 5985
    * **database:** customers

2. Build the application:
```bash
./gradlew build -x test
```

3. Run the application on localhost:
```bash
java -jar build/libs/micro-customer-0.0.1.jar
```

4. Validate. You should get a list of all customer items:
```bash
curl http://localhost:8082/micro/customer
```

That's it, you have successfully deployed and tested the Customer microservice.

## Deploy Customer Application on OpenLiberty

The Spring Boot applications can be deployed on WebSphere Liberty as well. In this case, the embedded server i.e. the application server packaged up in the JAR file will be Liberty. For instructions on how to deploy the Customer application optimized for Docker on Open Liberty, which is the open source foundation for WebSphere Liberty, follow the instructions [here](OpenLiberty.MD)

## Optional: Setup CI/CD Pipeline
If you would like to setup an automated Jenkins CI/CD Pipeline for this repository, we provided a sample [Jenkinsfile](Jenkinsfile), which uses the [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) syntax of the [Jenkins Kubernetes Plugin](https://github.com/jenkinsci/kubernetes-plugin) to automatically create and run Jenkis Pipelines from your Kubernetes environment.

To learn how to use this sample pipeline, follow the guide below and enter the corresponding values for your environment and for this repository:
* https://github.com/ibm-cloud-architecture/refarch-cloudnative-devops-kubernetes

## Conclusion
You have successfully deployed and tested the Customer Microservice and a CouchDB database both on a Kubernetes Cluster and in local Docker Containers.

To see the Customer app working in a more complex microservices use case, checkout our Microservice Reference Architecture Application [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring).

## Contributing
If you would like to contribute to this repository, please fork it, submit a PR, and assign as reviewers any of the GitHub users listed here:
* https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/graphs/contributors

### GOTCHAs
1. We use [Travis CI](https://travis-ci.org/) for our CI/CD needs, so when you open a Pull Request you will trigger a build in Travis CI, which needs to pass before we consider merging the PR. We use Travis CI to test the following:
    * Create and load a CouchDB database with the customer static data.
    * Building and running the Customer app against the CouchDB database and run API tests.
    * Build and Deploy a Docker Container, using the same CouchDB database.
    * Run API tests against the Docker Container.
    * Deploy a minikube cluster to test Helm charts.
    * Download Helm Chart dependencies and package the Helm chart.
    * Deploy the Helm Chart into Minikube.
    * Run API tests against the Helm Chart.

2. We use the Community Chart for CouchDB as the dependency chart for the Customer Chart. If you would like to learn more about that chart and submit issues/PRs, please check out its repo here:
    * https://github.com/helm/charts/tree/master/stable/couchdb

### Contributing a New Chart Package to Microservices Reference Architecture Helm Repository
To contribute a new chart version to the [Microservices Reference Architecture](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring) helm repository, follow its guide here:
* https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring#contributing-a-new-chart-to-the-helm-repositories
