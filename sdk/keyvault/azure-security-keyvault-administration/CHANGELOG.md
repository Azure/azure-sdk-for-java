# Release History

## 4.0.2 (2021-08-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`


## 4.0.1 (2021-07-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.0.0 (2021-06-17)
- Initial release of `KeyVaultAccessControlClient` and `KeyVaultAccessControlAsyncClient` to manage role assignments and definitions for Managed HSM.
- Initial release of `KeyVaultBackupClient` and `KeyVaultBackupAsyncClient` to backup and restore Managed HSM.

### Features Added
- Changed default service version to `7.2`.
- Added `KeyVaultKeyIdentifier` to parse key URLs.
- Added local-only support for `CryptographyClient` and `CryptographyAsyncClient` by providing a `JsonWebKey` during client creation.
- Added `KeyType.OCT-HSM` to support oct-HSM key operations to support Managed HSM.
- Added the `CreateOctKeyOptions` class and associated `createOctKey()` methods.
- Added AES-GCM and AES-CBC support for encrypting and decrypting, including new `Encrypt` and `Decrypt` overloads.
- Added the ability to set a public exponent on RSA keys during creation.
- Made all getters for properties of a `JsonWebKey` public.

### Changes since 4.0.0-beta.7

#### Bug Fixes
- Ensured that `RetryPolicy` and `HttpLogOptions` use a default implementation when creating Key Vault clients if not set or set to `null`.

#### Breaking Changes 
- Renamed `beginSelectiveRestore()` to `beginSelectiveKeyRestore()` in `KeyVaultBackupClient` and `KeyVaultBackupAsyncClient`. Made the aforementioned operation return the new `KeyVaultSelectiveKeyRestoreOperation` instead of a `KeyVaultRestoreOperation`.
- Changed the final return type of `beginRestore()` and `beginSelectiveKeyRestore()`'s poller types from `Void` to the new `KeyVaultRestoreResult` and `KeyVaultSelectiveKeyRestoreResult`, respectively.
- `KeyVaultBackupClient` and `KeyVaultBackupAsyncClient`'s long-running operations now throw a `RuntimeException` when cancellation is attempted, as it is not currently supported.
- Made `KeyVaultRoleDefinition` flat, as opposed to it having a `KeyVaultRoleDefinitionProperties` member with more properties inside of it.
- Moved `roleScope` from `KeyVaultRoleAssignment` to `KeyVaultRoleAssignmentProperties` and renamed it to `scope`.
- Moved `SetRoleDefinitionOptions` from the `options` package to the `models` package.
- Removed `roleType` from `SetRoleDefinitionOptions`.
- Removed service method overloads that take a `pollingInterval`, since `PollerFlux` and `SyncPoller` objects allow for setting this value directly on them.
- Delete methods on the `KeyVaultAccessControlClient` now return a `void` or `Mono<Void>` and ignore HTTP `404`s.
- Renamed `jobId` and `getJobId()` to `operationId` and `getOperationId()` in `KeyVaultLongRunningOperation` and its children classes.
- Made the following classes `final`:
    - `KeyVaultDataAction`
    - `KeyVaultRoleDefinitionType`
    - `KeyVaultRoleType`
    - `SetRoleDefinitionOptions`

## 4.0.0-beta.7 (2021-05-15)

### New features

- Added support for creating, retrieving, updating and deleting custom role definitions.

### Breaking Changes

- Added the public `KeyVaultAdministrationException`, which will be thrown in place of an exception with the same name in the `implementation` package.

#### Behavioral Changes

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.2`
- Upgraded `azure-identity` dependency to `1.3.0`

## 4.0.0-beta.6 (2021-04-09)

### New features
- Added support for service version `7.2`.
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

## 4.0.0-beta.5 (2021-03-12)

### Changed
- Changed logging level in `onRequest` and `onSuccess` calls for service operations from `INFO` to `VERBOSE`.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.0`
- Upgraded `azure-identity` dependency to `1.2.4`

## 4.0.0-beta.4 (2021-02-11)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.0.0-beta.3 (2020-11-12)

### New Features
- Added support for passing a `ServiceVersion` in clients and their builders.

### Breaking Changes
- Removed exposure of `implementation` (internal) package types via `module-info.java`
- Renamed `KeyVaultRoleAssignmentScope` to `KeyVaultRoleScope` to be in line with other languages.
- Changed the `KeyVaultRoleScope` enum from using `URI` to `URL` and added an overload that accepts a `String` representation of a `URL` too.
- Renamed the `scope` property of `KeyVaultRoleAssignment` to `roleScope` to align with the access client APIs.
- Renamed the `name` parameter to `roleAssignmentName` in role assignment-related APIs, as well as its type from `UUID` to `String`.
- Removed APIs for re-hydrating long-running operations as the guidelines regarding such methods are a still a work in progress
- Annotated read-only classes with `@Immutable`.
- Renamed `actions` and `dataActions` to `allowedActions` and `allowedDataActions` in `KeyVaultPermission`.
- Changed the type of `startTime` and `endTime` from `Long` to `OffsetDateTime` in `KeyVaultLongRunningOperation` and its sub-types.
- Renamed `azureStorageBlobContainerUri` to `azureStorageBlobContainerUrl` in `KeyVaultBackupOperation`, as well as its getter method.
- Removed the use of `KeyVaultRoleAssignmentProperties` in clients' public APIs in favor of using the `roleDefinitionId` and `servicePrincipalId` values directly.

## 4.0.0-beta.2 (2020-10-09)

### New Features
- Added the new public APIs `getBackupOperation` and `getRestoreOperation` for querying the status of long-running operations in `KeyVaultBackupClient` and `KeyVaultBackupAsyncClient`.
- Added API overloads that allow for passing specific polling intervals for long-running operations:
    - `KeyVaultBackupAsyncClient`
        - `beginBackup(String, String, Duration)`
        - `beginRestore(String, String, String, Duration)`
        - `beginSelectiveRestore(String, String, String, String, Duration)`
    - `KeyVaultBackupClient`
        - `beginBackup(String, String, Duration)`
        - `beginRestore(String, String, String, Duration)`
        - `beginSelectiveRestore(String, String, String, String, Duration)`
- Added support for `com.azure.core.util.ClientOptions` in client builders.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.0.0-beta.1 (2020-09-11)
- Added `KeyVaultBackupClient` and `KeyVaultBackupAsyncClient`.
- Added `KeyVaultAccessControlClient` and `KeyVaultAccessControlAsyncClient`.
