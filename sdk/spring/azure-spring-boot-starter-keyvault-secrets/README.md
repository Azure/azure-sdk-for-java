# Azure Key Vault Secrets Spring Boot starter client library for Java
Azure Key Vault Secrets Spring Boot Starter is Spring starter for [Azure Key Vault Secrets](https://docs.microsoft.com/rest/api/keyvault/about-keys--secrets-and-certificates#BKMK_WorkingWithSecrets). With this starter, Azure Key Vault is added as one of Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently accessed like other externalized configuration property, e.g. properties in files.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

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

## Key concepts
Key Vault provides secure storage of secrets, such as passwords and database connection strings.

From a developer's perspective, Key Vault APIs accept and return secret values as strings. Internally, Key Vault stores and manages secrets as sequences of octets (8-bit bytes), with a maximum size of 25k bytes each. The Key Vault service doesn't provide semantics for secrets. It merely accepts the data, encrypts it, stores it, and returns a secret identifier ("id"). The identifier can be used to retrieve the secret at a later time.

For highly sensitive data, clients should consider additional layers of protection for data. Encrypting data using a separate protection key prior to storage in Key Vault is one example.

Key Vault also supports a contentType field for secrets. Clients may specify the content type of a secret to assist in interpreting the secret data when it's retrieved. The maximum length of this field is 255 characters. There are no pre-defined values. The suggested usage is as a hint for interpreting the secret data.

Besides, this starter provides features of supporting multiple Key Vaults, case sensitive mode of Key Vault names and using placeholder presenting Key Vault names in property file

### Configuration Options
Azure Spring Boot Key Vault Starter deprecates all legacy properties of which the prefix is `azure.keyvault` and uses `spring.cloud.azure.keyvault` instead.
When a deprecated property is detected while its active property is not found, the active property will be configured into application environment with value from the deprecated property.

If you load configuration properties from Azure Key Vault, the preceding detection and replacement are also applicable for Key Vault property sources. Replaced properties from Key Vault have higher priorities than local ones.
Note that replaced properties will not be refreshed as common properties from Key Vault property source.

We also provide unified configuration properties that are applicable for Azure Spring Starters. When Key Vault properties are not configured, associated unified Azure Spring properties will take effects.
#### Key Vault properties
##### Active Properties
|Name|Description|Default Value|Comment|
|:---|:---|:---|:---
spring.cloud.azure.keyvault.case-sensitive-keys|Defines the constant for the property that enables/disables case sensitive keys.|false||
spring.cloud.azure.keyvault.credential.client-certificate-password|Password of the certificate file to use when performing service principal authentication with Azure.||||
spring.cloud.azure.keyvault.credential.client-certificate-path|Path of a PEM certificate file to use when performing service principal authentication with Azure.||||
spring.cloud.azure.keyvault.credential.client-id|Client id to use when performing service principal authentication with Azure.||||
spring.cloud.azure.keyvault.credential.client-secret|Client secret to use when performing service principal authentication with Azure.||||
spring.cloud.azure.keyvault.credential.tenant-id|Tenant id for the Azure resources.|||
spring.cloud.azure.keyvault.enabled|To turn on/off Azure Key Vault Secret property source.|true||
spring.cloud.azure.keyvault.environment.authority-host|Authority Host URI|https://login.microsoftonline.com/||
spring.cloud.azure.keyvault.order|Define the order of the key vaults you are delivering (comma delimited, e.g 'my-vault, my-vault-2').|||
spring.cloud.azure.keyvault.refresh-interval|Interval for PropertySource to refresh secret keys|1800000(ms)||
spring.cloud.azure.keyvault.secret-keys|If application using specific secret keys. If this property is set, application will only load the keys in the property and won't load all the keys from keyvault, that means if you want to update your secrets, you need to restart the application rather than only add secrets in the keyvault.|||
spring.cloud.azure.keyvault.secret-service-version|Valid secret-service-version value can be found [here][version_link].|The latest value||
spring.cloud.azure.keyvault.uri|Azure Key Vault Uri.|||

Note: for multiple Key Vault usage, specify your Key Vault name after the prefix of `spring.cloud.azure.keyvault`.
#### Deprecated Properties
|Name|Description|Default Value|Comment|
|:---|:---|:---|:---
azure.keyvault.authority-host|Authority Host URI|https://login.microsoftonline.com/ |Please use **spring.cloud.azure.keyvault.environment.authority-host** instead.|
azure.keyvault.case-sensitive-keys|Defines the constant for the property that enables/disables case sensitive keys.|false|Please use **spring.cloud.azure.keyvault.case-sensitive-keys** instead.|
azure.keyvault.certificate-password|Password of the certificate file to use when performing service principal authentication with Azure.| |Please use **spring.cloud.azure.keyvault.credential.client-certificate-password** instead.|
azure.keyvault.certificate-path|Path of a PEM certificate file to use when performing service principal authentication with Azure.| |Please use **spring.cloud.azure.keyvault.credential.client-certificate-path** instead.|
azure.keyvault.client-id|Client id to use when performing service principal authentication with Azure.| |Please use **spring.cloud.azure.keyvault.credential.client-id** instead.|
azure.keyvault.client-key|Client secret to use when performing service principal authentication with Azure.| |Please use **spring.cloud.azure.keyvault.credential.client-secret** instead.|
azure.keyvault.enabled|To turn on/off Azure Key Vault Secret property source.|true|Please use **spring.cloud.azure.keyvault.enabled** instead.|
azure.keyvault.order|Define the order of the key vaults you are delivering (comma delimited, e.g 'my-vault, my-vault-2').| |Please use **spring.cloud.azure.keyvault.order** instead.|
azure.keyvault.refresh-interval|Interval for PropertySource to refresh secret keys|1800000(ms) |Please use **spring.cloud.azure.keyvault.refresh-interval** instead.|
azure.keyvault.secret-keys|If application using specific secret keys. If this property is set, application will only load the keys in the property and won't load all the keys from keyvault, that means if you want to update your secrets, you need to restart the application rather than only add secrets in the keyvault.| |Please use **spring.cloud.azure.keyvault.secret-keys** instead.|
azure.keyvault.secret-service-version|Valid secret-service-version value can be found [here][version_link].|The latest value |Please use **spring.cloud.azure.keyvault.secret-service-version** instead.|
azure.keyvault.tenant-id|Tenant id for the Azure resources.| |Please use **spring.cloud.azure.keyvault.credential.tenant-id** instead.|
azure.keyvault.uri|Azure Key Vault Uri.| |Please use **spring.cloud.azure.keyvault.uri** instead.|

#### Unified Azure Spring Properties
1. Credential Properties
|Name|Description|Default Value|Comment|
|:---|:---|:---|:---
spring.cloud.azure.credential.client-id|Client id to use when performing service principal authentication with Azure.|||
spring.cloud.azure.credential.client-secret|Client secret to use when performing service principal authentication with Azure.|||
spring.cloud.azure.credential.client-certificate-path|Path of a PEM certificate file to use when performing service principal authentication with Azure.|||
spring.cloud.azure.credential.client-certificate-password|Password of the certificate file to use when performing service principal authentication with Azure.|||
spring.cloud.azure.credential.tenant-id|Tenant id for the Azure resources.||||

2. EnvironmentProperties
|Name|Description|Default Value|Comment|
|:---|:---|:---|:---
spring.cloud.azure.environment.authority-host|Authority Host URI|https://login.microsoftonline.com/||
   
### Multiple Key Vault support

If you want to use multiple Key Vaults you need to define names for each of the
Key Vaults you want to use and in which order the Key Vaults should be consulted.
If a property exists in multiple Key Vaults the order determine which value you
will get back.

### Case sensitive key mode

The new case sensitive mode allows you to use case sensitive Key Vault names. Note
that the Key Vault secret key still needs to honor the naming limitation as 
described in the “keyvault-name” element of [About keys, secrets, and certificates](https://docs.microsoft.com/azure/key-vault/general/about-keys-secrets-certificates).

If your Spring property is using a name that does not honor the Key Vault secret
key limitation use the following technique as described by 
[Externalized Configuration](https://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/features.html#boot-features-external-config-placeholders-in-properties) 
in the Spring Boot documentation.

## Examples

### Use MSI / Managed identities 
#### Azure Spring Cloud

Azure Spring Cloud supports system-assigned managed identity only at present. To use it for Azure Spring Cloud apps, add the below properties:
```
spring.cloud.azure.keyvault.enabled=true
spring.cloud.azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### App Services
To use managed identities for App Services - please refer to [How to use managed identities for App Service and Azure Functions](https://docs.microsoft.com/azure/app-service/app-service-managed-service-identity).

To use it in an App Service, add the below properties:
```
spring.cloud.azure.keyvault.enabled=true
spring.cloud.azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### VM       
To use it for virtual machines, please refer to [Azure AD managed identities for Azure resources documentation](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/).

To use it in a VM, add the below properties:
```
spring.cloud.azure.keyvault.enabled=true
spring.cloud.azure.keyvault.uri=put-your-azure-keyvault-uri-here
spring.cloud.azure.keyvault.credential.client-id=put-your-azure-client-id-here
``` 

If you are using system assigned identity you don't need to specify the client-id.

### Save secrets in Azure Key Vault
Save secrets in Azure Key Vault through [Azure Portal](https://blogs.technet.microsoft.com/kv/2016/09/12/manage-your-key-vaults-from-new-azure-portal/) or [Azure CLI](https://docs.microsoft.com/cli/azure/keyvault/secret).

You can use the following Azure CLI command to save secrets, if Key Vault is already created.
```
az keyvault secret set --name <your-property-name> --value <your-secret-property-value> --vault-name <your-keyvault-name>
```
> NOTE
> To get detail steps on how setup Azure Key Vault, please refer to sample code readme section ["Setup Azure Key Vault"](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/keyvault/azure-spring-boot-sample-keyvault-secrets/README.md)

> **IMPORTANT** 
> Allowed secret name pattern in Azure Key Vault is ^[0-9a-zA-Z-]+$, for some Spring system properties contains `.` like spring.datasource.url, do below workaround when you save it into Azure Key Vault: simply replace `.` to `-`. `spring.datasource.url` will be saved with name `spring-datasource-url` in Azure Key Vault. While in client application, use original `spring.datasource.url` to retrieve property value, this starter will take care of transformation for you. Purpose of using this way is to integrate with Spring existing property setting.

### Get Key Vault secret value as property
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

### Multiple Key Vault support
The example below shows a setup for 2 key vaults, named `keyvault1` and
`keyvault2`. The order specifies that `keyvault1` will be consulted first.

```
spring.cloud.azure.keyvault.order=keyvault1,keyvault2
spring.cloud.azure.keyvault.keyvault1.uri=put-a-azure-keyvault-uri-here
spring.cloud.azure.keyvault.keyvault1.credential.client-id=put-a-azure-client-id-here
spring.cloud.azure.keyvault.keyvault1.credential.client-secret=put-a-azure-client-key-here
spring.cloud.azure.keyvault.keyvault1.credential.tenant-id=put-a-azure-tenant-id-here
spring.cloud.azure.keyvault.keyvault2.uri=put-a-azure-keyvault-uri-here
spring.cloud.azure.keyvault.keyvault2.credential.client-id=put-a-azure-client-id-here
spring.cloud.azure.keyvault.keyvault2.credential.client-secret=put-a-azure-client-key-here
spring.cloud.azure.keyvault.keyvault2.credential.tenant-id=put-a-azure-tenant-id-here
```
Note if you decide to use multiple key vault support and you already have an
existing configuration, please make sure you migrate that configuration to the
multiple key vault variant. Mixing multiple key vaults with an existing single
key vault configuration is a non supported scenario.

### Case sensitive key mode
To enable case sensitive mode, you can set the following property in the `appliation.properties`:
```
spring.cloud.azure.keyvault.case-sensitive-keys=true
```
If your Spring property is using a name that does not honor the Key Vault secret key limitation use placeholders in properties. An example of using a placeholder:
```
my.not.compliant.property=${myCompliantKeyVaultSecret}
```

The application will take care of getting the value that is backed by the 
`myCompliantKeyVaultSecret` key name and assign its value to the non compliant
`my.not.compliant.property`.

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
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Key Vault Secrets](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/keyvault/azure-spring-boot-sample-keyvault-secrets)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-key-vault
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/keyvault/azure-spring-boot-sample-keyvault-secrets
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[version_link]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/main/java/com/azure/security/keyvault/secrets/SecretServiceVersion.java#L12
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
