## Release History

### 4.48.0-beta.1 (Unreleased)

#### Features Added
* Added new `CosmosItemsDataSource.readManyByPartitionKeys` Spark function to execute bulk queries by a list of pk-values with better efficiency. Configure null handling via `spark.cosmos.read.readManyByPk.nullHandling` - default `Null` treats a null PK column as JSON null (`addNullValue`), `None` treats it as `PartitionKey.NONE` (`addNoneValue` / `NOT IS_DEFINED`). These route to different physical partitions - picking the wrong mode silently returns zero rows. See [PR 48801](https://github.com/Azure/azure-sdk-for-java/pull/48801)
* Added Spark config `spark.cosmos.read.readManyByPk.maxConcurrentBatchPrefetch` (default `1`) to bound the per-task prefetch parallelism the SDK uses inside `readManyByPartitionKeys`. See [PR 48801](https://github.com/Azure/azure-sdk-for-java/pull/48801)
* Added Spark config `spark.cosmos.read.readManyByPk.maxBatchSize` (default `100`) to set the max. number of partition keys used for a single batch. See [PR 48930](https://github.com/Azure/azure-sdk-for-java/pull/48930)

#### Breaking Changes

#### Bugs Fixed

#### Other Changes
* Refactored to use shared `azure-cosmos-spark_4` base module for code common across Spark 4.x versions. - See [PR 48861](https://github.com/Azure/azure-sdk-for-java/pull/48861)

### 4.47.0 (2026-04-17)

#### Features Added
* Added support for change feed with `startFrom` point-in-time on merged partitions by enabling the `CHANGE_FEED_WITH_START_TIME_POST_MERGE` SDK capability in the azure-cosmos SDK. - See [PR 48752](https://github.com/Azure/azure-sdk-for-java/pull/48752)

#### Bugs Fixed
* Fixed `NoClassDefFoundError` for `MetadataVersionUtil` when using change feed Spark Structured Streaming on Databricks Runtime 17.3+ by inlining the version validation logic. - See [PR 48837](https://github.com/Azure/azure-sdk-for-java/pull/48837)
* Fixed JVM `<clinit>` deadlock when multiple threads concurrently trigger Cosmos SDK class loading for the first time. - See [PR 48689](https://github.com/Azure/azure-sdk-for-java/pull/48689)

### 4.46.0 (2026-03-27)

#### Bugs Fixed
* Fixed an issue where creating containers with hierarchical partition keys (multi-hash) through the Spark catalog on the AAD path would fail. - See [PR 48548](https://github.com/Azure/azure-sdk-for-java/pull/48548)

### 4.45.0 (2026-03-13)

#### Features Added
* Added `vectorEmbeddingPolicy` support in Spark catalog `TBLPROPERTIES` for creating vector-search-enabled containers. - See [PR 48349](https://github.com/Azure/azure-sdk-for-java/pull/48349)

### 4.44.2 (2026-03-05)

#### Other Changes
* Changed azure-resourcemanager-cosmos usage to a pinned version which is deployed across all public and non-public clouds - [PR 48268](https://github.com/Azure/azure-sdk-for-java/pull/48268)

### 4.44.1 (2026-03-03)

#### Other Changes
* Reduced noisy warning logs in Gateway mode - [PR 48189](https://github.com/Azure/azure-sdk-for-java/pull/48189)

### 4.44.0 (2026-02-27)

#### Features Added
* Added config entry `spark.cosmos.account.azureEnvironment.management.scope` to allow specifying the Entra ID scope/audience to be used when retrieving tokens to authenticate against the ARM/management endpoint of non-public clouds. - See [PR 48137](https://github.com/Azure/azure-sdk-for-java/pull/48137)

### 4.43.1 (2026-02-25)

#### Bugs Fixed
* Fixed an issue where `TransientIOErrorsRetryingIterator` would trigger extra query during retries and on close. - See [PR 47996](https://github.com/Azure/azure-sdk-for-java/pull/47996)

#### Other Changes
* Added status code history in `BulkWriterNoProgressException` error message. - See [PR 48022](https://github.com/Azure/azure-sdk-for-java/pull/48022)
* Reduced the log noise level for frequent transient errors - for example throttling - in Gateway mode - [PR 48112](https://github.com/Azure/azure-sdk-for-java/pull/48112)

### 4.43.0 (2026-02-10)

#### Features Added
* Initial release of Spark 4.0 connector with Scala 2.13 support
* Added transactional batch support. See [PR 47478](https://github.com/Azure/azure-sdk-for-java/pull/47478) and [PR 47697](https://github.com/Azure/azure-sdk-for-java/pull/47697) and [47803](https://github.com/Azure/azure-sdk-for-java/pull/47803)
* Added support for throughput bucket. - See [47856](https://github.com/Azure/azure-sdk-for-java/pull/47856)

#### Other Changes

### NOTE: See CHANGELOG.md in 3.3, 3.4, and 3.5 projects for changes in prior Spark versions
