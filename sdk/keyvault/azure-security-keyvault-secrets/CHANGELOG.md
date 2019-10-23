# Release History
## 4.0.0 (2019-10-31)
- This release consists of API improvements, performance enhancements, samples and updated documentation.

## 4.0.0-preview.4 (2019-09-08)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

- Updated to be fully compliant with the Java 9 Platform Module System.

### Breaking changes
- `SecretBase` has been renamed to `SecretProperties`.
- `Secret` and `DeletedSecret` no longer extend `SecretProperties`, but instead contain a `SecretProperties` property named `Properties`.
- `updateSecret` method has been renamed to `updateSecretProperties` in `SecretClient` and `SecretAsyncClient`.
- Getters and setters were updated to use Java Bean notation.
- Changed VoidResponse to Response<Void> on sync API, and Mono<VoidResponse> to Mono<Response<Void>> on async API.

## 4.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

## 4.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
- Combined SecretClientBuilder, SecretAsyncClientBuilder into SecretClientBuilder. Methods to create both sync and async clients type were added.
- Removed static builder method from clients. Builders are now instantiable.

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This library is not a direct replacement for secrets management operations from [microsoft-azure-keyvault](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/microsoft-azure-keyvault). Applications using that library would require code changes to use `azure-keyvault-secrets`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/secrets/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/keyvault/client/secrets/src/samples/java)
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
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
