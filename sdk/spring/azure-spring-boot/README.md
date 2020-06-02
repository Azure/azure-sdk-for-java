#Azure Spring Boot client library for Java

## Key concepts
This project provides auto-configuration for the following Azure services:

- [Azure Active Directory](../azure-spring-boot-starter-active-directory)
- [Azure Active Directory B2C](../azure-spring-boot-starter-active-directory-b2c)
- [Cosmos DB SQL API](../azure-spring-boot-starter-cosmosdb)
- [Key Vault](../azure-spring-boot-starter-keyvault-secrets)
- [Media Service](../azure-spring-boot-starter-mediaservices)
- [Service Bus](../azure-spring-boot-starter-servicebus)
- [Storage](../azure-spring-boot-starter-storage)

This module also provides the ability to automatically inject credentials from Cloud Foundry into your
applications consuming Azure services. It does this by reading the `VCAP_SERVICES` environment
variable and setting the appropriate properties used by auto-configuration code.

For details, please see sample code in the [azure-spring-boot-sample-cloud-foundry](../azure-spring-boot-samples/azure-spring-boot-sample-cloud-foundry) 

## Getting started
To start a new project using Azure, go on [start.spring.io](https://start.spring.io) and select "Azure
Support": this will configure the project to make sure you can integrate easily with Azure service.

For instance, let's assume that you want to use Service Bus, you can add the usual `azure-servicebus`
dependency to your project and the Spring Boot auto-configuration will kick-in: 

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus</artifactId>
</dependency>
```

Note that there is no need to add a `version` as those are managed already by the project.

Alternatively you may want to use the [starters](../azure-spring-boot-starters)

[//]: # ({x-version-update-start;com.azure:azure-servicebus-spring-boot-starter;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-servicebus-spring-boot-starter</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Examples
Please refer to the [samples](../azure-spring-boot-samples) for more getting started instructions.

## Troubleshooting
## Next steps
## Contributing

