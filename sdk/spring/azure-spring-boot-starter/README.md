# Azure Spring Boot Starters client library for Java
This repository is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services. It supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the Package
[//]: # ({x-version-update-start;com.azure:azure-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-boot-starter</artifactId>
    <version>3.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

This starter brings auto configuration code for all Azure Spring modules, but to enable the auto configuration of a specific service, you need to turn to its doc for detailed reference. 
### Usage

Below starters are available with latest release version. We recommend users to leverage latest version for bug fix and new features.
You can find them in [Maven Central Repository](https://search.maven.org/).
The first three starters are also available in [Spring Initializr](http://start.spring.io/). 

Starter Name | Version for Spring Boot 2.3.x | Version for Spring Boot 2.2.x | Version for Spring Boot 2.1.x | Version for Spring Boot 2.0.x
---|:---:|:---:|:---:|:---:
[azure-active-directory-spring-boot-starter][azure-active-directory-spring-boot-starter-README] | [![Maven Central][azure-active-directory-spring-boot-starter-Maven Central-2.3.x-img]][azure-active-directory-spring-boot-starter-Maven Central-2.3.x] | [![Maven Central][azure-active-directory-spring-boot-starter-Maven Central-2.2.x-img]][azure-active-directory-spring-boot-starter-Maven Central-2.2.x] | [![][azure-active-directory-spring-boot-starter-Maven Central-2.1.x-img]][azure-active-directory-spring-boot-starter-Maven Central-2.1.x] | [![][azure-active-directory-spring-boot-starter-Maven Central-2.0.x-img]][azure-active-directory-spring-boot-starter-Maven Central-2.0.x]
[azure-storage-spring-boot-starter][azure-storage-spring-boot-starter-README]] | N/A | [![Maven Central][azure-storage-spring-boot-starter-Maven Central-2.2.x-img]][azure-storage-spring-boot-starter-Maven Central-2.2.x] | [![][azure-storage-spring-boot-starter-Maven Central-2.1.x-img]][azure-storage-spring-boot-starter-Maven Central-2.1.x] | [![][azure-storage-spring-boot-starter-Maven Central-2.0.x-img]][azure-storage-spring-boot-starter-Maven Central-2.0.x]
[spring-starter-azure-storage][spring-starter-azure-storage-README] | [![Maven Central][spring-starter-azure-storage-Maven Central-2.3.x-img]][spring-starter-azure-storage-Maven Central-2.3.x] | N/A | N/A | N/A
[azure-keyvault-secrets-spring-boot-starter][azure-keyvault-secrets-spring-boot-starter-README] | [![Maven Central][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.3.x-img]][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.3.x] | [![Maven Central][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.2.x-img]][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.2.x] | [![][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.1.x-img]][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.1.x] | [![][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.0.x-img]][azure-keyvault-secrets-spring-boot-starter-Maven Central-2.0.x]
[azure-active-directory-b2c-spring-boot-starter][azure-active-directory-b2c-spring-boot-starter-README] | [![Maven Central][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.3.x-img]][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.3.x] | [![Maven Central][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.2.x-img]][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.2.x] | [![Maven Central][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.1.x-img]][azure-active-directory-b2c-spring-boot-starter-Maven Central-2.1.x] | N/A
[azure-cosmosdb-spring-boot-starter][azure-cosmosdb-spring-boot-starter-README] | [![Maven Central][azure-cosmosdb-spring-boot-starter-Maven Central-2.3.x-img]][azure-cosmosdb-spring-boot-starter-Maven Central-2.3.x] | [![Maven Central][azure-cosmosdb-spring-boot-starter-Maven Central-2.2.x-img]][azure-cosmosdb-spring-boot-starter-Maven Central-2.2.x] | [![][azure-cosmosdb-spring-boot-starter-Maven Central-2.1.x-img]][azure-cosmosdb-spring-boot-starter-Maven Central-2.1.x] | [![][azure-cosmosdb-spring-boot-starter-Maven Central-2.0.x-img]][azure-cosmosdb-spring-boot-starter-Maven Central-2.0.x]
[azure-mediaservices-spring-boot-starter][azure-mediaservices-spring-boot-starter-README] | N/A | [![Maven Central][azure-mediaservices-spring-boot-starter-Maven Central-2.2.x-img]][azure-mediaservices-spring-boot-starter-Maven Central-2.2.x] | [![][azure-mediaservices-spring-boot-starter-Maven Central-2.1.x-img]][azure-mediaservices-spring-boot-starter-Maven Central-2.1.x] | [![][azure-mediaservices-spring-boot-starter-Maven Central-2.0.x-img]][azure-mediaservices-spring-boot-starter-Maven Central-2.0.x]
[azure-servicebus-spring-boot-starter][azure-servicebus-spring-boot-starter-README] | N/A | [![Maven Central][azure-servicebus-spring-boot-starter-Maven Central-2.2.x-img]][azure-servicebus-spring-boot-starter-Maven Central-2.2.x] | [![][azure-servicebus-spring-boot-starter-Maven Central-2.1.x-img]][azure-servicebus-spring-boot-starter-Maven Central-2.1.x] | [![][azure-servicebus-spring-boot-starter-Maven Central-2.0.x-img]][azure-servicebus-spring-boot-starter-Maven Central-2.0.x]
[spring-data-gremlin-boot-starter][spring-data-gremlin-boot-starter-README] | [![Maven Central][spring-data-gremlin-boot-starter-Maven Central-2.3.x-img]][spring-data-gremlin-boot-starter-Maven Central-2.3.x] | [![Maven Central][spring-data-gremlin-boot-starter-Maven Central-2.2.x-img]][spring-data-gremlin-boot-starter-Maven Central-2.2.x] | [![][spring-data-gremlin-boot-starter-Maven Central-2.1.x-img]][spring-data-gremlin-boot-starter-Maven Central-2.1.x] | [![][spring-data-gremlin-boot-starter-Maven Central-2.0.x-img]][spring-data-gremlin-boot-starter-Maven Central-2.0.x]
[azure-spring-boot-metrics-starter][azure-spring-boot-metrics-starter-README] | [![Maven Central][azure-spring-boot-metrics-starter-Maven Central-2.3.x-img]][azure-spring-boot-metrics-starter-Maven Central-2.3.x] | [![Maven Central][azure-spring-boot-metrics-starter-Maven Central-2.2.x-img]][azure-spring-boot-metrics-starter-Maven Central-2.2.x] | [![][azure-spring-boot-metrics-starter-Maven Central-2.1.x-img]][azure-spring-boot-metrics-starter-Maven Central-2.1.x] | [![][azure-spring-boot-metrics-starter-Maven Central-2.0.x-img]][azure-spring-boot-metrics-starter-Maven Central-2.0.x]
[azure-servicebus-jms-spring-boot-starter][azure-servicebus-jms-spring-boot-starter-README] | [![Maven Central][azure-servicebus-jms-spring-boot-starter-Maven Central-2.3.x-img]][azure-servicebus-jms-spring-boot-starter-Maven Central-2.3.x] | [![Maven Central][azure-servicebus-jms-spring-boot-starter-Maven Central-2.2.x-img]][azure-servicebus-jms-spring-boot-starter-Maven Central-2.2.x] | [![Maven Central][azure-servicebus-jms-spring-boot-starter-Maven Central-2.1.x-img]][azure-servicebus-jms-spring-boot-starter-Maven Central-2.1.x] | N/A

## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Frontend](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory)
- [Azure Active Directory for Backend](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend)
- [Azure Active Directory for Backend with Microsoft Graph API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend-v2)
- [Azure Active Directory B2C](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb)
- [Gremlin SQL API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)
- [Key Vault Secrets](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 

## Next steps
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Frontend](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory)
- [Azure Active Directory for Backend](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend)
- [Azure Active Directory for Backend with Microsoft Graph API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend-v2)
- [Azure Active Directory B2C](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb)
- [Gremlin SQL API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)
- [Key Vault Secrets](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/spring-boot-starters-for-azure
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free

[azure-active-directory-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory/README.md
[azure-active-directory-spring-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter.svg
[azure-active-directory-spring-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%22
[azure-active-directory-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg
[azure-active-directory-spring-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.2.*
[azure-active-directory-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg
[azure-active-directory-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*
[azure-active-directory-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg
[azure-active-directory-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*

[azure-storage-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-storage/README.md
[azure-storage-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter.svg
[azure-storage-spring-boot-starter-Maven Central-2.2.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%22
[azure-storage-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg
[azure-storage-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*
[azure-storage-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg
[azure-storage-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*

[spring-starter-azure-storage-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-starter-storage/README.md
[spring-starter-azure-storage-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg
[spring-starter-azure-storage-Maven Central-2.3.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20%20a:spring-starter-azure-storage

[azure-keyvault-secrets-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-secrets/README.md
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter.svg
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%22
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg
[azure-keyvault-secrets-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*

[azure-active-directory-b2c-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-active-directory-b2c/README.md
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter.svg
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%22
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg
[azure-active-directory-b2c-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*
 
[azure-cosmosdb-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-cosmosdb/README.md
[azure-cosmosdb-spring-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter.svg
[azure-cosmosdb-spring-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-starter%22
[azure-cosmosdb-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg
[azure-cosmosdb-spring-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*
[azure-cosmosdb-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg
[azure-cosmosdb-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*
[azure-cosmosdb-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg
[azure-cosmosdb-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*

[azure-mediaservices-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-mediaservices/README.md
[azure-mediaservices-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg
[azure-mediaservices-spring-boot-starter-Maven Central-2.2.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22
[azure-mediaservices-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg
[azure-mediaservices-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*
[azure-mediaservices-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg
[azure-mediaservices-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*

[azure-servicebus-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus/README.md
[azure-servicebus-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg
[azure-servicebus-spring-boot-starter-Maven Central-2.2.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22
[azure-servicebus-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg
[azure-servicebus-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*
[azure-servicebus-spring-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg
[azure-servicebus-spring-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*

[spring-data-gremlin-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-data-gremlin/README.md
[spring-data-gremlin-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter.svg
[spring-data-gremlin-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22spring-data-gremlin-boot-starter%22
[spring-data-gremlin-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.2.svg
[spring-data-gremlin-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.2.*
[spring-data-gremlin-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.1.svg
[spring-data-gremlin-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.1.*
[spring-data-gremlin-boot-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.0.svg
[spring-data-gremlin-boot-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.0.*

[azure-spring-boot-metrics-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-metrics/README.md
[azure-spring-boot-metrics-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg
[azure-spring-boot-metrics-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22
[azure-spring-boot-metrics-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg
[azure-spring-boot-metrics-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*
[azure-spring-boot-metrics-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg
[azure-spring-boot-metrics-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*
[azure-spring-boot-metrics-starter-Maven Central-2.0.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg
[azure-spring-boot-metrics-starter-Maven Central-2.0.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*

[azure-servicebus-jms-spring-boot-starter-README]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-servicebus-jms/README.md
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.3.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter.svg
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.3.x]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%22
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.2.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.2.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.1.x-img]: https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg
[azure-servicebus-jms-spring-boot-starter-Maven Central-2.1.x]: https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*
