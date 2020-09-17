# Release History

## 12.7.0-beta.1 (Unreleased)


## 12.6.0 (2020-08-13)
- Added support to set tier on a snapshot or version.
- Added support to specify tags conditions and rehydrate priority on batch set tier. 

## 12.6.0-beta.1 (2020-07-07)
- Updated azure-storage-common and azure-storage-blob dependencies to add support for the 2019-12-12 service version. 

## 12.5.2 (2020-06-12)
- Updated azure-storage-common and azure-core dependencies.

## 12.5.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.5.0 (2020-04-06)
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.

## 12.4.0 (2020-03-11)
- Fixed bug where Blob Batch would fail when using AAD authorization.
- Updated `azure-storage-blob` to version 12.5.0

## 12.4.0-beta.1 (2020-02-12)
- Updated `azure-storage-blob` to version 12.4.0

## 12.3.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-blob` to version 12.3.1

## 12.3.0 (2020-01-16)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.3.0/sdk/storage/azure-storage-blob-batch/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.3.0/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch)

- Upgraded to version 12.3.0 of Azure Storage Blob.

## 12.2.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.2.0/sdk/storage/azure-storage-blob-batch/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.2.0/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch)

- Upgraded to version 12.2.0 of Azure Storage Blob.
- Updated batch request creation to prevent race condition which could result in a dropped operation.

## 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.1.0/sdk/storage/azure-storage-blob-batch/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.1.0/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch)

- Upgraded to version 1.1.0 of Azure Core.
- Upgraded to version 12.1.0 of Azure Storage Blob.

## 12.0.0 (2019-10-31)

- Added BlobBatchStorageException
- Changed exception throwing to throw StorageBlobException on invalid request and BlobBatchStorageException when batch operations fail

## 12.0.0-preview.4 (2019-10-8)
For details on the Azure SDK for Java (October 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.4/sdk/storage/azure-storage-blob-batch/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.4/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch)

- Initial release of this module.
- Support for Azure Storage Blob batching operations (delete and set tier).
