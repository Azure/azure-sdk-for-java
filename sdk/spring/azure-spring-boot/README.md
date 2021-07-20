# Azure Spring Boot client library for Java
This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the Package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-boot</artifactId>
</dependency>
```

## Key concepts
This project provides auto-configuration for the following Azure services:

- [Azure Active Directory](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-active-directory)
- [Azure Active Directory B2C](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c)
- [Cosmos DB SQL API](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-cosmos)
- [Key Vault Secrets](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-keyvault-secrets)
- [JMS Service Bus](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-servicebus-jms)
- [Storage]

This module also provides the ability to automatically inject credentials from Cloud Foundry into your
applications consuming Azure services. It does this by reading the `VCAP_SERVICES` environment
variable and setting the appropriate properties used by auto-configuration code.

For details, please see sample code in the [azure-spring-boot-sample-cloud-foundry](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/cloudfoundry/azure-cloud-foundry-service-sample) 

## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory for Resource Server](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server)
- [Azure Active Directory for Resource Server with Obo Clients](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server-obo)
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-topic)

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
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory for Resource Server](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server)
- [Azure Active Directory for Resource Server with Obo Clients](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server-obo)
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-topic)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

<!-- LINKS -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot/src
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/spring-boot-starters-for-azure
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-spring-boot
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
[Storage]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-storage
