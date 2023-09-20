## Release History

### 4.23.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 4.22.0 (2023-09-19)

#### Features Added
* Added throughput control support for `gateway mode`. See [PR 36687](https://github.com/Azure/azure-sdk-for-java/pull/36687)

#### Other Changes
* Reduce noisy log in `ThroughputControlHelper` from `INFO` to `DEBUG` - See [PR 36653](https://github.com/Azure/azure-sdk-for-java/pull/36653)

### 4.21.1 (2023-08-28)

#### Bugs Fixed
* Fixed an issue where spark job failed due to 409 when `ItemBulkUpdate` is being configured - See [PR 36541](https://github.com/Azure/azure-sdk-for-java/pull/36541)

### 4.21.0 (2023-08-09)

#### Features Added
* Added a new configuration setting `spark.cosmos.write.bulk.initialBatchSize` to allow specifying the initial micro batch size for bulk operations. The batch size will be tuned automatically based on the throttling rate afterwards - by default it starts initially with 100 documents per batch. This can lead to exceeding the requested throughput when using throughput control in the first few seconds of a Spark job. This usually isn't a problem - but if there is the desire to avoid this, reducing the initial micro batch size - for example setting it to `1` - would avoid the initial spike in RU/s usage. - See [PR 36068](https://github.com/Azure/azure-sdk-for-java/pull/36068)
* Added new strategy `ItemBulkUpdate` to allow patch with more than 10 columns - See [PR 35977](https://github.com/Azure/azure-sdk-for-java/pull/35977)

#### Bugs Fixed
* Fixed schema reference issue for empty array - See [PR 35746](https://github.com/Azure/azure-sdk-for-java/pull/35746)

### 4.20.0 (2023-06-26)

#### Features Added
* Added `feed_detail` diagnostics mode. - See [PR 35501](https://github.com/Azure/azure-sdk-for-java/pull/35501)

### 4.19.0 (2023-06-09)

#### Features Added
* Added support for priority based throttling - See [PR 35238](https://github.com/Azure/azure-sdk-for-java/pull/35238)
* Added new configuration parameter `spark.cosmos.write.bulk.targetedPayloadSizeInBytes` to allow increasing the micro batch payload size for better efficiency when documents are often above 110 KB.  - See [PR 35379](https://github.com/Azure/azure-sdk-for-java/pull/35379)

#### Bugs Fixed
* Addressed `NullPointerException` in `CosmosDataItemSource` constructor when Spark runtime initialization hasn't completed yet. - See [PR 35201](https://github.com/Azure/azure-sdk-for-java/pull/35201)

### 4.18.2 (2023-05-16)

#### Bugs Fixed
* Fixed `IllegalArgumentException` when different throughput control group is defined on the same container - See [PR 34702](https://github.com/Azure/azure-sdk-for-java/pull/34702)

### 4.18.1 (2023-04-10)

#### Bugs Fixed
* Fixed an issue where throughput control is not triggered properly when `spark.cosmos.throughputControl.targetThroughput` is being used - See [PR 34393](https://github.com/Azure/azure-sdk-for-java/pull/34393)

### 4.18.0 (2023-04-06)

#### Features Added
* Added throughput control support without using dedicated throughput control container - See [PR 34301](https://github.com/Azure/azure-sdk-for-java/pull/34301)

### 4.17.2 (2023-02-28)

#### Bugs Fixed
- Fixed LSN offset for Spark 2 -> Spark 3 offset conversion UDF function - See [PR 33795](https://github.com/Azure/azure-sdk-for-java/pull/33795)

### 4.17.1 (2023-02-27)

#### Bugs Fixed
- Fixed LSN offset for Spark 2 -> Spark 3 offset conversion UDF function - See [PR 33757](https://github.com/Azure/azure-sdk-for-java/pull/33757)

### 4.17.0 (2023-02-17)

#### Features Added
* Added Service Principle based AAD Auth - See [PR 32393](https://github.com/Azure/azure-sdk-for-java/pull/32393) and [PR 33449](https://github.com/Azure/azure-sdk-for-java/pull/33449)
* Added capability to allow modification of throughput in Spark via `ALTER TABLE` or `ALTER DATABASE` command. - See [PR 33369](https://github.com/Azure/azure-sdk-for-java/pull/33369)

#### Bugs Fixed
- Change feed pull API is using an incorrect key value for collection lookup, which can result in using the old collection in collection recreate scenarios. - See [PR 33178](https://github.com/Azure/azure-sdk-for-java/pull/33178)

### 4.16.0 (2023-01-13)

#### Features Added
* Added an option to use raw json when applying partial updates via WriteStrategy `ItemPatch`- See [PR 32610](https://github.com/Azure/azure-sdk-for-java/pull/32610)
* Added the `spark.cosmos.read.maxIntegratedCacheStalenessInMS` configuration key
  to make `MaxIntegratedCacheStaleness` tunable for caching queries. - See [PR 32592](https://github.com/Azure/azure-sdk-for-java/pull/32592)
* Adds a new config option to determine whether offsets provided in batch mode should be ignored (instead of throwing an `IllegalStateException`) when an offset for a different container (could even be same name after recreation) is provided. If set to ignore will have same behavior as not providing any start offset (using start from settings instead). - See [PR 32962](https://github.com/Azure/azure-sdk-for-java/pull/32962)

### 4.15.0 (2022-11-16)

#### Features Added
Spark 3.3 support: - See [PR 31666](https://github.com/Azure/azure-sdk-for-java/pull/31666).
#### Other Changes
* Fixed shading instructions to correct dependency issues in Azure Synapse with version 4.14.0 and 4.14.1. - See [PR 31980](https://github.com/Azure/azure-sdk-for-java/pull/31980)
* Reduced the logging noise level on CancellationExceptions from `RntbdReporter.reportIssue`. - See [PR 32175](https://github.com/Azure/azure-sdk-for-java/pull/32175)

### NOTE: See CHANGELOG.md in 3.1 and 3.2 projects for changes prior to 4.15.0
