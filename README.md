###### refarch-cloudnative-micro-customer

# Customer Service

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes*

## Table of Contents

* [Introduction](#introduction)
* [Implementation](#implementation)
* [References](#references)

## Introduction

This project is built to demonstrate the integration of [IBM Cloudant](https://www.ibm.com/cloud/cloudant) with Java Microservices. This application provides basic operations of creating and querying customer profiles from [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database as part of the Customer Profile function of BlueCompute. Additionally the Auth Microservice calls this microservice to perform Customer username/password authentication.

- Secured REST APIs.
- Persist Customer data in an [IBM Cloudant](https://www.ibm.com/cloud/cloudant) NoSQL database using the official [Cloudant Java library](https://github.com/cloudant/java-cloudant).

<p align="center">
    <img src="images/customer_microservice.png">
</p>

## Implementation

- [Microprofile](../../tree/microprofile/) - leverages the Microprofile framework.
- [Spring](../../tree/spring/) - leverages Spring Boot as the Java programming model of choice.

## References

- [Java MicroProfile](https://microprofile.io/)
- [Spring Boot](https://projects.spring.io/spring-boot/)
- [Kubernetes](https://kubernetes.io/)
- [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/)
- [Docker Edge](https://docs.docker.com/edge/)
- [IBM Cloud](https://www.ibm.com/cloud/)
- [IBM Cloud Private](https://www.ibm.com/cloud-computing/products/ibm-cloud-private/)
