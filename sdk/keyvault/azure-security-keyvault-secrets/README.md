# Azure Key Vault Secret client library for Java
Azure Key Vault is a cloud service that provides secure storage for secrets, such as passwords and database connection strings.

The Azure Key Vault Secrets client library allows you to securely store and tightly control the access to tokens, passwords, API keys, and other secrets. This library offers operations to create, retrieve, update, delete, purge, backup, restore, and list the secrets and its versions.

Use the Azure Key Vault Secrets client library to create and manage secrets.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][secrets_samples]

## Getting started
### Include the package
#### Include the BOM file
Please include the `azure-sdk-bom` to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-security-keyvault-secrets</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.4.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- An [Azure Subscription][azure_subscription].
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a key vault, you can do so in the Azure Portal by following the steps in [this document][azure_keyvault_portal]. Alternatively, you can use the Azure CLI by following the steps in [this document][azure_keyvault_cli].

### Authenticate the client
In order to interact with the Azure Key Vault service, you will need to create an instance of the [`CertificateClient`](#create-certificate-client) class. You need a **vault url** and a credential object. The examples shown in this document use a credential object named  [`DefaultAzureCredential`][default_azure_credential], which is appropriate for most scenarios where the application is intended to ultimately be run in the Azure Cloud. You can find more ways to authenticate with [azure-identity][azure_identity].

#### Create secret client
Once you perform [the `DefaultAzureCredential` set up that suits you best][default_azure_credential] and replaced **your-key-vault-url** with the URL for your key vault, you can create the `SecretClient`:

```java readme-sample-createSecretClient
SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `SecretAsyncClient` instead of `SecretClient` and call `buildAsyncClient()`.

## Key concepts
### Secret
A secret is the fundamental resource within Azure Key Vault. From a developer's perspective, Key Vault APIs accept and return secret values as strings. In addition to the secret data, the following attributes may be specified:
* enabled: Specifies whether the secret data can be retrieved.
* notBefore: Identifies the time after which the secret will be active.
* expires: Identifies the expiration time on or after which the secret data should not be retrieved.
* created: Indicates when this version of the secret was created.
* updated: Indicates when this version of the secret was updated.

### Secret client:
The secret client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing secrets and its versions. Asynchronous (`SecretAsyncClient`) and synchronous (`SecretClient`) clients exist in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a secret, you can interact with the primary resource types in Key Vault.

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

```java readme-sample-createSecret
KeyVaultSecret secret = secretClient.setSecret("<secret-name>", "<secret-value>");
System.out.printf("Secret created with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
```

### Retrieve a secret
Retrieve a previously stored secret by calling `getSecret`.

```java readme-sample-retrieveSecret
KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
```

### Update an existing secret
Update an existing secret by calling `updateSecretProperties`.

```java readme-sample-updateSecret
// Get the secret to update.
KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
// Update the expiry time of the secret.
secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secret.getProperties());
System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn());
```

### Delete a secret
Delete an existing secret by calling `beginDeleteSecret`.

```java readme-sample-deleteSecret
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

```java readme-sample-listSecrets
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

```java readme-sample-createSecretAsync
secretAsyncClient.setSecret("<secret-name>", "<secret-value>")
    .subscribe(secret -> System.out.printf("Created secret with name \"%s\" and value \"%s\"%n",
        secret.getName(), secret.getValue()));
```

### Retrieve a secret asynchronously
Retrieve a previously stored secret by calling `getSecret`.

```java readme-sample-retrieveSecretAsync
secretAsyncClient.getSecret("<secret-name>")
    .subscribe(secret -> System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n",
        secret.getName(), secret.getValue()));
```

### Update an existing secret asynchronously
Update an existing secret by calling `updateSecretProperties`.

```java readme-sample-updateSecretAsync
secretAsyncClient.getSecret("<secret-name>")
    .flatMap(secret -> {
        // Update the expiry time of the secret.
        secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
        return secretAsyncClient.updateSecretProperties(secret.getProperties());
    }).subscribe(updatedSecretProperties ->
        System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn()));
```

### Delete a secret asynchronously
Delete an existing secret by calling `beginDeleteSecret`.

```java readme-sample-deleteSecretAsync
secretAsyncClient.beginDeleteSecret("<secret-name>")
    .subscribe(pollResponse -> {
        System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
        System.out.printf("Deleted secret name: %s%n", pollResponse.getValue().getName());
        System.out.printf("Deleted secret value: %s%n", pollResponse.getValue().getValue());
    });
```

### List secrets asynchronously
List the secrets in the Azure Key Vault by calling `listPropertiesOfSecrets`.

```java readme-sample-listSecretsAsync
// The List secrets operation returns secrets without their value, so for each secret returned we call `getSecret`
// to get its value as well.
secretAsyncClient.listPropertiesOfSecrets()
    .flatMap(secretProperties ->
        secretAsyncClient.getSecret(secretProperties.getName(), secretProperties.getVersion()))
    .subscribe(secretResponse ->
        System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretResponse.getName(),
            secretResponse.getValue()));
```

## Troubleshooting
See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

### General
Azure Key Vault Secret clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    secretClient.getSecret("<deleted-secret-name>");
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
Samples are explained in detail [here][samples_readme].

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/general/overview
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/general/quick-create-cli
[azure_keyvault_portal]: https://docs.microsoft.com/azure/key-vault/general/quick-create-portal
[default_azure_credential]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[secrets_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-secrets%2FREADME.png)
