# Release History

## 12.9.0-beta.1 (Unreleased)


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

