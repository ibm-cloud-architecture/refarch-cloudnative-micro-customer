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
    1. [Microprofile](#microprofile)
5. [Features and App details](#features)
6. [Building the app](#building-the-app)
7. [Setting up Cloudant](#setting-up-cloudant)
8. [Running the app and stopping it](#running-the-app-and-stopping-it)
    1. [Pre-requisites](#pre-requisites)
    2. [Locally in Minikube](#locally-in-minikube)
    3. [Remotely in ICP](#remotely-in-icp)
9. [References](#references)

### Introduction

This project is built to demonstrate how to build Customer Microservices applications using Microprofile. This application provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database as part of the Customer Profile function of BlueCompute. Additionally the Auth Microservice calls this microservice to perform Customer username/password authentication.

- Based on [MicroProfile](https://microprofile.io/).
- OAuth protect the microservice REST API using JWT token signed with a HS256 shared secret.
- Persist Customer data in an [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).
- Deployment options for Minikube environment and ICP.

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

### Implementation

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

5. MicroProfile JWT Authentication 1.0 - Used Microprofile JWT Authentication for token based authentication. It uses OpenIDConnect based JSON Web Tokens (JWT) for role based access control of rest endpoints. This allows the system to verify, authorize and authenticate the user based the security token.

6. MicroProfile Health Check 1.0 - For MicroProfile implementations, this feature helps us to determine the status of the service as well as its availability. This helps us to know if the service is healthy. If not, we can know the reasons behind the termination or shutdown. 

In our sample application, we injected this `/health` endpoint in our liveness probes.

7. Microprofile Fault Tolerance - Helps to build faulttolerant microservices. In some situations, there may be some impact on the system and it may fail due to several reasons. To avoid such failures, we can design fault tolerant microservices using this feature.

In our sample application, we used @Timeout, @Retry and @Fallback.

8.MicroProfile Rest Client - This feature helps us to define typesafe rest clients. These are defined as Java interfaces. The available RESTful apis in our sample application are invoked in a type safe manner.

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

### Setting up Cloudant

The charts for Cloudant are included in the helm charts for Customer Service. Launching the helm charts for Customer Service also launches Cloudant.

Once the Customer service is deployed as [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/tree/microprofile#running-the-app-and-stopping-it), you can see the below.

`kubectl get deployments`

```
NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
cloudant-deployment                         1         1         1            1           2m

```

`kubectl get services`

```
NAME                       TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                          AGE
cloudant-service           NodePort    10.100.9.81      <none>        80:31222/TCP                     3m
```


### Running the app and stopping it

#### Pre-requisites

To run the Customer microservice, please complete the [Building the app](#building-the-app) section before proceeding to any of the following steps.

Also make sure [Auth](https://github.com/ibm-cloud-architecture/refarch-cloudnative-auth/tree/microprofile) service is running and Keystore is set.

1. Locally in Minikube

To run the Customer application locally on your laptop on a Kubernetes-based environment such as Minikube (which is meant to be a small development environment) we first need to get few tools installed:

- [Kubectl](https://kubernetes.io/docs/user-guide/kubectl-overview/) (Kubernetes CLI) - Follow the instructions [here](https://kubernetes.io/docs/tasks/tools/install-kubectl/) to install it on your platform.
- [Helm](https://github.com/kubernetes/helm) (Kubernetes package manager) - Follow the instructions [here](https://github.com/kubernetes/helm/blob/master/docs/install.md) to install it on your platform.

Finally, we must create a Kubernetes Cluster. As already said before, we are going to use Minikube:

- [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/) - Create a single node virtual cluster on your workstation. Follow the instructions [here](https://kubernetes.io/docs/tasks/tools/install-minikube/) to get Minikube installed on your workstation.

We not only recommend to complete the three Minikube installation steps on the link above but also read the [Running Kubernetes Locally via Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/) page for getting more familiar with Minikube.

2. Remotely in ICP

[IBM Cloud Private Cluster](https://www.ibm.com/cloud/private)

Create a Kubernetes cluster in an on-premise datacenter. The community edition (IBM Cloud private-ce) is free of charge.
Follow the instructions [here](https://www.ibm.com/support/knowledgecenter/en/SSBS6K_2.1.0.2/installing/install_containers_CE.html) to install IBM Cloud private-ce.

[Helm](https://github.com/kubernetes/helm) (Kubernetes package manager)

Follow the instructions [here](https://github.com/kubernetes/helm/blob/master/docs/install.md) to install it on your platform.
If using IBM Cloud Private version 2.1.0.2 or newer, we recommend you follow these [instructions](https://www.ibm.com/support/knowledgecenter/SSBS6K_2.1.0.2/app_center/create_helm_cli.html) to install helm.

### Locally in Minikube

#### Setting up your environment

1. Start your minikube. Run the below command.

`minikube start`

You will see output similar to this.

```
Setting up certs...
Connecting to cluster...
Setting up kubeconfig...
Starting cluster components...
Kubectl is now configured to use the cluster.
```
2. To install Tiller which is a server side component of Helm, initialize helm. Run the below command.

`helm init`

If it is successful, you will see the below output.

```
$HELM_HOME has been configured at /Users/user@ibm.com/.helm.

Tiller (the helm server side component) has been installed into your Kubernetes Cluster.
Happy Helming!
```
3. Check if your tiller is available. Run the below command.

`kubectl get deployment tiller-deploy --namespace kube-system`

If it available, you can see the availability as below.

```
NAME            DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
tiller-deploy   1         1         1            1           1m
```

4. Verify your helm before proceeding like below.

`helm version`

You will see the below output.

```
Client: &version.Version{SemVer:"v2.4.2", GitCommit:"82d8e9498d96535cc6787a6a9194a76161d29b4c", GitTreeState:"clean"}
Server: &version.Version{SemVer:"v2.5.0", GitCommit:"012cb0ac1a1b2f888144ef5a67b8dab6c2d45be6", GitTreeState:"clean"}
```

#### Running the application on Minikube

1. Build the docker image.

Before building the docker image, set the docker environment.

- Run the below command.

`minikube docker-env`

You will see the output similar to this.

```
export DOCKER_TLS_VERIFY="1"
export DOCKER_HOST="tcp://192.168.99.100:2376"
export DOCKER_CERT_PATH="/Users/user@ibm.com/.minikube/certs"
export DOCKER_API_VERSION="1.23"
# Run this command to configure your shell:
# eval $(minikube docker-env)
```
- For configuring your shell, run the below command.

`eval $(minikube docker-env)`

- Now run the docker build.

`docker build -t customer-mp:v1.0.0 .`

If it is a success, you will see the output below.

```
Successfully built 79c2f74d7hj
Successfully tagged customer-mp:v1.0.0
```

- You also need to build the `populate` image to populate the database with the default users.

`cd cloudant`

`docker build -t populate .`

If it is a success, you will see the output below.

```
Successfully built cbbdbc69a3dd
Successfully tagged populate:latest
```
Go back a directory up.
`cd ..`

2. Run the helm chart as below.

`helm install --name=customer chart/customer`

You will see message like below.

```
==> v1beta1/Deployment
NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
customer-deployment                         1         1         1            0           1s
```
Please wait till your deployment is ready. To verify run the below command and you should see the availability.

`kubectl get deployments`

You will see something like below.

```
==> v1beta1/Deployment
NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
customer-deployment                         1         1         1            1           2m
```

### Remotely in ICP

[IBM Cloud Private](https://www.ibm.com/cloud/private)

IBM Private Cloud has all the advantages of public cloud but is dedicated to single organization. You can have your own security requirements and customize the environment as well. Basically it has tight security and gives you more control along with scalability and easy to deploy options. You can run it externally or behind the firewall of your organization.

Basically this is an on-premise platform.

Includes docker container manager
Kubernetes based container orchestrator
Graphical user interface
You can find the detailed installation instructions for IBM Cloud Private [here](https://www.ibm.com/support/knowledgecenter/en/SSBS6K_2.1.0.2/installing/install_containers_CE.html)

#### Pushing the image to Private Registry

1. Now run the docker build.

`docker build -t customer-mp:v1.0.0 .`

If it is a success, you will see the below output.

```
Successfully built 79c2f74d7hj
Successfully tagged customer-mp:v1.0.0
```

2. Tag the image to your private registry.

`docker tag customer-mp:v1.0.0 <Your ICP registry>/customer-mp:v1.0.0`

3. Push the image to your private registry.

`docker push <Your ICP registry>/customer-mp:v1.0.0`

You should see something like below.

```
v1.0.0: digest: sha256:bb0df0cd06e4b97cbe89d23393253b33a5319ac3c08ddffbc5b386d162a27dd4 size: 3873
```
#### Running the application on ICP

1. Your [IBM Cloud Private Cluster](https://www.ibm.com/cloud/private) should be up and running.

2. Log in to the IBM Cloud Private.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/icp_dashboard.png">
</p>

3. Go to `admin > Configure Client`.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/client_config.png">
</p>

4. Grab the kubectl configuration commands.

<p align="center">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/blob/microprofile/static/imgs/kube_cmds.png">
</p>

5. Run those commands in your terminal.

6. If successful, you should see something like below.

```
Switched to context "xxx-cluster.icp-context".
```
7. Run the below command.

`helm init --client-only`

You will see the below

```
$HELM_HOME has been configured at /Users/user@ibm.com/.helm.
Not installing Tiller due to 'client-only' flag having been set
Happy Helming!
```

8. Verify the helm version

`helm version --tls`

You will see something like below.

```
Client: &version.Version{SemVer:"v2.7.2+icp", GitCommit:"d41a5c2da480efc555ddca57d3972bcad3351801", GitTreeState:"dirty"}
Server: &version.Version{SemVer:"v2.7.2+icp", GitCommit:"d41a5c2da480efc555ddca57d3972bcad3351801", GitTreeState:"dirty"}
```
9. Before running the helm chart in minikube, access [values.yaml](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-inventory/blob/microprofile/inventory/chart/inventory/values.yaml) and replace the repository with the your IBM Cloud Private .

`repository: <Your IBM Cloud Private Docker registry>`

Then run the helm chart

`helm install --name=customer chart/customer --tls`

You will see message like below.

```
==> v1beta1/Deployment
NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
customer-deployment                         1         1         1            0           1s
```
Please wait till your deployment is ready. To verify run the below command and you should see the availability.

`kubectl get deployments`

You will see something like below.

```
==> v1beta1/Deployment
NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
customer-deployment                         1         1         1            1           1m     
```
**NOTE**: If you are using a version of ICP older than 2.1.0.2, you don't need to add the --tls at the end of the helm command.

### References

1. [Developer Tools CLI](https://console.bluemix.net/docs/cloudnative/dev_cli.html#developercli)
2. [IBM Cloud Private](https://www.ibm.com/support/knowledgecenter/en/SSBS6K_2.1.0/kc_welcome_containers.html)
3. [IBM Cloud Private Installation](https://github.com/ibm-cloud-architecture/refarch-privatecloud)
4. [IBM Cloud Private version 2.1.0.2 Helm instructions](https://www.ibm.com/support/knowledgecenter/SSBS6K_2.1.0.2/app_center/create_helm_cli.html)
5. [Microprofile](https://microprofile.io/)
