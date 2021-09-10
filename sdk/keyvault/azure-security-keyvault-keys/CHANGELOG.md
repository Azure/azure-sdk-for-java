# Release History

## 4.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 4.3.3 (2021-09-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.3.2 (2021-08-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`

## 4.3.1 (2021-07-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.3.0 (2021-06-17)

### Features Added
- Changed default service version to `7.2`.
- Added `KeyVaultKeyIdentifier` to parse key URLs.
- Added local-only support for `CryptographyClient` and `CryptographyAsyncClient` by providing a `JsonWebKey` during client creation.
- Added `KeyType.OCT-HSM` to support oct-HSM key operations to support Managed HSM.
- Added the `CreateOctKeyOptions` class and associated `createOctKey()` methods.
- Added AES-GCM and AES-CBC support for encrypting and decrypting, including new `Encrypt` and `Decrypt` overloads.
- Added the ability to set a public exponent on RSA keys during creation.
- Made all getters for properties of a `JsonWebKey` public.

### Changes since 4.3.0-beta.8

#### Bug Fixes
- Ensured that `RetryPolicy` and `HttpLogOptions` use a default implementation when creating Key Vault clients if not set or set to `null`.

#### New Features
- Added `createOctKey()` and `createOctKeyWithResponse()` to `KeyClient` and `KeyAsyncClient`.
- Added factory methods for RSA algorithms in `DecryptParameters` and `EncryptParameters`:
    - `createRsa15Parameters()`
    - `createRsaOaepParameters()`
    - `createRsaOaep256Parameters()`

#### Breaking Changes
- Removed `EXPORT` from the `KeyOperation` enum.
- Re-ordered parameters in the `EncryptResult` constructor to show `authenticationTag` before `additionalAuthenticatedData` to align with classes like `DecryptParameters`.
- Removed service method overloads that take a `pollingInterval`, since `PollerFlux` and `SyncPoller` objects allow for setting this value directly on them.
- Moved `EncryptParameters` and `DecryptParameters` from the `cryptography` package to the `cryptography.models` package and made them both `final`.

#### Non-Breaking
- Renamed `keyId` to `sourceId` in `KeyVaultKeyIdentifier`.
- `KeyVaultKeyIdentifier` can now be used to parse any Key Vault identifier.
- Added the `@ServiceMethod` annotation to all public methods that call the Key Vault service in `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

## 4.3.0-beta.8 (2021-05-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.2`
- Upgraded `azure-identity` dependency to `1.3.0`

## 4.3.0-beta.7 (2021-04-29)

### Bug fixes
- Fixed issue that prevented setting tags on keys when creating or importing them.

### Breaking Changes
- Removed the `exportKey()` operation from `KeyAsyncClient` and `KeyClient`, as it is not yet supported in the current service version.

## 4.3.0-beta.6 (2021-04-09)

### Breaking Changes
- Renamed `EncryptOptions` to `EncryptParameters`.
- Renamed `DecryptOptions` to `DecryptParameters`.
- Changed `KeyVaultKeyIdentifier` so it is instantiated via its constructor as opposed to via a `parse()` factory method.
- Removed the following classes:
    - `LocalCryptographyAsyncClient`
    - `LocalCryptographyClient`
    - `LocalCryptographyClientBuilder`
    - `LocalKeyEncryptionKeyClient`
    - `LocalKeyEncryptionKeyAsyncClient`
    - `LocalKeyEncryptionKeyClientBuilder`

### New features
- Added support for service version `7.2`.
- Made all `JsonWebKey` properties settable.
- Added support to specify whether or not a pipeline policy should be added per call or per retry.
- Added convenience class `CreateOctKeyOptions`.
- Added support for building local-only cryptography clients by providing a `JsonWebKey` for local operations:
    - `CryptograhpyClientBuilder.jsonWebKey(JsonWebKey)`
- Added support for building local-only key encryption key clients by providing a `JsonWebKey` for local operations:
    - `KeyEncryptionKeyClientBuilder.buildKeyEncryptionKey(JsonWebKey)`
    - `KeyEncryptionKeyClientBuilder.buildAsyncKeyEncryptionKey(JsonWebKey)`
- `CryptograhpyClientBuilder.keyIdentifier(String)` now throws a `NullPointerException` if a `null` value is provided as an argument.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.15.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.1`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.1`
- Upgraded `azure-identity` dependency to `1.2.5`

## 4.3.0-beta.5 (2021-03-12)

### Breaking Changes
- Removed local support for encryption and decryption using AESGCM, as per guidance of Microsoft's cryptography board. Remote encryption and decryption using said algorithm is still supported.

### Changed
- Changed logging level in `onRequest` and `onSuccess` calls for service operations from `INFO` to `VERBOSE`.

### Bug fixes
- Fixed issue that caused a `NullPointerException` when attempting to use a `CryptographyClient` for symmetric key encryption operations after the first one.
- Fixed issue where `JsonWebKey` byte array contents would get serialized/deserialized using Base64 instead of URL-safe Base64.
- Fixed issue where properties of responses received when using a `CryptographyClient` for encryption/decryption were not populated on the `EncryptResult` and `DecryptResult` classes.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.0`
- Upgraded `azure-identity` dependency to `1.2.4`

## 4.3.0-beta.4 (2021-02-11)

### Bug Fixes
- Fixed issue where cryptographic operations would be attempted locally for symmetric keys that were missing their key material ('k' component).

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.2.5 (2021-02-11)

### Bug Fixes
- Fixed issue where cryptographic operations would be attempted locally for symmetric keys that were missing their key material ('k' component).

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.2.4 (2021-01-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.12.0`
- Upgraded `azure-core-http-netty` dependency to `1.7.1`
- Upgraded `azure-core-http-okhttp` dependency to `1.4.1`
- Upgraded `azure-identity` dependency to `1.2.2`

## 4.3.0-beta.3 (2020-11-19)

### New Features
- Added support for encrypting and decrypting AES-GCM and AES-CBC keys.
- Added `KeyType.OCT_HSM` to support "oct-HSM" key operations.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.2.3 (2020-11-12)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.3.0-beta.2 (2020-10-09)

### New Features
- Added `KeyVaultKeyIdentifier`. Use its [`parse`](https://github.com/Azure/azure-sdk-for-java/blob/ff52067a3772a430e5913b898f2806078aec8ef2/sdk/keyvault/azure-security-keyvault-keys/src/main/java/com/azure/security/keyvault/keys/models/KeyVaultKeyIdentifier.java#L78) method to parse the different elements of a given key identifier.
- Added API overloads that allow for passing specific polling intervals for long-running operations:
    - `KeyAsyncClient`
        - `beginDeleteKey(String, Duration)`
        - `beginRecoverDeletedKey(String, Duration)`
    - `KeyClient`
        - `beginDeleteKey(String, Duration)`
        - `beginRecoverDeletedKey(String, Duration)`
- Added support for `com.azure.core.util.ClientOptions` in client builders.

### Bug Fixes
- Fixed an issue that prevented the `tags` and `managed` members of `KeyProperties` from getting populated when retrieving a single key using `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

### Dependency updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.2.2 (2020-10-08)

### Bug Fixes
- Fixed an issue that prevented the `tags` and `managed` members of `KeyProperties` from getting populated when retrieving a single key using `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.3.0-beta.1 (2020-09-11)
- Updated versions for azure-core and azure-identity.

## 4.2.1 (2020-09-10)
- Updated versions for azure-core and azure-identity.

## 4.2.0 (2020-08-12)
- Added support for service version `7.1`.
- Added `retryPolicy` setter in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`.
- Added `recoverableDays` property to `KeyProperties`.
- Added `Import` operation to `KeyOperation`.

## 4.2.0-beta.5 (2020-07-08)
- Updated versions for azure-core, azure-identity.

## 4.1.5 (2020-07-08)
- Updated versions for azure-core and azure-identity.

## 4.2.0-beta.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.
- `404` responses from `listPropertiesOfKeyVersions` in `KeyAsyncClient` and `KeyClient` now throw a `ResourceNotFoundException`.
- `buildAsyncKeyEncryptionKey` in `LocalKeyEncryptionKeyClientBuilder` now throws an exception when no ID is present in a given `JsonWebKey`.

## 4.1.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.
- `404` responses from `listPropertiesOfKeyVersions` in `KeyAsyncClient` and `KeyClient` now throw a `ResourceNotFoundException`.

## 4.1.3 (2020-05-06)
- Update azure-core dependency to version 1.5.0.

## 4.2.0-beta.3 (2020-04-09)
- Added `LocalCryptographyClient`, `LocalCryptographyAsyncClient`, `LocalKeyEncryptionKeyClient` and `LocalKeyEncryptionKeyAsyncClient` to perform cryptography operations locally.
- Added `retryPolicy` setter in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`
- Update azure-core dependency to version 1.4.0.

## 4.1.2 (2020-04-07)
- Update azure-core dependency to version 1.4.0.

## 4.1.1 (2020-03-25)
- Update azure-core dependency to version 1.3.0.

## 4.2.0-beta.2 (2020-03-10)
### Added
- Added `recoverableDays` property to `KeyProperties`.
- Added `Import` operation to `KeyOperation`.
- Added support for `7.1-Preview` service version


## 4.2.0-beta.1
- `KeyVaultKey` model can be instantiated using `fromKeyId(String keyId, JsonWebKey jsonWebKey)` and `fromName(String name, JsonWebKey jsonWebKey)` methods on the `KeyVaultKey` model.
- Allows `KeyEncryptionKeyClientBuilder` to consume `KeyVaultKey` model and build `KeyEncryptionKey` and `AsyncKeyEncryptionKey` via`buildKeyEncryptionKey(KeyVaultKey key) ` and `buildAsyncKeyEncryptionKey(KeyVaultKey key)` methods respectively.

## 4.1.0 (2020-01-07)
- Fixes the logic of `getKeyId()` method in `KeyEncryptionKeyClient` and `KeyEncryptionKeyAsyncClient` to ensure key id is available in all scenarios.
- Update azure-core dependency to version 1.2.0.

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
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/index.html).

- Added service side Cryptography Operations support for asymmetric keys (sign, un/wrap, verify, encrypt and decrypt)
- Added client side Cryptography Operations support both asymmetric and symmetric keys.
- Added Cryptography clients to `azure-keyvault-keys` package.
    - `azure-keyvault-keys` contains a `CryptographyClient` and `CryptographyAsyncClient` for cryptography operations and  `KeyClient` and `KeyAsyncClient` for key operations.
    - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-keys_4.0.0-preview.2/sdk/keyvault/README.md) and
  [samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-keyvault-keys_4.0.0-preview.2/sdk/keyvault/azure-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys) for more information.
- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
- Combined KeyClientBuilder, KeyAsyncClientBuilder into KeyClientBuilder. Methods to create both sync and async clients type were added.
- Removed static builder method from clients. Builders are now instantiable.

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This library is not a direct replacement for keys management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-keys`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-keys_4.0.0-preview.1/keyvault/client/keys/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-keyvault-keys_4.0.0-preview.1/keyvault/client/keys/src/samples/java)
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
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity/README.md)
  for more information
  
### `azure-keyvault` features not implemented in this library
- Certificate management APIs
- Cryptographic operations, e.g. sign, un/wrap, verify, encrypt and decrypt
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
