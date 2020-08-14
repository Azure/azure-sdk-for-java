# Spring Cloud Azure Config Conversion Sample client library for Java

This sample shows how to convert a Spring Cloud Application with Cosmos DB to be using App Configuration + Key Vault

## Key concepts
## Getting started
### Prerequisite

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/free/).

* A [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable), version 8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

### Quick Start

#### Create an Azure Cosmos DB on Azure

1. Use the Azure CLI [az cosmosdb create](https://docs.microsoft.com/cli/azure/cosmosdb?view=azure-cli-latest#az-cosmosdb-create).

    ```azurecli
    az cosmosdb create --name my-cosmos-db --resource-group MyResourceGroup
    ```

    This operation will return json, among them is a documentEndpoint, record this.

    ```azurecli
    {
      ...
      "documentEndpoint": "https://my-cosmos.documents.azure.com:443/",
      ...
    }
    ```

1. Then use the [az cosmosdb keys list](https://docs.microsoft.com/cli/azure/cosmosdb/keys?view=azure-cli-latest#az-cosmosdb-keys-list).

    ```azurecli
        az cosmosdb keys list --name my-cosmos-db -g MyResourceGroup
    ```

    Record the primaryMasterKey.

    ```azurecli
    {
      "primaryMasterKey": "...",
      "primaryReadonlyMasterKey": "...",
      "secondaryMasterKey": "...",
      "secondaryReadonlyMasterKey": "..."
    }
    ```

#### Clone the sample Project

In this section, you clone a containerized Spring Boot application and test it locally.

1. Open a command prompt or terminal window and create a local directory to hold your Spring Boot application, and change to that directory; for example:

   ```shell
   md C:\SpringBoot
   cd C:\SpringBoot
   ```

   -- or --

   ```shell
   md /users/robert/SpringBoot
   cd /users/robert/SpringBoot
   ```

1. Clone the [Spring Boot on Docker Getting Started] sample project into the directory you created; for example:

   ```shell
   git clone https://github.com/microsoft/spring-cloud-azure.git
   ```

1. Change directory to the initial project; for example:

   ```shell
   cd sdk/spring/azure-spring-boot-samples/azure-appconfiguration-conversion-sample-initial
   ```

#### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.

1. Replace below properties in `application.properties` with information from your database.

   ```properties
   azure.cosmosdb.uri=your-cosmosdb-uri
   azure.cosmosdb.key=your-cosmosdb-key
   azure.cosmosdb.database=your-cosmosdb-databasename

   ```

#### Run the sample

1. Build the JAR file using Maven; for example:

   ```shell
   mvn clean package
   ```

1. When the web app has been created, start the web app using Maven; for example:

   ```shell
   mvn spring-boot:run
   ```

1. View the results in the console.

1. You should see the following message displayed: **findOne in User collection get result: testFirstName**

#### Convert to Using App Configuration

1. Use the Azure CLI [az keyvault create](https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest#az-keyvault-create)

    ```azurecli
    az keyvault create --name myVaultName -g MyResourceGroup
    ```

1. Use the Azure CLI [az ad sp](https://docs.microsoft.com/cli/azure/ad/sp?view=azure-cli-latest#az-ad-sp-create-for-rbac)

    ```azurecli
    az ad sp create-for-rbac -n "http://mySP" --sdk-auth
    ```

    This operation returns a series of key/value pairs:

    ```console
    {
    "clientId": "7da18cae-779c-41fc-992e-0527854c6583",
    "clientSecret": "b421b443-1669-4cd7-b5b1-394d5c945002",
    "subscriptionId": "443e30da-feca-47c4-b68f-1636b75e16b3",
    "tenantId": "35ad10f1-7799-4766-9acf-f2d946161b77",
    "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
    "resourceManagerEndpointUrl": "https://management.azure.com/",
    "activeDirectoryGraphResourceId": "https://graph.windows.net/",
    "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
    "galleryEndpointUrl": "https://gallery.azure.com/",
    "managementEndpointUrl": "https://management.core.windows.net/"
    }
    ```

1. Run the following command to let the service principal access your key vault:

    ```console
    az keyvault set-policy -n <your-unique-keyvault-name> --spn <clientId-of-your-service-principal> --secret-permissions delete get
    ```

1. Use the Azure CLI [az appconfig create](https://docs.microsoft.com/cli/azure/appconfig?view=azure-cli-latest#az-appconfig-create)

    ```azurecli
    az appconfig create -n myAppconfigName -g MyResourceGroup -l westus --sku Standard
    ```

1. Run the following command to get your object-id, then add it to App Configuration.

    ```console
    az ad sp show --id <clientId-of-your-service-principal>
    az role assignment create --role "App Configuration Data Reader" --assignee-object-id <objectId-of-your-service-principal> --resource-group <your-resource-group>
    ```

1. Create the following environment variables, using the values for the service principal that were displayed in the previous step:

    * **AZURE_CLIENT_ID**: *clientId*
    * **AZURE_CLIENT_SECRET**: *clientSecret*
    * **AZURE_TENANT_ID**: *tenantId*

1. Upload your Cosmos DB key to Key Vault.

    ```azurecli
        az keyvault secret set --vault-name myVaultName --name "COSMOSDB-KEY" --value your-cosmosdb-key
    ```

1. Upload your Configurations Cosmos DB name and URI to App Configuration

    ```azurecli
        az appconfig kv set --name myConfigStoreName --key "/application/azure.cosmosdb.database" --value your-cosmos-db-databasename --yes
        az appconfig kv set --name myConfigStoreName --key "/application/azure.cosmosdb.uri" --value your-cosmosdb-uri  --yes
    ```

1. Add a Key Vault Reference to App Configuration, make sure to update the uri with your config store name.

    ```azurecli
        az appconfig kv set-keyvault --name myConfigStoreName --key "/application/azure.cosmosdb.key" --secret-identifier https://myVaultName.vault.azure.net/secrets/COSMOSDB-KEY --yes
    ```

1. Delete `application.propertes` from `src/main/resources`.

1. Create a new file called `bootstrap.properties` in `src/main/resources`, and add the following.

    ```properties
        spring.cloud.azure.appconfiguration.stores[0].endpoint=https://{my-configstore-name}.azconfig.io

    ```

1. Update the pom.xml file to now include.

    ```xml
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>spring-cloud-starter-azure-appconfiguration-config</artifactId>
        <version>1.2.2</version>
    </dependency>
    ```

1. Create a new file called *AzureCredentials.java* and add the code below.

    ```java
    /*
     * Copyright (c) Microsoft Corporation. All rights reserved.
     * Licensed under the MIT License. See LICENSE in the project root for
     * license information.
     */
    package sample.convert;

    import com.azure.core.credential.TokenCredential;
    import com.azure.identity.EnvironmentCredentialBuilder;
    import com.microsoft.azure.spring.cloud.config.AppConfigurationCredentialProvider;
    import com.microsoft.azure.spring.cloud.config.KeyVaultCredentialProvider;

    public class AzureCredentials implements AppConfigurationCredentialProvider, KeyVaultCredentialProvider{

        @Override
        public TokenCredential getKeyVaultCredential(String uri) {
            return getCredential();
        }

        @Override
        public TokenCredential getAppConfigCredential(String uri) {
            return getCredential();
        }

        private TokenCredential getCredential() {
            return new EnvironmentCredentialBuilder().build();
        }

    }
    ```

    1. Create a new file called *AppConfiguration.java*. And add the code below.

    ```java
    /*
     * Copyright (c) Microsoft Corporation. All rights reserved.
     * Licensed under the MIT License. See LICENSE in the project root for
     * license information.
     */
    package sample.convert;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class AppConfiguration {

        @Bean
        public AzureCredentials azureCredentials() {
            return new AzureCredentials();
        }
    }
    ```

1. Create a new folder in your resources directory called META-INF. Then in that folder create a file called *spring.factories* and add.

    ```factories
    org.springframework.cloud.bootstrap.BootstrapConfiguration=\
    sample.convert.AppConfiguration
    ```

#### Run the updated sample

1. Build the JAR file using Maven; for example:

   ```shell
   mvn clean package
   ```

1. When the web app has been created, start the web app using Maven; for example:

   ```shell
   mvn spring-boot:run
   ```

1. View the results in the console.

1. You should see the following message displayed: **findOne in User collection get result: testFirstName**

## Examples
## Troubleshooting
## Next steps
## Contributing
