# Azure Key Vault Secret client library for Java
Azure Key Vault is a tool for securely storing and accessing secrets. A secret is anything that you want to tightly control access to, such as API keys or passwords. A vault is logical group of secrets.
Secret client library allows you to securely store and tightly control access to tokens, passwords, API keys, and other secrets. THe library offers operations to create, retrieve, update, delete, purge, backup, restore and and list the secrets.

Use the secret client library to create and manage secrets.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Azure KeyVault][azure_keyvault]

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-secrets</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Create a Secret in the Azure Key Vault.

To create an Azure Key Vault Store you can use the Azure Portal or [Azure CLI][azure_cli].

Create the Azure Key Vault:
```Powershell
az keyvault create --name <keyvault-name> --resource-group <resource-group-name> --location eastus
```

After that, create the secret in the Azure Key Vault:
```Powershell
az keyvault secret set --vault-name <keyvault-name> --name "<secret-name>" --value "<secret-value>"
```

### Authenticate the client

Applications that use a key vault must authenticate by using a token from Azure Active Directory. The owner of the application must register it in Azure Active Directory first. At the end of registration, the application owner gets the following values:

* An Application ID (also known as the AAD Client ID or appID)
* An authentication key (also known as the shared secret).

The application must present both these values to Azure Active Directory, to get a token. 

#### Create/Get Credentials

Create an application in the Azure Active Directory.
```Powershell
az ad sp create-for-rbac -n <application-name> --password <application-password> --skip-assignment
# If you don't specify a password, one will be created for you.
```

To authorize the same application to perform secret operarions in your vault, type the following command:
```Powershell
az keyvault set-policy --name <keyvault-name> --spn 8f8c4bbd-485b-45fd-98f7-ec6300b7b4ed --secret-permissions <secret-permissions>
```

#### Create Client

In order to interact with the Azure Key Vault Secrets service you'll need to create an instance of the Secret Client class. To make this possible you'll need the application id and application key of an application in Azure Active Directory authorized with access to key vault.

Once you have the values of the appliication id and application key you can create the secret client:

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

A secret is the fundamental resource within an Azure KeyVault. In its simplest form it is a name and a value. However, there are additional properties such as the modifiable content type and tags fields that allow the value to be interpreted or associated in different ways.
The expires property of a Secret provides a way to specify a UTC time at which it will no loger be active. The notBefore property of a Secret allows to specify a UTC time after which secret will be active.

### Secret Client

The client performs the interactions with the Azure Key Vault service, getting, setting, updating, deleting, and listing secrets. An asynchronous, `SecretAsyncClient`, and synchronous, `SecretClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

## Sync API Examples

The following sections provide several code snippets covering some of the most common Azure Key Vault Secret Service tasks, including:
- [Create a Secret](#create-a-Secret)
- [Retrieve a Secret](#retrieve-a-Secret)
- [Update an existing Secret](#update-an-existing-Secret)
- [Delete a Secret](#delete-a-Secret)

### Create a Secret

Create a Secret to be stored in the Azure Key Vault. There are two ways to store a Secret:
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

Create a Secret to be stored in the Azure Key Vault. There are two ways to store a Secret:
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
