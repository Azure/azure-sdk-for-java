# Azure Key Vault Secrets Spring Boot starter client library for Java

Azure Key Vault Secrets Spring Boot Starter is Spring starter for [Azure Key Vault Secrets]. With
this starter, Azure Key Vault is added as one of Spring PropertySource, so secrets stored in Azure
Key Vault could be easily used and conveniently accessed like other externalized configuration
property, e.g. properties in files.

[Package (Maven)] | [API reference documentation] | [Product documentation] | [Samples]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
    ```xml
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-boot-starter-keyvault-secrets</artifactId>
    </dependency>
    ```

### Save secrets in Azure Key Vault
  Save secrets in Azure Key Vault through Azure Portal or Azure CLI:
    - [Set and retrieve a secret from Azure Key Vault using Azure CLI].
    - [Set and retrieve a secret from Azure Key Vault using the Azure portal]

### Configure necessary properties.
    Configure these properties:
    ```
    azure.keyvault.enabled=true
    azure.keyvault.uri=put-your-azure-keyvault-uri-here
    azure.keyvault.client-id=put-your-azure-client-id-here
    azure.keyvault.client-key=put-your-azure-client-key-here
    ``` 

###  Get Key Vault secret value as property
  Now, you can get Azure Key Vault secret value as a configuration property.

    <!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultSample.java#L18-L32 -->
    ```
    @SpringBootApplication
    public class KeyVaultSample implements CommandLineRunner {
    
        @Value("${your-property-name}")
        private String mySecretProperty;
    
        public static void main(String[] args) {
            SpringApplication.run(KeyVaultSample.class, args);
        }
    
        @Override
        public void run(String... args) {
            System.out.println("property your-property-name value is: " + mySecretProperty);
        }
    }
    ```

You can refer to [Key Vault Secrets Sample project] to get more information.

## Key concepts

By adding [PropertySource] in [ConfigurableEnvironment], values saved in [Azure Key Vault Secrets]
can be resolved in `${...}` placeholder in `@Value` annotation.

## Examples

### Use MSI / Managed identities
#### Spring Cloud for Azure

Spring Cloud for Azure supports system-assigned managed identity only at present. To use it for
Spring Cloud for Azure apps, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### App Services
To use managed identities for App Services - please refer to
[How to use managed identities for App Service and Azure Functions].

To use it in an App Service, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### VM
To use it for virtual machines, please refer to
[Azure AD managed identities for Azure resources documentation].

To use it in a VM, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
``` 

If you are using system assigned identity, you don't need to specify the client-id.

### Use multiple Key Vault in one application

If you want to use multiple Key Vaults in one project, you need to define names for each of the
Key Vaults you want to use and in which order the Key Vaults should be consulted. If a property
exists in multiple Key Vaults, the order determines which value you will get back.

The example below shows a setup for 2 Key Vaults, named `keyvault1` and
`keyvault2`. The order specifies that `keyvault1` will be consulted first.

```
azure.keyvault.order=keyvault1,keyvault2
azure.keyvault.keyvault1.uri=put-a-azure-keyvault-uri-here
azure.keyvault.keyvault1.client-id=put-a-azure-client-id-here
azure.keyvault.keyvault1.client-key=put-a-azure-client-key-here
azure.keyvault.keyvault1.tenant-id=put-a-azure-tenant-id-here
azure.keyvault.keyvault2.uri=put-a-azure-keyvault-uri-here
azure.keyvault.keyvault2.client-id=put-a-azure-client-id-here
azure.keyvault.keyvault2.client-key=put-a-azure-client-key-here
azure.keyvault.keyvault2.tenant-id=put-a-azure-tenant-id-here
```
Note if you decide to use multiple Key Vault support, and you already have an
existing configuration, please make sure you migrate that configuration to the
multiple Key Vault variant. Mixing multiple Key Vaults with an existing single
Key Vault configuration is a non-supported scenario.

### Case-sensitive key mode

The new case-sensitive mode allows you to use case-sensitive Key Vault names. Note
that the Key Vault secret key still needs to honor the naming limitation as
described [Vault-name and Object-name].

To enable case-sensitive mode, you can set the following property in the `application.properties`:
```
azure.keyvault.case-sensitive-keys=true
```

If your Spring property is using a name that does not honor the Key Vault secret key limitation,
use placeholders in properties. An example of using a placeholder:
```
my.not.compliant.property=${myCompliantKeyVaultSecret}
```

The application will take care of getting the value that is backed by the 
`myCompliantKeyVaultSecret` key name and assign its value to the non-compliant
`my.not.compliant.property`.

### Handle special property name

Allowed secret name pattern in Azure Key Vault is `^[0-9a-zA-Z-]+$`. This section tells how to
handle special names.
 - When property name contains `.`

   `.` is not supported in secret name. If your application have property name which contain `.`,
like `spring.datasource.url`, just replace `.` to `-` when save secret in Azure Key Vault.
For example: Save `spring-datasource-url` in Azure Key Vault. In your application, you can still
use `spring.datasource.url` to retrieve property value.

 - Use [Property Placeholders] as a workaround.

### Custom settings
To use the custom configuration, open the `application.properties` file and add below properties to
specify your Azure Key Vault URI, Azure service principal client id and client key.
- `azure.keyvault.enabled` is used to turn on/off Azure Key Vault Secret as a Spring Boot property
  source, the default value is true.
- `azure.keyvault.token-acquiring-timeout-seconds` is optional. Its value is used to specify the
  timeout in seconds when acquiring a token from Azure AAD, the default value is 60 seconds.
- `azure.keyvault.refresh-interval` is optional. Its value is used to specify the period for
  PropertySource to refresh secret keys, the default value is 1800000(ms).
- `azure.keyvault.secret-keys` is used to indicate that if an application using specific secret keys
  and this property is set, the application will only load the keys in the property and won't load
  all the keys from Key Vault, that means if you want to update your secrets, you need to restart
  the application rather than only add secrets in the Key Vault.
- `azure.keyvault.authority-host` is the URL at which your identity provider can be reached.
    - If working with azure global, just left the property blank, and the value will be filled with
      the default value.
    - If working with azure stack, set the property with authority URI.
- `azure.keyvault.secret-service-version`
    - The valid values for this property can be found [SecretServiceVersion].
    - This property is optional. If not set, the property will be filled with the latest value.

```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.token-acquire-timeout-seconds=60
azure.keyvault.refresh-interval=1800000
azure.keyvault.secret-keys=key1,key2,key3
azure.keyvault.authority-host=put-your-own-authority-host-here(fill with default value if empty)
azure.keyvault.secret-service-version=specify secretServiceVersion value(fill with default value if empty)
```



## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application
errors and expedite their resolution. The logs produced will capture the flow of an application
before reaching the terminal state to help locate the root issue. View the [logging] wiki for
guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment
(for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level
is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by 
using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

Please refer to [Spring logging documentation] to get more information.
 

## Next steps

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [CONTRIBUTING guide] to build from source or contribute.

<!-- LINKS -->
[Product documentation]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-key-vault
[API reference documentation]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[Package (Maven)]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-keyvault-secrets
[Samples]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets
[Azure Key Vault Secrets]: https://docs.microsoft.com/azure/key-vault/secrets/about-secrets
[PropertySource]: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/env/PropertySource.html
[ConfigurableEnvironment]: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/env/ConfigurableEnvironment.html
[Vault-name and Object-name]: https://docs.microsoft.com/azure/key-vault/general/about-keys-secrets-certificates#vault-name-and-object-name
[Property Placeholders]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files.property-placeholders
[How to use managed identities for App Service and Azure Functions]: https://docs.microsoft.com/azure/app-service/app-service-managed-service-identity
[Azure AD managed identities for Azure resources documentation]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/.
[Set and retrieve a secret from Azure Key Vault using Azure CLI]: https://docs.microsoft.com/azure/key-vault/secrets/quick-create-cli
[Set and retrieve a secret from Azure Key Vault using the Azure portal]: https://docs.microsoft.com/azure/key-vault/secrets/quick-create-portal
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[Spring logging documentation]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[SecretServiceVersion]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/main/java/com/azure/security/keyvault/secrets/SecretServiceVersion.java#L12
[Key Vault Secrets Sample project]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/keyvault/azure-spring-boot-sample-keyvault-secrets
[CONTRIBUTING guide]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
