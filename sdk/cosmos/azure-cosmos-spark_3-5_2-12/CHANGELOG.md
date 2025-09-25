## Release History

### 4.40.0-beta.1 (Unreleased)

#### Features Added
* Added support for feed range cache refresh interval config. - See [46759](https://github.com/Azure/azure-sdk-for-java/pull/46759)

#### Breaking Changes

#### Bugs Fixed

#### Other Changes
* Added improvement to reduce partition planning time for large containers. - See [46727](https://github.com/Azure/azure-sdk-for-java/pull/46727)

### 4.39.0 (2025-09-05)

#### Bugs Fixed
* Reverted known issue due to shading log4j (which was introduced in 4.38.1). - See [PR 46546](https://github.com/Azure/azure-sdk-for-java/pull/46546) and [PR 46608](https://github.com/Azure/azure-sdk-for-java/pull/46608)
* Added change feed performance monitoring which is used to improve end lsn calculation in `CosmosPartitionPlanner`. - See [PR 46320](https://github.com/Azure/azure-sdk-for-java/pull/46320)
* Added `spark.cosmos.auth.aad.audience` as a valid configuration option to allow using AAD tokens with custom audiences. - See [PR 46554](https://github.com/Azure/azure-sdk-for-java/pull/46554)

### 4.38.1 (2025-08-22)

**NOTE: This version has a known issue due to shading log4j - Please use more recent versions >= 4.38.2 or 4.38.0 instead**

#### Other Changes
* Added log4j-core to the list of shaded packages to avoid conflicts when customers use log4j in a different version. **NOTE: This change caused known issue - Please use a more recent version instead** - See [PR 45924](https://github.com/Azure/azure-sdk-for-java/pull/46451)

### 4.38.0 (2025-07-31)

#### Features Added
* Added telemetry support by adding OTEL span attribute naming schemes, introducing Azure Monitor integration, and sampled diagnostics. - See [PR 45924](https://github.com/Azure/azure-sdk-for-java/pull/45924)

#### Other Changes
* Added compatibility with CosmosDB Fabric Native Accounts using the `FabricAccountDataResolver` for authentication. - See [PR 45890](https://github.com/Azure/azure-sdk-for-java/pull/45890)

### 4.37.2 (2025-05-14)

#### Features Added
* Added option to use the connector in non-public Azure clouds. - See [PR 45310](https://github.com/Azure/azure-sdk-for-java/pull/45310)

#### Bugs Fixed
* Fixed an issue during bulk write operations that could result in failing the Spark job in `BulkWriter.flushAndClose` too eagerly in certain cases. - See [PR 44992](https://github.com/Azure/azure-sdk-for-java/pull/44992)
* Fixed hang issue in `CosmosPagedIterable#handle` by preventing race conditions in underlying subscription of `Flux<FeedResponse>`. - [PR 45290](https://github.com/Azure/azure-sdk-for-java/pull/45290)

### 4.37.1 (2025-03-04)

#### Features Added
* Added config option `spark.cosmos.read.responseContinuationTokenLimitInKb` to reduce query continuation token size. - See [PR 44480](https://github.com/Azure/azure-sdk-for-java/pull/44480)

### 4.37.0 (2025-02-20)

#### Other Changes
* Updated netty dependency

### 4.36.1 (2025-02-08)

#### Bugs Fixed
* Fixed an issue in change feed where under certain rare race conditions records could be skipped and excessive requests are prefetched. - See [PR 43788](https://github.com/Azure/azure-sdk-for-java/pull/43788)

### 4.36.0 (2025-01-14)
> [!IMPORTANT]
> We strongly recommend our customers to use version 4.36.0 and above especially if using all versions and deletes change feed.

#### Features Added
* Added the udfs `GetFeedRangesForContainer` and `GetOverlappingFeedRange` to ease mapping of cosmos partition key to databricks table partition key. - See [PR 43092](https://github.com/Azure/azure-sdk-for-java/pull/43092)

#### Bugs Fixed
* Added null checking for previous images for deletes in full fidelity change feed. - See [PR 43483](https://github.com/Azure/azure-sdk-for-java/pull/43483)

#### Other Changes
* Added options to fine-tune settings for bulk operations. - [PR 43509](https://github.com/Azure/azure-sdk-for-java/pull/43509)

### 4.35.0 (2024-11-27)

#### Bugs Fixed
* Fixed an issue when using `ChangeFeed` causing some cosmos partitions to not be fully processed in some cases. - See [PR 42553](https://github.com/Azure/azure-sdk-for-java/pull/42553)

### 4.34.0 (2024-10-10)
#### Bugs Fixed
* Fixed an issue to avoid transient `IllegalArgumentException` due to duplicate json properties for the `uniqueKeyPolicy` property in `DocumentCollection`. - See [PR 41608](https://github.com/Azure/azure-sdk-for-java/pull/41608) and [PR 42244](https://github.com/Azure/azure-sdk-for-java/pull/42244)

### 4.33.1 (2024-08-23)

#### Bugs Fixed
* Fixed an issue to avoid transient `IllegalArgumentException` due to duplicate json properties for the `uniqueKeyPolicy` property. - See [PR 41608](https://github.com/Azure/azure-sdk-for-java/pull/41608)

#### Other Changes
* Added retries on a new `BulkWriter` instance when first attempt to commit times out for bulk write jobs. - See [PR 41553](https://github.com/Azure/azure-sdk-for-java/pull/41553)

### 4.33.0 (2024-06-22)

#### Features Added
* Added a service trait `CosmosClientBuilderInterceptor` to allow intercepting and customizing the CosmosClient creation. - See [PR 40714](https://github.com/Azure/azure-sdk-for-java/pull/40714)

#### Bugs Fixed
* Fixed a race condition resulting in not always re-enqueueing retries for bulk writes. - See [PR 40714](https://github.com/Azure/azure-sdk-for-java/pull/40714)

### 4.32.1 (2024-06-07)

#### Other Changes
* Added retries when retrieving new pages for query or readMany operations are timing out to avoid unbounded awaits. - See [PR 40506](https://github.com/Azure/azure-sdk-for-java/pull/40506)
* Ensured that no statistics are reported when custom queries via `spark.cosmos.read.customQuery` are used. - See [PR 40506](https://github.com/Azure/azure-sdk-for-java/pull/40506)

### 4.32.0 (2024-05-24)

#### Features Added
* Added config option `spark.cosmos.auth.aad.clientCertPemBase64` to allow using SPN (ServicePrincipal name) authentication with certificate instead of client secret. - See [PR 40325](https://github.com/Azure/azure-sdk-for-java/pull/40325)
* Added config option `spark.cosmos.accountDataResolverServiceName` to allow specifying which `AccountDataResolver` trait implementation to use if there are multiple on the class path. - See [PR 40325](https://github.com/Azure/azure-sdk-for-java/pull/40325)

#### Bugs Fixed
* Fixed an issue where `SHOW DATABASES IN` only return one database even though multiple databases exist. - See [PR 40277](https://github.com/Azure/azure-sdk-for-java/pull/40277)
* Fixed an issue where `SHOW TABLES FROM` only return one container even though multiple containers exist. - See [PR 40277](https://github.com/Azure/azure-sdk-for-java/pull/40277)
* Fixed UserAgent encoding when the suffix contains non-ASCII characters. - See[PR 40293](https://github.com/Azure/azure-sdk-for-java/pull/40293)

#### Other Changes
* Added robustness improvement to avoid client-side parsing errors `java.lang.IllegalArgumentException: Unable to parse JSON` when Gateway returns duplicate `unqiueKeyPolicy` in IndexPolicy (invalid json) - See[PR 40306](https://github.com/Azure/azure-sdk-for-java/pull/40306)

### 4.31.0 (2024-05-20)

#### Features Added
* Added capability in azure-cosmos-spark to allow the spark environment to support access tokens via AccountDataResolver. - See [PR 40079](https://github.com/Azure/azure-sdk-for-java/pull/40079)

### 4.30.0 (2024-04-27)

#### Features Added
* Added capability to use (and enforce) native netty transport. The native transport is more efficient - esepcially when the number of TCP connections being used is high. - See [PR 39834](https://github.com/Azure/azure-sdk-for-java/pull/39834)
* Added ManagedIdentity authentication support for azure-cosmos-spark in Databricks. - See [PR 39870](https://github.com/Azure/azure-sdk-for-java/pull/39870)

### 4.29.0 (2024-04-16)

#### Features Added
* Spark 3.5 support: - See [PR 39395](https://github.com/Azure/azure-sdk-for-java/pull/39395).

#### Bugs Fixed
* Fixed an issue causing failures when using change feed in batch mode with a batch location and `ChangeFeedBatch.planInputPartitions` is called multiple times (for example because physcial query plan gets retrieved) and some changes have been made in the monitored container between those calls). - See [PR 39635](https://github.com/Azure/azure-sdk-for-java/pull/39635)
* Made `AccountDataResolver` trait public again. - See [PR 39736](https://github.com/Azure/azure-sdk-for-java/pull/39736)

#### Other Changes
* Optimized the partitioning strategy implementation details to avoid unnecessarily high RU usage. - See [PR 39438](https://github.com/Azure/azure-sdk-for-java/pull/39438)
  
### NOTE: See CHANGELOG.md in 3.1, 3.2, 3.3 and 3.4 projects for changes prior to 4.29.0
