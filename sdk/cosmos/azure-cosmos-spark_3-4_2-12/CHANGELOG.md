## Release History

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
