# Release History
## 4.0.1 (2019-12-06)

### Major changes
- `KeyEncryptionKeyClientBuilder.buildKeyEncryptionKey` and `KeyEncryptionKeyClientBuilder.buildAsyncKeyEncryptionKey`supports consumption of a secret id representing the symmetric key stored in the Key Vault as a secret.
- Dropped third party dependency on apache commons codec library.


### Breaking changes
- Key has been renamed to KeyVaultKey to avoid ambiguity with other libraries and to yield better search results.
- Key.keyMaterial has been renamed to KeyVaultKey.key.
- The setters of JsonWebKey properties have been removed.
- JsonWebKey methods fromRsa, fromEc and fromAes now take an optional collection of key operations.
- JsonWebKey.keyOps is now read-only. You must pass a list of key operations at construction time.
- endpoint method on KeyClientBuilder has been renamed to vaultUrl.
- hsm properties and parameters have been renamed to hardwareProtected.
- On KeyProperties, expires, created, and updated have been renamed to expiresOn, createdOn, and updatedOn respectively.
- On DeletedKey, deletedDate has been renamed to DeletedOn.
- listKeys and listKeyVersions methods have been renamed to listPropertiesOfKeys and listPropertiesOfKeyVersions respectively in `KeyClient` and `KeyAsyncClient`.
- restoreKey method has been renamed to restoreKeyBackup in `KeyClient` and `KeyAsyncClient` to better associate it with KeyClient.backupKey.
- deleteKey method has been renamed to beginDeleteKey and now returns a SyncPoller in `KeyClient` and PollerFlux in `KeyAsyncClient` to track this long-running operation.
- recoverDeletedKey method has been renamed to beginRecoverDeletedKey and now returns a SyncPoller in `KeyClient` and PollerFlux in `KeyAsyncClient` to track this long-running operation.
- KeyCreateOptions has been renamed to CreateKeyOptions.
- EcCreateKeyOptions has been renamed to CreateEcKeyOptions.
- CreateEcKeyOptions.curve has been renamed to curveName to be consistent.
- RsaKeyCreateOptions has been renamed to CreateRsaKeyOptions.
- KeyImportOptions has been renamed to ImportKeyOptions.

### Major changes
- JsonWebKey.keyType and JsonWebKey.keyOps have been exposed as KeyVaultKey.keyType and KeyVaultKey.keyOperations respectively.
- KeyClient.vaultUrl has been added with the original value pass to KeyClient.

## 4.0.0-preview.4 (2019-10-08)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

- Updated to be fully compliant with the Java 9 Platform Module System.

### Breaking changes
- `KeyBase` has been renamed to `KeyProperties`.
- `Key` and `DeletedKey` no longer extend `KeyProperties`, but instead contain a `KeyProperties` property named `Properties`.
- `updateKey` method has been renamed to `updateKeyProperties` in `KeyClient` and `KeyAsyncClient`.
- Getters and setters were updated to use Java Bean notation.
- Changed VoidResponse to Response<Void> on sync API, and Mono<VoidResponse> to Mono<Response<Void>> on async API.
- Enumerations including `KeyCurveName`, `KeyOperation`, and `KeyType` are now structures that define well-known, supported static fields.

## 4.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

## 4.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Added service side Cryptography Operations support for asymmetric keys (sign, un/wrap, verify, encrypt and decrypt)
- Added client side Cryptography Operations support both asymmetric and symmetric keys.
- Added Cryptography clients to `azure-keyvault-keys` package.
    - `azure-keyvault-keys` contains a `CryptographyClient` and `CryptographyAsyncClient` for cryptography operations and  `KeyClient` and `KeyAsyncClient` for key operations.
    - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/keys/README.md) and
  [samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys) for more information.
- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
- Combined KeyClientBuilder, KeyAsyncClientBuilder into KeyClientBuilder. Methods to create both sync and async clients type were added.
- Removed static builder method from clients. Builders are now instantiable.

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This library is not a direct replacement for keys management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-keys`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/keys/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/keys/src/samples/java)
demonstrate the new API.


### Major changes from `azure-keyvault`
- Packages scoped by functionality
    - `azure-keyvault-keys` contains a `KeyClient` and `KeyAsyncClient` for key operations, 
    `azure-keyvault-secrets` contains a `SecretClient` and `SecretAsyncClient` for secret operations
- Client instances are scoped to vaults (an instance interacts with one vault
only)
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Authentication using `azure-identity` credentials
  - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/keys/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity/README.md)
  for more information
  
### `azure-keyvault` features not implemented in this library
- Certificate management APIs
- Cryptographic operations, e.g. sign, un/wrap, verify, encrypt and decrypt
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
