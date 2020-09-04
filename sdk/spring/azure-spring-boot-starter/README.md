# Azure Spring Boot Starters client library for Java
This repository is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services. It supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the Package
[//]: # ({x-version-update-start;com.microsoft.azure:azure-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-spring-boot-starter</artifactId>
    <version>2.3.3</version>
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
[azure-active-directory-spring-boot-starter](../azure-spring-boot-starter-active-directory/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*)
[azure-storage-spring-boot-starter](../azure-spring-boot-starter-storage/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*)
[azure-keyvault-secrets-spring-boot-starter](../azure-spring-boot-starter-keyvault-secrets/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*)
[azure-active-directory-b2c-spring-boot-starter](../azure-spring-boot-starter-active-directory-b2c/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*) | N/A
[azure-cosmosdb-spring-boot-2-2-starter](../azure-spring-boot-2-2-starter-cosmosdb/README.md) | N/A | N/A | N/A | N/A
[azure-cosmosdb-spring-boot-2-3-starter](../azure-spring-boot-2-3-starter-cosmosdb/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-2-3-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*)
[azure-mediaservices-spring-boot-starter](../azure-spring-boot-starter-mediaservices/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*)
[azure-servicebus-spring-boot-starter](../azure-spring-boot-starter-servicebus/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*)
[spring-data-gremlin-boot-starter](../azure-spring-boot-starter-data-gremlin/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22spring-data-gremlin-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-metrics-starter](../azure-spring-boot-starter-metrics) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*)
 | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*) | N/A

## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Frontend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory)
- [Azure Active Directory for Backend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend)
- [Azure Active Directory for Backend with Microsoft Graph API](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend-v2)
- [Azure Active Directory B2C](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb)
- [Gremlin SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)
- [Key Vault Secrets](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)

## Troubleshooting
### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting loging in pring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 

## Next steps
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Frontend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory)
- [Azure Active Directory for Backend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend)
- [Azure Active Directory for Backend with Microsoft Graph API](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend-v2)
- [Azure Active Directory B2C](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb)
- [Gremlin SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)
- [Key Vault Secrets](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/spring-boot-starters-for-azure
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
