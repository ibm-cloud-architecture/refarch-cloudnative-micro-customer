###### refarch-cloudnative-micro-customer

## Microprofile based Microservice Apps Integration with IBM Cloudant

This repository contains the **MicroProfile** implementation of the **Customer Service** which is a part of 'IBM Cloud Native Reference Architecture' suite, available [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/microprofile).

<p align="center">
  <a href="https://microprofile.io/">
    <img src="https://github.com/ibm-cloud-architecture/refarch-cloudnative-wfd/blob/microprofile/static/imgs/microprofile_small.png" width="300" height="100">
  </a>
</p>

* [Introduction](#introduction)
* [How it works](#how-it-works)
* [API Endpoints](#api-endpoints)
* [Implementation](#implementation)
    + [Microprofile](#microprofile)
* [Features](#features)
* [Deploying the App](#deploying-the-app)
    + [IBM Cloud Private](#ibm-cloud-private)
    + [Minikube](#minikube)
    + [Run Customer Service locally](#run-customer-service-locally)
* [References](#references)

### Introduction

This project is built to demonstrate how to build Customer Microservices applications using Microprofile. This application provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database as part of the Customer Profile function of BlueCompute. Additionally the Auth Microservice calls this microservice to perform Customer username/password authentication.

- Based on [MicroProfile](https://microprofile.io/).
- OAuth protect the microservice REST API using JWT token signed with a HS256 shared secret.
- Persist Customer data in an [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).
- Deployment options for Minikube environment and ICP.

### How it works

The Customer Microservice serves 'IBM Cloud Native Reference Architecture' suite, available at https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes, Microservice-based reference application. Though it is a part of a bigger application, Customer service is itself an application in turn that queries the Cloudant Database.

### API Endpoints

```
GET     /customer/rest/customer           # Returns all customers.
GET     /customer/rest/customer/{id}      # Returns customer by id
```

### Implementation

### [MicroProfile](https://microprofile.io/)

MicroProfile is an open platform that optimizes the Enterprise Java for microservices architecture. In this application, we are using [**MicroProfile 1.3**](https://github.com/eclipse/microprofile-bom). This includes

- MicroProfile 1.0 ([JAX-RS 2.0](https://jcp.org/en/jsr/detail?id=339), [CDI 1.2](https://jcp.org/en/jsr/detail?id=346), and [JSON-P 1.0](https://jcp.org/en/jsr/detail?id=353))
- MicroProfile 1.1
- MicroProfile 1.2 ([MicroProfile Fault Tolerance 1.0](https://github.com/eclipse/microprofile-fault-tolerance), [MicroProfile Health Check 1.0](https://github.com/eclipse/microprofile-health), [MicroProfile JWT Authentication 1.0](https://github.com/eclipse/microprofile-jwt-auth)).
- [MicroProfile Config 1.2](https://github.com/eclipse/microprofile-config) (supercedes MicroProfile Config 1.1), [MicroProfile Metrics 1.1](https://github.com/eclipse/microprofile-metrics) (supercedes MicroProfile Metrics 1.0), [MicroProfile OpenAPI 1.0](https://github.com/eclipse/microprofile-open-api), [MicroProfile OpenTracing 1.0](https://github.com/eclipse/microprofile-opentracing), [MicroProfile Rest Client 1.0](https://github.com/eclipse/microprofile-rest-client).

You can make use of this feature by including this dependency in Maven.

```
<dependency>
    <groupId>org.eclipse.microprofile</groupId>
    <artifactId>microprofile</artifactId>
    <version>1.3</version>
    <scope>provided</scope>
    <type>pom</type>
</dependency>
```


You should also include a feature in [server.xml](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/blob/microprofile/src/main/liberty/config/server.xml).

```
<server description="Sample Liberty server">

  <featureManager>
      <feature>microprofile-1.3</feature>
  </featureManager>

  <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
      id="defaultHttpEndpoint" host="*" />

</server>
```

### Features

1. Java SE 8 - Used Java Programming language

2. [CDI 1.2](https://jcp.org/en/jsr/detail?id=346) - Used CDI for typesafe dependency injection

3. [JAX-RS 2.0](https://jcp.org/en/jsr/detail?id=339) - JAX-RS is used for providing both standard client and server APIs for RESTful communication by MicroProfile applications.

4. [Eclipse MicroProfile Config](https://github.com/eclipse/microprofile-config) - Configuration data comes from different sources like system properties, system environment variables, `.properties` file etc. These values may change dynamically. Using this feature helps us to pick up configured values immediately after they got changed.

The config values are sorted according to their ordinal. We can override the lower importance values from outside. The config sources by default, below is the order of importance.

- System.getProperties()
- System.getenv()
- all META-INF/microprofile-config.properties files on the ClassPath.

In our sample application, we obtained the configuration programatically.

5. [Microprofile Fault Tolerance](https://github.com/eclipse/microprofile-fault-tolerance) - This feature helps us to build faulttolerance microservices. In some situations, there may be some impact on the system and it may fail due to several reasons. To avoid such failures, we can design fault tolerant microservices using this feature.

In our sample application, we used @Timeout, @Retry and @Fallback.

6. [MicroProfile Health Check](https://github.com/eclipse/microprofile-health) - This feature helps us to determine the status of the service as well as its availability. This helps us to know if the service is healthy. If not, we can know the reasons behind the termination or shutdown.

In our sample application, we injected this `/health` endpoint in our liveness probes.

7. [MicroProfile Rest Client](https://github.com/eclipse/microprofile-rest-client) - This feature helps us to define typesafe rest clients. These are defined as Java interfaces. The available RESTful apis in our sample application are invoked in a type safe manner.

8. [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api) - This feature helps us to expose the API documentation for the RESTful services. It allows the developers to produce OpenAPI v3 documents for their JAX-RS applications.

In our sample application we used @OpenAPIDefinition, @Info, @Contact, @License, @APIResponses, @APIResponse, @Content, @Schema, @Operation and @Parameter annotations.

9. [MicroProfile Metrics](https://github.com/eclipse/microprofile-metrics) - This feature allows us to expose telemetry data. Using this, developers can monitor their services with the help of metrics.

In our sample application, we used @Timed, @Counted and @Metered annotations. These metrics are reused using reuse functionality. We also integrated Microprofile metrics with Prometheus.

10. [MicroProfile OpenTracing](https://github.com/eclipse/microprofile-opentracing) - This feature enables distributed tracing. It helps us to analyze the transcation flow so that the we can easily debug the problematic services and fix them. It enables and allows for custom tracing of JAX-RS and non-JAX-RS methods.

In our sample application, we used [Zipkin](https://zipkin.io/) as our distributed tracing system. We used @Traced and an ActiveSpan object to retrieve messages.


## Deploying the App

To build and run the entire BlueCompute demo application, each MicroService must be spun up together. This is due to how we
set up our Helm charts structure and how we dynamically produce our endpoints and URLs.  

Further instructions are provided [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/microprofile).

### IBM Cloud Private

To deploy it on IBM Cloud Private, please follow the instructions provided [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/microprofile#remotely-on-ibm-cloud-private).

### Minikube

To deploy it on Minikube, please follow the instructions provided [here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/microprofile#locally-in-minikube).

### Run The Customer Service locally
To deploy the app locally and test the individual service, please follow the instructions provided
[here](https://github.com/ibm-cloud-architecture/refarch-cloudnative-micro-customer/blob/microprofile/building-locally.md)
## References

1. [Microprofile](https://microprofile.io/)
2. [MicroProfile Config on Liberty](https://www.ibm.com/support/knowledgecenter/en/SSAW57_liberty/com.ibm.websphere.wlp.nd.multiplatform.doc/ae/twlp_microprofile_appconfig.html)
3. [MicroProfile Health Checks on Liberty](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_microprofile_healthcheck.html)
4. [MicroProfile OpenAPI on Liberty](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_mpopenapi.html)
5. [MicroProfile Metrics on Liberty](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/cwlp_mp_metrics_api.html)
6. [MicroProfile OpenTracing on Liberty](https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.liberty.autogen.base.doc/ae/rwlp_feature_mpOpenTracing-1.0.html)
