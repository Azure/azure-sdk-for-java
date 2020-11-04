# Azure Spring Boot client library for Java

## Getting started
### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support
* This repository supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping][spring-boot-dependency-mapping] for dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven][maven] 3.0 and above

## Key concepts
### Azure Spring Boot

Below starters are available with latest release version. We recommend users to leverage latest version for bug fix and new features.
You can find them in [Maven Central Repository][maven-central-repository].
The first three starters are also available in [Spring Initializr][spring-initializr]. 

Artifact Id | Version for Spring Boot 2.3.x | Version for Spring Boot 2.2.x | Version for Spring Boot 2.1.x | Version for Spring Boot 2.0.x
---|:---:|:---:|:---:|:---:
azure-active-directory-spring-boot-starter | [![Maven Central][aad-starter-mvn-2.3.x-img]][aad-starter-mvn-2.3.x] | [![Maven Central][aad-starter-mvn-2.2.x-img]][aad-starter-mvn-2.2.x] | [![][aad-starter-mvn-2.1.x-img]][aad-starter-mvn-2.1.x] | [![][aad-starter-mvn-2.0.x-img]][aad-starter-mvn-2.0.x]
[azure-spring-boot-starter-active-directory][aad-starter-readme] | N/A | N/A | N/A | N/A
[azure-storage-spring-boot-starter][storage-starter-readme] | N/A | [![Maven Central][storage-starter-mvn-2.2.x-img]][storage-starter-mvn-2.2.x] | [![][storage-starter-mvn-2.1.x-img]][storage-starter-mvn-2.1.x] | [![][storage-starter-mvn-2.0.x-img]][storage-starter-mvn-2.0.x]
spring-starter-azure-storage | [![Maven Central][spring-starter-storage-mvn-2.3.x-img]][spring-starter-storage-mvn-2.3.x] | N/A | N/A | N/A
[azure-spring-starter-storage][spring-starter-storage-readme] | N/A | N/A | N/A | N/A
azure-keyvault-secrets-spring-boot-starter | [![Maven Central][keyvault-starter-mvn-2.3.x-img]][keyvault-starter-mvn-2.3.x] | [![Maven Central][keyvault-starter-mvn-2.2.x-img]][keyvault-starter-mvn-2.2.x] | [![][keyvault-starter-mvn-2.1.x-img]][keyvault-starter-mvn-2.1.x] | [![][keyvault-starter-mvn-2.0.x-img]][keyvault-starter-mvn-2.0.x]
[azure-spring-boot-starter-keyvault-secrets][keyvault-starter-readme] | N/A | N/A | N/A | N/A
[azure-spring-boot-starter-keyvault-certificates][keyvault-starter-certificates-readme] | N/A | N/A | N/A | N/A
azure-active-directory-b2c-spring-boot-starter | [![Maven Central][aad-b2c-starter-mvn-2.3.x-img]][aad-b2c-starter-mvn-2.3.x] | [![Maven Central][aad-b2c-starter-mvn-2.2.x-img]][aad-b2c-starter-mvn-2.2.x] | [![Maven Central][aad-b2c-starter-mvn-2.1.x-img]][aad-b2c-starter-mvn-2.1.x] | N/A
[azure-spring-boot-starter-active-directory-b2c][aad-b2c-starter-readme] | N/A | N/A | N/A | N/A
azure-cosmosdb-spring-boot-starter | [![Maven Central][cosmosdb-starter-mvn-2.3.x-img]][cosmosdb-starter-mvn-2.3.x] | [![Maven Central][cosmosdb-starter-mvn-2.2.x-img]][cosmosdb-starter-mvn-2.2.x] | [![][cosmosdb-starter-mvn-2.1.x-img]][cosmosdb-starter-mvn-2.1.x] | [![][cosmosdb-starter-mvn-2.0.x-img]][cosmosdb-starter-mvn-2.0.x]
[azure-spring-boot-starter-cosmos][cosmos-starter-readme] | N/A | N/A | N/A | N/A
azure-mediaservices-spring-boot-starter | N/A | [![Maven Central][mediaservices-starter-mvn-2.2.x-img]][mediaservices-starter-mvn-2.2.x] | [![][mediaservices-starter-mvn-2.1.x-img]][mediaservices-starter-mvn-2.1.x] | [![][mediaservices-starter-mvn-2.0.x-img]][mediaservices-starter-mvn-2.0.x]
[azure-spring-boot-starter-mediaservices][mediaservices-starter-readme] | N/A | N/A | N/A | N/A
azure-servicebus-spring-boot-starter | N/A | [![Maven Central][servicebus-starter-mvn-2.2.x-img]][servicebus-starter-mvn-2.2.x] | [![][servicebus-starter-mvn-2.1.x-img]][servicebus-starter-mvn-2.1.x] | [![][servicebus-starter-mvn-2.0.x-img]][servicebus-starter-mvn-2.0.x]
[azure-spring-boot-starter-servicebus][servicebus-starter-readme] | N/A | N/A | N/A | N/A
spring-data-gremlin-boot-starter | [![Maven Central][gremlin-starter-mvn-2.3.x-img]][gremlin-starter-mvn-2.3.x] | [![Maven Central][gremlin-starter-mvn-2.2.x-img]][gremlin-starter-mvn-2.2.x] | [![][gremlin-starter-mvn-2.1.x-img]][gremlin-starter-mvn-2.1.x] | [![][gremlin-starter-mvn-2.0.x-img]][gremlin-starter-mvn-2.0.x]
[azure-spring-boot-starter-data-gremlin][gremlin-starter-readme] | N/A | N/A | N/A | N/A
azure-spring-boot-metrics-starter | [![Maven Central][metrics-starter-mvn-2.3.x-img]][metrics-starter-mvn-2.3.x] | [![Maven Central][metrics-starter-mvn-2.2.x-img]][metrics-starter-mvn-2.2.x] | [![][metrics-starter-mvn-2.1.x-img]][metrics-starter-mvn-2.1.x] | [![][metrics-starter-mvn-2.0.x-img]][metrics-starter-mvn-2.0.x]
[azure-spring-boot-starter-metrics][metrics-starter-readme] | N/A | N/A | N/A | N/A
azure-servicebus-jms-spring-boot-starter | [![Maven Central][jms-starter-mvn-2.3.x-img]][jms-starter-mvn-2.3.x] | [![Maven Central][jms-starter-mvn-2.2.x-img]][jms-starter-mvn-2.2.x] | [![Maven Central][jms-starter-mvn-2.1.x-img]][jms-starter-mvn-2.1.x] | N/A
[azure-spring-boot-starter-servicebus-jms][jms-starter-readme] | N/A | N/A | N/A | N/A

### Azure Spring Cloud

[Spring Cloud][spring-cloud] provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes. 

#### Module and Starter

Below packages are available with latest release version. **We recommend users to leverage latest version for bug fix and new features.**

##### [Spring Cloud Stream][spring-cloud-stream]

Artifact Id | Version
------ |---
spring-cloud-azure-eventhubs-stream-binder  | [![Maven Central][azure-spring-cloud-eventhubs-stream-binder-mvn-1.2.x-img]][azure-spring-cloud-eventhubs-stream-binder-mvn-1.2.x]
[azure-spring-cloud-eventhubs-stream-binder][azure-spring-cloud-eventhubs-stream-binder-readme] | N/A 
spring-cloud-starter-azure-eventhubs-kafka | [![Maven Central][azure-spring-cloud-starter-eventhubs-kafka-mvn-1.2.x-img]][azure-spring-cloud-starter-eventhubs-kafka-mvn-1.2.x]
[azure-spring-cloud-starter-eventhubs-kafka][azure-spring-cloud-starter-eventhubs-kafka-readme] | N/A
spring-cloud-azure-servicebus-topic-stream-binder | [![Maven Central][spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x-img]][spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x]
[azure-spring-cloud-servicebus-topic-stream-binder][spring-cloud-azure-servicebus-topic-stream-binder-readme] | N/A
spring-cloud-azure-servicebus-queue-stream-binder | [![Maven Central][spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x-img]][spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x]
[azure-spring-cloud-servicebus-queue-stream-binder][spring-cloud-azure-servicebus-queue-stream-binder-readme] | N/A
spring-cloud-starter-azure-eventhubs | [![Maven Central][spring-cloud-starter-azure-eventhubs-mvn-1.2.x-img]][spring-cloud-starter-azure-eventhubs-mvn-1.2.x]
[azure-spring-integration-eventhubs][spring-cloud-starter-azure-eventhubs-readme] | N/A 
spring-cloud-starter-azure-servicebus | [![Maven Central][spring-cloud-starter-azure-servicebus-mvn-1.2.x-img]][spring-cloud-starter-azure-servicebus-mvn-1.2.x]
[azure-spring-integration-servicebus][spring-cloud-starter-azure-servicebus-readme] | N/A
spring-cloud-starter-azure-storage-queue | [![Maven Central][azure-spring-integration-storage-queue-mvn-1.2.x-img]][azure-spring-integration-storage-queue-mvn-1.2.x]
[azure-spring-integration-storage-queue][azure-spring-integration-storage-queue-readme] | N/A
spring-starter-azure-storage | [![Maven Central][azure-spring-starter-storage-mvn-1.2.x-img]][azure-spring-starter-storage-mvn-1.2.x]
[azure-spring-starter-storage][azure-spring-starter-storage-readme] | N/A
spring-starter-azure-cache | [![Maven Central][azure-spring-cloud-starter-cache-mvn-1.2.x-img]][azure-spring-cloud-starter-cache-mvn-1.2.x]
[azure-spring-cloud-starter-cache][azure-spring-cloud-starter-cache-readme] | N/A 
spring-cloud-azure-appconfiguration-config | [![Maven Central][azure-spring-cloud-appconfiguration-config-mvn-1.2.x-img]][azure-spring-cloud-appconfiguration-config-mvn-1.2.x] 
[azure-spring-cloud-appconfiguration-config][azure-spring-cloud-appconfiguration-config-readme] | N/A 
spring-cloud-azure-appconfiguration-config-web | [![Maven Central][spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x-img]][spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x] 
[azure-spring-cloud-appconfiguration-config-web][spring-cloud-azure-appconfiguration-config-web-readme] | N/A 
spring-cloud-starter-azure-appconfiguration-config | [![Maven Central][spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x-img]][spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x]
[azure-spring-cloud-starter-appconfiguration-config][spring-cloud-starter-azure-appconfiguration-config-readme] | N/A 
spring-cloud-azure-feature-management | [![Maven Central][azure-spring-cloud-feature-management-mvn-1.2.x-img]][azure-spring-cloud-feature-management-mvn-1.2.x]
[azure-spring-cloud-feature-management][azure-spring-cloud-feature-management-readme] | N/A 
spring-cloud-azure-feature-management-web | [![Maven Central][azure-spring-cloud-feature-management-web-mvn-1.2.x-img]][azure-spring-cloud-feature-management-web-mvn-1.2.x]
[azure-spring-cloud-feature-management-web][azure-spring-cloud-feature-management-web-readme] | N/A 

### Snapshots  
[![Nexus OSS][nexus-azure-spring-boot-mvn-img]][nexus-azure-spring-boot-mvn]

Snapshots built from `master` branch are available, add [maven repositories][maven.apache-repositories] configuration to your pom file as below. 
```xml
<repositories>
  <repository>
    <id>nexus-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>
```
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

[nexus-azure-spring-boot-mvn-img]: https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.microsoft.azure/azure-spring-boot.svg
[nexus-azure-spring-boot-mvn]: https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/azure/azure-spring-boot/
[maven.apache-repositories]: https://maven.apache.org/settings.html#Repositories

[spring-contributing]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md
[azure-sdk-for-java-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[gitter-spring-on-azure-img]: https://badges.gitter.im/Microsoft/spring-on-azure.svg
[gitter-spring-on-azure]: https://gitter.im/Microsoft/spring-on-azure
[azure-sdk-for-java-compare]: https://github.com/Azure/azure-sdk-for-java/compare

[codeofconduct]: https://opensource.microsoft.com/codeofconduct/faq/
[codeofconduct-faq]: https://opensource.microsoft.com/codeofconduct/faq/
[privacy-statement]: https://privacy.microsoft.com/privacystatement

[aad-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/README.md
[aad-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter.svg
[aad-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%22
[aad-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg
[aad-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.2.*
[aad-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg
[aad-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*
[aad-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg
[aad-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*

[storage-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-storage/README.md
[storage-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter.svg
[storage-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%22
[storage-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg
[storage-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*
[storage-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg
[storage-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*

[spring-starter-storage-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md
[spring-starter-storage-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg
[spring-starter-storage-mvn-2.3.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20%20a:spring-starter-azure-storage

[keyvault-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-secrets/README.md
[keyvault-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter.svg
[keyvault-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%22
[keyvault-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg
[keyvault-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*
[keyvault-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg
[keyvault-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*
[keyvault-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg
[keyvault-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*

[keyvault-starter-certificates-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md

[aad-b2c-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory-b2c/README.md
[aad-b2c-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter.svg
[aad-b2c-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%22
[aad-b2c-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg
[aad-b2c-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*
[aad-b2c-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg
[aad-b2c-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*
 
[cosmos-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-cosmos/README.md
[cosmosdb-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter.svg
[cosmosdb-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-starter%22
[cosmosdb-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg
[cosmosdb-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*
[cosmosdb-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg
[cosmosdb-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*
[cosmosdb-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg
[cosmosdb-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*

[mediaservices-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-mediaservices/README.md
[mediaservices-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg
[mediaservices-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22
[mediaservices-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg
[mediaservices-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*
[mediaservices-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg
[mediaservices-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*

[servicebus-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus/README.md
[servicebus-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg
[servicebus-starter-mvn-2.2.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22
[servicebus-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg
[servicebus-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*
[servicebus-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg
[servicebus-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*

[gremlin-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-data-gremlin/README.md
[gremlin-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter.svg
[gremlin-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22spring-data-gremlin-boot-starter%22
[gremlin-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.2.svg
[gremlin-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.2.*
[gremlin-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.1.svg
[gremlin-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.1.*
[gremlin-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.0.svg
[gremlin-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.0.*

[metrics-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-metrics/README.md
[metrics-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg
[metrics-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22
[metrics-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg
[metrics-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*
[metrics-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg
[metrics-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*
[metrics-starter-mvn-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg
[metrics-starter-mvn-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*

[jms-starter-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus-jms/README.md
[jms-starter-mvn-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter.svg
[jms-starter-mvn-2.3.x]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%22
[jms-starter-mvn-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg
[jms-starter-mvn-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*
[jms-starter-mvn-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg
[jms-starter-mvn-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*

[azure-spring-cloud-eventhubs-stream-binder-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-eventhubs-stream-binder/README.md
[azure-spring-cloud-eventhubs-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-eventhubs-stream-binder.svg
[azure-spring-cloud-eventhubs-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-eventhubs-stream-binder%22

[azure-spring-cloud-starter-eventhubs-kafka-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-starter-eventhubs-kafka/README.md
[azure-spring-cloud-starter-eventhubs-kafka-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs-kafka.svg
[azure-spring-cloud-starter-eventhubs-kafka-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs-kafka%22

[spring-cloud-azure-servicebus-topic-stream-binder-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder/README.md
[spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder.svg
[spring-cloud-azure-servicebus-topic-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-topic-stream-binder%22

[spring-cloud-azure-servicebus-queue-stream-binder-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-servicebus-queue-stream-binder/README.md
[spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-queue-stream-binder.svg
[spring-cloud-azure-servicebus-queue-stream-binder-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-queue-stream-binder%22

[spring-cloud-starter-azure-eventhubs-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-eventhubs/README.md
[spring-cloud-starter-azure-eventhubs-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs.svg
[spring-cloud-starter-azure-eventhubs-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs%22

[spring-cloud-starter-azure-servicebus-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-servicebus/README.md
[spring-cloud-starter-azure-servicebus-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-servicebus.svg
[spring-cloud-starter-azure-servicebus-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-servicebus%22

[azure-spring-integration-storage-queue-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-storage-queue/README.md
[azure-spring-integration-storage-queue-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-storage-queue.svg
[azure-spring-integration-storage-queue-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-storage-queue%22

[azure-spring-starter-storage-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md
[azure-spring-starter-storage-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg
[azure-spring-starter-storage-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-storage%22

[azure-spring-cloud-starter-cache-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-starter-cache/README.md
[azure-spring-cloud-starter-cache-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-cache.svg
[azure-spring-cloud-starter-cache-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-cache%22

[azure-spring-cloud-appconfiguration-config-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/README.md
[azure-spring-cloud-appconfiguration-config-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config.svg
[azure-spring-cloud-appconfiguration-config-mvn-1.2.x]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-appconfiguration-config%22

[spring-cloud-azure-appconfiguration-config-web-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-appconfiguration-config-web/README.md
[spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config-web.svg
[spring-cloud-azure-appconfiguration-config-web-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-appconfiguration-config-web

[spring-cloud-starter-azure-appconfiguration-config-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-starter-appconfiguration-config/README.md
[spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-appconfiguration-config.svg
[spring-cloud-starter-azure-appconfiguration-config-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-starter-azure-appconfiguration-config

[azure-spring-cloud-feature-management-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-feature-management/README.md
[azure-spring-cloud-feature-management-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management.svg
[azure-spring-cloud-feature-management-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-feature-management

[azure-spring-cloud-feature-management-web-readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-feature-management-web/README.md
[azure-spring-cloud-feature-management-web-mvn-1.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management-web.svg
[azure-spring-cloud-feature-management-web-mvn-1.2.x]: https://search.maven.org/search?q=a:spring-cloud-azure-feature-management-web
