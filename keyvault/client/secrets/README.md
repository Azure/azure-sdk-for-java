# Azure Key Vault Secret client library for Java
Azure Key Vault is a tool for securely storing and accessing secrets. A secret is anything that you want to tightly control access to, such as API keys or passwords. A vault is logical group of secrets.
Secret client library allows you to securely store and tightly control access to tokens, passwords, API keys, and other secrets. The library offers operations to create, retrieve, update, delete, purge, backup, restore and and list the secrets.

Use the secret client library to create and manage secrets.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault] : Note that this document shows how to create one.

### Adding the package to your project

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-secrets</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Create an Azure Key Vault.

To create an Azure Key Vault you can use the Azure Portal or [Azure Keyvault CLI][azure_keyvault_cli].

Create the Azure Key Vault:
```Powershell
az keyvault create --name <keyvault-name> --resource-group <resource-group-name> --location eastus
```

### Authenticate the client

Applications that use a key vault must authenticate by using a token from Azure Active Directory. The owner of the application must register it in Azure Active Directory first. At the end of registration, the application owner gets the following values:

* An Application ID (also known as the AAD Client ID or appID)
* An authentication key (also known as the shared secret).

The application must present both these values to Azure Active Directory, to get a token. 

#### Create/Get Credentials

To create an application/service-principal you can use the [Azure Portal][azure_create_application_in_portal] or [Azure CLI][azure_cli].

Create an application/service-principal in the Azure Active Directory.
```Powershell
az ad sp create-for-rbac -n <application-name> --password <application-password> --skip-assignment
# If you don't specify a password, one will be created for you.
```

To authorize the same application to perform secret operations in your vault, type the following command:
```Powershell
az keyvault set-policy --name <keyvault-name> --spn <your-service-principal-id> --secret-permissions <secret-permissions>
```

#### Create Client

In order to interact with the Azure Key Vault Secrets service you'll need to create an instance of the Secret Client class. To do this you'll need the application id and application key of an application in Azure Active Directory authorized with access to key vault.

Once you have the values of the application id, application key and tenant id you can create the secret client.
The following environment varaibles need to be configured for authorizing with your key vault via default credentials.
1. AZURE_CLIENT_ID - The application id.
2. AZURE_CLIENT_KEY - The application key.
3. AZURE_TENANT_ID - The id of the Azure Active Directory under which your application is registered.

```Java
SecretClient client = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
```

or

```Java
SecretAsyncClient client = SecretAsyncClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
```

## Key concepts

### Secret

A secret is the fundamental resource within an Azure Key Vault. In its simplest form it is a name and a value. However, there are additional properties such as:
   1. content type : Type of the secret value such as a password.
   2. tags : Application specific metadata in the form of key-value pairs.
   3. expires : Specifies a UTC time at which secret will no loger be active.
   4. notBefore : Specifies a UTC time after which secret will be active.

### Secret Client

The client performs the interactions with the Azure Key Vault service, getting, setting, updating, deleting, and listing secrets. An asynchronous, `SecretAsyncClient`, and synchronous, `SecretClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

## Sync API Examples

The following sections provide several code snippets covering some of the most common Azure Key Vault Secret Service tasks, including:
- [Create a Secret](#create-a-Secret)
- [Retrieve a Secret](#retrieve-a-Secret)
- [Update an existing Secret](#update-an-existing-Secret)
- [Delete a Secret](#delete-a-Secret)

### Create a Secret

Create a Secret to be stored in the Azure Key Vault.
- setSecret creates a new secret in the key vault. if the secret with name already exists then a new version of the secret is created.
```Java
SecretClient secretClient = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
Secret secret = secretClient.setSecret("secret_name", "secret_value").value();
System.out.printf("Secret is created with name %s and value %s \n", secret.name(), secret.value());

```

### Retrieve a Secret

Retrieve a previously stored Secret by calling getSecret.
```Java
SecretClient secretClient = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
secretClient.setSecret("secret_name", "secret_value");
Secret secret = secretClient.getSecret("secret_name").value();
System.out.printf("Secret is returned with name %s and value %s \n", secret.name(), secret.value());
```

### Update an existing Secret

Update an existing Secret by calling updateSecret.
```Java
SecretClient secretClient = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
Secret secret = secretClient.setSecret("secret_name", "secret_value").value();
secret.expires(OffsetDateTime.now().plusDays(30));
SecretBase updatedSecret = secretClient.updateSecret(secret).value();
System.out.printf("Secret's updated expiry time %s \n", updatedSecret.expires().toString());
```

### Delete a Secret

Delete an existing Secret by calling deleteSecret.
```Java
SecretClient secretClient = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
Secret secret = secretClient.setSecret("secret_name", "secret_value").value();
DeletedSecret deletedSecret = client.deleteSecret("secret_name").value();
System.out.printf("Deleted Secret's deletion time %s", deletedSecret.deletedDate().toString());
```

## Async API Examples

The following sections provide several code snippets covering some of the most common Azure Key Vault Secret Service tasks, including:
- [Create a Secret](#create-a-Secret)
- [Retrieve a Secret](#retrieve-a-Secret)
- [Update an existing Secret](#update-an-existing-Secret)
- [Delete a Secret](#delete-a-Secret)

### Create a Secret

Create a Secret to be stored in the Azure Key Vault.
- setSecret creates a new secret in the key vault. if the secret with name already exists then a new version of the secret is created.
```Java
SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();

secretAsyncClient.setSecret("secret_name", "secret_value").subscribe(secretResponse ->
  System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));
```

### Retrieve a Secret

Retrieve a previously stored Secret by calling getSecret.
```Java
SecretClient secretAsyncClient = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
        
secretAsyncClient.getSecret("secretName").subscribe(secretResponse ->
  System.out.printf("Secret with name %s , value %s \n", secretResponse.value().name(),
  secretResponse.value().value()));
```

### Update an existing Secret

Update an existing Secret by calling updateSecret.
```Java
SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();

secretAsyncClient.getSecret("secretName").subscribe(secretResponse -> {
     Secret secret = secretResponse.value();
     //Update the expiry time of the secret.
     secret.expires(OffsetDateTime.now().plusDays(50));
     secretAsyncClient.updateSecret(secret).subscribe(secretResponse ->
         System.out.printf("Secret's updated not before time %s \n", secretResponse.value().notBefore().toString()));
   });
```

### Delete a Secret

Delete an existing Secret by calling deleteSecret.
```Java
SecretClient client = SecretClient.builder()
        .endpoint("https://myvault.vault.azure.net")
        .credentials(AzureCredential.DEFAULT)
        .build();
        
secretAsyncClient.deleteSecret("secretName").subscribe(deletedSecretResponse ->
   System.out.printf("Deleted Secret's deletion time %s \n", deletedSecretResponse.value().deletedDate().toString()));
```

## Troubleshooting

### General

When you interact with Azure Key Vault Secrets service using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][azkeyvault_rest] requests. For example, if you try to retrieve a Secret that doesn't exist in your Key Vault, a `404` error is returned, indicating `Not Found`.

## Next steps
Several KeyVault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

### Sync API Samples
* [HelloWorld.java](TODO) - Contains sync api snippets found in this article.

* [ListOperations.java](TODO) 
    * Creating Secrets
    * Listing Secrets
    * Create new version of existing secret.
    * List secret versions

* [BackupAndRestoreOperations.java](TODO)
    * Create a Secret
    * Backup a Secret -- Write it to a file.
    * Delete a secret
    * Restore a secret

* [ManagingDeletedSecrets.java](TODO)
    * Create a Secret
    * Delete a secret
    * List deleted secrets
    * Recover a deleted secret
    * Purge Deleted secret
    
### Async API Samples
* [HelloWorldAsync.java] (TODO) - Contains async api snippets found in this article.
* [ListOperationsAsync.java](TODO)
    * Creating Secrets
    * Listing Secrets
    * Create new version of existing secret.
    * List secret versions
* [BackupAndRestoreOperationsAsync.java](TODO) - Java code to backup and restore a deleted secret:
    * Create a Secret
    * Backup a Secret -- Write it to a file.
    * Delete a secret
    * Restore a secret
* [ManagingDeletedSecretsAsync.java](TODO) - Java code for working with deleted secrets:
    * Create a Secret
    * Delete a secret
    * List deleted secrets
    * Recover a deleted secret
    * Purge Deleted secret

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/secrets/src
[package]:not-valid-link
[api_documentation]: not-valid-link
[azkeyvault_docs]: https://docs.microsoft.com/en-us/azure/key-vault/
[jdk]: https://docs.microsoft.com/en-us/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/en-us/free/
[azure_keyvault]: https://docs.microsoft.com/en-us/azure/key-vault/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/en-us/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/en-us/rest/api/keyvault/
[azure_create_application_in_portal]:https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]:https://docs.microsoft.com/en-us/azure/key-vault/quick-create-cli
