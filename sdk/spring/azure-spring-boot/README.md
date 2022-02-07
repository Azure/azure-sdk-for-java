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

For details, please see sample code in the [azure-spring-boot-sample-cloud-foundry](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cloudfoundry/azure-cloud-foundry-service-sample) 

## Health indicator

You can use health information to check the status of your running application. It is often used by
monitoring software to alert someone when a production system goes down. The information exposed by
the health endpoint depends on the management.endpoint.health.show-details and
management.endpoint.health.show-components properties which can be configured with one of the
following values:

|  key   | Name  |
|  ----  | ----  |
| never | Details are never shown. |
| when-authorized | Details are only shown to authorized users. Authorized roles can be configured using management.endpoint.health.roles. |
| always |Details are shown to all users. |

The default value is never. A user is considered to be authorized when they are in one or more of
the endpoint’s roles. If the endpoint has no configured roles (the default) all authenticated users
are considered to be authorized. The roles can be configured using the
management.endpoint.health.roles property.

**NOTE:** If you have secured your application and wish to use `always`, your security configuration
must permit access to the health endpoint for both authenticated and unauthenticated users.

### Auto-configured HealthIndicators

The following HealthIndicators are auto-configured by Azure Spring Boot when appropriate. You can
also enable/disable selected indicators by configuring management.health.key.enabled, with the key
listed in the table below.

| key | Name | Description |
| ---- | ---- | ---- |
| azure-cosmos  | CosmosHealthIndicator | Checks that a cosmos database is up. |
| azure-key-vault | KeyVaultHealthIndicator | Checks that a key vault is up. |
| azure-storage | BlobStorageHealthIndicator | Checks that a storage blob is up. |
| azure-storage | FileStorageHealthIndicator | Checks that a storage file is up. |

### Add the dependent

```yaml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
```

### Enabling the Actuator

When you do the following configuration in `application.yml` , you can access the endpoint to get
the health of the component.

```yaml
management:
  health:
    azure-cosmos:
      enabled: true
    azure-key-vault:
      enabled: true
    azure-storage:
      enabled: true
  endpoint:
    health:
      show-details: always
```

Access the Health Endpoint：

```json
{
  "status": "UP",
  "components": {
    "blobStorage": {
      "status": "UP",
      "details": {
        "URL": "https://xxxx.blob.core.windows.net"
      }
    },
    "cosmos": {
      "status": "UP",
      "details": {
        "database": "xxx"
      }
    },
    "keyVault": {
      "status": "UP"
    }
  }
}
```


## Examples
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory for Resource Server](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server)
- [Azure Active Directory for Resource Server with Obo Clients](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-obo)
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-topic)

## Troubleshooting
### Logging setting
Please refer to [spring logging document] to get more information about logging.

#### Logging setting examples
- Example: Setting logging level of hibernate
```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

## Next steps
The following section provides sample projects illustrating how to use the Azure Spring Boot starters.
### More sample code
- [Azure Active Directory for Web Application](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp)
- [Azure Active Directory for Resource Server](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server)
- [Azure Active Directory for Resource Server with Obo Clients](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-obo)
- [Azure Active Directory for Resource Server by Filter(Deprecated)](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter)
- [Azure Active Directory B2C](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc)
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos)
- [Key Vault](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets)
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-boot-sample-servicebus-jms-topic)

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
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
[Storage]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-storage
