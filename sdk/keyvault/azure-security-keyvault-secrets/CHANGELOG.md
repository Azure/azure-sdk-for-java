# Release History

## 4.4.0-beta.1 (Unreleased)

### Features Added
- Added support for service version `7.3-preview`.

## 4.3.1 (2021-07-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.3.0 (2021-06-17)

### Features Added
- Changed default service version to `7.2`.
- Added `KeyVaultSecretIdentifier` to parse secret URLs.

### Changes since 4.3.0-beta.6

#### Bug Fixes
- Ensured that `RetryPolicy` and `HttpLogOptions` use a default implementation when creating Key Vault clients if not set or set to `null`.

#### Breaking Changes
- Removed service method overloads that take a `pollingInterval`, since `PollerFlux` and `SyncPoller` objects allow for setting this value directly on them.

#### Non-Breaking Changes
- Renamed `secretId` to `sourceId` in `KeyVaultSecretIdentifier`.
- `KeyVaultSecretIdentifier` can now be used to parse any Key Vault identifier.
- Added the `@ServiceMethod` annotation to all public methods that call the Key Vault service in `SecretClient` and `SecretAsyncClient`.

## 4.3.0-beta.6 (2021-05-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.2`
- Upgraded `azure-identity` dependency to `1.3.0`

## 4.3.0-beta.5 (2021-04-09)

### New features
- Added support for service version `7.2`.
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

### Breaking Changes
- Changed `KeyVaultSecretIdentifier` so it is instantiated via its constructor as opposed to via a `parse()` factory method.

## 4.3.0-beta.4 (2021-03-12)

### Changed
- Changed logging level in `onRequest` and `onSuccess` calls for service operations from `INFO` to `VERBOSE`.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.0`
- Upgraded `azure-identity` dependency to `1.2.4`

## 4.3.0-beta.3 (2021-02-11)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.2.5 (2021-02-11)

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

## 4.2.3 (2020-11-12)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.3.0-beta.2 (2020-10-09)

### New Features
- Added `KeyVaultSecretIdentifier`. Use its [`parse`](https://github.com/Azure/azure-sdk-for-java/blob/ff52067a3772a430e5913b898f2806078aec8ef2/sdk/keyvault/azure-security-keyvault-secrets/src/main/java/com/azure/security/keyvault/secrets/models/KeyVaultSecretIdentifier.java#L79) method to parse the different elements of a given secret identifier.
- Added API overloads that allow for passing specific polling intervals for long-running operations:
    - `SecretAsyncClient`
        - `beginDeleteSecret(String, Duration)`
        - `beginRecoverDeletedSecret(String, Duration)`
    - `SecretClient`
        - `beginDeleteSecret(String, Duration)`
        - `beginRecoverDeletedSecret(String, Duration)`
- Added support for `com.azure.core.util.ClientOptions` in client builders.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.2.2 (2020-10-08)

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
- Added `retryPolicy` setter in `SecretClientBuilder`.
- Added `recoverableDays` property to `SecretProperties`.

## 4.2.0-beta.4 (2020-07-08)
- Updated versions for azure-core, azure-identity.

## 4.1.5 (2020-07-08)
- Updated versions for azure-core and azure-identity.

## 4.2.0-beta.3 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.
- Fixed `ByteBuf` resource leak in `KeyVaultCredentialPolicy`.

## 4.1.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.

## 4.1.3 (2020-05-06)
- Update azure-core dependency to version 1.5.0.
- Fixed `ByteBuff` resource leak in `KeyVaultCredentialPolicy`.

## 4.2.0-beta.2 (2020-04-09)
- Update azure-core dependency to version 1.4.0.
- Added `retryPolicy` setter in `SecretClientBuilder`

## 4.1.2 (2020-04-07)
- Update azure-core dependency to version 1.4.0.

## 4.1.1 (2020-03-25)
- Update azure-core dependency to version 1.3.0.

## 4.2.0-beta.1 (2020-03-10)
### Added
- Added `recoverableDays` property to `SecretProperties`.
- Added support for `7.1-Preview` service version

## 4.1.0 (2020-01-07)
- Update azure-core dependency to version 1.2.0
- Drop commons-codec dependency

## 4.0.1 (2019-12-04)

## 4.0.0 (2019-10-31)
### Breaking changes

- Secret has been renamed to KeyVaultSecret to avoid ambiguity with other libraries and to yield better search results.
- endpoint method on SecretClientBuilder has been renamed to vaultUrl.
- On SecretProperties, expires, created, and updated have been renamed to expiresOn, createdOn, and updatedOn respectively.
- On DeletedSecret, deletedDate has been renamed to deletedOn.
- listSecrets and listSecretVersions methods have been renamed to listPropertiesOfSecrets and listPropertiesOfSecretVersions in `SecretClient` and `SecretAsyncClient` respectively.
- restoreSecret method has been renamed to restoreSecretBackup in `SecretClient` and `SecretAsyncClient` to better associate it with SecretClient.backupSecret.
- deleteSecret method has been renamed to beginDeleteSecret and now returns a SyncPoller in `SecretClient` and PollerFlux in `SecretAsyncClient` to track this long-running operation.
- recoverDeletedSecret method has been renamed to beginRecoverDeletedSecret and now returns a SyncPoller in `SecretClient` and PollerFlux in `SecretAsyncClient` to track this long-running operation.

### Major changes
- SecretClient.vaultUrl has been added with the original value pass to SecretClient.

## 4.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

## 4.0.0-preview.4 (2019-09-08)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

- Updated to be fully compliant with the Java 9 Platform Module System.

### Breaking changes
- `SecretBase` has been renamed to `SecretProperties`.
- `Secret` and `DeletedSecret` no longer extend `SecretProperties`, but instead contain a `SecretProperties` property named `Properties`.
- `updateSecret` method has been renamed to `updateSecretProperties` in `SecretClient` and `SecretAsyncClient`.
- Getters and setters were updated to use Java Bean notation.
- Changed VoidResponse to Response<Void> on sync API, and Mono<VoidResponse> to Mono<Response<Void>> on async API.

## 4.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/java.html).

- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
- Combined SecretClientBuilder, SecretAsyncClientBuilder into SecretClientBuilder. Methods to create both sync and async clients type were added.
- Removed static builder method from clients. Builders are now instantiable.

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This library is not a direct replacement for secrets management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-secrets`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-secrets_4.0.0-preview.1/keyvault/client/secrets/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-secrets_4.0.0-preview.1/keyvault/client/secrets/src/samples/java)
demonstrate the new API.


### Major changes from `azure-keyvault`
- Packages scoped by functionality
    - `azure-keyvault-secrets` contains a `SecretClient` and `SecretAsyncClient` for secret operations,
    `azure-keyvault-keys` contains a `KeyClient` and `KeyAsyncClient` for key operations
- Client instances are scoped to vaults (an instance interacts with one vault
only)
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Authentication using `azure-identity` credentials
  - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-secrets_4.0.0-preview.1/keyvault/client/secrets/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity/README.md)
  for more information
  
### `azure-keyvault` features not implemented in this library
- Certificate management APIs
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
