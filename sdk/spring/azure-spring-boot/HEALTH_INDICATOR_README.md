# Azure Spring Boot - Actuator

Azure Spring Boot Actuator to help you monitor and manage your application when it's pushed to
production. You can choose to manage and monitor your application using HTTP or JMX endpoints.Health
gathering can be automatically applied to your application.
 
## Health Information

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

## Auto-configured HealthIndicators

The following HealthIndicators are auto-configured by Azure Spring Boot when appropriate. You can
also enable/disable selected indicators by configuring management.health.key.enabled, with the key
listed in the table below.

| key | Name | Description |
| ---- | ---- | ---- |
| azure-cosmos  | CosmosHealthIndicator | Checks that a cosmos database is up. |
| azure-key-vault | KeyVaultHealthIndicator | Checks that a key vault is up. |
| azure-storage | BlobStorageHealthIndicator | Checks that a storage blob is up. |
| azure-storage | FileStorageHealthIndicator | Checks that a storage file is up. |

## Examples

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
  status: "UP",
  components: {
    blobStorage: {
      status: "UP",
      details: {
        URL: "https://xxxx.blob.core.windows.net"
      }
    },
    cosmos: {
      status: "UP",
      details: {
        database: "xxx"
      }
    },
    keyVault: {
      status: "UP"
    }
  }
}
```

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md


