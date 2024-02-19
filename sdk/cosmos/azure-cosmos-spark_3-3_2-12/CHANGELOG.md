## Release History

### 4.27.0 (2024-02-20)

#### Features Added
* Added an option to retrieve the `CosmosAsyncClient` used by the connector internally to allow Spark applications to reuse the same instance instead of having to instantiate their own `CosmsoAsyncClient` when also using the Cosmos Java SDK besides the Spark connector. - See [PR 38834](https://github.com/Azure/azure-sdk-for-java/pull/38834)

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
* Fixed LSN offset for Spark 2 -> Spark 3 offset conversion UDF function - See [PR 33757](https://github.com/Azure/azure-sdk-for-java/pull/33757)

### 4.17.0 (2023-02-17)

#### Features Added
* Added Service Principal based AAD Auth - See [PR 32393](https://github.com/Azure/azure-sdk-for-java/pull/32393) and [PR 33449](https://github.com/Azure/azure-sdk-for-java/pull/33449)
* Added capability to allow modification of throughput in Spark via `ALTER TABLE` or `ALTER DATABASE` command. - See [PR 33369](https://github.com/Azure/azure-sdk-for-java/pull/33369)

#### Bugs Fixed
* Change feed pull API is using an incorrect key value for collection lookup, which can result in using the old collection in collection recreate scenarios. - See [PR 33178](https://github.com/Azure/azure-sdk-for-java/pull/33178)

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
