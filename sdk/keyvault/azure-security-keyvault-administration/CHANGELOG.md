# Release History

## 4.0.0-beta.6 (Unreleased)

### New features
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
