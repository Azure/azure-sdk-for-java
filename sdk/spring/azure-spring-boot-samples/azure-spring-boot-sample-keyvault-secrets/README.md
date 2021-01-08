# Sample for Azure Key Vault Secrets Spring Boot Starter client library for Java

## Key concepts
This sample illustrates how to use [Azure Spring Boot Starter Key Vault Secrets ](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-secrets/README.md).

In this sample, a secret named `spring-data-source-url` is stored into an Azure Key Vault, and a sample Spring application will use its value as a configuration property value.

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Store Secret
We need to store secret `spring-data-source-url` into Azure Key Vault.

- Create one azure service principal by using Azure CLI or via [Azure Portal](https://docs.microsoft.com/azure/azure-resource-manager/resource-group-create-service-principal-portal). Save your service principal id and password for later use.
You can use the following az cli commands to create a service principal:
```bash
az login
az account set --subscription <your_subscription_id>

# create azure service principal by azure cli
az ad sp create-for-rbac --name <your_azure_service_principal_name>
# save the appId and password from output
```
Save the service principal id and password contained in the output from above command.

- Create Azure Key Vault by using Azure CLI or via [Azure Portal](https://portal.azure.com). You also need to grant appropriate permissions to the service principal created.
You can use the following az cli commands:
```bash
az keyvault create --name <your_keyvault_name>            \
                   --resource-group <your_resource_group> \
                   --location <location>                  \
                   --enabled-for-deployment true          \
                   --enabled-for-disk-encryption true     \
                   --enabled-for-template-deployment true \
                   --sku standard
az keyvault set-policy --name <your_keyvault_name>   \
                       --secret-permission get list  \
                       --spn <your_sp_id_create_in_step1>
```
> **IMPORTANT** 
>
> The property `azure.keyvault.secret-keys` specifies which exact secrets the application will load from Key Vault. If this property is not set, which means the application will have to **list** all the secrets in Key Vault, you have to grant both **LIST** and **GET** secret permission to the service principal. Otherwise, only **GET** secret permission is needed.  

Save the displayed Key Vault uri for later use.

- Set secret in Azure Key Vault by using Azure CLI or via Azure Portal. 
You can use the following az cli commands:
```bash
az keyvault secret set --name spring-data-source-url                \
                       --value jdbc:mysql://localhost:3306/moviedb \
                       --vault-name <your_keyvault_name>
az keyvault secret set --name <yourSecretPropertyName>   \
                       --value <yourSecretPropertyVaule> \
                       --vault-name <your_keyvault_name>
```

- If you want to use certificate authentication, upload the certificate file to App registrations or  in Azure Active Directory. 
    - Upload using Azure Portal
        1. Select **App registrations**, then select the application name or service principal name just created.
        
        1. Select **Certificates & secrets**, then select **Upload Certificate**, upload your cer, pem, or crt type certificate, click **Add** button to complete the upload.
        
        1. If you add a new application, one more step is to grant appropriate permissions to the application created. Please see [Assign an access policy][assign-an-access-policy]. 
           You can also use the above `az keyvault set-policy` command to authorize the application id to access the Key Vault.
        
    - Upload using Azure Cli
        1. You can use the following az cli commands to create a service principal with the certificate, and complete the certificate configuration in one step. Please see [Certificate-based authentication][certificate-based-authentication].
           ```bash
           # create azure service principal with the certificate by azure cli
           az ad sp create-for-rbac --name <your_azure_service_principal_name> --cert @/path/to/cert.pem
           # save the appId and password from output
           az keyvault set-policy --name <your_keyvault_name>   \
                                  --secret-permission get list  \
                                  --spn <your_sp_id_create_in_current_step>
           ```
    
## Examples

### The key-based authentication property setting
Open `application.properties` file and add below properties to specify your Azure Key Vault url, Azure service principal client id and client key.

```properties
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.authority-host=put-your-own-authority-host-here(fill with default value if empty)
azure.keyvault.secret-service-version=specify secretServiceVersion value(fill with default value if empty)


# Uncomment following property if you want to specify the secrets to load from Key Vault
# azure.keyvault.secret-keys=yourSecretPropertyName1,yourSecretPropertyName2
```

`azure.keyvault.authority-host`

The URL at which your identity provider can be reached.

- If working with azure global, just left the property blank, and the value will be filled with the default value.

- If working with azure stack, set the property with authority URL.

`azure.keyvault.secret-service-version`

The valid secret-service-version value can be found [here][version_link]. 

If property not set, the property will be filled with the latest value.

### The certificate-based authentication property setting
If you use certificate authentication, you only need to replace the property `azure.keyvault.client-key` with `azure.keyvault.certificate-path`, which points to your certificate.

```properties
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.certificate-path=put-your-certificate-file-path-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.authority-host=put-your-own-authority-host-here(fill with default value if empty)
azure.keyvault.secret-service-version=specify secretServiceVersion value(fill with default value if empty)
```

## Run with Maven
```
cd azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets
mvn spring-boot:run
```

## Troubleshooting
## Next steps
## Contributing


<!-- links -->
[version_link]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/main/java/com/azure/security/keyvault/secrets/SecretServiceVersion.java#L12
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[certificate-based-authentication]: https://docs.microsoft.com/cli/azure/create-an-azure-service-principal-azure-cli#certificate-based-authentication
[assign-an-access-policy]: https://docs.microsoft.com/azure/key-vault/general/assign-access-policy-portal#assign-an-access-policy
