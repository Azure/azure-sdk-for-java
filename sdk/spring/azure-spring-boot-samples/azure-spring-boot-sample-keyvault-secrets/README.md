# Sample for Azure Key Vault Secrets Spring Boot Starter client library for Java

## Key concepts
This sample illustrates how to use [Azure Key Vault Secrets Spring Boot Starter](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-secrets/README.md).

In this sample, a secret named `spring-datasource-url` is stored into an Azure Key Vault, and a sample Spring application will use its value as a configuration property value.

## Getting started
First, we need to store secret `spring-datasource-url` into Azure Key Vault.

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
az keyvault secret set --name spring-datasource-url                \
                       --value jdbc:mysql://localhost:3306/moviedb \
                       --vault-name <your_keyvault_name>
az keyvault secret set --name <yourSecretPropertyName>   \
                       --value <yourSecretPropertyVaule> \
                       --vault-name <your_keyvault_name>
```


## Examples

### Add the property setting
Open `application.properties` file and add below properties to specify your Azure Key Vault url, Azure service principle client id and client key.

```properties
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here

# Uncomment following property if you want to specify the secrets to load from Key Vault
# azure.keyvault.secret-keys=yourSecretPropertyName1,yourSecretPropertyName2
```


## Run with Maven
```
# Under sdk/spring project root directory
mvn clean install -DskipTests
cd azure-spring-boot-samples/azure-spring-boot-sample-keyvault-secrets
mvn spring-boot:run
```

## Troubleshooting
## Next steps
## Contributing
