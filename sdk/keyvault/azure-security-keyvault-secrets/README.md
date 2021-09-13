# Azure Key Vault Secret client library for Java
Azure Key Vault is a cloud service that provides secure storage for secrets, such as passwords and database connection strings.

The Azure Key Vault Secrets client library allows you to securely store and tightly control the access to tokens, passwords, API keys, and other secrets. This library offers operations to create, retrieve, update, delete, purge, backup, restore, and list the secrets and its versions.

Use the Azure Key Vault Secrets client library to create and manage secrets.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][secrets_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Key Vault Secrets client library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.4.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Key Vault service, you'll need to create an instance of the [SecretClient](#create-secret-client) class. You would need a **vault url** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell][azure_cloud_shell]

Here is an [Azure Cloud Shell][azure_cloud_shell] snippet below to 

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

* Use the above returned credentials information to set **AZURE_CLIENT_ID** (appId), **AZURE_CLIENT_SECRET** (password), and **AZURE_TENANT_ID** (tenant) environment variables. The following example shows a way to do this in Bash:

  ```Bash
    export AZURE_CLIENT_ID="generated-app-ID"
    export AZURE_CLIENT_SECRET="random-password"
    export AZURE_TENANT_ID="tenant-ID"
  ```

* Grant the aforementioned application authorization to perform secret operations on the Key Vault:

    ```Bash
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --secret-permissions backup delete get list set
    ```

    > --secret-permissions:
    > Accepted values: backup, delete, get, list, purge, recover, restore, set

    If you have enabled role-based access control (RBAC) for Key Vault instead, you can find roles like "Key Vault Secrets Officer" in our [RBAC guide][rbac_guide].

* Use the aforementioned Key Vault name to retrieve details of your Vault, which also contain your Key Vault URL:

    ```Bash
    az keyvault show --name <your-key-vault-name> 
    ```

#### Create secret client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the SecretClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use SecretAsyncClient instead of SecretClient and call `buildAsyncClient()`

## Key concepts
### Secret
A secret is the fundamental resource within Azure Key Vault. From a developer's perspective, Key Vault APIs accept and return secret values as strings. In addition to the secret data, the following attributes may be specified:
* enabled: Specifies whether the secret data can be retrieved.
* notBefore: Identifies the time after which the secret will be active.
* expires: Identifies the expiration time on or after which the secret data should not be retrieved.
* created: Indicates when this version of the secret was created.
* updated: Indicates when this version of the secret was updated.

### Secret client:
The secret client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing secrets and its versions. Asynchronous (SecretAsyncClient) and synchronous (SecretClient) clients exist in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a secret, you can interact with the primary resource types in Key Vault.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Secret service tasks, including:
- [Create a secret](#create-a-secret)
- [Retrieve a secret](#retrieve-a-secret)
- [Update an existing secret](#update-an-existing-secret)
- [Delete a secret](#delete-a-secret)
- [List secrets](#list-secrets)

### Create a secret
Create a secret to be stored in the Azure Key Vault.
- `setSecret` creates a new secret in the Azure Key Vault. If a secret with the given name already exists then a new version of the secret is created.

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

KeyVaultSecret secret = secretClient.setSecret("<secret-name>", "<secret-value>");
System.out.printf("Secret created with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
```

### Retrieve a secret
Retrieve a previously stored secret by calling `getSecret`.

```Java
KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
```

### Update an existing secret
Update an existing secret by calling `updateSecretProperties`.

```Java
// Get the secret to update.
KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
// Update the expiry time of the secret.
secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secret.getProperties());
System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn());
```

### Delete a secret
Delete an existing secret by calling `beginDeleteSecret`.

```Java
SyncPoller<DeletedSecret, Void> deletedSecretPoller = secretClient.beginDeleteSecret("<secret-name>");

// Deleted secret is accessible as soon as polling begins.
PollResponse<DeletedSecret> deletedSecretPollResponse = deletedSecretPoller.poll();

// Deletion date only works for a SoftDelete-enabled Key Vault.
System.out.printf("Deletion date: %s%n", deletedSecretPollResponse.getValue().getDeletedOn());

// Secret is being deleted on server.
deletedSecretPoller.waitForCompletion();
```

### List secrets
List the secrets in the Azure Key Vault by calling `listPropertiesOfSecrets`.

```Java
// List operations don't return the secrets with value information. So, for each returned secret we call getSecret to
// get the secret with its value information.
for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
    KeyVaultSecret secretWithValue = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());
    System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretWithValue.getName(),
        secretWithValue.getValue());
}
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Secret Service tasks, including:
- [Create a secret asynchronously](#create-a-secret-asynchronously)
- [Retrieve a secret asynchronously](#retrieve-a-secret-asynchronously)
- [Update an existing secret asynchronously](#update-an-existing-secret-asynchronously)
- [Delete a secret asynchronously](#delete-a-secret-asynchronously)
- [List secrets asynchronously](#list-secrets-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Create a secret asynchronously
Create a secret to be stored in the Azure Key Vault.
- `setSecret` creates a new secret in the Azure Key Vault. If a secret with the given name already exists then a new version of the secret is created.

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.Secret;

SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();

secretAsyncClient.setSecret("<secret-name>", "<secret-value>")
    .subscribe(secret ->
        System.out.printf("Created secret with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue()));
```

### Retrieve a secret asynchronously
Retrieve a previously stored secret by calling `getSecret`.

```Java
secretAsyncClient.getSecret("<secret-name>")
    .subscribe(secret ->
        System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue()));
```

### Update an existing secret asynchronously
Update an existing secret by calling `updateSecretProperties`.

```Java
secretAsyncClient.getSecret("<secret-name>")
    .subscribe(secret -> {
        // Update the expiry time of the secret.
        secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
        secretAsyncClient.updateSecretProperties(secret.getProperties())
            .subscribe(updatedSecretProperties ->
                System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn()));
    });
```

### Delete a secret asynchronously
Delete an existing secret by calling `beginDeleteSecret`.

```Java
secretAsyncClient.beginDeleteSecret("<secret-name>")
    .subscribe(pollResponse -> {
        System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
        System.out.printf("Deleted secret name: %s%n", pollResponse.getValue().getName());
        System.out.printf("Deleted secret value: %s%n", pollResponse.getValue().getValue());
    });
```

### List secrets asynchronously
List the secrets in the Azure Key Vault by calling `listPropertiesOfSecrets`.

```Java
// The List secrets operation returns secrets without their value, so for each secret returned we call `getSecret`
// to get its value as well.
secretAsyncClient.listPropertiesOfSecrets()
    .subscribe(secretProperties ->
        secretAsyncClient.getSecret(secretProperties.getName(), secretProperties.getVersion())
            .subscribe(secretResponse ->
                System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretResponse.getName(),
                    secretResponse.getValue())));
```

## Troubleshooting
### General
Azure Key Vault Secret clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    secretClient.getSecret("<deleted-secret-name>")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Key Vault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

## Next steps samples
Samples are explained in detail [here][samples].

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/secrets/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]: https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[secrets_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[rbac_guide]: https://docs.microsoft.com/azure/key-vault/general/rbac-guide

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-secrets%2FREADME.png)
