# Guide for migrating to azure-security-keyvault-keys from azure-keyvault
This guide is intended to assist in the migration to `azure-security-keyvault-keys` from `azure-keyvault`. It will focus on side-by-side comparisons for similar operations between the two packages.

Familiarity with the `azure-keyvault` package is assumed. For those new to the Key Vault Key client library for Java, please refer to the [README for azure-security-keyvault-keys](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md) rather than this guide.

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
        - [Create a key](#create-a-key)
        - [Import a key](#import-a-key)
        - [Retrieve a key](#retrieve-a-key)
        - [List properties of keys](#list-properties-of-keys)
        - [Delete a key](#delete-a-key)
        - [Perform cryptographic operations](#perform-cryptographic-operations)
- [Additional samples](#additional-samples)

## Migration benefits
A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. One of the most important is that the client libraries for different Azure services have not had a consistent approach to organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To try and improve the development experience across Azure services, a set of uniform [design guidelines](https://azure.github.io/azure-sdk/general_introduction.html) was created for all languages to drive a consistent experience with established API patterns for all services. A set of [Java-specific guidelines](https://azure.github.io/azure-sdk/java_introduction.html) was also introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines for those interested.

### Cross Service SDK improvements
The modern Key Vault Key client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as:

- Using the new Azure Identity library to share a single authentication approach between clients.
- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.

## Important changes
### Separate packages and clients
In the interest of simplifying the API for working with Key Vault certificates, keys and secrets, the `azure-keyvault` was split into separate packages:

- [`azure-security-keyvault-certificates`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md) contains `CertificateClient` for working with Key Vault certificates.
- `azure-security-keyvault-keys` contains `KeyClient` for working with Key Vault keys and `CryptographyClient` for performing cryptographic operations.
- [`azure-security-keyvault-secrets`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md) contains `SecretClient` for working with Key Vault secrets.

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
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
KeyClientBuilder keyClientBuilder = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// Create a client.
KeyClient someKeyClient = keyClientBuilder.buildClient();

// Create a client with the same configuration, plus some more.
KeyClient anotherKeyClient = keyClientBuilder
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

Now in `azure-security-keyvault-keys` you can create a `KeyClient` using any credential from [`azure-identity`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md). Below is an example using [`DefaultAzureCredential`](https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential):

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

KeyClient keyClient = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential)
    .buildClient();
```

### Async operations
The modern `azure-security-keyvault-keys` library includes a complete set of async APIs that return [Project Reactor-based types](https://projectreactor.io/), as opposed to `azure-keyvault` async APIs that return either [Observable](https://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) or [ServiceFuture](https://azure.github.io/ref-docs/java/com/microsoft/rest/ServiceFuture.html).

Another difference is that async operations are available on their own separate async clients, which include the word `Async` in their name, like `KeyAsyncClient`.

All modern Azure async clients can be created virtually the same way as sync clients, with the slight difference of calling `buildAsyncClient` on the client builder instead of `buildClient`:

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
KeyClientBuilder keyClientBuilder = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// To create an async client.
KeyAsyncClient keyAsyncClient = keyClientBuilder.buildAsyncClient();

// To create a sync client.
KeyClient keyClient = keyClientBuilder.buildClient();
```

### Common scenarios
#### Create a key
In `azure-keyvault` you could create a key by using `KeyVaultClient`'s `createKey` method, which required a vault endpoint, key name, and key type. This method returned a `KeyBundle` containing the key. 

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";

// Create an RSA key.
KeyBundle rsaKey = keyVaultClient.createKey(keyVaultUrl, "<rsa-key-name>", JsonWebKeyType.RSA);

// Create an EC key.
KeyBundle ecKey = keyVaultClient.createKey(keyVaultUrl, "<ec-key-name>", JsonWebKeyType.EC);
```

Now in `azure-security-keyvault-keys` there are multiple ways to create keys: you can provide either a key name and type or creation options to the `createKey` method, or provide creation options to `createRsaKey` or `createEcKey`. These methods all return the created key as a `KeyVaultKey`.

```java
// Create a key specifying the key type.
KeyVaultKey octKey = keyClient.createKey("<oct-key-name>", KeyType.OCT);

// Create a key with creation options.
KeyVaultKey octKeyWithOptions = keyClient.createKey(new CreateKeyOptions("<oct-key-name-with-options>", KeyType.OCT)
    .setExpiresOn(OffsetDateTime.now().plusYears(1)));
    
// Create an RSA key.
KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions("<rsa-key-name>")
    .setKeySize(2048));

// Create an EC key.
KeyVaultKey ecKey = keyClient.createEcKey(new CreateEcKeyOptions("<ec-key-name>")
    .setCurveName(KeyCurveName.P_256K));
```

#### Import a key
In `azure-keyvault` you could import a key by using `KeyVaultClient`'s `importKey` method, which required a vault endpoint, key name, and key contents as a `JsonWebKey`. This method returned a `KeyBundle`.

```java
KeyBundle importedKey = keyVaultClient.importKey(keyVaultUrl, "<key-name>", jsonWebKey);
```

Now in `azure-security-keyvault-keys` you can still import a key by providing the key name and contents as a  `JsonWebKey` to `importKey`, but you can also do so by providing an options object. This method returns a `KeyVaultKey`.

```java
// Import key using name and contents.
KeyVaultKey importedKey = keyClient.importKey(new ImportKeyOptions("<key-name>", jsonWebKey));

// Import key using options.
KeyVaultKey anotherImportedKey = keyClient.importKey(new ImportKeyOptions("<key-name>", keyContents)
    .setExpiresOn(OffsetDateTime.now().plusYears(1)));
```

#### Retrieve a key
In `azure-keyvault` you could retrieve a key (in a `KeyBundle`) by using `getKey` in one of the following ways:

- Using the desired key vault endpoint and key name to get the latest version of a key.
- Using the desired key vault endpoint, key name and key version to get a specific key version.
- Using the key identifier to get a specific key version.

Additionally, you could list the properties of the versions of a key with the `getKeyVersions` method, which returned a `PagedList` of `KeyItem`.

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";

// Get a key's latest version. 
KeyBundle key = keyVaultClient.getKey(keyVaultUrl, "<key-name>");
 
// Get a key's specific version.
KeyBundle keyVersion = keyVaultClient.getKey(keyVaultUrl, "<key-name>", "<key-version>");

// Get a key's specific version using its id.
String keyIdentifier = "https://<your-key-vault-name>.vault.azure.net/keys/<key-name>/<key-version>";
KeyBundle keyWithId = keyVaultClient.getKey(keyIdentifier);

// Get a key's versions.
PagedList<KeyItem> keyVersions = keyVaultClient.getKeyVersions(keyVaultUrl, "<key-name>");
```

Now in `azure-security-keyvault-keys` you can retrieve a key (as a `KeyVaultKey`) by using `getKey` in one of the following ways:

- Using the key name to get the latest version of the key.
- Using the key name and key version to get a specific version of the key.

Additionally, you con list the properties of the versions of a key with the `getKeyVersions` method, which returned a `PagedIterable` of `KeyProperties`. 

```java
// Get a key's latest version.
KeyVaultKey key = keyClient.getKey("<key-name>");

// Get a key's specific version.
KeyVaultKey keyVersion = keyClient.getKey("<key-name>", "<key-version>");

// Get a key's versions' propeties.
PagedIterable<KeyProperties> keyVersionsProperties = keyClient.listPropertiesOfKeyVersions("<key-name>");
```

#### List properties of keys
In `azure-keyvault` you could list the properties of keys in a specified vault with the `getKeys` methods. This returned a `PagedList` containing `KeyItem` instances.

```java
PagedList<KeyItem> keysProperties = keyVaultClient.getKeys(keyVaultUrl);
```

Now in `azure-security-keyvault-keys` you can list the properties of keys in a vault with the `listPropertiesOfKeys` method. This returns an iterator-like object containing `KeyProperties` instances.

```java
PagedIterable<KeyProperties> keysProperties = keyClient.listPropertiesOfKeys();
```

#### Delete a key
In `azure-keyvault` you could delete all versions of a key with the `deleteKey` method. This returned information about the deleted key (as a `DeletedKeyBundle`), but you could not poll the deletion operation to know when it completed. This would be valuable information if you intended to permanently delete the deleted key with `purgeDeletedKey`.

```java
DeletedKeyBundle deletedKey = keyVaultClient.deleteKey(keyVaultUrl, "<key-name>");

// This purge would fail if deletion hadn't finished
keyVaultClient.purgeDeletedKey(keyVaultUrl, "<key-name>");
```

Now in `azure-security-keyvault-keys` you can delete a key with `beginDeleteKey`, which returns a long operation poller object that can be used to wait/check on the operation. Calling `poll` on the poller will return information about the deleted key (as a `DeletedKey`) without waiting for the operation to complete, but calling `waitForCompletion` will wait for the deletion to complete. Again, `purgeDeletedKey` will permanently delete your deleted key and make it unrecoverable.

```java
SyncPoller<DeletedKey, Void> deleteKeyPoller = keyClient.beginDeleteKey("<key-name>");
PollResponse<DeletedKey> deletePollResponse = deleteKeyPoller.poll();
DeletedKey deletedKey = deletePollResponse.getValue();

// Wait for completion before attempting to purge the key.
deleteKeyPoller.waitForCompletion();
keyClient.purgeDeletedKey("<key-name>");
```

#### Perform cryptographic operations
In `azure-keyvault` you could perform cryptographic operations with keys by using the `encrypt`/`decrypt`, `wrapKey`/`unwrapKey`, and `sign`/`verify` methods. Each of these methods accepted a key vault endpoint, key name, key version, and algorithm along with other parameters.

```java
// Encrypt data using a key.
byte[] plaintext = "plaintext".getBytes();
KeyOperationResult keyOperationResult = keyVaultClient.encrypt(keyVaultUrl, "<key-name>", "<key-version>",
    JsonWebKeyEncryptionAlgorithm.RSA_OAEP_256, plaintext);
byte[] ciphertext = keyOperationResult.result();
```

Now in `azure-security-keyvault-keys` you can perform these cryptographic operations by using a `CryptographyClient`. The key used to create the client will be used for these operations. Cryptographic operations are now performed locally by the client when it's initialized with the necessary key material or is able to get that material from Key Vault, and are only performed by the Key Vault service when required key material is unavailable.

```java
String keyIdentifier = "https://<your-key-vault-name>.vault.azure.net/keys/<key-name>/<key-version>";
TokenCredentials tokenCredentials = new DefaultAzureCredentialBuilder().build();

CryptographyClient cryptographyClient = new CryptographyClientBuilder()
    .keyIdentifier(keyIdentifier)
    .credential(tokenCredentials)
    .buildClient();

byte[] plaintext = "plaintext".getBytes();
EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP_256, plaintext);
byte[] ciphertext = encryptResult.getCipherText();
```

## Additional samples
More examples can be found [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-keys/src/samples).
