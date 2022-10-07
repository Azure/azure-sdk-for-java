# Azure Key Vault Key client library for Java
Azure Key Vault is a cloud service that provides secure storage of keys for encrypting your data. Multiple keys, and multiple versions of the same key, can be kept in the Azure Key Vault. Cryptographic keys in Azure Key Vault are represented as [JSON Web Key [JWK]][jwk_specification] objects.

Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

The Azure Key Vault keys library client supports RSA keys and Elliptic Curve (EC) keys, each with corresponding support in hardware security modules (HSM). It offers operations to create, retrieve, update, delete, purge, backup, restore, and list the keys and its versions.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][keys_samples]

## Getting started
### Include the package
#### Include the BOM file
Please include the `azure-sdk-bom` to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number. To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

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
        <artifactId>azure-security-keyvault-keys</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-keys;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
    <version>4.5.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- An [Azure Subscription][azure_subscription].
- One of the following:
  - An existing [Azure Key Vault][azure_keyvault]. If you need to create a key vault, you can do so in the Azure Portal by following the steps in [this document][azure_keyvault_portal]. Alternatively, you can use the Azure CLI by following the steps in [this document][azure_keyvault_cli].
  - An existing [Azure Key Vault Managed HSM][azure_keyvault_mhsm]. If you need to create a Managed HSM, you can do so using the Azure CLI by following the steps in [this document][azure_keyvault_mhsm_cli].

### Authenticate the client
In order to interact with the Azure Key Vault service, you will need to create an instance of either the [`KeyClient`](#create-key-client) class or the [`CryptographyClient`](#create-cryptography-client) class, as well as a **vault url** and a credential object. The examples shown in this document use a credential object named  [`DefaultAzureCredential`][default_azure_credential], which is appropriate for most scenarios, including local development and production environments. Additionally, we recommend using a [managed identity][managed_identity] for authentication in production environments.

You can find more information on different ways of authenticating and their corresponding credential types in the [Azure Identity documentation][azure_identity].

#### Create key client
Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced **your-key-vault-url** with the URL for your key vault or managed HSM, you can create the `KeyClient`:

```java readme-sample-createKeyClient
KeyClient keyClient = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyAsyncClient` instead of `KeyClient` and call `buildAsyncClient()`.

#### Create cryptography client
Once you perform [the `DefaultAzureCredential` set up that suits you best][default_azure_credential] and replaced **your-key-vault-url** with the URL for your key vault or managed HSM, you can create the `CryptographyClient`:

```java readme-sample-createCryptographyClient
// Create client with key identifier from Key Vault.
CryptographyClient cryptoClient = new CryptographyClientBuilder()
    .keyIdentifier("<your-key-id-from-key-vault>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `CryptographyAsyncClient` instead of `CryptographyClient` and call `buildAsyncClient()`.

## Key concepts
### Key
Azure Key Vault supports multiple key types (`RSA` & `EC`) and algorithms, and enables the use of Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may be specified:
* enabled: Specifies whether the key is enabled and usable for cryptographic operations.
* not_before: Identifies the time before which the key must not be used for cryptographic operations.
* expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operations.
* created: Indicates when this version of the key was created.
* updated: Indicates when this version of the key was updated.

### Key client:
The key client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing keys and its versions. Asynchronous (`KeyAsyncClient`) and synchronous (`KeyClient`) clients exist in the SDK allowing for the selection of a client based on an application's use case. Once you have initialized a key, you can interact with the primary resource types in Key Vault.

### Cryptography client:
The cryptography client performs the cryptographic operations locally or calls the Azure Key Vault service depending on how much key information is available locally. It supports encrypting, decrypting, signing, verifying, key wrapping, key unwrapping, and retrieving the configured key. Asynchronous (`CryptographyAsyncClient`) and synchronous (`CryptographyClient`) clients exist in the SDK allowing for the selection of a client based on an application's use case.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Key service tasks, including:
- [Create a key](#create-a-key)
- [Retrieve a key](#retrieve-a-key)
- [Update an existing key](#update-an-existing-key)
- [Delete a key](#delete-a-key)
- [List keys](#list-keys)
- [Encrypt](#encrypt)
- [Decrypt](#decrypt)

#### Create a key
Create a key to be stored in the Azure Key Vault.
- `createKey` creates a new key in the key vault. If a key with the same name already exists then a new version of the key is created.

```java readme-sample-createKey
KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048));
System.out.printf("Key created with name \"%s\" and id %s%n", rsaKey.getName(), rsaKey.getId());

KeyVaultKey ecKey = keyClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
    .setCurveName(KeyCurveName.P_256)
    .setExpiresOn(OffsetDateTime.now().plusYears(1)));
System.out.printf("Key created with name \"%s\" and id %s%n", ecKey.getName(), ecKey.getId());
```

#### Retrieve a key
Retrieve a previously stored key by calling `getKey`.

```java readme-sample-retrieveKey
KeyVaultKey key = keyClient.getKey("<key-name>");
System.out.printf("A key was returned with name \"%s\" and id %s%n", key.getName(), key.getId());
```

#### Update an existing key
Update an existing key by calling `updateKeyProperties`.

```java readme-sample-updateKey
// Get the key to update.
KeyVaultKey key = keyClient.getKey("<key-name>");
// Update the expiry time of the key.
key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties());
System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn());
```

#### Delete a key
Delete an existing key by calling `beginDeleteKey`.

```java readme-sample-deleteKey
SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey("<key-name>");

PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();

// Deleted key is accessible as soon as polling begins.
DeletedKey deletedKey = deletedKeyPollResponse.getValue();
// Deletion date only works for a soft-delete enabled key vault.
System.out.printf("Deletion date: %s%n", deletedKey.getDeletedOn());

// The key is being deleted on the server.
deletedKeyPoller.waitForCompletion();
```

#### List keys
List the keys in the key vault by calling `listPropertiesOfKeys`.

```java readme-sample-listKeys
// List operations don't return the keys with key material information. So, for each returned key we call getKey to
// get the key with its key material information.
for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys()) {
    KeyVaultKey keyWithMaterial = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());
    System.out.printf("Received key with name \"%s\" and type \"%s\"%n", keyWithMaterial.getName(),
        keyWithMaterial.getKey().getKeyType());
}
```

#### Encrypt
Encrypt plain text by calling `encrypt`.

```java readme-sample-encrypt
byte[] plaintext = new byte[100];
new SecureRandom(SEED).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);
System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
    encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
```

#### Decrypt
Decrypt encrypted content by calling `decrypt`.

```java readme-sample-decrypt
byte[] plaintext = new byte[100];
new SecureRandom(SEED).nextBytes(plaintext);
EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);

//Let's decrypt the encrypted result.
DecryptResult decryptionResult = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText());
System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length);
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Key service tasks, including:
- [Create a key asynchronously](#create-a-key-asynchronously)
- [Retrieve a key asynchronously](#retrieve-a-key-asynchronously)
- [Update an existing key asynchronously](#update-an-existing-key-asynchronously)
- [Delete a key asynchronously](#delete-a-key-asynchronously)
- [List keys asynchronously](#list-keys-asynchronously)
- [Encrypt asynchronously](#encrypt-asynchronously)
- [Decrypt asynchronously](#decrypt-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

#### Create a key asynchronously
Create a key to be stored in the Azure Key Vault.
- `createKey` creates a new key in the key vault. If a key with the same name already exists then a new version of the key is created.

```java readme-sample-createKeyAsync
keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
        .setExpiresOn(OffsetDateTime.now().plusYears(1))
        .setKeySize(2048))
    .subscribe(key ->
        System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));

keyAsyncClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
        .setExpiresOn(OffsetDateTime.now().plusYears(1)))
    .subscribe(key ->
        System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));
```

#### Retrieve a key asynchronously
Retrieve a previously stored key by calling `getKey`.

```java readme-sample-retrieveKeyAsync
keyAsyncClient.getKey("<key-name>")
    .subscribe(key ->
        System.out.printf("Key was returned with name \"%s\" and id %s%n", key.getName(), key.getId()));
```

#### Update an existing key asynchronously
Update an existing key by calling `updateKeyProperties`.

```java readme-sample-updateKeyAsync
keyAsyncClient.getKey("<key-name>")
    .flatMap(key -> {
        // Update the expiry time of the key.
        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
        return keyAsyncClient.updateKeyProperties(key.getProperties());
    }).subscribe(updatedKey ->
        System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn()));
```

#### Delete a key asynchronously
Delete an existing key by calling `beginDeleteKey`.

```java readme-sample-deleteKeyAsync
keyAsyncClient.beginDeleteKey("<key-name>")
    .subscribe(pollResponse -> {
        System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
        System.out.printf("Deleted key name: %s%n", pollResponse.getValue().getName());
        System.out.printf("Key deletion date: %s%n", pollResponse.getValue().getDeletedOn());
    });
```

#### List keys asynchronously
List the keys in the Azure Key Vault by calling `listPropertiesOfKeys`.

```java readme-sample-listKeysAsync
// The List Keys operation returns keys without their value, so for each key returned we call `getKey` to get its value
// as well.
keyAsyncClient.listPropertiesOfKeys()
    .flatMap(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion()))
    .subscribe(key ->
        System.out.printf("Received key with name \"%s\" and type \"%s\"", key.getName(), key.getKeyType()));
```

#### Encrypt asynchronously
Encrypt plain text by calling `encrypt`.

```java readme-sample-encryptAsync
byte[] plaintext = new byte[100];
new SecureRandom(SEED).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
    .subscribe(encryptionResult -> System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
        encryptionResult.getCipherText().length, encryptionResult.getAlgorithm()));
```

#### Decrypt asynchronously
Decrypt encrypted content by calling `decrypt`.

```java readme-sample-decryptAsync
byte[] plaintext = new byte[100];
new SecureRandom(SEED).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
    .flatMap(encryptionResult -> {
        System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
            encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
        //Let's decrypt the encrypted response.
        return cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText());
    }).subscribe(decryptionResult ->
        System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length));
```

## Troubleshooting
See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

### General
Azure Key Vault Key clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    keyClient.getKey("<deleted-key-name>");
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Azure Key Vault Java client library samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

## Next steps samples
Samples are explained in detail [here][samples_readme].

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/keys/quick-create-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/general/quick-create-cli
[azure_keyvault_portal]: https://docs.microsoft.com/azure/key-vault/general/quick-create-portal
[azure_keyvault_mhsm]: https://docs.microsoft.com/azure/key-vault/managed-hsm/overview
[azure_keyvault_mhsm_cli]: https://docs.microsoft.com/azure/key-vault/managed-hsm/quick-create-cli
[default_azure_credential]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential
[managed_identity]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[keys_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[jwk_specification]: https://tools.ietf.org/html/rfc7517
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-keys%2FREADME.png)
