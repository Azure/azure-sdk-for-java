# Release History
## 1.0.0-beta.4 (2019-12-02)
- Artifact name changed from `preview` to `beta`.
- `BlobCheckpointStore` implementation updated to match changes in `CheckpointStore` interface.

## 1.0.0-preview.3 (2019-11-01)
- Renamed `BlobPartitionManager` to `BlobEventProcessorStore`
- Added fully qualified namespace to list ownership API.

## 1.0.0-preview.2 (2019-10-08)
- Added modules support for JDK 9+.

## 1.0.0-preview.1 (2019-09-09)

Version 1.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### Features

- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Receive messages from all partitions of an Azure Event Hub using `EventProcessor`.
- Provide an instance of `BlobCheckpointStore` to your Event Processor. `BlobCheckpointStore` uses Azure Blob Storage to 
store checkpoints and balance partition load among all instances of Event Processors.
- Store checkpoint and partition ownership details in [Azure Storage Blobs](https://azure.microsoft.com/en-us/services/storage/blobs/).

### Known issues

- Initial offset provider for each partition is not implemented.
- Interoperability with Event Processors of other language SDKs like Python is not supported.
