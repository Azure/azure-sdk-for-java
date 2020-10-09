# Azure Key Vault Secrets Spring Boot starter client library for Java
Azure Key Vault Secrets Spring boot starter is Spring starter for [Azure Key Vault Secrets](https://docs.microsoft.com/rest/api/keyvault/about-keys--secrets-and-certificates#BKMK_WorkingWithSecrets). With this starter, Azure Key Vault is added as one of Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently accessed like other externalized configuration property, e.g. properties in files.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.azure:azure-spring-boot-starter-keyvault-secrets;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-boot-starter-keyvault-secrets</artifactId>
    <version>3.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Key Vault provides secure storage of secrets, such as passwords and database connection strings.

From a developer's perspective, Key Vault APIs accept and return secret values as strings. Internally, Key Vault stores and manages secrets as sequences of octets (8-bit bytes), with a maximum size of 25k bytes each. The Key Vault service doesn't provide semantics for secrets. It merely accepts the data, encrypts it, stores it, and returns a secret identifier ("id"). The identifier can be used to retrieve the secret at a later time.

For highly sensitive data, clients should consider additional layers of protection for data. Encrypting data using a separate protection key prior to storage in Key Vault is one example.

Key Vault also supports a contentType field for secrets. Clients may specify the content type of a secret to assist in interpreting the secret data when it's retrieved. The maximum length of this field is 255 characters. There are no pre-defined values. The suggested usage is as a hint for interpreting the secret data.

Besides, this starter provides features of supporting multiple Key Vaults, case sensitive mode of Key Vault names and using placeholder presenting Key Vault names in property file
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
[Externalized Configuration](https://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/spring-boot-features.html#boot-features-external-config-placeholders-in-properties) 
in the Spring Boot documentation.

## Examples
### Custom settings
To use the custom configuration, open `application.properties` file and add below properties to specify your Azure Key Vault url, Azure service principal client id and client key. `azure.keyvault.enabled` is used to turn on/off Azure Key Vault Secret property source, default is true. `azure.keyvault.token-acquiring-timeout-seconds` is used to specify the timeout in seconds when acquiring token from Azure AAD. Default value is 60 seconds. This property is optional. `azure.keyvault.refresh-interval` is the period for PropertySource to refresh secret keys, its value is 1800000(ms) by default. This property is optional. `azure.keyvault.secret-keys` is a property to indicate that if application using specific secret keys, if this property is set, application will only load the keys in the property and won't load all the keys from keyvault, that means if you want to update your secrets, you need to restart the application rather than only add secrets in the keyvault.
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.token-acquire-timeout-seconds=60
azure.keyvault.refresh-interval=1800000
azure.keyvault.secret-keys=key1,key2,key3
```

### Use MSI / Managed identities 
#### Azure Spring Cloud

Azure Spring Cloud supports system-assigned managed identity only at present. To use it for Azure Spring Cloud apps, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### App Services
To use managed identities for App Services - please refer to [How to use managed identities for App Service and Azure Functions](https://docs.microsoft.com/azure/app-service/app-service-managed-service-identity).

To use it in an App Service, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### VM       
To use it for virtual machines, please refer to [Azure AD managed identities for Azure resources documentation](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/).

To use it in a VM, add the below properties:
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
``` 

If you are using system assigned identity you don't need to specify the client-id.

### Save secrets in Azure Key Vault
Save secrets in Azure Key Vault through [Azure Portal](https://blogs.technet.microsoft.com/kv/2016/09/12/manage-your-key-vaults-from-new-azure-portal/) or [Azure CLI](https://docs.microsoft.com/cli/azure/keyvault/secret).

You can use the following Azure CLI command to save secrets, if Key Vault is already created.
```
az keyvault secret set --name <your-property-name> --value <your-secret-property-value> --vault-name <your-keyvault-name>
```
> NOTE
> To get detail steps on how setup Azure Key Vault, please refer to sample code readme section ["Setup Azure Key Vault"](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets/README.md)

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
Note if you decide to use multiple key vault support and you already have an
existing configuration, please make sure you migrate that configuration to the
multiple key vault variant. Mixing multiple key vaults with an existing single
key vault configuration is a non supported scenario.

### Case sensitive key mode
To enable case sensitive mode, you can set the following property in the `appliation.properties`:
```
azure.keyvault.case-sensitive-keys=true
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

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 

## Next steps
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Key Vault Secrets](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-key-vault
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-keyvault-secrets-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
