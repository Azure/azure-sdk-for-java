# Azure Spring Boot client library for Java

## Getting started
### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support
* This repository supports Spring Boot 2.2.x, 2.3.x and 2.4.x. Please read [Spring Boot Dependency Mapping][spring-boot-dependency-mapping] for dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven][maven] 3.0 and above

## Key concepts
### Azure Spring Boot

Below starters are available with the latest release version. We recommend users to leverage the latest version for bug fix and new features.
You can find them in [Maven Central Repository][maven-central-repository]. The starters of active directory, storage and keyvault secrets are also available in [Spring Initializr][spring-initializr]. 

> ❗ From Azure Spring Boot starters 3.0.0 onwards, we changed the groupId from `com.microsoft.azure` to `com.azure.spring`, and changed artifactIds, too.

Artifact Id | Version for Spring Boot 2.3.x and 2.2.x | Version for Spring Boot 2.4.x
---|:---:|---
[azure-spring-boot-starter-active-directory][aad-starter-readme] | [![Maven Central][new-aad-starter-mvn-2.3.x-img]][new-aad-starter-mvn-2.3.x] | [![Maven Central][new-aad-starter-mvn-2.4.x-img]][new-aad-starter-mvn-2.4.x]
[azure-spring-boot-starter-active-directory-b2c][aad-b2c-starter-readme] | [![Maven Central][new-aad-b2c-starter-mvn-2.3.x-img]][new-aad-b2c-starter-mvn-2.3.x]| [![Maven Central][new-aad-b2c-starter-mvn-2.4.x-img]][new-aad-b2c-starter-mvn-2.4.x]
[azure-spring-boot-starter-cosmos][cosmos-starter-readme] | [![Maven Central][new-cosmosdb-starter-mvn-2.3.x-img]][new-cosmosdb-starter-mvn-2.3.x] | [![Maven Central][new-cosmosdb-starter-mvn-2.4.x-img]][new-cosmosdb-starter-mvn-2.4.x]
[azure-spring-boot-starter-keyvault-certificates][keyvault-certificates-starter-readme] | [![Maven Central][new-keyvault-certificates-starter-mvn-2.3.x-img]][new-keyvault-certificates-starter-mvn-2.3.x] | [![Maven Central][new-keyvault-certificates-starter-mvn-2.4.x-img]][new-keyvault-certificates-starter-mvn-2.4.x]
[azure-spring-boot-starter-keyvault-secrets][keyvault-secrets-starter-readme] | [![Maven Central][new-keyvault-secrets-starter-mvn-2.3.x-img]][new-keyvault-secrets-starter-mvn-2.3.x] | [![Maven Central][new-keyvault-secrets-starter-mvn-2.4.x-img]][new-keyvault-secrets-starter-mvn-2.4.x]
[azure-spring-boot-starter-servicebus-jms][jms-starter-readme] | [![Maven Central][new-jms-starter-mvn-2.3.x-img]][new-jms-starter-mvn-2.3.x] | [![Maven Central][new-jms-starter-mvn-2.4.x-img]][new-jms-starter-mvn-2.4.x]
[azure-spring-boot-starter-storage][storage-starter-readme] | [![Maven Central][new-storage-starter-mvn-2.3.x-img]][new-storage-starter-mvn-2.3.x] | [![Maven Central][new-storage-starter-mvn-2.4.x-img]][new-storage-starter-mvn-2.4.x]

Below packages are Azure Spring Boot starters with original artifactIds and groupId of `com.microsoft.azure`.

Artifact Id | Version for Spring Boot 2.3.x | Version for Spring Boot 2.2.x | Version for Spring Boot 2.1.x | Version for Spring Boot 2.0.x
---|:---:|:---:|:---:|:---:
azure-active-directory-b2c-spring-boot-starter | [![Maven Central][aad-b2c-starter-mvn-2.3.x-img]][aad-b2c-starter-mvn-2.3.x] | [![Maven Central][aad-b2c-starter-mvn-2.2.x-img]][aad-b2c-starter-mvn-2.2.x] | [![Maven Central][aad-b2c-starter-mvn-2.1.x-img]][aad-b2c-starter-mvn-2.1.x] | N/A
azure-active-directory-spring-boot-starter | [![Maven Central][aad-starter-mvn-2.3.x-img]][aad-starter-mvn-2.3.x] | [![Maven Central][aad-starter-mvn-2.2.x-img]][aad-starter-mvn-2.2.x] | [![][aad-starter-mvn-2.1.x-img]][aad-starter-mvn-2.1.x] | [![][aad-starter-mvn-2.0.x-img]][aad-starter-mvn-2.0.x]
azure-cosmosdb-spring-boot-starter | [![Maven Central][cosmosdb-starter-mvn-2.3.x-img]][cosmosdb-starter-mvn-2.3.x] | [![Maven Central][cosmosdb-starter-mvn-2.2.x-img]][cosmosdb-starter-mvn-2.2.x] | [![][cosmosdb-starter-mvn-2.1.x-img]][cosmosdb-starter-mvn-2.1.x] | [![][cosmosdb-starter-mvn-2.0.x-img]][cosmosdb-starter-mvn-2.0.x]
azure-keyvault-secrets-spring-boot-starter | [![Maven Central][keyvault-starter-mvn-2.3.x-img]][keyvault-starter-mvn-2.3.x] | [![Maven Central][keyvault-starter-mvn-2.2.x-img]][keyvault-starter-mvn-2.2.x] | [![][keyvault-starter-mvn-2.1.x-img]][keyvault-starter-mvn-2.1.x] | [![][keyvault-starter-mvn-2.0.x-img]][keyvault-starter-mvn-2.0.x]
azure-mediaservices-spring-boot-starter **(Deprecated)** | N/A | [![Maven Central][mediaservices-starter-mvn-2.2.x-img]][mediaservices-starter-mvn-2.2.x] | [![][mediaservices-starter-mvn-2.1.x-img]][mediaservices-starter-mvn-2.1.x] | [![][mediaservices-starter-mvn-2.0.x-img]][mediaservices-starter-mvn-2.0.x]
azure-servicebus-jms-spring-boot-starter | [![Maven Central][jms-starter-mvn-2.3.x-img]][jms-starter-mvn-2.3.x] | [![Maven Central][jms-starter-mvn-2.2.x-img]][jms-starter-mvn-2.2.x] | [![Maven Central][jms-starter-mvn-2.1.x-img]][jms-starter-mvn-2.1.x] | N/A
azure-servicebus-spring-boot-starter **(Deprecated)** | N/A | [![Maven Central][servicebus-starter-mvn-2.2.x-img]][servicebus-starter-mvn-2.2.x] | [![][servicebus-starter-mvn-2.1.x-img]][servicebus-starter-mvn-2.1.x] | [![][servicebus-starter-mvn-2.0.x-img]][servicebus-starter-mvn-2.0.x]
azure-spring-boot-metrics-starter **(Deprecated)** | [![Maven Central][metrics-starter-mvn-2.3.x-img]][metrics-starter-mvn-2.3.x] | [![Maven Central][metrics-starter-mvn-2.2.x-img]][metrics-starter-mvn-2.2.x] | [![][metrics-starter-mvn-2.1.x-img]][metrics-starter-mvn-2.1.x] | [![][metrics-starter-mvn-2.0.x-img]][metrics-starter-mvn-2.0.x]
azure-storage-spring-boot-starter | N/A | [![Maven Central][storage-starter-mvn-2.2.x-img]][storage-starter-mvn-2.2.x] | [![][storage-starter-mvn-2.1.x-img]][storage-starter-mvn-2.1.x] | [![][storage-starter-mvn-2.0.x-img]][storage-starter-mvn-2.0.x]

### Azure Spring Cloud

[Spring Cloud][spring-cloud] provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes. 

#### Module and Starter

Below packages are available with latest release version. **We recommend users to leverage latest version for bug fix and new features.**

##### [Spring Cloud Stream][spring-cloud-stream]
> ❗ From Azure Spring Cloud 2.0.0 onwards, we changed the groupId from `com.microsoft.azure` to `com.azure.spring`, and changed artifactIds, too.

Artifact Id | Version for Spring Boot 2.3.x and 2.2.x | Version for Spring Boot 2.4.x
------ |--------- |--- 
[azure-spring-cloud-starter-cache][azure-spring-cloud-starter-cache-readme] | [![Maven Central][azure-spring-cloud-starter-cache-mvn-2.2.x-img]][azure-spring-cloud-starter-cache-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-starter-cache-mvn-2.3.x-img]][azure-spring-cloud-starter-cache-mvn-2.3.x]
[azure-spring-cloud-stream-binder-eventhubs][azure-spring-cloud-stream-binder-eventhubs-readme] | [![Maven Central][azure-spring-cloud-stream-binder-eventhubs-mvn-2.2.x-img]][azure-spring-cloud-stream-binder-eventhubs-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-stream-binder-eventhubs-mvn-2.3.x-img]][azure-spring-cloud-stream-binder-eventhubs-mvn-2.3.x]
[azure-spring-cloud-starter-eventhubs-kafka][azure-spring-cloud-starter-eventhubs-kafka-readme] | [![Maven Central][azure-spring-cloud-starter-eventhubs-kafka-mvn-2.2.x-img]][azure-spring-cloud-starter-eventhubs-kafka-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-starter-eventhubs-kafka-mvn-2.3.x-img]][azure-spring-cloud-starter-eventhubs-kafka-mvn-2.3.x]
[azure-spring-cloud-starter-eventhubs][azure-spring-cloud-starter-eventhubs-readme] | [![Maven Central][azure-spring-cloud-starter-eventhubs-mvn-2.2.x-img]][azure-spring-cloud-starter-eventhubs-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-starter-eventhubs-mvn-2.3.x-img]][azure-spring-cloud-starter-eventhubs-mvn-2.3.x]
[azure-spring-integration-eventhubs][azure-spring-integration-eventhubs-readme] | [![Maven Central][azure-spring-integration-eventhubs-mvn-2.2.x-img]][azure-spring-integration-eventhubs-mvn-2.2.x] | [![Maven Central][azure-spring-integration-eventhubs-mvn-2.3.x-img]][azure-spring-integration-eventhubs-mvn-2.3.x]
[azure-spring-cloud-stream-binder-servicebus-queue][azure-spring-cloud-stream-binder-servicebus-queue-readme] | [![Maven Central][azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.2.x-img]][azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.3.x-img]][azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.3.x]
[azure-spring-cloud-stream-binder-servicebus-topic][azure-spring-cloud-stream-binder-servicebus-topic-readme] | [![Maven Central][azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.2.x-img]][azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.3.x-img]][azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.3.x]
[azure-spring-cloud-starter-servicebus][azure-spring-cloud-starter-servicebus-readme] | [![Maven Central][azure-spring-cloud-starter-servicebus-mvn-2.2.x-img]][azure-spring-cloud-starter-servicebus-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-starter-servicebus-mvn-2.3.x-img]][azure-spring-cloud-starter-servicebus-mvn-2.3.x]
[azure-spring-integration-servicebus][azure-spring-integration-servicebus-readme] | [![Maven Central][azure-spring-integration-servicebus-mvn-2.2.x-img]][azure-spring-integration-servicebus-mvn-2.2.x] | [![Maven Central][azure-spring-integration-servicebus-mvn-2.3.x-img]][azure-spring-integration-servicebus-mvn-2.3.x]
[azure-spring-cloud-starter-storage-queue][azure-spring-cloud-starter-storage-queue-readme] | [![Maven Central][azure-spring-cloud-starter-storage-queue-mvn-2.2.x-img]][azure-spring-cloud-starter-storage-queue-mvn-2.2.x] | [![Maven Central][azure-spring-cloud-starter-storage-queue-mvn-2.3.x-img]][azure-spring-cloud-starter-storage-queue-mvn-2.3.x]
[azure-spring-integration-storage-queue][azure-spring-integration-storage-queue-readme] | [![Maven Central][azure-spring-integration-servicebus-mvn-2.2.x-img]][azure-spring-integration-servicebus-mvn-2.2.x] | [![Maven Central][azure-spring-integration-servicebus-mvn-2.3.x-img]][azure-spring-integration-servicebus-mvn-2.3.x]

Below packages are Azure Spring Cloud packages with original artifactIds and groupId of `com.microsoft.azure`.

Artifact Id | Version
------ |---
spring-cloud-azure-appconfiguration-config-web | [![Maven Central][spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x-img]][spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x] 
spring-cloud-azure-appconfiguration-config | [![Maven Central][spring-cloud-azure-appconfiguration-config-mvn-1.2.x-img]][spring-cloud-azure-appconfiguration-config-mvn-1.2.x] 
spring-cloud-starter-azure-appconfiguration-config | [![Maven Central][spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x-img]][spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x]
spring-cloud-azure-feature-management-web | [![Maven Central][spring-cloud-azure-feature-management-web-mvn-1.2.x-img]][spring-cloud-azure-feature-management-web-mvn-1.2.x]
spring-cloud-azure-feature-management | [![Maven Central][spring-cloud-azure-feature-management-mvn-1.2.x-img]][spring-cloud-azure-feature-management-mvn-1.2.x]
spring-starter-azure-cache | [![Maven Central][azure-spring-cloud-starter-cache-mvn-1.2.x-img]][azure-spring-cloud-starter-cache-mvn-1.2.x]
spring-cloud-azure-eventhubs-stream-binder  | [![Maven Central][spring-cloud-azure-eventhubs-stream-binder-mvn-1.2.x-img]][spring-cloud-azure-eventhubs-stream-binder-mvn-1.2.x]
spring-cloud-starter-azure-eventhubs | [![Maven Central][spring-cloud-starter-azure-eventhubs-mvn-1.2.x-img]][spring-cloud-starter-azure-eventhubs-mvn-1.2.x]
spring-cloud-starter-azure-eventhubs-kafka | [![Maven Central][spring-cloud-starter-azure-eventhubs-kafka-mvn-1.2.x-img]][spring-cloud-starter-azure-eventhubs-kafka-mvn-1.2.x]
spring-cloud-azure-servicebus-queue-stream-binder | [![Maven Central][spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x-img]][spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x]
spring-cloud-azure-servicebus-topic-stream-binder | [![Maven Central][spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x-img]][spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x]
spring-cloud-starter-azure-servicebus | [![Maven Central][spring-cloud-starter-azure-servicebus-mvn-1.2.x-img]][spring-cloud-starter-azure-servicebus-mvn-1.2.x]
spring-cloud-starter-azure-storage-queue | [![Maven Central][spring-cloud-starter-azure-storage-queue-mvn-1.2.x-img]][spring-cloud-starter-azure-storage-queue-mvn-1.2.x]
spring-starter-azure-storage **(Deprecated)** | [![Maven Central][azure-spring-starter-storage-mvn-1.2.x-img]][azure-spring-starter-storage-mvn-1.2.x]

### Daily built dev feed  
Daily built beta feeds of all Spring packages from `master` branch are available. The packages are published to an Azure Artifacts public feed hosted at the following URL: 
> https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java

For developers wishing to use the daily packages, refer to [the connect to feed][connect-to-feed-link] instructions in Azure Artifacts.

Note: the daily package feed is considered volatile and taking dependencies on a daily package should be considered a temporary arrangement. We reserve the right to remove packages from this feed at any point in time.
## Examples
You could check [Spring Integration Guides][spring-integration-guides] articles to learn more on usage of specific starters.

## Troubleshooting
## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][spring-contributing] to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here][azure-sdk-for-java-issues].

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter][gitter-spring-on-azure-img]][gitter-spring-on-azure]

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here][azure-sdk-for-java-compare]. When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct][codeofconduct]. For more information see the [Code of Conduct FAQ][codeofconduct-faq] or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy][privacy-statement] statement to learn more.


[maven]: https://maven.apache.org/
[spring-initializr]: https://start.spring.io/
[maven-central-repository]: https://search.maven.org/
[spring-cloud]: https://spring.io/projects/spring-cloud
[spring-cloud-stream]: https://cloud.spring.io/spring-cloud-stream/
[spring-boot-dependency-mapping]: https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping

[spring-integration-guides]: https://docs.microsoft.com/java/azure/spring-framework
[connect-to-feed-link]: https://dev.azure.com/azure-sdk/public/_packaging?_a=connect&feed=azure-sdk-for-java

[spring-contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[azure-sdk-for-java-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[gitter-spring-on-azure-img]: https://badges.gitter.im/Microsoft/spring-on-azure.svg
[gitter-spring-on-azure]: https://gitter.im/Microsoft/spring-on-azure
[azure-sdk-for-java-compare]: https://github.com/Azure/azure-sdk-for-java/compare

[codeofconduct]: https://opensource.microsoft.com/codeofconduct/faq/
[codeofconduct-faq]: https://opensource.microsoft.com/codeofconduct/faq/
[privacy-statement]: https://privacy.microsoft.com/privacystatement

<!-- Begin Track2 Spring Boot starters -->
[aad-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-active-directory
[new-aad-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-active-directory/3.2.svg
[new-aad-starter-mvn-2.3.x]: https://search.maven.org/search?q=g:com.azure.spring%20AND%20a:azure-spring-boot-starter-active-directory%20AND%20v:3.2.*
[new-aad-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-active-directory.svg
[new-aad-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-active-directory%22

[aad-b2c-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c
[new-aad-b2c-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-active-directory-b2c/3.2.svg
[new-aad-b2c-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-active-directory-b2c%22AND%20v:3.2.*
[new-aad-b2c-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-active-directory-b2c.svg
[new-aad-b2c-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-active-directory-b2c%22

[cosmos-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-cosmos
[new-cosmosdb-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-cosmos/3.2.svg
[new-cosmosdb-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-cosmos%22AND%20v:3.2.*
[new-cosmosdb-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-cosmos.svg
[new-cosmosdb-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-cosmos%22

[keyvault-certificates-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-keyvault-certificates
[new-keyvault-certificates-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-keyvault-certificates.svg
[new-keyvault-certificates-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-keyvault-certificates%22
[new-keyvault-certificates-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-keyvault-certificates.svg
[new-keyvault-certificates-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-keyvault-certificates%22

[keyvault-secrets-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-keyvault-secrets
[new-keyvault-secrets-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-keyvault-secrets/3.2.svg
[new-keyvault-secrets-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-keyvault-secrets%22AND%20v:3.2.*
[new-keyvault-secrets-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-keyvault-secrets.svg
[new-keyvault-secrets-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-keyvault-secrets%22

[storage-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-storage
[new-storage-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-storage/3.2.svg
[new-storage-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-storage%22AND%20v:3.2.*
[new-storage-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-storage.svg
[new-storage-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-storage%22

[servicebus-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-servicebus
[servicebus-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg
[servicebus-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22
[servicebus-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg
[servicebus-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*
[servicebus-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg
[servicebus-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*

[jms-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-servicebus-jms
[new-jms-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-servicebus-jms/3.2.svg
[new-jms-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-servicebus-jms%22AND%20v:3.2.*
[new-jms-starter-mvn-2.4.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-boot-starter-servicebus-jms.svg
[new-jms-starter-mvn-2.4.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.azure.spring%22%20AND%20a%3A%22azure-spring-boot-starter-servicebus-jms%22
<!-- End Track2 Spring Boot starters -->


<!-- Begin Track2 Spring Cloud -->
[azure-spring-cloud-stream-binder-eventhubs-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-cloud-stream-binder-eventhubs
[azure-spring-cloud-stream-binder-eventhubs-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-eventhubs/2.2.svg
[azure-spring-cloud-stream-binder-eventhubs-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-eventhubs%22AND%20v:2.2.*
[azure-spring-cloud-stream-binder-eventhubs-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-eventhubs.svg
[azure-spring-cloud-stream-binder-eventhubs-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-eventhubs%22

[azure-spring-cloud-stream-binder-servicebus-queue-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-queue
[azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-queue/2.2.svg
[azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-servicebus-queue%22AND%20v:2.2.*
[azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-queue.svg
[azure-spring-cloud-stream-binder-servicebus-queue-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-servicebus-queue%22

[azure-spring-cloud-stream-binder-servicebus-topic-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-topic
[azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-topic/2.2.svg
[azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-servicebus-topic%22AND%20v:2.2.*
[azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-topic.svg
[azure-spring-cloud-stream-binder-servicebus-topic-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-stream-binder-servicebus-topic%22

[azure-spring-integration-eventhubs-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-integration-eventhubs
[azure-spring-integration-eventhubs-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-eventhubs/2.2.svg
[azure-spring-integration-eventhubs-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-eventhubs%22AND%20v:2.2.*
[azure-spring-integration-eventhubs-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-eventhubs.svg
[azure-spring-integration-eventhubs-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-eventhubs%22

[azure-spring-integration-servicebus-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-integration-servicebus
[azure-spring-integration-servicebus-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-servicebus/2.2.svg
[azure-spring-integration-servicebus-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-servicebus%22AND%20v:2.2.*
[azure-spring-integration-servicebus-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-servicebus.svg
[azure-spring-integration-servicebus-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-servicebus%22

[azure-spring-integration-storage-queue-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-integration-storage-queue
[azure-spring-integration-storage-queue-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-storage-queue/2.2.svg
[azure-spring-integration-storage-queue-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-storage-queue%22AND%20v:2.2.*
[azure-spring-integration-storage-queue-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-integration-storage-queue.svg
[azure-spring-integration-storage-queue-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-integration-storage-queue%22

[azure-spring-cloud-starter-eventhubs-readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-starter-eventhubs
[azure-spring-cloud-starter-eventhubs-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-eventhubs/2.2.svg
[azure-spring-cloud-starter-eventhubs-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-eventhubs%22AND%20v:2.2.*
[azure-spring-cloud-starter-eventhubs-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-eventhubs.svg
[azure-spring-cloud-starter-eventhubs-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-eventhubs%22

[azure-spring-cloud-starter-eventhubs-kafka-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-cloud-starter-eventhubs-kafka
[azure-spring-cloud-starter-eventhubs-kafka-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-eventhubs-kafka/2.2.svg
[azure-spring-cloud-starter-eventhubs-kafka-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-eventhubs-kafka%22AND%20v:2.2.*
[azure-spring-cloud-starter-eventhubs-kafka-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-eventhubs-kafka.svg
[azure-spring-cloud-starter-eventhubs-kafka-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-eventhubs-kafka%22

[azure-spring-cloud-starter-servicebus-readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-starter-servicebus
[azure-spring-cloud-starter-servicebus-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-servicebus/2.2.svg
[azure-spring-cloud-starter-servicebus-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-servicebus%22AND%20v:2.2.*
[azure-spring-cloud-starter-servicebus-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-servicebus.svg
[azure-spring-cloud-starter-servicebus-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-servicebus%22

[azure-spring-cloud-starter-storage-queue-readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-starter-storage-queue
[azure-spring-cloud-starter-storage-queue-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-storage-queue/2.2.svg
[azure-spring-cloud-starter-storage-queue-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-storage-queue%22AND%20v:2.2.*
[azure-spring-cloud-starter-storage-queue-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-storage-queue.svg
[azure-spring-cloud-starter-storage-queue-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-storage-queue%22

[azure-spring-cloud-starter-cache-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-cloud-starter-cache
[azure-spring-cloud-starter-cache-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-cache/1.2.svg
[azure-spring-cloud-starter-cache-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-cache%20AND%20v:1.2.*
[azure-spring-cloud-starter-cache-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-cache/2.2.svg
[azure-spring-cloud-starter-cache-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-cache%22AND%20v:2.2.*
[azure-spring-cloud-starter-cache-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.azure.spring/azure-spring-cloud-starter-cache.svg
[azure-spring-cloud-starter-cache-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-spring-cloud-starter-cache%22
<!-- End Track2 Spring Cloud -->

<!-- Begin Track1 Spring Boot starters -->
[aad-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg
[aad-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*
[aad-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg
[aad-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*
[aad-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg
[aad-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.2.*
[aad-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.3.svg
[aad-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%20AND%20v:2.3.*

[aad-b2c-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg
[aad-b2c-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*
[aad-b2c-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg
[aad-b2c-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*
[aad-b2c-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.3.svg
[aad-b2c-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.3.*

[cosmosdb-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg
[cosmosdb-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*
[cosmosdb-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg
[cosmosdb-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*
[cosmosdb-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg
[cosmosdb-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*
[cosmosdb-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.3.svg
[cosmosdb-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-starter%20AND%20v:2.3.*

[jms-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg
[jms-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*
[jms-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg
[jms-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*
[jms-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.3.svg
[jms-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%20AND%20v:2.3.*

[keyvault-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg
[keyvault-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*
[keyvault-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg
[keyvault-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*
[keyvault-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg
[keyvault-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*
[keyvault-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.3.svg
[keyvault-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.3.*

[mediaservices-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-mediaservices
[mediaservices-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg
[mediaservices-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*
[mediaservices-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg
[mediaservices-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*
[mediaservices-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg
[mediaservices-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22

[metrics-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg
[metrics-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*
[metrics-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg
[metrics-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*
[metrics-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg
[metrics-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*
[metrics-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg
[metrics-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22

[spring-starter-storage-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg
[spring-starter-storage-mvn-2.3.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20%20a:spring-starter-azure-storage

[storage-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg
[storage-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*
[storage-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg
[storage-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*
[storage-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.2.svg
[storage-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%20AND%20v:2.2.*
<!-- End Track1 Spring Boot starters -->


<!-- Begin Track1 Spring Cloud -->
[azure-spring-starter-storage-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-starter-storage
[azure-spring-starter-storage-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg
[azure-spring-starter-storage-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-storage%22

[spring-cloud-azure-eventhubs-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-eventhubs-stream-binder/1.2.svg
[spring-cloud-azure-eventhubs-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-eventhubs-stream-binder%20AND%20v:1.2.*

[spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder/1.2.svg
[spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-topic-stream-binder%20AND%20v:1.2.*

[spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-queue-stream-binder/1.2.svg
[spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-queue-stream-binder%20AND%20v:1.2.*

[spring-cloud-starter-azure-eventhubs-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs/1.2.svg
[spring-cloud-starter-azure-eventhubs-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs%20AND%20v:1.2.*

[spring-cloud-starter-azure-eventhubs-kafka-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs-kafka/1.2.svg
[spring-cloud-starter-azure-eventhubs-kafka-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs-kafka%20AND%20v:1.2.*

[spring-cloud-starter-azure-servicebus-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-servicebus/1.2.svg
[spring-cloud-starter-azure-servicebus-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-servicebus%20AND%20v:1.2.*

[spring-cloud-starter-azure-storage-queue-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-storage-queue/1.2.svg
[spring-cloud-starter-azure-storage-queue-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-storage-queue%20AND%20v:1.2.*

[spring-cloud-azure-appconfiguration-config-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/spring-cloud-starter-azure-appconfiguration-config
[spring-cloud-azure-appconfiguration-config-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config.svg
[spring-cloud-azure-appconfiguration-config-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-appconfiguration-config%22

[spring-cloud-azure-appconfiguration-config-web-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/spring-cloud-azure-appconfiguration-config-web
[spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config-web.svg
[spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-appconfiguration-config-web

[spring-cloud-starter-azure-appconfiguration-config-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/spring-cloud-starter-azure-appconfiguration-config
[spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-appconfiguration-config.svg
[spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-starter-azure-appconfiguration-config

[spring-cloud-azure-feature-management-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/spring-cloud-azure-feature-management
[spring-cloud-azure-feature-management-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management.svg
[spring-cloud-azure-feature-management-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-feature-management

[spring-cloud-azure-feature-management-web-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/spring-cloud-azure-feature-management-web
[spring-cloud-azure-feature-management-web-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management-web.svg
[spring-cloud-azure-feature-management-web-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-feature-management-web
<!-- End Track1 Spring Cloud -->


