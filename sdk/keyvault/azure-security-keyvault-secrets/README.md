# Azure Key Vault Secret client library for Java
Azure Key Vault is a cloud service that provides a secure storage of secrets, such as passwords and database connection strings.

Secret client library allows you to securely store and tightly control the access to tokens, passwords, API keys, and other secrets. This library offers operations to create, retrieve, update, delete, purge, backup, restore and list the secrets and its versions.

Use the secret client library to create and manage secrets.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][secrets_samples]

## Getting started
### Adding the package to your project

Maven dependency for Azure Secret Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.0.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
KeyVault Secrets to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<!-- Add KeyVault Secrets dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.0.1</version>
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId>
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with KeyVault Secrets -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-secret-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;

SecretClient client = new SecretClientBuilder()
        .vaultUrl(<your-vault-url>)
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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.Secret;

SecretClient secretClient = new SecretClientBuilder()
        .vaultUrl(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

KeyVaultSecret secret = secretClient.setSecret("secret_name", "secret_value");
System.out.printf("Secret is created with name %s and value %s \n", secret.getName(), secret.getValue());
```

### Retrieve a Secret

Retrieve a previously stored Secret by calling `getSecret`.

```Java
KeyVaultSecret secret = secretClient.getSecret("secret_name");
System.out.printf("Secret is returned with name %s and value %s \n", secret.getName(), secret.getValue());
```

### Update an existing Secret

Update an existing Secret by calling `updateSecretProperties`.

```Java
// Get the secret to update.
KeyVaultSecret secret = secretClient.getSecret("secret_name");
// Update the expiry time of the secret.
secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secret.getProperties());
System.out.printf("Secret's updated expiry time %s \n", updatedSecretProperties.getExpiresOn().toString());
```

### Delete a Secret

Delete an existing Secret by calling `beginDeleteSecret`.

```Java
SyncPoller<DeletedSecret, Void> deletedSecretPoller = secretClient.beginDeleteSecret("secretName");

// Deleted Secret is accessible as soon as polling begins
PollResponse<DeletedSecret> deletedSecretPollResponse = deletedSecretPoller.poll();

System.out.println("Deleted Date  %s" + deletedSecretPollResponse.getValue().getDeletedOn().toString());

// Secret is being deleted on server.
deletedSecretPoller.waitForCompletion();
```

### List Secrets

List the secrets in the key vault by calling `listPropertiesOfSecrets`.

```Java
// List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
for (SecretProperties secretProperties : client.listPropertiesOfSecrets()) {
    KeyVaultSecret secretWithValue  = client.getSecret(secretProperties.getName(), secretProperties.getVersion());
    System.out.printf("Received secret with name %s and value %s \n", secretWithValue.getName(), secretWithValue.getValue());
}
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Secret Service tasks, including:
- [Create a Secret Asynchronously](#create-a-secret-asynchronously)
- [Retrieve a Secret Asynchronously](#retrieve-a-secret-asynchronously)
- [Update an existing Secret Asynchronously](#update-an-existing-secret-asynchronously)
- [Delete a Secret Asynchronously](#delete-a-secret-asynchronously)
- [List Secrets Asynchronously](#list-secrets-asynchronously)

> Note : You should add "System.in.read()" or "Thread.Sleep()" after the function calls in the main class/thread to allow Async functions/operations to execute and finish before the main application/thread exits.

### Create a Secret Asynchronously

Create a Secret to be stored in the Azure Key Vault.
- `setSecret` creates a new secret in the key vault. if the secret with name already exists then a new version of the secret is created.
```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.Secret;

SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
        .vaultUrl(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

secretAsyncClient.setSecret("secret_name", "secret_value").subscribe(secret ->
  System.out.printf("Secret is created with name %s and value %s \n", secret.getName(), secret.getValue()));
```

### Retrieve a Secret Asynchronously

Retrieve a previously stored Secret by calling `getSecret`.

```Java
secretAsyncClient.getSecret("secretName").subscribe(secret ->
  System.out.printf("Secret with name %s , value %s \n", secret.getName(),
  secret.getValue()));
```

### Update an existing Secret Asynchronously

Update an existing Secret by calling `updateSecretProperties`.

```Java
secretAsyncClient.getSecret("secretName").subscribe(secret -> {
     // Update the expiry time of the secret.
     secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
     secretAsyncClient.updateSecretProperties(secret.getProperties()).subscribe(updatedSecretProperties ->
         System.out.printf("Secret's updated expiry time %s \n", updatedSecretProperties.getExpiresOn().toString()));
   });
```

### Delete a Secret Asynchronously

Delete an existing Secret by calling `beginDeleteSecret`.

```Java
secretAsyncClient.beginDeleteSecret("secretName")
    .subscribe(pollResponse -> {
        System.out.println("Delete Status: " + pollResponse.getStatus().toString());
        System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
        System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
    });
```

### List Secrets Asynchronously

List the secrets in the key vault by calling `listPropertiesOfSecrets`.

```Java
// The List Secrets operation returns secrets without their value, so for each secret returned we call `getSecret` to get its // value as well.
secretAsyncClient.listPropertiesOfSecrets()
    .subscribe(secretProperties -> secretAsyncClient
        .getSecret(secretProperties.getName(), secretProperties.getVersion())
        .subscribe(secretResponse -> System.out.printf("Received secret with name %s and value %s",
            secretResponse.getName(), secretResponse.getValue())));
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

## Next Steps Samples
Samples are explained in detail [here][samples].

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
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
[samples]: src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-secrets%2FREADME.png)
