# Azure Spring Boot client library for Java
This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

## Getting started
To start a new project using Azure, go on [start.spring.io](https://start.spring.io) and select "Azure
Support": this will configure the project to make sure you can integrate easily with Azure service.

For instance, let's assume that you want to use Key Vault secrets, you can add the usual `azure-security-keyvault-secrets`
dependency to your project and the Spring Boot auto-configuration will kick-in: 

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-keyvault-secrets</artifactId>
</dependency>
```

Note that there is no need to add a `version` as those are managed already by the project.

Alternatively you may want to use the [starters](../azure-spring-boot-starters)

[//]: # ({x-version-update-start;com.microsoft.azure:azure-keyvault-secrets-spring-boot-starter;dependency})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
    <version>2.3.3-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This project provides auto-configuration for the following Azure services:

- [Azure Active Directory](../azure-spring-boot-starter-active-directory)
- [Azure Active Directory B2C](../azure-spring-boot-starter-active-directory-b2c)
- [Cosmos DB SQL API](../azure-spring-boot-starter-cosmosdb)
- [Gremlin SQL API](../azure-spring-boot-starter-data-gremlin)
- [Key Vault](../azure-spring-boot-starter-keyvault-secrets)
- [Metrics Service](../azure-spring-boot-starter-metrics)
- [JMS Service Bus](../azure-spring-boot-starter-servicebus-jms)

This module also provides the ability to automatically inject credentials from Cloud Foundry into your
applications consuming Azure services. It does this by reading the `VCAP_SERVICES` environment
variable and setting the appropriate properties used by auto-configuration code.

For details, please see sample code in the [azure-spring-boot-sample-cloud-foundry](../azure-spring-boot-samples/azure-spring-boot-sample-cloud-foundry) 

## Examples
Please refer to the [samples](../azure-spring-boot-samples) for more getting started instructions.

## Troubleshooting
If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

## Next steps
Microsoft would like to collect data about how users use this Spring boot starter.
Microsoft uses this information to improve our tooling experience. Participation is voluntary.
If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```properties
azure.activedirectory.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)


