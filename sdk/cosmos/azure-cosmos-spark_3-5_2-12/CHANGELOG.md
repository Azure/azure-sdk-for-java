## Release History

### 4.34.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 4.33.0 (2024-06-22)

#### Features Added
* Added a service trait `CosmosClinetBuilderInterceptor` to allow intercepting and customizing the CosmosClient creation. - See [PR 40714](https://github.com/Azure/azure-sdk-for-java/pull/40714)

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
