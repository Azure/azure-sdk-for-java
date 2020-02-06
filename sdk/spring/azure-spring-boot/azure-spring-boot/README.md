# Azure Spring Boot Support
This project provides auto-configuration for the following Azure services:

- [Azure Active Directory](../azure-spring-boot-starters/azure-active-directory-spring-boot-starter)
- [Cosmos DB SQL API](../azure-spring-boot-starters/azure-cosmosdb-spring-boot-starter)
- [Key Vault](../azure-spring-boot-starters/azure-keyvault-secrets-spring-boot-starter)
- [Media Service](../azure-spring-boot-starters/azure-mediaservices-spring-boot-starter)
- [Service Bus](../azure-spring-boot-starters/azure-servicebus-spring-boot-starter)
- [Storage](../azure-spring-boot-starters/azure-storage-spring-boot-starter)

This module also provides the ability to automatically inject credentials from Cloud Foundry into your
applications consuming Azure services. It does this by reading the `VCAP_SERVICES` environment
variable and setting the appropriate properties used by auto-configuration code.

For details, please see sample code in the [azure-cloud-foundry-service-sample](../azure-spring-boot-samples/azure-cloud-foundry-service-sample) 

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

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus-spring-boot-starter</artifactId>
</dependency>
```

Please refer to the [samples](../azure-spring-boot-samples) for more getting started instructions.