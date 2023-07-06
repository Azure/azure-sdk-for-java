# Release History

## 4.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes
- Migrate test recordings to assets repo.

## 4.3.3 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 4.3.2 (2023-05-23)

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 4.3.1 (2023-04-20)

### Other Changes

- Test proxy server migration.
- Made all logger instances static.

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 4.3.0 (2023-03-18)

### Features Added
- Added support for service version `7.4`.
- Added `KeyVaultSettingsClient` and `KeyVaultSettingsAsyncClient` to get and update Managed HSM settings.

### Breaking Changes
> These changes do not impact the API of stable versions such as `4.2.4`. Only code written against a beta version such as `4.3.0-beta.1` may be affected.
- Removed support for service version `7.4-preview.1`.
- Removed `KeyVaultSetting.asString()`, as well as the `KeyVaultSetting(String, String, KeyVaultSettingType)` constructor.

### Other Changes
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 4.2.4 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 4.2.3 (2023-01-09)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 4.3.0-beta.1 (2022-11-11)

### Features Added
- Added `KeyVaultSettingsClient` and `KeyVaultSettingsAsyncClient` to get and update Managed HSM settings.
- Added support for service version `7.4-preview.1`.

## 4.2.2 (2022-11-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 4.2.1 (2022-10-17)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 4.2.0 (2022-09-20)

### Breaking Changes
- Made it so that we verify that the challenge resource matches the vault domain by default. This should affect few customers who can use the `disableChallengeResourceVerification()` method in client builders to disable this functionality. See https://aka.ms/azsdk/blog/vault-uri for more information.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 4.1.5 (2022-08-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.31.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.4`.

## 4.1.4 (2022-07-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.30.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.3`.

## 4.1.3 (2022-06-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.29.1`.
- Upgraded `azure-core-http-netty` dependency to `1.12.2`.

## 4.1.2 (2022-05-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.28.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.0`.

## 4.1.1 (2022-04-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.27.0`.
- Upgraded `azure-core-http-netty` dependency to `1.11.9`.

## 4.1.0 (2022-03-31)

### Features Added
- Added support for service version `7.3`.
- Added the following values to `KeyVaultDataAction`:
  - `WRITE_ROLE_DEFINITION`
  - `DELETE_ROLE_DEFINITION`
  - `RELEASE_KEY`
  - `DOWNLOAD_HSM_SECURITY_DOMAIN_STATUS`
  - `RANDOM_NUMBERS_GENERATE`
- Implemented new traits (micro-interfaces) in `KeyVaultAccessControlClientBuilder` and `KeyVaultBackupClientBuilder`. This makes the experience of using client builders more consistent across libraries in the Azure SDK for Java.

## 4.0.8 (2022-03-17)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.26.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.8`

## 4.1.0-beta.5 (2022-02-11)

### Features added
- Implemented new traits (micro-interfaces) in `KeyVaultAccessControlClientBuilder` and `KeyVaultBackupClientBuilder`. This makes the experience of using client builders more consistent across libraries in the Azure SDK for Java.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.25.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.7`

## 4.0.7 (2022-02-11)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.25.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.7`

## 4.1.0-beta.4 (2022-01-13)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.24.1`
- Upgraded `azure-core-http-netty` dependency to `1.11.6`

## 4.0.6 (2022-01-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.24.1`
- Upgraded `azure-core-http-netty` dependency to `1.11.6`

## 4.1.0-beta.3 (2021-11-12)

### Features Added

- Added support for multi-tenant authentication in clients.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.22.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.2`

## 4.0.5 (2021-11-12)

### Features Added

- Added support for multi-tenant authentication in clients.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.22.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.2`

## 4.1.0-beta.2 (2021-10-07)

### Bugs Fixed
- Fixed an issue that made clients send unnecessary unauthorized requests to obtain a bearer challenge from the service even when already possessing a valid bearer token.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.21.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.1`

## 4.0.4 (2021-10-06)

### Bugs Fixed
- Fixed an issue that made clients send unnecessary unauthorized requests to obtain a bearer challenge from the service even when already possessing a valid bearer token.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.21.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.1`

## 4.1.0-beta.1 (2021-09-10)

### Features Added
- Added support for service version `7.3-preview`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.0.3 (2021-09-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

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
