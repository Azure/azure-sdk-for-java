# Release History

## 4.3.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 4.3.0-beta.1 (2021-09-10)

### Features Added
- Added support for service version `7.3-preview`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.2.3 (2021-09-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.2.2 (2021-08-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`


## 4.2.1 (2021-07-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.2.0 (2021-06-17)

### Features Added
- Changed default service version to `7.2`.
- Added `KeyVaultCertificateIdentifier` to parse certificate URLs.

### Changes since 4.2.0-beta.6

#### Bug Fixes
- Ensured that `RetryPolicy` and `HttpLogOptions` use a default implementation when creating Key Vault clients if not set or set to `null`.

#### New Features
- `KeyVaultCertificateIdentifier` can now be used to parse any Key Vault identifier.

#### Breaking Changes
- Removed service method overloads that take a `pollingInterval`, since `PollerFlux` and `SyncPoller` objects allow for setting this value directly on them.

#### Non-Breaking Changes
- Renamed `certificateId` to `sourceId` in `KeyVaultCertificateIdentifier`.
- Added the `@ServiceMethod` annotation to all public methods that call the Key Vault service in `CertificateClient` and `CertificateAsyncClient`.

## 4.2.0-beta.6 (2021-05-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.2`
- Upgraded `azure-identity` dependency to `1.3.0`

## 4.2.0-beta.5 (2021-04-09)

### New features
- Added support for service version `7.2`.
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

### Breaking Changes
- Changed `KeyVaultCertificateIdentifier` so it is instantiated via its constructor as opposed to via a `parse()` factory method.

## 4.2.0-beta.4 (2021-03-12)

### Changed
- Changed logging level in `onRequest` and `onSuccess` calls for service operations from `INFO` to `VERBOSE`.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.0`
- Upgraded `azure-identity` dependency to `1.2.4`

## 4.2.0-beta.3 (2021-02-11)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.1.5 (2021-02-11)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.1.4 (2021-01-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.12.0`
- Upgraded `azure-core-http-netty` dependency to `1.7.1`
- Upgraded `azure-core-http-okhttp` dependency to `1.4.1`
- Upgraded `azure-identity` dependency to `1.2.2`

## 4.1.3 (2020-11-12)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.2.0-beta.2 (2020-10-09)

### New Features
- Added `KeyVaultCertificateIdentifier`. Use its [`parse`](https://github.com/Azure/azure-sdk-for-java/blob/ff52067a3772a430e5913b898f2806078aec8ef2/sdk/keyvault/azure-security-keyvault-certificates/src/main/java/com/azure/security/keyvault/certificates/models/KeyVaultCertificateIdentifier.java#L79) method to parse the different elements of a given certificate identifier.
- Added API overloads that allow for passing specific polling intervals for long-running operations:
    - `CertificateAsyncClient`
        - `beginCreateCertificate(String, CertificatePolicy, Boolean, Map<String, String>, Duration)`
        - `getCertificateOperation(String, Duration)`
        - `beginDeleteCertificate(String, Duration)`
        - `beginRecoverDeletedCertificate(String, Duration)`
    - `CertificateClient`
        - `beginCreateCertificate(String, CertificatePolicy, Boolean, Map<String, String>, Duration)`
        - `getCertificateOperation(String, Duration)`
        - `beginDeleteCertificate(String, Duration)`
        - `beginRecoverDeletedCertificate(String, Duration)`
- Added support for `com.azure.core.util.ClientOptions` in client builders.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.1.2 (2020-10-08)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.2.0-beta.1 (2020-09-11)
- Updated versions for azure-core and azure-identity.

## 4.1.1 (2020-09-10)
- Updated versions for azure-core and azure-identity.

## 4.1.0 (2020-08-12)
- Added support for service version `7.1`.
- Added `retryPolicy` setter in `CertificateClientBuilder`.
- Added `recoverableDays` property to `CertificateProperties`.

## 4.1.0-beta.4 (2020-07-08)
- Updated versions for azure-core, azure-identity.

## 4.0.5 (2020-07-08)
- Updated versions for azure-core and azure-identity.

## 4.1.0-beta.3 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.

## 4.0.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.

## 4.0.3 (2020-05-06)
- Update azure-core dependency to version 1.5.0.

## 4.1.0-beta.2 (2020-04-09)
- Added `retryPolicy` setter in `CertificateClientBuilder`
- Update azure-core dependency to version 1.4.0.

## 4.0.2 (2020-04-07)
- Update azure-core dependency to version 1.4.0.

## 4.0.1 (2020-03-25)
- Update azure-core dependency to version 1.3.0.

## 4.1.0-beta.1 (2020-03-10)
- Added `recoverableDays` property to `CertificateProperties`.
- Added support for `7.1-Preview` service version

## 4.0.0 (2020-01-07)
- Update azure-core dependency to version 1.2.0.

## 4.0.0-beta.7 (2019-12-17)
- `beginDeleteCertificate` and `beginRecoverDeletedCertificate` methods now return a poll response with a status of SUCCESSFULLY_COMPLETED when service returns 403 status.
- `CertificateClient.createIssuer` and `CertificateAsyncClient.createIssuer` now require a `CertificateIssuer` with both a name and provider.
- Removed constructor overload for `CertificateIssuer(String name, String provider)` from `CertificateIssuer` model.
- Removed `AdministratorContact` constructor overloads and introduced setters for all parameters.
- Removed `CertificateContact` constructor overloads and introduced setters for all parameters.

For details on the Azure SDK for Java (December 2019 beta) release refer to the [release announcement](https://aka.ms/azure-sdk-beta7-java).

## 4.0.0-beta.6 (2019-12-04)
For details on the Azure SDK for Java (November 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview5-java).


### Breaking changes

- Moved `CertificateClient.cancelCertificationOperation` to `SyncPoller.cancel` and `CertificateAsyncClient.cancelCertificationOperation` to `PollerFlux.cancel` respectively.
- deleteCertificate method has been renamed to beginDeleteCertificate and now returns a SyncPoller in `CertificateClient` and PollerFlux in `CertificateAsyncClient` to track this long-running operation.
- recoverDeletedCertificate method has been renamed to beginRecoverDeletedCertificate and now returns a SyncPoller in `CertificateClient` and PollerFlux in `CertificateAsyncClient` to track this long-running operation.
- `subject` and `issuerName` constructor parameters have been switched on `CertificatePolicy`.
- `subjectAlternativeNames` and `issuerName` constructor parameters have been switched on `CertificatePolicy`.
- The `SubjectAlternativeNames` class has been rewritten to contain `DnsNames`, `Emails`, and `UserPrincipalNames` collection properties.
- `CertificateIssuer.administrators` has been renamed to `CertificateIssuer.administratorContacts`.
- `CertificateKeyType.Oct` has been removed.
- `ImportCertificateOptions.value` has been renamed to `ImportCertificateOptions.certificate`.
- `LifeTimeAction` has been renamed to `LifetimeAction`.
- `CertificateKeyCurveName` and `CertificateKeyType` have been moved from package `com.azure.security.keyvault.certificates.models.webkey` to `com.azure.security.keyvault.certificates.models`.
- On `CertificateProperties`, expires, created, and updated have been renamed to expiresOn, createdOn, and updatedOn respectively.
- On `DeletedCertificate`, deletedDate has been renamed to DeletedOn.
- `CertificateImportOptions` has been renamed to `ImportCertificateOptions`.
- `CertificateMergeOptions` has been renamed to `MergeCertificateOptions`.
- `CertificatePolicy.DEFAULT` has been removed.


### Major changes

- The `SubjectAlternativeNames` class now allows you to set multiple types of subject alternative names using any of the `DnsNames`, `Emails`, and `UserPrincipalNames` collection properties.
- A new `CertificatePolicy` constructor allows you to both pass in both the `subject` and `subjectAlternativeNames` parameters.
- `CertificateIssuer.provider` was added.
- `CertificatePolicy.getDefault()` was added and allows you to get the default policy.


## 4.0.0-preview.5 (2019-11-01)
For details on the Azure SDK for Java (November 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview5-java).

### Breaking changes
- `beginCreateCertificate` method now returns a SyncPoller in `CertificateClient` and a PollerFlux in`CertificateAsyncClient`
- Certificate has been renamed to KeyVaultCertificate to avoid ambiguity with other libraries and to yield better search results.
- endpoint method on CertificateClientBuilder has been renamed to vaultUrl.
- listCertificates and listCertificateVersions methods have been renamed to listPropertiesOfCertificates and listPropertiesOfCertificateVersions respectively in `CertificateClient` and `CertificateAsyncClient`.
- restoreCertificate method has been renamed to restoreCertificateBackup in `CertificateClient` and `CertificatAsyncClient` to better associate it with CertificateClient.backupCertificate.
- LifetimeActionType class has been renamed to CertificatePolicyAction.
- Contact class has been renamed to CertificateContact.
- Issuer class has been renamed to CertificateIssuer.
- getCertificate method has been renamed to getCertificateVersion in `CertificateClient` and `CertificateAsyncClient`
- getCertificateWithPolicy method has been renamed to getCertificate and now returns KeyVaultCertificateWithPolicy in `CertificateClient` and `CertificateAsyncClient`
- getPendingCertificateSigningRequest method has been removed from `CertificateClient` and `CertificateAsyncClient`

### Major changes
- CertificateClient.vaultUrl has been added with the original value pass to CertificateClient.
- KeyVaultCertificateWithPolicy has been added and is returned by getCertificate method in `CertificateClient` and `CertificateAsyncClient`


## 4.0.0-preview.4 (2019-10-08)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

- `importCertificate` API has been added to `CertificateClient` and `CertificateAsyncClient`.
-  Updated to be fully compliant with the Java 9 Platform Module System.

### Breaking changes

- `CertificateBase` has been renamed to `CertificateProperties`.
- `Certificate` no longer extends `CertificateProperties`, but instead contains a `CertificateProperties` property named `Properties`.
- `IssuerBase` has been renamed to `IssuerProperties`.
- `Issuer` no longer extends `IssuerProperties`, but instead contains a `IssuerProperties` property named `Properties`.
- `CertificatePolicy` has been flattened to include all properties from `KeyOptions` and derivative classes.
- `KeyOptions` and derivative classes have been removed.
- `KeyType` has been renamed to `CertificateKeyType`.
- `KeyCurveName` has been renamed to `CertificateKeyCurveName`.
- `KeyUsage` has been renamed to `CertificateKeyUsage`.
- `SecretContentType` has been renamed to `CertificateContentType`.
- `updateCertificate` method has been renamed to `updateCertificateProperties` in `CertificateClient` and `CertificateAsyncClient`.
-  Getters and setters were updated to use Java Bean notation.
-  Changed VoidResponse to Response<Void> on sync API, and Mono<VoidResponse> to Mono<Response<Void>> on async API.
- `createCertificate` API has been renamed to `beginCreateCertificate` in in `CertificateClient` and `CertificateAsyncClient`.
-  Enumerations including `CertificateKeyCurveName`, `CertificateKeyUsage`, `CertificateContentType` and `CertificateKeyType` are now structures that define well-known, supported static fields.

## 4.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

This library is not a direct replacement for certificates management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-certificates`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java)
demonstrate the new API.


### Features Added
- Packages scoped by functionality
    - `azure-keyvault-keys` contains a `KeyClient` and `KeyAsyncClient` for key operations, 
    `azure-keyvault-secrets` contains a `SecretClient` and `SecretAsyncClient` for secret operations,
    `azure-keyvault-certificates` contains a `CertificateClient` and `CertificateAsyncClient` for certificate operations
- Client instances are scoped to vaults (an instance interacts with one vault
only)
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Authentication using `azure-identity` credentials
  - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md)
  for more information
- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
