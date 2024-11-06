## Release History

### 2.1.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 2.0.0 (2024-11-04)

#### Features Added
* General Availability release of the Source and Sink Kafka Connectors - See [PR 42785](https://github.com/Azure/azure-sdk-for-java/pull/42785)

#### Other Changes
* Improved error message when the database provided in the config does not exist - See [PR 42599](https://github.com/Azure/azure-sdk-for-java/pull/42599)

### 1.0.0-beta.4 (2024-07-26)

#### Other Changes
* Connector status appears as `FAILED` if try to create a connector with incorrect container names - See [PR 41160](https://github.com/Azure/azure-sdk-for-java/pull/41160) 

### 1.0.0-beta.3 (2024-06-24)

#### Bugs Fixed
* Changed to only disable `PartitionKeyRangeGoneRetryPolicy` when enable `disableSplitHandling` in `ChangeFeedRequestOptions`. - See [PR 40738](https://github.com/Azure/azure-sdk-for-java/pull/40738)
* Fixed an issue where the task will fail due to single `RequestRateTooLarge` exception - See [PR 40738](https://github.com/Azure/azure-sdk-for-java/pull/40738)

### 1.0.0-beta.2 (2024-05-01)

#### Other Changes
* Added support to create metadata container if not exists when using `Cosmos` as the metadata storage type and using `MasterKey` auth - See [PR 39973](https://github.com/Azure/azure-sdk-for-java/pull/39973)

### 1.0.0-beta.1 (2024-04-26)

#### Features Added
* Added Source connector. See [PR 39410](https://github.com/Azure/azure-sdk-for-java/pull/39410) and [PR 39919](https://github.com/Azure/azure-sdk-for-java/pull/39919)
* Added Sink connector. See [PR 39434](https://github.com/Azure/azure-sdk-for-java/pull/39434)
* Added throughput control support. See [PR 39218](https://github.com/Azure/azure-sdk-for-java/pull/39218)
* Added `ServicePrincipal` support - See [PR 39490](https://github.com/Azure/azure-sdk-for-java/pull/39490)
* Added `ItemPatch support` in sink connector - See [PR 39558](https://github.com/Azure/azure-sdk-for-java/pull/39558)
* Added support to use CosmosDB container for tracking metadata - See [PR 39634](https://github.com/Azure/azure-sdk-for-java/pull/39634)

