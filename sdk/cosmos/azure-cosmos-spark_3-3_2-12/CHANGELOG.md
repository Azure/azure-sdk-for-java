## Release History

### 4.19.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

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
