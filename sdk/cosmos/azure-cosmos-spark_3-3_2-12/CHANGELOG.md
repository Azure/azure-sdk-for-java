## Release History

### 4.17.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

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
