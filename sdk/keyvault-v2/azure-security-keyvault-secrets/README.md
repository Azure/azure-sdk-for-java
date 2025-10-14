# Azure Key Vault Secrets client library for Java

Azure Key Vault is a cloud service that provides secure storage for secrets, such as passwords and database connection
strings.

The Azure Key Vault Secrets client library allows you to securely store and tightly control the access to tokens,
passwords, API keys, and other secrets. This library offers operations to create, retrieve, update, delete, purge,
backup, restore, and list the secrets and its versions.

Use the Azure Key Vault Secrets client library to create and manage secrets.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azure_keyvault_docs] | [Samples][secrets_samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority][azure_ca].
- An [Azure Subscription][azure_subscription].
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a key vault, you can do so in the Azure Portal by
following the steps in [this document][azure_keyvault_portal]. Alternatively, you can use the Azure CLI by following the
steps in [this document][azure_keyvault_cli].

### Adding the package to your product

#### Use the Azure SDK BOM

Please include the `azure-sdk-bom` to your project to take dependency on the General Availability (GA) version of the
library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number. To learn
more about the BOM, see the [AZURE SDK BOM README][azure_sdk_bom].

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure.v2</groupId>
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
        <groupId>com.azure.v2</groupId>
        <artifactId>azure-security-keyvault-secrets</artifactId>
    </dependency>
</dependencies>
```

#### Use a direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct
dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure.v2:azure-security-keyvault-secrets;current})

```xml
<dependency>
    <groupId>com.azure.v2</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>5.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authentication

In order to interact with the Azure Key Vault service, you will need to create an instance of the
[`SecretClient`](#create-secret-client) class, a vault **endpoint** and a credential object. The examples shown in this
document use a credential object named  [`DefaultAzureCredential`][default_azure_credential], which is appropriate for
most scenarios, including local development and production environments. Additionally, we recommend using a
[managed identity][managed_identity] for authentication in production environments.

You can find more information on different ways of authenticating and their corresponding credential types in the
[Azure Identity documentation][azure_identity].

#### Create secret client

Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced
**your-key-vault-endpoint** with the URL for your key vault, you can create a `SecretClient`:

```java readme-sample-createSecretClient
SecretClient secretClient = new SecretClientBuilder()
    .endpoint("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts

### Secret

A secret is the fundamental resource within Azure Key Vault. From a developer's perspective, Key Vault APIs accept and
return secret values as strings. In addition to the secret data, the following attributes may be specified:

- **enabled:** Specifies whether the secret data can be retrieved.
- **notBefore:** Identifies the time after which the secret will be active.
- **expires:** Identifies the expiration time on or after which the secret data should not be retrieved.
- **created:** Indicates when this version of the secret was created.
- **updated:** Indicates when this version of the secret was updated.

### Secret client

The secret client performs interactions with the Azure Key Vault service for getting, setting, updating, deleting, and
listing secrets and its versions. Once you've initialized a secret, you can interact with the primary resource types in
Key Vault.

## Examples

The following sections provide several code snippets covering some of the most common Azure Key Vault Secret service
tasks, including:

- [Create a secret](#create-a-secret)
- [Retrieve a secret](#retrieve-a-secret)
- [Update an existing secret](#update-an-existing-secret)
- [Delete a secret](#delete-a-secret)
- [List secrets](#list-secrets)

### Create a secret

Create a secret to be stored in the key vault. `setSecret` creates a new secret in the key vault. If a secret with the
given name already exists then a new version of the secret is created.

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
Poller<DeletedSecret, Void> deletedSecretPoller = null;
    //secretClient.beginDeleteSecret("<secret-name>");

// Deleted secret is accessible as soon as polling begins.
PollResponse<DeletedSecret> deletedSecretPollResponse = deletedSecretPoller.poll();

// Deletion date only works for a SoftDelete-enabled Key Vault.
System.out.printf("Deletion date: %s%n", deletedSecretPollResponse.getValue().getDeletedOn());

// Secret is being deleted on server.
deletedSecretPoller.waitForCompletion();
```

### List secrets

List the secrets in the key vault by calling `listPropertiesOfSecrets`.

```java readme-sample-listSecrets
// List operations don't return the secrets with value information. So, for each returned secret we call getSecret to
// get the secret with its value information.
for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
    KeyVaultSecret secretWithValue = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());
    System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretWithValue.getName(),
        secretWithValue.getValue());
}
```

### Service API versions

The client library targets the latest service API version by default. The service client builder accepts an optional
service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the
service client builder. This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API
version. If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it
aligns with the service's versioning policy.

## Troubleshooting

See our [troubleshooting guide][troubleshooting_guide] for details on how to diagnose various failure scenarios.

### General

Azure Key Vault clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error
is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by
catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    secretClient.getSecret("<deleted-secret-name>");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP Client
<!-- TODO (vcolin7): Update with default client after discussing with the team. -->
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the
client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki][http_clients_wiki].

### Default SSL library
<!-- TODO (vcolin7): Confirm if we're still using the Boring SSL library with clientcore/azure-core-v2. -->
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

Several Azure Key Vault Java client library samples are available to you in the SDK's GitHub repository. These samples
provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

## Next steps samples

Samples are explained in detail [here][samples_readme].

### Additional documentation

For more extensive documentation on Azure Key Vault, see the [API reference documentation][azure_keyvault_rest].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, see the [Microsoft CLA][microsoft_cla].

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our [CLA][microsoft_cla].

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information
see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

For details on contributing to this repository, see the [contributing guide][contributing_guide].

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azure_keyvault_docs]: https://learn.microsoft.com/azure/key-vault/
[azure_keyvault_rest]: https://learn.microsoft.com/rest/api/keyvault/
[azure_ca]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_keyvault]: https://learn.microsoft.com/azure/key-vault/general/overview
[azure_keyvault_cli]: https://learn.microsoft.com/azure/key-vault/general/quick-create-cli
[azure_keyvault_portal]: https://learn.microsoft.com/azure/key-vault/general/quick-create-portal
[azure_subscription]: https://azure.microsoft.com/free/
[azure_sdk_bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md
[contributing_guide]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[default_azure_credential]: https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential
[http_clients_wiki]: https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[managed_identity]: https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[microsoft_cla]: https://cla.microsoft.com
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[secrets_samples]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/java/com/azure/v2/security/keyvault/secrets
[samples_readme]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[source_code]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src
[troubleshooting_guide]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/TROUBLESHOOTING.md
