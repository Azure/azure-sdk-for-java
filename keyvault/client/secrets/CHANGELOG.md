# Release History

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for
.Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For release notes and more information please visit https://aka.ms/azure-sdk-preview1-java

This library is not a direct replacement for `azure-keyvault`. Applications using that library would require code changes to use `azure-keyvault-secrets`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/secrets/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-python/tree/master/keyvault/client/secrets/src/samples/java)
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
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/secrets/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity/README.md)
  for more information
  
### `azure-keyvault` features not implemented in this library
- Certificate management APIs
- Crytographic operations in Key management APIs
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
