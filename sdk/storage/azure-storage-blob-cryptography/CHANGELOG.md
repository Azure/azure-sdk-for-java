# Release History
## Version 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/cryptography)

- Upgraded to version 12.1.0 of Azure Storage Blob.
- Upgraded to version 1.1.0 of Azure Core.
- Added a check in EncryptedBlobClientBuilder to enforce HTTPS for bearer token authentication.

## Version 12.0.0 (2019-10-31)
- Removed EncryptedBlobClientBuilder inheritance of BaseBlobClientBuilder

### Features included in `azure-storage-blob-cryptography`
- This package supports client side encryption for blob storage.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
  
