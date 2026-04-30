# Roadmap 

This page list our (spring cloud azure) team's roadmap, things are expected to change :)

## 2022

## Feature Candidates 

### Security

* Support [Azure Private Link](https://docs.microsoft.com/azure/private-link/private-link-overview) in all supported azure services
* ✅ Support credential-free connection for 3rd party libraries

### Messaging

* Support AsyncAPI, integrated with https://github.com/asyncapi/java-spring-cloud-stream-template

### Framework

* ✅ Support Spring Framework 6 and Spring Boot 3.
* Support Spring Native.
* ✅ Multi-version support policy (N - 2), see Spring-Versions-Mapping for more detail about the version supported by Spring Cloud Azure

### Development Experience

* ✅ GA Spring Cloud Azure 4.0 https://spring.io/projects/spring-cloud-azure
* Azure Spring Initializr
* ✅ Spring on Azure site to provide consistent and unified doc/samples for spring users.



## 2021

| when  | what | owner | status |
| --------  | ------------------------------------------------------- | ---------------------  | ---------------------- |
| Jul | Production Ready - Spring Boot Health indicators support      | Xiaolu Dai             | ✅                    |
| Jul | Security - Realize azure credential chain to simplify and unify developer credential management experience across spring libraries. |    Xiaolu Dai         |✅|
| Aug | Production Ready - Support multiple active spring versions    | Xiaolu Dai             | ✅                     |
| Aug | AAD - Support Web-application and resource-server in one spring boot application | Rujun Chen  | ✅            |
| Sep | E2E Sample demonstrating all Azure Spring Cloud Starters      | Zhihao Guo                       |✅                       |
| Oct | Messaging - enable configurations supported by Azure SDK      | Xiaolu Dai             |          ✅              |
| Nov | Observability - Support metrics and tracing for Azure messaging and database|Xiaolu Dai|          blocked             |
| Dec | Reactive API support - Compatible with Spring Boot Webflux    |                        |          put back to backlog             |


