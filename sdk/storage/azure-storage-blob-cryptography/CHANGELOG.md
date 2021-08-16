# Release History

## 12.14.0-beta.2 (Unreleased)


## 12.14.0-beta.1 (2021-07-28)
- Added support for the 2020-10-02 service version.

## 12.13.0 (2021-07-22)
- Added support to get a blob client that uses an encryption scope and customer provided key. 

## 12.12.0 (2021-06-09)
- GA release

## 12.12.0-beta.1 (2021-05-13)
- Added support for specifying an encryption scope.
- Added support for the 2020-08-04 service version.

## 12.11.1 (2021-05-13)
### Dependency Updates
- Updated `azure-storage-blob` to version `12.11.1`
- Updated `azure-core` to version `1.16.0`

## 12.11.0 (2021-04-29)
- GA release

## 12.11.0-beta.3 (2021-04-16)
- Update `azure-storage-blob` to version `12.11.0-beta.3`

## 12.11.0-beta.2 (2021-03-29)
- Update `azure-storage-blob` to version `12.11.0-beta.2`

## 12.11.0-beta.1 (2021-02-10)
- Updated azure-storage-common and azure-storage-blob dependencies to add support for the 2020-06-12 service version. 

## 12.10.0 (2021-01-14)
- GA release

## 12.10.0-beta.1 (2020-12-07)
- Exposed ClientOptions on all client builders, allowing users to set a custom application id and custom headers.
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.

## 12.9.0 (2020-11-11)
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

## 12.9.0-beta.1 (2020-10-01)
- Added support to set BlobParallelUploadOptions.computeMd5 so the service can perform an md5 verification.
- Added support to specify 'requiresEncryption' on the EncryptedBlobClientBuilder to specify whether or not to enforce that the blob is encrypted on download.
- Fixed a bug where the TokenCredential scope would be incorrect for custom URLs.
- Fixed a bug where a custom application id in HttpLogOptions would not be added to the User Agent String.

## 12.8.0 (2020-08-13)
- GA release for 2019-12-12 service version

## 12.8.0-beta.1 (2020-07-07)
- Added support for setting blob tags on upload. 

## 12.7.0 (2020-06-12)
- Updated azure-storage-common and azure-core dependencies.

## 12.6.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.6.0 (2020-04-06)
- It is now possible to specify a key/keyResolver after they specify a pipeline/client on the builder.
- The builder will now throw if a pipeline/client was already configured for decryption as the encryption info may conflict.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.

## 12.5.0 (2020-03-11)
- Fixed a bug where the EncryptedBlockClientBuilder.pipeline method would not allow the client to support decryption.
- Added support for specifying a customer provided key.
- Fixed a bug that would cause EncryptedBlobClient.upload(InputStream, long) to not encrypt the data.
- Changed getAppendBlobClient, getBlockBlobClient, and getPageBlobClient on EncryptedBlobClient to throw as working with such clients is not supported.

## 12.4.0 (2020-02-12)
- Added support for upload via OutputStream by adding EncryptedBlobClient.getOutputStream methods

## 12.3.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-blob` to version 12.3.1

## 12.3.0 (2020-01-15)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.3.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.3.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.3.0 of Azure Storage Blob.
- Added .blobClient(BlobClient) and .blobAsyncClient(BlobAsyncClient) methods on EncryptedBlobClientBuilder to create an EncryptedBlobClient from a BlobClient.

## 12.2.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.2.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.2.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.2.0 of Azure Storage Blob.

## 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.1.0 of Azure Storage Blob.
- Upgraded to version 1.1.0 of Azure Core.
- Added a check in EncryptedBlobClientBuilder to enforce HTTPS for bearer token authentication.

## 12.0.0 (2019-10-31)
- Removed EncryptedBlobClientBuilder inheritance of BaseBlobClientBuilder

### Features included in `azure-storage-blob-cryptography`
- This package supports client side encryption for blob storage.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

