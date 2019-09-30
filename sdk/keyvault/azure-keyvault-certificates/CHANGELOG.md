# Release History

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
