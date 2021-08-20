# Release History

## 1.8.2 (2021-08-19)


## 1.8.1 (2021-07-09)
### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.9.0`.

## 1.8.0 (2021-06-14)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.8.0`.
- Update `azure-storage-blob` to `12.12.0`.

## 1.7.1 (2021-06-01)

### Dependency Updates

- Update `azure-storage-blob` to `12.11.1`.

## 1.7.0 (2021-05-10)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.7.1`.

## 1.6.0 (2021-04-12)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.7.0`.

## 1.5.1 (2021-03-10)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.5.1`.

## 1.5.0 (2021-02-15)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.5.0`.
- Update `azure-storage-blob` dependency to `12.10.0`.

## 1.4.0 (2021-01-14)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.4.0`.

## 1.4.0-beta.1 (2020-11-12)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.4.0-beta.1`.

## 1.3.1 (2020-10-30)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.3.1`.

## 1.3.0 (2020-10-12)

### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.3.0`.

## 1.2.0 (2020-09-11)
- Updated dependency version of `azure-messaging-eventhubs` to `5.2.0`.

## 1.2.0-beta.2 (2020-08-14)
- Updated dependency version of `azure-messaging-eventhubs` to `5.2.0-beta.2`.

## 1.1.2 (2020-07-08)
- Updated dependency version of `azure-messaging-eventhubs` to `5.1.2`

## 1.1.1 (2020-06-12)
- Updated dependency version of `azure-messaging-eventhubs` to `5.1.1`

## 1.1.0 (2020-05-07)
- Updated version of `azure-messaging-eventhubs` to `5.1.0`.

## 1.1.0-beta.1 (2020-04-08)
- Updated version of `azure-messaging-eventhubs` to `5.1.0-beta.1` that supports receiving events in batches.

## 1.0.3 (2020-04-08)
- Fix bug where processor would hang after a reconnect due to being unable to load balance partitions.

## 1.0.2 (2020-02-12)
- Dependency fixed so `EventProcessor` consumers can use blocking method calls in their code.

## 1.0.1 (2020-02-11)
- Dependency updates.

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
- Store checkpoint and partition ownership details in [Azure Storage Blobs](https://azure.microsoft.com/services/storage/blobs/).

### Known issues

- Initial offset provider for each partition is not implemented.
- Interoperability with Event Processors of other language SDKs like Python is not supported.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs-checkpointstore-blob%2FCHANGELOG.png)
