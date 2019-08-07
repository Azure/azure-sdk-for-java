# Release History

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
