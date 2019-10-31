# Release History
## 4.0.0 (2019-11-01)
For details on the Azure SDK for Java (November 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview5-java).

### Breaking changes
- `beginCreateCertificate` method now returns a SyncPoller in `CertificateClient` and a PollerFlux in`CertificateAsyncClient`


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

This library is not a direct replacement for certificates management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-certificates`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-keyvault-certificates/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-keyvault-certificates/src/samples/java)
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
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-keyvault-certificates/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity/README.md)
  for more information
- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
