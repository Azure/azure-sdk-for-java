# Azure Spring Boot client library for Java
This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the Package
To start a new project using Azure, go on [start.spring.io](https://start.spring.io) and select "Azure
Support": this will configure the project to make sure you can integrate easily with Azure service.

For instance, let's assume that you want to use Key Vault secrets, you can add the usual `azure-security-keyvault-secrets`
dependency to your project and the Spring Boot auto-configuration will kick-in: 

```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-security-keyvault-secrets</artifactId>
</dependency>
```

Note that there is no need to add a `version` as those are managed already by the project.

Alternatively you may want to use the [starters](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-starter)

[//]: # ({x-version-update-start;com.microsoft.azure:azure-keyvault-secrets-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
    <version>2.3.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This project provides auto-configuration for the following Azure services:

- [Azure Active Directory](../azure-spring-boot-starter-active-directory)
- [Azure Active Directory B2C](../azure-spring-boot-starter-active-directory-b2c)
- [Spring Boot 2.2 for Cosmos DB SQL API](../azure-spring-boot-2-2-starter-cosmosdb)
- [Spring Boot 2.3 for Cosmos DB SQL API](../azure-spring-boot-2-3-starter-cosmosdb)
- [Gremlin SQL API](../azure-spring-boot-starter-data-gremlin)
- [Key Vault Secrets](../azure-spring-boot-starter-keyvault-secrets)
- [Metrics Service](../azure-spring-boot-starter-metrics)
- [JMS Service Bus](../azure-spring-boot-starter-servicebus-jms)

This module also provides the ability to automatically inject credentials from Cloud Foundry into your
applications consuming Azure services. It does this by reading the `VCAP_SERVICES` environment
variable and setting the appropriate properties used by auto-configuration code.

For details, please see sample code in the [azure-spring-boot-sample-cloud-foundry](../azure-spring-boot-samples/azure-cloud-foundry-service-sample) 

## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Frontend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory)
- [Azure Active Directory for Backend](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend)
- [Azure Active Directory for Backend with Microsoft Graph API](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-backend-v2)
- [Azure Active Directory B2C](../azure-spring-boot-samples/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb)
- [Gremlin SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)
- [Key Vault](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
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
- [Key Vault](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

<!-- LINKS -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot/src
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/spring-boot-starters-for-azure
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-spring-boot
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
