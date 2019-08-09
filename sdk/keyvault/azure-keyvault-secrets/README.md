# Azure Key Vault Secret client library for Java
Azure Key Vault is a cloud service that provides a secure storage of secrets, such as passwords and database connection strings.

Secret client library allows you to securely store and tightly control the access to tokens, passwords, API keys, and other secrets. This library offers operations to create, retrieve, update, delete, purge, backup, restore and list the secrets and its versions.

Use the secret client library to create and manage secrets.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][secrets_samples]

## Getting started
### Adding the package to your project

Maven dependency for Azure Secret Client library. Add it to your project's pom file.
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-secrets</artifactId>
    <version>4.0.0-preview.2</version>
</dependency>
```

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell](https://shell.azure.com/bash) to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Key Vault service, you'll need to create an instance of the [SecretClient](#create-secret-client) class. You would need a **vault url** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

 #### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell](https://shell.azure.com/bash)

Here is [Azure Cloud Shell](https://shell.azure.com/bash) snippet below to 

 * Create a service principal and configure its access to Azure resources:
    ```Bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```
    Output:
    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "dummy-app-name",
        "name": "http://dummy-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```
* Use the above returned credentials information to set **AZURE_CLIENT_ID**(appId), **AZURE_CLIENT_SECRET**(password) and **AZURE_TENANT_ID**(tenant) environment variables. The following example shows a way to do this in Bash:
  ```Bash
    export AZURE_CLIENT_ID="generated-app-ID"
    export AZURE_CLIENT_SECRET="random-password"
    export AZURE_TENANT_ID="tenant-ID"
  ```

* Grant the above mentioned application authorization to perform secret operations on the keyvault:
    ```Bash
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --secret-permissions backup delete get list set
    ```
    > --secret-permissions:
    > Accepted values: backup, delete, get, list, purge, recover, restore, set

* Use the above mentioned Key Vault name to retreive details of your Vault which also contains your Key Vault URL:
    ```Bash
    az keyvault show --name <your-key-vault-name> 
    ```

#### Create Secret client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the above returned URI, you can create the SecretClient:

```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;

SecretClient client = new SecretClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```
> NOTE: For using Asynchronous client use SecretAsyncClient instead of SecretClient and call buildAsyncClient()


## Key concepts
### Secret
  A secret is the fundamental resource within Azure KeyVault. From a developer's perspective, Key Vault APIs accept and return secret values as strings. In addition to the secret data, the following attributes may be specified:
* expires: Identifies the expiration time on or after which the secret data should not be retrieved.
* notBefore: Identifies the time after which the secret will be active.
* enabled: Specifies whether the secret data can be retrieved.
* created: Indicates when this version of the secret was created.
* updated: Indicates when this version of the secret was updated.

### Secret Client:
The Secret client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing secrets and its versions. An asynchronous and synchronous, SecretClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a SecretClient, you can interact with the primary resource types in Key Vault.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Secret Service tasks, including:
- [Create a Secret](#create-a-secret)
- [Retrieve a Secret](#retrieve-a-secret)
- [Update an existing Secret](#update-an-existing-secret)
- [Delete a Secret](#delete-a-secret)
- [List Secrets](#list-secrets)

### Create a Secret

Create a Secret to be stored in the Azure Key Vault.
- `setSecret` creates a new secret in the key vault. if the secret with name already exists then a new version of the secret is created.
```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.Secret;

SecretClient secretClient = new SecretClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

Secret secret = secretClient.setSecret("secret_name", "secret_value").value();
System.out.printf("Secret is created with name %s and value %s \n", secret.name(), secret.value());
```

### Retrieve a Secret

Retrieve a previously stored Secret by calling `getSecret`.
```Java
Secret secret = secretClient.getSecret("secret_name").value();
System.out.printf("Secret is returned with name %s and value %s \n", secret.name(), secret.value());
```

### Update an existing Secret

Update an existing Secret by calling `updateSecret`.
```Java
// Get the secret to update.
Secret secret = secretClient.getSecret("secret_name").value();
// Update the expiry time of the secret.
secret.expires(OffsetDateTime.now().plusDays(30));
SecretBase updatedSecret = secretClient.updateSecret(secret).value();
System.out.printf("Secret's updated expiry time %s \n", updatedSecret.expires().toString());
```

### Delete a Secret

Delete an existing Secret by calling `deleteSecret`.
```Java
DeletedSecret deletedSecret = client.deleteSecret("secret_name").value();
System.out.printf("Deleted Secret's deletion date %s", deletedSecret.deletedDate().toString());
```

### List Secrets

List the secrets in the key vault by calling `listSecrets`.
```Java
// List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
for (SecretBase secret : client.listSecrets()) {
    Secret secretWithValue  = client.getSecret(secret).value();
    System.out.printf("Received secret with name %s and value %s \n", secretWithValue.name(), secretWithValue.value());
}
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Secret Service tasks, including:
- [Create a Secret Asynchronously](#create-a-secret-asynchronously)
- [Retrieve a Secret Asynchronously](#retrieve-a-secret-asynchronously)
- [Update an existing Secret Asynchronously](#update-an-existing-secret-asynchronously)
- [Delete a Secret Asynchronously](#delete-a-secret-asynchronously)
- [List Secrets Asynchronously](#list-secrets-asynchronously)

### Create a Secret Asynchronously

Create a Secret to be stored in the Azure Key Vault.
- `setSecret` creates a new secret in the key vault. if the secret with name already exists then a new version of the secret is created.
```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.Secret;

SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

secretAsyncClient.setSecret("secret_name", "secret_value").subscribe(secretResponse ->
  System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));
```

### Retrieve a Secret Asynchronously

Retrieve a previously stored Secret by calling `getSecret`.
```Java
secretAsyncClient.getSecret("secretName").subscribe(secretResponse ->
  System.out.printf("Secret with name %s , value %s \n", secretResponse.value().name(),
  secretResponse.value().value()));
```

### Update an existing Secret Asynchronously

Update an existing Secret by calling `updateSecret`.
```Java
secretAsyncClient.getSecret("secretName").subscribe(secretResponse -> {
     // Get the Secret
     Secret secret = secretResponse.value();
     // Update the expiry time of the secret.
     secret.expires(OffsetDateTime.now().plusDays(50));
     secretAsyncClient.updateSecret(secret).subscribe(secretResponse ->
         System.out.printf("Secret's updated not before time %s \n", secretResponse.value().notBefore().toString()));
   });
```

### Delete a Secret Asynchronously

Delete an existing Secret by calling `deleteSecret`.
```Java
secretAsyncClient.deleteSecret("secretName").subscribe(deletedSecretResponse ->
   System.out.printf("Deleted Secret's deletion time %s \n", deletedSecretResponse.value().deletedDate().toString()));
```

### List Secrets Asynchronously

List the secrets in the key vault by calling `listSecrets`.
```Java
// The List Secrets operation returns secrets without their value, so for each secret returned we call `getSecret` to get its // value as well.
secretAsyncClient.listSecrets()
  .flatMap(secretAsyncClient::getSecret).subscribe(secretResponse ->
    System.out.printf("Secret with name %s , value %s \n", secretResponse.value().name(), secretResponse.value().value()));
```

## Troubleshooting
### General
Key Vault clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    SecretClient.getSecret("deletedSecret")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Several KeyVault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Retrieve a Secret
    * Update a Secret
    * Delete a Secret

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Secret
    * List Secrets
    * Create new version of existing secret.
    * List versions of an existing secret.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Backup a Secret -- Write it to a file.
    * Delete a secret
    * Restore a secret

### Managing Deleted Secrets Samples:
* [ManagingDeletedSecrets.java][sample_ManageDeleted] and [ManagingDeletedSecretsAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Delete a secret
    * List deleted secrets
    * Recover a deleted secret
    * Purge Deleted secret

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[api_documentation]: https://azure.github.io/azure-sdk-for-java/track2reports/index.html
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/identity/client
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]:https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]:https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]:https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[secrets_samples]: src/samples/java/com/azure/security/keyvault/secrets
[sample_helloWorld]: src/samples/java/com/azure/security/keyvault/secrets/HelloWorld.java
[sample_helloWorldAsync]: src/samples/java/com/azure/security/keyvault/secrets/HelloWorldAsync.java
[sample_list]: src/samples/java/com/azure/security/keyvault/secrets/ListOperations.java
[sample_listAsync]: src/samples/java/com/azure/security/keyvault/secrets/ListOperationsAsync.java
[sample_BackupRestore]: src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecrets.java
[sample_ManageDeletedAsync]: src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecretsAsync.java
