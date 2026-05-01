## Release History

### 4.48.0-beta.1 (Unreleased)

#### Features Added
* Added Spark 4.1 support with updated HDFSMetadataLog import path for SPARK-52787 package reorganization. - See [PR 48861](https://github.com/Azure/azure-sdk-for-java/pull/48861)
* Added `additionalHeaders` support to allow setting additional headers (e.g., `x-ms-cosmos-workload-id`) that are sent with every request. - See [PR 48128](https://github.com/Azure/azure-sdk-for-java/pull/48128)

#### Breaking Changes

#### Bugs Fixed

#### Other Changes
* Introduced shared `azure-cosmos-spark_4` base module for code common across Spark 4.x versions. - See [PR 48861](https://github.com/Azure/azure-sdk-for-java/pull/48861)
