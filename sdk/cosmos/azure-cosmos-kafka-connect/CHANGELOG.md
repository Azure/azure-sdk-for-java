## Release History

### 2.6.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 2.5.1 (2025-08-22)

#### Bugs Fixed
* Fixed an issue where `CosmosSourceConnector` got stuck when restart - See [PR 46378](https://github.com/Azure/azure-sdk-for-java/pull/46378)
* Fixed `BadRequestException` in `CosmosSourceConnector` when using `azure.cosmos.source.containers.includeAll=true` - See [PR 46389](https://github.com/Azure/azure-sdk-for-java/pull/46389)
* Fixed `NullPointerException` in `CosmosSourceConnector` when using `azure.cosmos.source.changeFeed.mode=AllVersionsAndDeletes` - See [PR 46396](https://github.com/Azure/azure-sdk-for-java/pull/46396)
* Fixed warning logs for `java.lang.NoClassDefFoundError: io/micrometer/observation/ObservationHandler` - See [PR 46396](https://github.com/Azure/azure-sdk-for-java/pull/46396)

#### Other Changes
* Reduced log frequency in `CosmosSourceTask` and `CosmosSinkTask` to log aggregrated logs every 1 min - See [PR 46396](https://github.com/Azure/azure-sdk-for-java/pull/46396)

### 2.5.0 (2025-07-31)

#### Other Changes
* Added more logs in `CosmosSourceTask` and `CosmosSinkTask` - See [PR 46224](https://github.com/Azure/azure-sdk-for-java/pull/46224)

### 2.4.0 (2025-06-24)

#### Bugs Fixed
* Fixed an issue where Cosmos client is not being closed properly when connector failed to start - See [PR 45633](https://github.com/Azure/azure-sdk-for-java/pull/45633)
* Fixed `NullReferenceException` in Kafka Source Connector when split happens - See [PR 45838](https://github.com/Azure/azure-sdk-for-java/pull/45838)

### 2.3.1 (2025-05-14)

#### Bugs Fixed
* Fixed hang issue in `CosmosPagedIterable#handle` by preventing race conditions in underlying subscription of `Flux<FeedResponse>`. - [PR 45290](https://github.com/Azure/azure-sdk-for-java/pull/45290)

### 2.3.0 (2025-04-23)

#### Bugs Fixed
* Prevented the usage of different schemas in the `cosmos.metadata.topic` topic helps when a customer selects output Kafka record value format as JSON_SR, AVRO or PROTOBUF (ie any SR format). With this change there is a unified schema used for the metadata topic instead. - See [PR 45018](https://github.com/Azure/azure-sdk-for-java/pull/45018)

#### Other Changes
* Added `authEndpointOverride` option for all AAD authentication types - See [PR 45016](https://github.com/Azure/azure-sdk-for-java/pull/45016)

### 2.2.0 (2025-02-20)

#### Other Changes
* Updated `azure-cosmos` to version `4.67.0`.

### 2.1.1 (2025-02-08)

#### Other Changes
* Updated `azure-cosmos` to version `4.66.1`.

### 2.1.0 (2025-01-14)

#### Bugs Fixed
* Fixed `BadRequestException` when customer using Serverless CosmosDB database account and metadata container does not exists. - See [PR 43125](https://github.com/Azure/azure-sdk-for-java/pull/43125) 

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

