# Azure Key Vault Secrets Spring Boot Starter Sample
This sample illustrates how to use [Azure Key Vault Secrets Spring Boot Starter](../../azure-spring-boot-starters/azure-keyvault-secrets-spring-boot-starter/README.md).

In this sample, a secret named `spring-datasource-url` is stored into an Azure Key Vault, and a sample Spring application will use its value as a configuration property value.

## Setup Azure Key Vault
First, we need to store secret `spring-datasource-url` into Azure Key Vault.

- Create one azure service principal by using Azure CLI or via [Azure Portal](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-create-service-principal-portal). Save your service principal id and password for later use.
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
> The property `azure.keyvault.secret.keys` specifies which exact secrets the application will load from Key Vault. If this property is not set, which means the application will have to **list** all the secrets in Key Vault, you have to grant both **LIST** and **GET** secret permission to the service principal. Otherwise, only **GET** secret permission is needed.  

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


## Add Dependency

"azure-keyvault-secrets-spring-boot-starter" is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
    <version>2.1.7</version>
</dependency>
```

## Add the property setting
Open `application.properties` file and add below properties to specify your Azure Key Vault url, Azure service principle client id and client key.

```properties
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here

# Uncomment following property if you want to specify the secrets to load from Key Vault
# azure.keyvault.secret.keys=yourSecretPropertyName1,yourSecretPropertyName2
```

## Get Key Vault secret value as property
Now, you can use Azure Key Vault secret value as a configuration property.

```
@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    @Value("${yourSecretPropertyName}")
    private String mySecretProperty;
    
    @Value("${spring.datasource.url}")
    private String dbUrl;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... varl) throws Exception {        
        System.out.println("property yourSecretPropertyName value is: " + mySecretProperty);
        System.out.println("property spring.datasource.url is: " + dbUrl);
    }

}
```


## Run the sample
   - Use Maven 

     ```
     mvn package
     java -jar target/azure-keyvault-secrets-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

   - Use Gradle 
   
     ```
     gradle bootRepackage
     java -jar build/libs/azure-keyvault-secrets-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```
