# Customer Microservice
[![Build Status](https://travis-ci.org/ibm-cloud-architecture/refarch-cloudnative-micro-customer.svg?branch=master)](https://travis-ci.org/ibm-cloud-architecture/refarch-cloudnative-micro-customer)

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes*

## Introduction

This project demonstrates how to build a Microservices application implemented as a Spring Boot application and deployed on Bluemix Container Service.  It provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://console.ng.bluemix.net/docs/services/Cloudant/index.html#Cloudant) NoSQL database as part of the Customer Profile function of BlueCompute.  Additionally the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) calls this microservice to perform Customer username/password authentication.  This project covers the following technical areas:

- Build a microservice as a Spring Boot Java application
- OAuth protect the microservice REST API using JWT token signed with a HS256 shared secret
- Deploy the Customer microservice as a container on the [IBM Bluemix Container Service](https://console.ng.bluemix.net/docs/containers/container_index.html).
- Persist Customer data in an [IBM Cloudant](https://console.ng.bluemix.net/docs/services/Cloudant/index.html#Cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).

![Customer Microservice](customer_microservice.png)

### REST API

The Customer Microservice REST API is OAuth protected.  

- `GET /micro/customer`
  - Returns all customers.  The caller of this API must pass a valid OAuth token with the scope `blue`.  The OAuth token is a JWT signed and is verified using a HS256 shared key.  A JSON object array is returned consisting of only users that match the customer ID embedded in the JWT claim `user_name`, either length 0 or 1.

- `GET /micro/customer/{id}`
  - Return customer by ID.  The caller of this API must pass a valid OAuth token with the scope `blue`.  The OAuth token is a JWT signed and is verified using a HS256 shared key.  If the `id` matches the customer ID passed in the `user_name` claim in the JWT, it is returned as a JSON object in the response; otherwise `HTTP 401` is returned.

- `GET /micro/customer/search`
  - Return customer by username.  The caller of this API must pass a valid OAuth token with the scope `admin`.  This API is called by the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) when authenticating a user.  A JSON object array is returned consisting of only users that match the customer username (either length 0 or 1).

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


- `DELETE /micro/customer/{id}` (private)
  - Delete a customer record.  The caller of this API must pass a valid OAuth token with the scope `blue`.  If the `id` matches the customer ID passed in the `user_name` claim in the JWT, the customer object is deleted; otherwise `HTTP 401` is returned.  This API is currently not called as it is not a function of the BlueCompute application.


## Pre-requisites

### Install Docker

Install [Docker](https://www.docker.com)

### Install Bluemix CLI and IBM Container Service plugins

Install the [bx CLI](https://clis.ng.bluemix.net/ui/home.html), the Bluemix container-registry Plugin and the Bluemix container-service plugin.  The plugins can be installed directly [here](http://plugins.ng.bluemix.net/ui/repository.html), or using the following commands:

```
# bx plugin install container-service -r Bluemix
# bx plugin install conatiner-registry -r Bluemix
```

### Install kubectl

Install the [kubectl CLI](https://kubernetes.io/docs/tasks/kubectl/install/) to manage the Kubernetes cluster.

### Install helm

The customer microservice is packaged as a [Helm Chart](https://github.com/kubernetes/helm/blob/master/docs/charts.md).  Install the [helm CLI](https://github.com/kubernetes/helm/blob/master/docs/install.md).

### Provision Cloudant Database in Bluemix

*Note that two components use Cloudant in BlueCompute, the Customer microservice and the [Social Review microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-socialreview).  If deploying both components to the same space, they can share the Cloudant database instance, as the Customer microservice saves documents to the `customers` database, and the Social Review microservice saves documents to the `socialreviewdb` and `socialreviewdb-staging` databases.*

1. Login to your Bluemix console  
2. Open browser to create Cloudant Service using this link [https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db](https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db)  
3. Name your Cloudant service name like `refarch-cloudantdb`  
4. For testing, you can select the "Lite" plan, then click "Create"  
5. Once the service has been created, note the service credentials under `Service Credentials`.  In particular, the Customer microservice requires the `url` property.



## Build the code

Build the application:

```
# ./gradlew build
```

## Validate the Customer microservice

### Create a temporary HS256 shared secret

As the APIs in this microservice as OAuth protected, the HS256 shared secret used to sign the JWT generated by the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is needed to validate the access token provided by the caller.

A 2048-bit secret can be generated using the following command:

```
# cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 1 | xargs echo -n
```

Note that if the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is also deployed, it must use the *same* HS256 shared secret.

### Run the microservice

Start the service using the following, make sure that the parameters are replaced correctly:

```
# java \
    -Deureka.client.fetchRegistry=false \
    -Deureka.client.registerWithEureka=false \
    -Dspring.application.cloudant.username=<Cloudant username> \
    -Dspring.application.cloudant.password=<Cloudant password> \
    -Dspring.application.cloudant.host=<Cloudant host> \
    -Dspring.application.cloudant.port=<Cloudant port> \
    -Dspring.application.cloudant.database=customers \
    -Djwt.sharedSecret=<HS256 key> \
    -jar build/libs/micro-customer-0.0.1.jar
```
    
This starts the application in the current console.

### Generate a temporary access token for `admin`

Use the shared secret to generate a valid JWT signed with the shared secret generated above.  You can do this at [jwt.io](https://jwt.io) using the Debugger.  Paste the HS256 shared secret in the bottom in the box (and leave base64 encoded unchecked).  You can use the following payload initially:

```
{
  "scope": [ "admin" ],
  "user_name": "admin"
}
```

Copy the text that appears in "Encoded"; this is the signed JWT that will be used for the "Create a Customer" call.

### Create a Customer

Create a customer profile for the user `foo` with the password `bar`.  Make sure that you replace `<JWT>` with your generated JWT from the previous step.

```
# curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer <JWT>" -d '{"username": "foo", "password": "bar", "firstName": "foo", "lastName": "bar", "email": "foo@bar.com"}' -i http://localhost:8080/micro/customer
HTTP/1.1 201 Created
X-Backside-Transport: OK OK
Connection: Keep-Alive
Transfer-Encoding: chunked
Date: Wed, 08 Feb 2017 21:41:31 GMT
Location: http://localhost:8080/micro/customer/bff5631f24c849e8897645be8b66af16
X-Application-Context: zuul-proxy:8080
X-Global-Transaction-ID: 1311769839
```

Note the `Location` header returned, which contains the ID of the created customer.  For the GET calls below, copy the ID in the `Location` header (e.g. in the above, `bff5631f24c849e8897645be8b66af16`).

### Get Customer

#### Generate temporary JWT for customer

Use the id returned in the location above as the `user_name` field, and change the `scope` list to contain `blue`.  For example,

```
{
  "scope": [ "blue" ],
  "user_name": "bff5631f24c849e8897645be8b66af16"
}
```

Paste this to the `payload` using the [jwt.io](https://jwt.io) debugger, add the HS256 secret generated above, and copy the resulting encoded JWT for the next test.

#### Call the GET REST API

Verify the customer.  The caller identifies itself using the encoded `user_name` in the JWT passed in the Authorization header as Bearer token.

```
# curl -H "Authorization: Bearer <JWT>"  http://localhost:8080/micro/customer
{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","imageUrl":null,"customerId":"bff5631f24c849e8897645be8b66af16","email":"foo@bar.com"}
```

Note that *only* the customer object identified by the encoded `user_name` is returned to the caller.

## Deploy to Bluemix

The service can be packaged as a Docker container and deployed to a Kubernetes cluster running on Bluemix.

### Build Docker Container

1. Copy the binaries to the `docker` directory and build the image:
   
   ```
   # ./gradlew docker
   # cd docker
   # docker build -t customer-microservice .
   ```

### Push the Docker image to the Bluemix private container registry

1. Log into the Bluemix CLI

   ```
   # bx login
   ```
   
   Be sure to set the correct target space where Cloudant instance was provisioned.
   
2. Initialize the Bluemix Container Service plugin
   
   ```
   # bx cs init
   ```
   
   Initialize the Bluemix Container Registry plugin:
   
   ```
   # bx cr login
   ```
   
   Get the registry namespace:
   
   ```
   # bx cr namespaces
   ```
   
   If there are no namespaces available, use the following command to create one:
   
   ```
   # bx cr namespace-add <namespace>
   ```
   
3. Tag and push the docker image to the Bluemix private registry:

   ```
   # docker tag customer-microservice registry.ng.bluemix.net/<namespace>/customer-microservice:latest
   # docker push registry.ng.bluemix.net/<namespace>/customer-microservice:latest
   ```

4. Create a Kubernetes Cluster (if applicable)
   
   If a Kubernetes cluster has not previously been created, create a free Kubernetes cluster using the following:
   
   ```
   # bx cs cluster-create --name <cluster_name>
   ```
   
   You can monitor the cluster creation using `bx cs clusters` and `bx cs workers <cluster_name>`. 
   
5. Set up kubectl

   Once the cluster has been created, download the configuration:
   
   ```
   # bx cs cluster-config <cluster_name>
   ```
   
   Cut and paste the `export KUBECONFIG` command to set up the kubectl CLI to talk to the Kubernetes instance.
   
6. Install Helm in the Kubernetes Cluster

   Install helm in the Kubernetes cluster.  This is installes the server-side Tiller component in Kubernetes that manages packages.
   
   ```
   # helm init
   ```

7. Update the Chart `values.yaml`
   
   Update the `values.yaml` in the `charts/bluecompute-customer` directory to use the docker image you built earlier.  Here is an example excerpt from `values.yaml`:
   
   ```
   image:
     repository: registry.ng.bluemix.net/<namespace>/customer-microservice
     tag: latest
   ```
   
   Additionally, if the Cloudant database service created earlier is not named `refarch-cloudantdb`, change the `cloudant.serviceName` value in the yaml.  Otherwise, the chart will attempt to create a new Cloudant instance in your Bluemix space called `refarch-cloudantdb`.
   
8. Create the configmap and secrets

   The chart depends on a Kubernetes [configmap](https://kubernetes.io/docs/tasks/configure-pod-container/configmap/) containing the Bluemix target.
   
   Update the config-map in `kubernetes/bluemix-target-configmap.yaml` to include your Bluemix account information.
   
   Additionally, the chart uses a Kubernetes [secret](https://kubernetes.io/docs/concepts/configuration/secret/) containing the Bluemix API key.  Use the following commands to generate one:
   
   ```
   # bx iam api-key-create my-kube-api-key
   ```
   
   Copy the output and create the Kubernetes secret:
   
   ```
   # kubectl create secret bluemix-api-key --from-literal=api-key=<API KEY>
   ```
  
9. Install the helm chart

   Execute the following commands to install the chart:
   
   ```
   # cd chart
   # helm install bluecompute-customer
   ```
   
   The deployment attempts to create a Cloudant instance in your target space , bind it to your Kubernetes cluster, creates a new HS256 shared secret, deploys the customer microservice pods and services, then creates a user with username `user` and password `passw0rd`.
   
   To view the package once it's installed, use the following command:
   
   ```
   # helm list
   ```
   
   To view the pods used to create the deployment:
   
   ```
   # kubectl get pods -l app=bluecompute -l micro=customer
   ```
   
10. Verify the deployment

    The customer service can be reached by forwarding a local port to the customer service.  Select a pod used in the deployment above and execute the following command to forward local port 8080:
    
    ```
    # kubectl port-forward <pod-name> 8080
    ```
    
    Retrieve the HS256 shared secret from Kubernetes:
    
    ```
    # kubectl get secrets hs256-key -o go-template --template '{{ .data.key }}'
    ```
    
    Paste this into the `secret` section at [jwt.io](https://jwt.io), and check `base 64 encoded`, then create the JWT with the following payload:
    
    ```
    {
      "scope": [ "admin" ],
      "user_name": "admin"
    }
    ```
    
    Copy the text in `Encoded` as the JWT.  Run the following curl command to call the API from another console:
    
    ```
    curl -H "Authorization: Bearer <JWT>" http://localhost:8080/micro/customer/search?username=user
    ```
    
    This should return a user with the name `user` that was created during the deployment.