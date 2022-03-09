# Guide for migrating to azure-security-keyvault-secrets from azure-keyvault
This guide is intended to assist in the migration to `azure-security-keyvault-secrets` from `azure-keyvault`. It will focus on side-by-side comparisons for similar operations between the two packages.

Familiarity with the `azure-keyvault` package is assumed. For those new to the Key Vault Secret client library for Java, please refer to the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md) rather than this guide.

## Table of contents
- [Migration benefits](#migration-benefits)
- [Cross Service SDK improvements](#cross-service-sdk-improvements)
- [Important changes](#important-changes)
    - [Separate packages and clients](#separate-packages-and-clients)
    - [Package names and namespaces](#package-names-and-namespaces)
    - [Client instantiation](#client-instantiation)
    - [Authentication](#authentication)
    - [Common scenarios](#common-scenarios)
        - [Async operations](#async-operations)
        - [Create a secret](#create-a-secret)
        - [Retrieve a secret](#retrieve-a-secret)
        - [List properties of secrets](#list-properties-of-secrets)
        - [Delete a secret](#delete-a-secret)
- [Additional samples](#additional-samples)

## Migration benefits
A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. One of the most important is that the client libraries for different Azure services have not had a consistent approach to organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To try and improve the development experience across Azure services, a set of uniform [design guidelines](https://azure.github.io/azure-sdk/general_introduction.html) was created for all languages to drive a consistent experience with established API patterns for all services. A set of [Java-specific guidelines](https://azure.github.io/azure-sdk/java_introduction.html) was also introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines for those interested.

### Cross Service SDK improvements
The modern Key Vault Secret client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as:

- Using the new Azure Identity library to share a single authentication approach between clients.
- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.

## Important changes
### Separate packages and clients
In the interest of simplifying the API for working with Key Vault certificates, keys and secrets, the `azure-keyvault` was split into separate packages:

- [`azure-security-keyvault-certificates`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md) contains `CertificateClient` for working with Key Vault certificates.
- [`azure-security-keyvault-keys`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md) contains `KeyClient` for working with Key Vault keys and `CryptographyClient` for performing cryptographic operations.
- `azure-security-keyvault-secrets` contains `SecretClient` for working with Key Vault secrets.

### Package names and namespaces
Package names and the namespace root for the modern Azure client libraries for Java have changed. Each will follow the pattern `com.azure.<area>.<service>` where the legacy clients followed the pattern `com.microsoft.azure.<service>`. This provides a quick and accessible means to help understand, at a glance, whether you are using the modern or legacy clients.

In the case of the Key Vault, the modern client libraries have packages and namespaces that begin with `com.azure.security.keyvault` and were released beginning with version `4.0.0`. The legacy client libraries have packages and namespaces that begin with `com.microsoft.azure.keyvault` and a version of `1.x.x` or below.

### Client instantiation
Previously in `azure-keyvault` you could create a `KeyVaultClient`, via a public constructor that took an authentication delegate and could be used for multiple Key Vault endpoints.

```java
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

KeyVaultCredentials keyVaultCredentials = new MyKeyVaultCredentials("<client-id>", "<client-key>");
KeyVaultClient keyVaultClient = new KeyVaultClient(keyVaultCredentials);
```

Now, across all modern Azure client libraries, client instances are created via builders, which consistently take an endpoint or connection string along with token credentials. This means that you can use a single client builder to instantiate multiple clients that share some configuration.

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
SecretClientBuilder secretClientBuilder = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// Create a client.
SecretClient someSecretClient = secretClientBuilder.buildClient();

// Create a client with the same configuration, plus some more.
SecretClient anotherSecretClient = secretClientBuilder
    .addPolicy(new AddDatePolicy())
    .buildClient();
```

### Authentication
Previously in `azure-keyvault` you could create a `KeyVaultClient` by passing either a `KeyVaultCredential` or `RestClient` from `client-runtime`:

```java
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

KeyVaultCredentials keyVaultCredentials = new MyKeyVaultCredentials("<client-id>", "<client-key>");
KeyVaultClient keyVaultClient = new KeyVaultClient(keyVaultCredentials);
```

Now in `azure-security-keyvault-secrets` you can create a `SecretClient` using any credential from [`azure-identity`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md). Below is an example using [`DefaultAzureCredential`](https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential):

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

TokenCredential tokenCredentials = new DefaultAzureCredentialBuilder().build();

SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential)
    .buildClient();
```

### Async operations
The modern `azure-security-keyvault-secrets` library includes a complete set of async APIs that return [Project Reactor-based types](https://projectreactor.io/), as opposed to `azure-keyvault` async APIs that return either [Observable](https://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) or [ServiceFuture](https://azure.github.io/ref-docs/java/com/microsoft/rest/ServiceFuture.html).

Another difference is that async operations are available on their own separate async clients, which include the word `Async` in their name, like `SecretAsyncClient`.

All modern Azure async clients can be created virtually the same way as sync clients, with the slight difference of calling `buildAsyncClient` on the client builder instead of `buildClient`:

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
SecretClientBuilder secretClientBuilder = new SecretClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// To create an async client.
SecretAsyncClient secretAsyncClient = secretClientBuilder.buildAsyncClient();

// To create a sync client.
SecretClient secretClient = secretClientBuilder.buildClient();
```

### Common scenarios
#### Create a secret
In `azure-keyvault` you could set a secret by using `KeyVaultClient`'s `setSecret` method, which required a vault endpoint, secret name, and secret value. This method returned a `SecretBundle` containing the secret.

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";

SecretBundle secret = keyVaultClient.setSecret(keyVaultUrl, "<secret-name>", "secret-value");
```

Now in `azure-security-keyvault-secrets` there are couple ways to create secrets: you can provide either a secret name and value or a `KeyVaultSecret` object to the `setSecret` method. These methods all return the created secret as a `KeyVaultSecret`.

```java
// Create a secret by providing name and value.
KeyVaultSecret secret = secretClient.setSecret("<secret-name>", "<secret-value>");

// Create a secret by providing a KeyVaultSecret object.
KeyVaultSecret anotherSecret = secretClient.setSecret(new KeyVaultSecret("<other-secret-name>", "<secret-value>"));
```

#### Retrieve a secret
In `azure-keyvault` you could retrieve a secret (in a `SecretBundle`) by using `getSecret` in one of the following ways:

- Using the desired key vault endpoint and secret name to get the latest version of a secret.
- Using the desired key vault endpoint, secret name and secret version to get a specific secret version.
- Using the secret identifier to get a specific secret version.

Additionally, you could list the properties of the versions of a secret with the `getSecretVersions` method, which returned a `PagedList` of `SecretItem`.

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";

// Get a secret's latest version.
SecretBundle secret = keyVaultClient.getSecret(keyVaultUrl, "<secret-name>");

// Get a secret's specific version.
SecretBundle secretVersion = keyVaultClient.getSecret(keyVaultUrl, "<secret-name>", "<secret-version>");

// Get a secret's specific version using its id.
String secretIdentifier = "https://<your-key-vault-name>.vault.azure.net/secrets/<secret-name>/<secret-version>";
SecretBundle secretWithId = keyVaultClient.getSecret(secretIdentifier);

// Get a key's versions.
PagedList<SecretItem> secretVersions = keyVaultClient.getSecretVersions(keyVaultUrl, "<secret-name>");
```

Now in `azure-security-keyvault-secrets` you can retrieve a secret (as a `KeyVaultSecret`) by using `getSecret` in one of the following ways:

- Using the secret name to get the latest version of the secret.
- Using the secret name and secret version to get a specific version of the secret.

Additionally, you con list the properties of the versions of a secret with the `getSecretVersions` method, which returned a `PagedIterable` of `SecretProperties`.

```java
// Get a secret's latest version.
KeyVaultSecret secret = secretClient.getSecret("<secret-name>");

// Get a secret's specific version.
KeyVaultSecret secretVersion = secretClient.getSecret("<secret-name>", "<secret-version>");

// Get a secret's versions' properties.
PagedIterable<SecretProperties> secretVersionsProperties = secretClient.listPropertiesOfSecretVersions("<secret-name>");
```

#### List properties of secrets
In `azure-keyvault` you could list the properties of secrets in a specified vault with the `getSecrets` methods. This returned a `PagedList` containing `SecretItem` instances.

```java
PagedList<SecretItem> secretsProperties = keyVaultClient.getSecrets(keyVaultUrl);
```

Now in `azure-security-keyvault-secrets` you can list the properties of secrets in a vault with the `listPropertiesOfSecrets` method. This returns an iterator-like object containing `SecretProperties` instances.

```java
PagedIterable<SecretProperties> secretsProperties = secretClient.listPropertiesOfSecrets();
```

#### Delete a secret
In `azure-keyvault` you could delete all versions of a secret with the `deleteSecret` method. This returned information about the deleted secret (as a `DeletedSecretBundle`), but you could not poll the deletion operation to know when it completed. This would be valuable information if you intended to permanently delete the deleted secret with `purgeDeletedSecret`.

```java
DeletedSecretBundle deletedSecret = keyVaultClient.deleteSecret(keyVaultUrl, "<secret-name>");

// This purge would fail if deletion hadn't finished
keyVaultClient.purgeDeletedSecret(keyVaultUrl, "<secret-name>");
```

Now in `azure-security-keyvault-secrets` you can delete a secret with `beginDeleteSecret`, which returns a long operation poller object that can be used to wait/check on the operation. Calling `poll` on the poller will return information about the deleted secret (as a `DeletedSecret`) without waiting for the operation to complete, but calling `waitForCompletion` will wait for the deletion to complete. Again, `purgeDeletedSecret` will permanently delete your deleted secret and make it unrecoverable.

```java
SyncPoller<DeletedSecret, Void> deletedSecretPoller = secretClient.beginDeleteSecret("<secret-name>");
PollResponse<DeletedSecret> pollResponse = deletedSecretPoller.poll();
DeletedSecret deletedSecret = pollResponse.getValue();

// Wait for completion before attempting to purge the secret.
deletedSecretPoller.waitForCompletion();
secretClient.purgeDeletedSecret("<secret-name>");
```

## Additional samples
More examples can be found [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets/src/samples).
