# Release History

## 1.20.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.20.0-beta.2 (2024-08-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.6` to version `5.19.0-beta.2`.
- Upgraded `azure-storage-blob` from `12.26.1` to version `12.27.0`.

## 1.19.6 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.5` to version `5.18.6`.

## 1.19.5 (2024-06-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.4` to version `5.18.5`.
- Upgraded `azure-storage-blob` from `12.26.0` to version `12.26.1`.

## 1.20.0-beta.1 (2024-05-21)

### Features Added

- Adds support for persisting replication segment in `Checkpoint`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.3` to version `5.19.0-beta.1`.

## 1.19.3 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.2` to version `5.18.3`.
- Upgraded `azure-storage-blob` from `12.25.2` to version `12.25.3`.

## 1.19.2 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.1` to version `5.18.2`.
- Upgraded `azure-storage-blob` from `12.25.1` to version `12.25.2`.


## 1.19.1 (2024-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.18.0` to version `5.18.1`.

## 1.19.0 (2024-01-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.17.1` to version `5.18.0`.

## 1.18.1 (2023-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.25.0` to version `12.25.1`.
- Upgraded `azure-messaging-eventhubs` from `5.17.0` to version `5.17.1`.

## 1.18.0 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.24.1` to version `12.25.0`.
- Upgraded `azure-messaging-eventhubs` from `5.16.1` to version `5.16.2`.

## 1.17.1 (2023-10-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.16.0` to version `5.16.1`.
- Upgraded `azure-storage-blob` from `12.24.0` to version `12.24.1`.

## 1.17.0 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.8` to version `5.16.0`.

## 1.16.9 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.7` to version `5.15.8`.

## 1.16.8 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.6` to version `5.15.7`.
- Upgraded `azure-storage-blob` from `12.22.3` to version `12.23.0`.


## 1.16.7 (2023-06-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.5` to version `5.15.6`.
- Upgraded `azure-storage-blob` from `12.22.1` to version `12.22.3`.

## 1.16.6 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.22.0` to version `12.22.1`.
- Upgraded `azure-messaging-eventhubs` from `5.15.4` to version `5.16.0-beta.1`.

## 1.16.5 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.21.1` to version `12.22.0`.
- Upgraded `azure-messaging-eventhubs` from `5.15.3` to version `5.15.4`.

## 1.16.4 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.2` to version `5.15.3`.
- Upgraded `azure-storage-blob` from `12.20.3` to version `12.21.1`.

## 1.16.3 (2023-02-13)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.15.2`.
- Update `azure-storage-blob` dependency to `12.20.3`.

## 1.16.2 (2023-01-18)

### Breaking Changes

- Remove `messaging.eventhubs.checkpoints` counter and replace it with `messaging.eventhubs.checkpoint.duration`
  histogram that can be used to count checkpoint calls.

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.15.1`.
- Update `azure-storage-blob` dependency to `12.20.2`.

## 1.16.1 (2022-11-16)

### Bugs Fixed

- Reverted behavior changes of `claimOwnership` back to return empty when error occurred, to avoid throw out 412 status code error. ([#31672](https://github.com/Azure/azure-sdk-for-java/issues/31672))

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.15.0`.
- Update `azure-storage-blob` dependency to `12.20.1`.

## 1.16.0 (2022-10-13)

### Breaking Changes

- Remove `com.azure.messaging.eventhubs.checkpointstore.blob.Messages` from public API.

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.14.0`.
- Update `azure-storage-blob` dependency to `12.20.0`.

## 1.15.1 (2022-09-11)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.13.1`.
- Update `azure-storage-blob` dependency to `12.19.1`.

## 1.15.0 (2022-08-18)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.13.0`.
- Update `azure-storage-blob` dependency to `12.19.0`.

## 1.15.0-beta.1 (2022-08-01)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.13.0-beta.1`.

## 1.14.0 (2022-07-07)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.12.2`.
- Update `azure-storage-blob` dependency to `12.18.0`.

## 1.13.0 (2022-06-10)

### Features Added

- Updated return error when claim ownership occurs an error. Changed the behavior of `claimOwnership` method from __return empty when error__ to __return error when error__

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.12.1`.
- Update `azure-storage-blob` dependency to `12.17.1`.

## 1.12.2 (2022-05-16)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.12.0`.
- Update `azure-storage-blob` dependency to `12.16.1`.

## 1.12.1 (2022-04-11)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.11.2`.
- Update `azure-storage-blob` dependency to `12.16.0`.

## 1.12.0 (2022-03-17)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.11.1`.
- Update `azure-storage-blob` dependency to `12.15.0`.

## 1.11.0 (2022-02-11)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.11.0`.
- Update `azure-storage-blob` dependency to `12.14.3`.

## 1.10.3 (2022-01-18)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.10.4`.
- Update `azure-storage-blob` dependency to `12.14.3`.

## 1.10.2 (2021-11-16)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.10.3`.
- Update `azure-storage-blob` dependency to `12.14.2`.

## 1.10.1 (2021-10-13)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.10.2`.
- Update `azure-storage-blob` dependency to `12.14.1`.

## 1.10.0 (2021-09-20)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.10.1`.
- Update `azure-storage-blob` to `12.14.0`.

## 1.9.0 (2021-08-19)

### Other Changes

#### Dependency Updates

- Update `azure-messaging-eventhubs` dependency to `5.10.0`.
- Update `azure-storage-blob` to `12.13.0`.

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
- Fix bug where processor would not respond after a reconnect due to being unable to load balance partitions.

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
