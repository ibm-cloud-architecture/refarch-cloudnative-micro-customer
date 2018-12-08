# refarch-cloudnative-micro-customer: Spring Boot Microservice with CouchDB Database

## Introduction
This chart will deploy a Spring Boot Application with a CouchDB database onto a Kubernetes Cluster.

![Application Architecture](https://raw.githubusercontent.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/spring/static/customer.png?raw=true)

Here is an overview of the chart's features:
- Leverage [`Spring Boot`](https://projects.spring.io/spring-boot/) framework to build a Microservices application.
- Uses [`Spring Data JPA`](http://projects.spring.io/spring-data-jpa/) to persist data to CouchDB database.
- Uses [`CouchDB`](http://couchdb.apache.org/) as the customer database.
- Uses [`Docker`](https://docs.docker.com/) to package application binary and its dependencies.
- Uses [`Helm`](https://helm.sh/) to package application and CouchDB deployment configuration and deploy to a [`Kubernetes`](https://kubernetes.io/) cluster.

## Chart Source
The source for the `Customer` chart can be found at:
* https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/tree/spring/chart/customer

The source for the `CouchDB` chart can be found at:
* https://github.com/helm/charts/tree/master/incubator/couchdb

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

## Deploy Customer Application to Kubernetes Cluster from CLI
To deploy the Customer Chart and its CouchDB dependency Chart to a Kubernetes cluster using Helm CLI, follow the instructions below:
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

# Clone customer repository:
git clone -b spring --single-branch https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer.git

# Go to Chart Directory
cd refarch-cloudnative-micro-customer/chart/customer

# Deploy Customer to Kubernetes cluster
helm upgrade --install customer --set service.type=NodePort .
```