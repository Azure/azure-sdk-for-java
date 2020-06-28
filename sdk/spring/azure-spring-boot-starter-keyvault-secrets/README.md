## Azure Key Vault Secrets Spring boot starter client library for Java
Azure Key Vault Secrets Spring boot starter is Spring starter for [Azure Key Vault Secrets](https://docs.microsoft.com/rest/api/keyvault/about-keys--secrets-and-certificates#BKMK_WorkingWithSecrets). With this starter, Azure Key Vault is added as one of Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently accessed like other externalized configuration property, e.g. properties in files.

## Key concepts

## Getting started
### Add the dependency

`azure-keyvault-secrets-spring-boot-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-keyvault-secrets-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
    <version>2.3.3-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Custom settings
To use the custom configuration, open `application.properties` file and add below properties to specify your Azure Key Vault url, Azure service principal client id and client key. `azure.keyvault.enabled` is used to turn on/off Azure Key Vault Secret property source, default is true. `azure.keyvault.token-acquiring-timeout-seconds` is used to specify the timeout in seconds when acquiring token from Azure AAD. Default value is 60 seconds. This property is optional. `azure.keyvault.refresh-interval` is the period for PropertySource to refresh secret keys, its value is 1800000(ms) by default. This property is optional. `azure.keyvault.secret.keys` is a property to indicate that if application using specific secret keys, if this property is set, application will only load the keys in the property and won't load all the keys from keyvault, that means if you want to update your secrets, you need to restart the application rather than only add secrets in the keyvault.
```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.token-acquire-timeout-seconds=60
azure.keyvault.refresh-interval=1800000
azure.keyvault.secret.keys=key1,key2,key3
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
> To get detail steps on how setup Azure Key Vault, please refer to sample code readme section ["Setup Azure Key Vault"](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets/README.md)

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
## Examples
Please refer to [sample project here](../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets).

## Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
azure.keyvault.allow.telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.    
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Multiple Key Vault support

If you want to use multiple key vaults you need to define names for each of the
key vaults you want to use and in which order the key vaults should be consulted.
If a property exists in multiple key vaults the order determine which value you
will get back.

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

## Case sensitive key mode

The new case sensitive mode allows you to use case sensitive key vault names. Note
that the key vault secret key still needs to honor the naming limitation as 
described in [About keys, secrets, and certificates](https://docs.microsoft.com/en-us/azure/key-vault/general/about-keys-secrets-certificates).

To enable case sensitive mode use:

```
azure.keyvault.case-sensitive-keys=true
```

## Placeholders in properties

If your Spring property is using a name that does not honor the key vault secret
key limitation use the following technique as described by 
[Externalized Configuration](https://docs.spring.io/autorepo/docs/spring-boot/2.2.7.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-placeholders-in-properties) 
in the Spring Boot documentation.

An example of using a placeholder:

```
my.not.compliant.property=${myCompliantKeyVaultSecret}
```

The application will take care of getting the value that is backed by the 
`myCompliantKeyVaultSecret` key name and assign its value to the non compliant
`my.not.compliant.property`.

## Troubleshooting
## Next steps
## Contributing
