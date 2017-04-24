# Customer Microservice
*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative*

## Introduction

This project demonstrates how to build a Microservices application implemented as a Spring Boot application and deployed in a docker container.  It provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://console.ng.bluemix.net/docs/services/Cloudant/index.html#Cloudant) NoSQL database as part of the Customer Profile function of BlueCompute.  Additionally the [Auth Microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) calls this microservice to perform Customer username/password authentication.  This project covers the following technical areas:

- Build a microservice as a Spring Boot Java application
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

- `POST /micro/customer` (private)
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

Install the [kubectl CLI](https://kubernetes.io/docs/tasks/kubectl/install/).

### Provision Cloudant Database in Bluemix

*Note that two components use Cloudant in BlueCompute, the Customer microservice and the [Social Review microservice](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-socialreview).  If deploying both components to the same space, they can share the Cloudant database instance, as the Customer microservice saves documents to the `customers` database, and the Social Review microservice saves documents to the `socialreviewdb` and `socialreviewdb-staging` databases.*

1. Login to your Bluemix console  
2. Open browser to create Cloudant Service using this link [https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db](https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db)  
3. Name your Cloudant service name like `refarch-cloudantdb`  
4. For testing, you can select the "Lite" plan, then click "Create"  
5. Once the service has been created, note the service credentials under `Service Credentials`.  In particular, the Customer microservice requires the `url` property.

### Create a HS256 shared key

As the APIs in this microservice as OAuth protected, the HS256 shared key used to sign the JWT generated by the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is needed to validate the access token provided by the caller.

A 2048-bit secret can be generated using the following command:

```
# cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 1 | xargs echo -n
```

Note that if the [Authorization Server](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth) is also deployed, it must use the *same* HS256 shared key.

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
# docker run \
    --name customer-service \
    --publish 8080 \
    -e eureka.client.fetchRegistry=false \
    -e eureka.client.registerWithEureka=false \
    -e spring.application.name=customer-microservice \
    -e server.context-path=/micro \
    -e spring.application.cloudant.username=<Cloudant username> \
    -e spring.application.cloudant.password=<Cloudant password> \
    -e spring.application.cloudant.host=<Cloudant host> \
    -e spring.application.cloudant.port=<Cloudant port> \
    -e spring.application.cloudant.database=customers \
    -e jwt.sharedSecret=<HS256 key> \
    customer-microservice
```

## Run the as a Kubernetes Deployment locally on minikube (optional)

TODO

## Run the Docker container on Bluemix
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
   
   If it is not set, use the following command to set it:
   ```
   # bx cr namespace-add <namespace>
   ```
   
3. Tag and push the docker image to the Bluemix private registry:

   ```
   # docker tag customer-microservice registry.ng.bluemix.net/<namespace>/customer-microservice
   # docker push registry.ng.bluemix.net/<namespace>/customer-microservice
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
   
6. Bind Bluemix services to the Kubernetes cluster
   
   Bind the Cloudant service to the `default` namespace in the Kubernetes cluster:
   ```
   # bx cs cluster-service-bind <cluster_name> default <cloudant service>
   ```
   
   This should create a Kubernetes secret instance that is usually named `binding-<cloudant-svc-name>` (e.g. `binding-refarch-cloudantdb`).
   
   ```
   # kubectl get secrets
   ```
7. Create a secret for the HS256 shared key

   Create a secret for the HS256 shared key in the Kubernetes cluster.
   
   ```
   # kubectl create secret generic hs256-key --from-literal=key=<HS256-key>
   ```
   
8. Update the deployment yaml for the Customer microservice:
   
   Open and editor and update the yaml:
   ```
   # vi kubernetes/customer.yaml
   ```
   
   a. Update the the path under `spec.template.spec.containers[0].image` to correspond to the image pushed to the registry (in step 3).
   b. Update the secret name under `spec.template.spec.volumes.name.secret[0].secretName` to correspond to the name of the Kubernetes secret for the Cloudant binding (e.g. `binding-refarch-cloudantdb`)
   c. Update the secret name under `spec.template.spec.volumes.name.secret[1].secretName` to correspond to the name of the Kubernetes secret for the HS256 shared secret (e.g. `hs256-key` by default).
   
   Here is an example of what the updated deployment may look like:
   ```
   ---
   apiVersion: extensions/v1beta1
   kind: Deployment
   metadata:
     name: customer-microservice
   spec:
     replicas: 2
     template:
       metadata:
         labels:
           app: bluecompute
           tier: backend
           micro: customer
       spec:
         containers:
         - name: customer-service
           image: registry.ng.bluemix.net/myimages/customer-service:latest
           imagePullPolicy: Always
           volumeMounts:
           - mountPath: /var/run/secrets/binding-refarch-cloudantdb
             name: binding-refarch-cloudantdb
           - mountPath: /var/run/secrets/hs256-key
             name: hs256-key
           ports:
           - containerPort: 8080
         volumes:
         - name: binding-refarch-cloudantdb
           secret:
             defaultMode: 420
             secretName: binding-refarch-cloudantdb
         - name: hs256-key
           secret:
             defaultMode: 420
             secretName: hs256-key
   ```
   
9. Create the deployment

   Deploy the pods.
   ```
   # kubectl create -f kubernetes/customer.yaml
   ```

   Also deploy the service
   ```
   # kubectl create -f kubernetes/customer-service.yaml
   ```

## Validate the Customer microservice

By default, the customer microservice is not exposed outside of the cluster.  We can publish this as an ingress resource by adding the following file `customer-ingress.yaml` to validate the microservice.

```
---        
apiVersion: v1
kind: Service
metadata:
  name: customer-service
  labels:
    app: bluecompute
    tier: backend
    micro: customer
spec:
  ports:
  - protocol: TCP
    port: 8080
  selector:
    app: bluecompute
    tier: backend
    micro: customer
```

This will expose the customer microservice at the `/micro/customer` endpoint in the ingress controller, which is at `<cluster_name>.<region>.containers.mybluemix.net`.  Create this file:

```
# kubectl create -f customer-ingress.yaml
```

### Generate a temporary JWT

Use the shared secret to generate a valid JWT signed with the shared secret generated above.  You can do this at [jwt.io](https://jwt.io) using the Debugger.  Paste the secret in the bottom in the box (and leave base64 encoded unchecked).  You can use the following payload initially:

```
{
  "scope": [ "blue" ],
  "user_name": "admin"
}
```

Copy the text that appears in "Encoded"; this is the signed JWT that will be used for the "Create Customer" call.

### Create a Customer

Create a customer profile for the user `foo` with the password `bar`.  Make sure that you replace `<temp-routename>` with your temporary route.

```
# curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer <JWT>" -d '{"username": "foo", "password": "bar", "firstName": "foo", "lastName": "bar", "email": "foo@bar.com"}' -i https://<cluster_name>.<region>.containers.mybluemix.net/micro/customer
HTTP/1.1 201 Created
X-Backside-Transport: OK OK
Connection: Keep-Alive
Transfer-Encoding: chunked
Date: Wed, 08 Feb 2017 21:41:31 GMT
Location: http://<cluster_name>.<region>.containers.mybluemix.net/micro/customer/bff5631f24c849e8897645be8b66af16
X-Application-Context: zuul-proxy:8080
X-Global-Transaction-ID: 1311769839
```

Note the `Location` header returned, which contains the ID of the created customer.  For the GET calls below, copy the ID in the `Location` header (e.g. in the above, `bff5631f24c849e8897645be8b66af16`) to the `user_name` in the payload using the [jwt.io](https://jwt.io) debugger and copy the resulting encoded JWT for the next test.

### Get Customer

Verify the customer.  The caller identifies itself using the encoded `user_name` in the JWT passed in the Authorization header as Bearer token.  For testing, pass the header manually.

```
# curl -H "Auhtorization: Bearer <JWT>"  http://<cluster_name>.<region>.containers.mybluemix.net/micro/customer
{"username":"foo","password":"bar","firstName":"foo","lastName":"bar","imageUrl":null,"customerId":"bff5631f24c849e8897645be8b66af16","email":"foo@bar.com"}
```

Note that *only* the customer object identified by the encoded `user_name` is returned to the caller.

### Unmap the temporary route

When verification is complete, you may optionally delete the ingress resource if you do not want to allow public access to the customer microservice API:

```
# kubectl delete ingress customer-service
```
