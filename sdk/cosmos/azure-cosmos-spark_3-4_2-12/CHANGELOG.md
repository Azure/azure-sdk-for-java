## Release History

### 4.34.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed
* Fixed an issue to avoid transient `IllegalArgumentException` due to duplicate json properties for the `uniqueKeyPolicy` property. - See [PR 41608](https://github.com/Azure/azure-sdk-for-java/pull/41608)

#### Other Changes
* Added retries on a new `BulkWriter` instance when first attempt to commit times out for bulk write jobs. - See [PR 41553](https://github.com/Azure/azure-sdk-for-java/pull/41553)

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

#### Bugs Fixed
* Fixed an issue causing failures when using change feed in batch mode with a batch location and `ChangeFeedBatch.planInputPartitions` is called multiple times (for example because physcial query plan gets retrieved) and some changes have been made in the monitored container between those calls). - See [PR 39635](https://github.com/Azure/azure-sdk-for-java/pull/39635)
* Made `AccountDataResolver` trait public again. - See [PR 39736](https://github.com/Azure/azure-sdk-for-java/pull/39736)

#### Other Changes
* Optimized the partitioning strategy implementation details to avoid unnecessarily high RU usage. - See [PR 39438](https://github.com/Azure/azure-sdk-for-java/pull/39438)

### 4.28.4 (2024-03-18)

#### Other Changes
* Increased queue length of Scheduler in `BulkWriter` by using different schedulers to handle request and response to avoid `ReactorRejectedExecutionException: Scheduler unavailable` error message. - See [PR 39260](https://github.com/Azure/azure-sdk-for-java/pull/39260)

### 4.28.3 (2024-03-12)

#### Other Changes
* Reduced noise level in logs with WARN level but added more verbose logs when retries have been re-enqueued. - See [PR 39169](https://github.com/Azure/azure-sdk-for-java/pull/39169)

### 4.28.2 (2024-03-05)

#### Other Changes
* Reduced the number of `GET Collection` requests - especially when using Spark streaming with many, short-lived micro batches. - See [PR 39076](https://github.com/Azure/azure-sdk-for-java/pull/39076)

### 4.28.1 (2024-03-01)

#### Bugs Fixed
* Fixed the UserAgent suffix when using a client retrieved via UDF `CosmosAsyncClientCache.getCosmosClientFuncFromCache` on an executor. - See [PR 39045](https://github.com/Azure/azure-sdk-for-java/pull/39045)

### 4.28.0 (2024-02-26)

#### Features Added
* Added UDF `CosmosAsyncClientCache.getCosmosClientFuncFromCache` to allow Spark applications to reuse the same instance instead of having to instantiate their own `CosmsoAsyncClient` when also using the Cosmos Java SDK besides the Spark connector even on executors. - See [PR 38939](https://github.com/Azure/azure-sdk-for-java/pull/38939)

### 4.27.1 (2024-02-23)

#### Other Changes
* Prevented any types in azure-cosmos package from being omitted during shading process. - See [PR 38902](https://github.com/Azure/azure-sdk-for-java/pull/38902)

### 4.27.0 (2024-02-20)

#### Features Added
* Added an option to retrieve the `CosmosAsyncClient` used by the connector internally to allow Spark applications to reuse the same instance instead of having to instantiate their own `CosmsoAsyncClient` when also using the Cosmos Java SDK besides the Spark connector. - See [PR 38834](https://github.com/Azure/azure-sdk-for-java/pull/38834) and [PR 38939](https://github.com/Azure/azure-sdk-for-java/pull/38939)

### 4.26.1 (2024-02-13)

#### Other Changes
* Limited max. number of threads possible to be used by BulkExecutor instances . - See [PR 38745](https://github.com/Azure/azure-sdk-for-java/pull/38745)

### 4.26.0 (2024-02-08)

#### Features Added
* Added optimization for query to use readMany internally when applicable - See [PR 38299](https://github.com/Azure/azure-sdk-for-java/pull/38299), [PR 38675](https://github.com/Azure/azure-sdk-for-java/pull/38675) and [PR 38433](https://github.com/Azure/azure-sdk-for-java/pull/38433) and [PR 38670](https://github.com/Azure/azure-sdk-for-java/pull/38670)
* Added option to use custom Schema with StringType (raw json) for a nested property - See [PR 38481](https://github.com/Azure/azure-sdk-for-java/pull/38481)

#### Bugs Fixed
* Fixed warning related to custom metrics in Spark 3.2 / 3.3 and 3.4. - See [PR 38315](https://github.com/Azure/azure-sdk-for-java/pull/38315)

#### Other Changes
* Added additional retry being en-queued when bulk ingestion hangs. - See [PR 38630](https://github.com/Azure/azure-sdk-for-java/pull/38630)

### 4.25.1 (2024-01-14)

#### Features Added
* Added config option `spark.cosmos.http.connectionPoolSize` to override the Http Connection Pool size in Gateway mode. Increasing the connection pool size beyond 1000 can be useful when the number of concurrent requests in Gateway mode is very high and you see a `reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException: Pending acquire queue has reached its maximum size of 2000` error. - See [PR 38305](https://github.com/Azure/azure-sdk-for-java/pull/38305)

### 4.25.0 (2024-01-03)

#### Other Changes
* Perf-improvement avoiding extra-buffer copy for query and point operations - See [PR 38072](https://github.com/Azure/azure-sdk-for-java/pull/38072)

### 4.24.1 (2023-12-06)

#### Bugs Fixed
* Fixed `SecurityException` with message `java.lang.SecurityException: class "org.apache.spark.SparkInternalsBridge$"'s signer information does not match signer information of other classes in the same package` when deploying the Spark connector in Databricks by copying it directly to `/databricks/jars` instead of going through the usual deployment APIs or UI-deployment. To fix this issue, instead of using a `bridge-approach` reflection is used to use the internal API necessary to publish custom metrics. See [PR 37934](https://github.com/Azure/azure-sdk-for-java/pull/37934)

### 4.24.0 (2023-12-01)

#### Features Added
* Added hierarchical partition key support. See [PR 37184](https://github.com/Azure/azure-sdk-for-java/pull/37184)
* Added `bytesWritten`, `recordsWritten` and `cosmos.totalRequestCharge` metrics in the sink of the Azure Cosmos DB connector. - See [PR 37510](https://github.com/Azure/azure-sdk-for-java/pull/37510)

#### Other Changes
* Improved DirectTcp config Defaults for Spark workloads - transit timeout health checks as well as request and connect timeout are too aggressive considering that many Spark jobs unlike latency sensitive apps is throughput optimized and executors will often hit CPU usage >70%. - See [PR 37878](https://github.com/Azure/azure-sdk-for-java/pull/37878)

### 4.23.0 (2023-10-09)

#### Features Added
* Added configuration option to control the maximum batch size used - by default the batch size is determined automatically based on the throttling rate - and will auto-adjust to meet the throughput control limits when applied. This setting is mostly added to simplify Spark 2.4 migrations where it was possible to specify a fixed batch size. This setting should only be used when not enabling throughput control - and for new workloads not being migrated from Spark 2.4 using throughput control is preferred. See [PR 37072](https://github.com/Azure/azure-sdk-for-java/pull/37072)

#### Bugs Fixed
* Fixed an issue with backpressure when using WriteStrategy `ItemBulkUpdate` - with this write strategy a Reactor operator `bufferTimeout` was used, which has issues when backpressure happens and can result in an error `verflowException: Could not emit buffer due to lack of requests`. See [PR 37072](https://github.com/Azure/azure-sdk-for-java/pull/37072)
* Fixed misspelled authType from `ServicePrinciple` to `ServicePrincipal`. For back compatibility support, `ServicePrinciple` will still be supported in the config - See [PR 37121](https://github.com/Azure/azure-sdk-for-java/pull/37121)

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
* Spark 3.4 support: - See [PR 35176](https://github.com/Azure/azure-sdk-for-java/pull/35176).
* Added a new configuration setting `spark.cosmos.write.bulk.initialBatchSize` to allow specifying the initial micro batch size for bulk operations. The batch size will be tuned automatically based on the throttling rate afterwards - by default it starts initially with 100 documents per batch. This can lead to exceeding the requested throughput when using throughput control in the first few seconds of a Spark job. This usually isn't a problem - but if there is the desire to avoid this, reducing the initial micro batch size - for example setting it to `1` - would avoid the initial spike in RU/s usage. - See [PR 36068](https://github.com/Azure/azure-sdk-for-java/pull/36068)
* Added new strategy `ItemBulkUpdate` to allow patch with more than 10 columns - See [PR 35977](https://github.com/Azure/azure-sdk-for-java/pull/35977)

#### Bugs Fixed
* Fixed schema reference issue for empty array - See [PR 35746](https://github.com/Azure/azure-sdk-for-java/pull/35746)

#### Other Changes

### NOTE: See CHANGELOG.md in 3.1, 3.2 and 3.3 projects for changes prior to 4.21.0
