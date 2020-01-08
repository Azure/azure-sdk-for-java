# Change Log azure-storage-blob-batch
## Version 12.0.2 (2020-01-08)

- Changed BlobBatch batch queuing to use a thread-safe collection

## Version 12.0.1 (2019-11-21)

- Updated azure-storage-blob dependency to 12.0.1

## Version 12.0.0 (2019-10-31)

- Added BlobBatchStorageException
- Changed exception throwing to throw StorageBlobException on invalid request and BlobBatchStorageException when batch operations fail

## Version 12.0.0-preview.4 (2019-10-8)
For details on the Azure SDK for Java (October 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.0.0-preview.4/sdk/storage/azure-storage-blob-batch/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-batch_12.0.0-preview.4/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/batch)

- Initial release of this module.
- Support for Azure Storage Blob batching operations (delete and set tier).
