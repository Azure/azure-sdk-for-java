## Release History

### 4.50.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed
* Added a defensive guard in bounded change feed reads (with `endLsn`) that fails the Spark task with `IllegalStateException` when the underlying paginator stops before the latest continuation token has advanced to `endLsn`. - See [PR 49393](https://github.com/Azure/azure-sdk-for-java/pull/49393)

#### Other Changes

### 4.49.0 (2026-06-08)

#### Bugs Fixed
* Improved partition planning performance for change feed with large number of feed ranges. - See [PR 49086](https://github.com/Azure/azure-sdk-for-java/pull/49086)
* Fixed `UnsupportedOperationException` when using `readManyByPartitionKeys` for empty pages. - See [PR 49311](https://github.com/Azure/azure-sdk-for-java/pull/49311)
* Fixed `OperationCancelledException` ("End-to-end timeout hit") on sparse cross-partition queries by opting into the SDK's `allowEmptyPages` behavior, so the per-page timeout applies per page instead of being exceeded by serial empty-page drains. Note: this surfaces one iterator callback per empty page where previously a single callback could drain many. - See [PR 49276](https://github.com/Azure/azure-sdk-for-java/pull/49276)

#### Other Changes
* Updated `azure-cosmos` to version `4.81.0`.

### 4.48.0 (2026-05-01)

#### Features Added
* Added Spark 4.1 support with updated HDFSMetadataLog import path for SPARK-52787 package reorganization. - See [PR 48861](https://github.com/Azure/azure-sdk-for-java/pull/48861)
* Added `additionalHeaders` support to allow setting additional headers (e.g., `x-ms-cosmos-workload-id`) that are sent with every request. - See [PR 48128](https://github.com/Azure/azure-sdk-for-java/pull/48128)

#### Other Changes
* Introduced shared `azure-cosmos-spark_4` base module for code common across Spark 4.x versions. - See [PR 48861](https://github.com/Azure/azure-sdk-for-java/pull/48861)
