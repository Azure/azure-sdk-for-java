# Azure Spring Boot Starters client library for Java
This repository is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services. It supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the Package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-boot-starter</artifactId>
</dependency>
```

## Key concepts

This starter brings auto configuration code for all Azure Spring modules, but to enable the auto configuration of a specific service, you need to turn to its doc for detailed reference. 

## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault Secrets](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-topic)

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

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging).
 

## Next steps
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault Secrets](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-topic)


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/spring-boot-starters-for-azure
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
