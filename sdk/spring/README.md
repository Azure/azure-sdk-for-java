# Azure Spring Boot client library for Java

## Getting started
### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support
* This repository supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven](https://maven.apache.org/) 3.0 and above

## Key concepts
### Azure Spring Boot

Below starters are available with latest release version. We recommend users to leverage latest version for bug fix and new features.
You can find them in [Maven Central Repository](https://search.maven.org/).
The first three starters are also available in [Spring Initializr](https://start.spring.io/). 

Artifact Id | Version for Spring Boot 2.3.x | Version for Spring Boot 2.2.x | Version for Spring Boot 2.1.x | Version for Spring Boot 2.0.x
---|:---:|:---:|:---:|:---:
azure-active-directory-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-active-directory](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/README.md) | N/A | N/A | N/A | N/A
[azure-storage-spring-boot-starter](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-storage/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*)
spring-starter-azure-storage | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20%20a:spring-starter-azure-storage) | N/A | N/A | N/A
[azure-spring-starter-storage](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md) | N/A | N/A | N/A | N/A
azure-keyvault-secrets-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-keyvault-secrets](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-secrets/README.md) | N/A | N/A | N/A | N/A
[azure-spring-boot-starter-keyvault-certificates](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md) | N/A | N/A | N/A | N/A
azure-active-directory-b2c-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*) | N/A
[azure-spring-boot-starter-active-directory-b2c](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory-b2c/README.md) | N/A | N/A | N/A | N/A
azure-cosmosdb-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-cosmos](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-cosmos/README.md) | N/A | N/A | N/A | N/A
azure-mediaservices-spring-boot-starter | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-mediaservices](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-mediaservices/README.md) | N/A | N/A | N/A | N/A
azure-servicebus-spring-boot-starter | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-servicebus](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus/README.md) | N/A | N/A | N/A | N/A
spring-data-gremlin-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22spring-data-gremlin-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-data-gremlin](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-data-gremlin/README.md) | N/A | N/A | N/A | N/A
azure-spring-boot-metrics-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*)
[azure-spring-boot-starter-metrics](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-metrics/README.md) | N/A | N/A | N/A | N/A
azure-servicebus-jms-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*) | N/A
[azure-spring-boot-starter-servicebus-jms](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus-jms/README.md) | N/A | N/A | N/A | N/A

### Azure Spring Cloud

[Spring Cloud](https://spring.io/projects/spring-cloud) provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes. 

#### Module and Starter

Below packages are available with latest release version. **We recommend users to leverage latest version for bug fix and new features.**

##### [Spring Cloud Stream](https://cloud.spring.io/spring-cloud-stream/)
Artifact Id | Version
------ |---
spring-cloud-azure-eventhubs-stream-binder  | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-eventhubs-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-eventhubs-stream-binder%22)
[azure-spring-cloud-eventhubs-stream-binder](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-eventhubs-stream-binder/README.md) | N/A 
spring-cloud-starter-azure-eventhubs-kafka | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs-kafka.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs-kafka%22)
[azure-spring-cloud-starter-eventhubs-kafka](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-starter-eventhubs-kafka/README.md) | N/A
spring-cloud-azure-servicebus-topic-stream-binder | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-topic-stream-binder%22)
[azure-spring-cloud-servicebus-topic-stream-binder](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder/README.md) | N/A
spring-cloud-azure-servicebus-queue-stream-binder | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-queue-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-queue-stream-binder%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.1.0.RC5/spring-cloud-azure-samples/servicebus-queue-binder-sample)
[azure-spring-cloud-servicebus-queue-stream-binder](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder/README.md) | N/A
spring-cloud-starter-azure-eventhubs | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhubs.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhubs%22) 
[azure-spring-integration-eventhubs](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-eventhubs/README.md) | N/A 
spring-cloud-starter-azure-servicebus | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-servicebus.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-servicebus%22)
[azure-spring-integration-servicebus](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-servicebus/README.md) | N/A
spring-cloud-starter-azure-storage-queue | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-storage-queue.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-storage-queue%22)
[azure-spring-integration-storage-queue](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-storage-queue/README.md) | N/A
spring-starter-azure-storage | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-storage%22)
[azure-spring-starter-storage](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md) | N/A
spring-starter-azure-cache | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-cache.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-cache%22) 
[azure-spring-cloud-starter-cache](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md) | N/A 
spring-cloud-azure-appconfiguration-config | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-appconfiguration-config%22) 
[azure-spring-cloud-appconfiguration-config](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/README.md) | N/A 
spring-cloud-azure-appconfiguration-config-web | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-appconfiguration-config-web.svg)](https://search.maven.org/search?q=a:spring-cloud-azure-appconfiguration-config-web) 
[azure-spring-cloud-appconfiguration-config-web](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-appconfiguration-config-web/README.md) | N/A 
spring-cloud-starter-azure-appconfiguration-config | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-appconfiguration-config.svg)](https://search.maven.org/search?q=a:spring-cloud-starter-azure-appconfiguration-config) 
[azure-spring-cloud-starter-appconfiguration-config](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-starter-appconfiguration-config/README.md) | N/A 
spring-cloud-azure-feature-management | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management.svg)](https://search.maven.org/search?q=a:spring-cloud-azure-feature-management) 
[azure-spring-cloud-feature-management](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-feature-management/README.md) | N/A 
spring-cloud-azure-feature-management-web | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-feature-management-web.svg)](https://search.maven.org/search?q=a:spring-cloud-azure-feature-management-web) 
[azure-spring-cloud-feature-management-web](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-spring-cloud-feature-management-web/README.md) | N/A 

### Snapshots  
[![Nexus OSS](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.microsoft.azure/azure-spring-boot.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/azure/azure-spring-boot/)

Snapshots built from `master` branch are available, add [maven repositories](https://maven.apache.org/settings.html#Repositories) configuration to your pom file as below. 
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
You could check [Spring Integration Guides](https://docs.microsoft.com/java/azure/spring-framework) articles to learn more on usage of specific starters.

## Troubleshooting
## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here](https://github.com/Microsoft/azure-spring-boot/compare). When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/privacystatement) statement to learn more.

