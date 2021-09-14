## Release History

### 4.4.0-beta.1 (Unreleased)

### 4.3.1 (2021-09-13)
#### Key Bug Fixes
* Fixed issue resulting in option `spark.cosmos.read.maxItemCount` not always being honored
* Fixed issue resulting in dropping some events when using Spark Streaming when config option `spark.cosmos.changeFeed.itemCountPerTriggerHint` is configured.

### 4.3.0 (2021-08-11)
#### Configuration Changes
* Introduced a new config option `spark.cosmos.read.maxItemCount` to allow modifying the page size for query and change feed requests against the Cosmos DB backend. The previous default was 100 items per request - the new default is 1000 and can be modified via the new config option if necessary, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/23466).

#### Key Bug Fixes
* Improved robustness for bulk ingestion jobs to avoid transient hangs - when the Spark job did not finish gracefully although the actual ingestion work has been finished. See [PR 22950](https://github.com/Azure/azure-sdk-for-java/pull/22950), [PR 23262](https://github.com/Azure/azure-sdk-for-java/pull/23262), [PR 23329](https://github.com/Azure/azure-sdk-for-java/pull/23329), [PR 23334](https://github.com/Azure/azure-sdk-for-java/pull/23334), [PR 23360](https://github.com/Azure/azure-sdk-for-java/pull/23360), [PR 23335](https://github.com/Azure/azure-sdk-for-java/pull/23335) and [PR 23461](https://github.com/Azure/azure-sdk-for-java/pull/23461)  

### 4.2.1-beta.1 (2021-07-15)
* Fixed Catalog api synapse integration.

### 4.2.0 (2021-06-23)
#### Configuration Changes
* Changed the default value of `spark.cosmos.read.inferSchema.forceNullableProperties` from `false` to `true` based on user feedback, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22049).

#### Key Bug Fixes
* Fixes conversion for MapType schema, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22291).
* Not-nullable properties to include "id", see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22143).
* Support CustomQuery to be used for inference, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22079).
* Fixes collision resolution on schema inference, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/21933).
* Fixes max length of userAgent header, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22018).
* Improves bulk ingestion throttling rate by dynamically adjusting the max micro-batch size, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22290).

### 4.1.0 (2021-05-27)
#### New Features
* Added support for bulk deletes via `spark.cosmos.write.strategy` `ItemDelete` or `ItemDeleteIfNotModified`
* Added support for enforcing custom queries via `spark.cosmos.read.customQuery`. Custom queries will be sent to the Cosmos backend instead of dynamically generating the query from predicate push-downs.

#### Key Bug Fixes
* Fixes an issue resulting in invalid query plans when using string filter operators (StartsWith, EndsWith, Contains)

### 4.0.0 (2021-05-14)
#### Configuration Renames
* Renamed data source name `cosmos.changeFeed` to `cosmos.oltp.changeFeed`, See [PR](https://github.com/Azure/azure-sdk-for-java/pull/21184).

#### Key Bug Fixes
* Fixed a bug in bulk write when using Gateway mode that could cause job failures during partition splits
* Improved the client-side throughput control algorithm to allow saturating the allowed throughput
* Added debug-level logging to help client-side throughput control investigations

### 4.0.0-beta.3 (2021-05-05)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.3` Release.
#### Configuration Renames
* Renamed data source name `cosmos.changeFeed` to `cosmos.oltp.changeFeed`, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/21121).
* Configuration renamed. See [PR](https://github.com/Azure/azure-sdk-for-java/pull/21004) for list of changes. See [Configuration-Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md) for more details.

#### Key Bug Fixes
* Added validation for all config-settings with a name starting with "spark.cosmos."
* Fixed a bug in bulk write causing hang.

### 4.0.0-beta.2 (2021-04-19)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.2` Release.

#### New Features
* The beta-2 is feature-complete now
* Spark structured streaming (micro batches) for consuming change feed
* Spark structured streaming (micro batches) support added for writes (TableCapability.STREAMING_WRITE)
* Allowing configuration of "Cosmos views" in the Spark catalog to enable direct queries against Spark catalog

#### Key Bug Fixes
* Perf validation and optimizations (resulting in significant better throughput for read code path)
* Row conversion: Allow configuration of behavior on schema mismatch - error vs. null
* Row conversion: Supporting InternalRow type to avoid failures when using nested StructType of InternalRow (not Row)

#### Known limitations
* No support for continuous processing (change feed) yet. (will be added after GA)
* No perf tests / optimizations have been done yet - we will iterate on perf in the next preview releases. So usage should be limited to non-production environments with this preview.

### 4.0.0-beta.1 (2021-03-22)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.1` Release.
#### Features
* Supports Spark 3.1.1 and Scala 2.12.
* Integrated against Spark3 DataSourceV2 API.
* Devloped ground up using Cosmos DB Java V4 SDK.
* Added support for Spark Query, Write, and Streaming.
* Added support for Spark3 Catalog metadata APIs.
* Added support for Java V4 Throughput Control.
* Added support for different partitioning strategies.
* Integrated against Cosmos DB TCP protocol.
* Added support for Databricks automated Maven Resolver.
* Added support for broadcasting CosmosClient caches to reduce bootstrapping RU throttling.
* Added support for unified jackson ObjectNode to SparkRow Converter.
* Added support for Raw Json format.
* Added support for Config Validation.
* Added support for Spark application configuration consolidation.
* Integrated against Cosmos DB FeedRange API to support Partition Split Proofing.
* Automated CI testing on DataBricks and Cosmos DB live endpoint.
* Automated CI Testing on Cosmos DB Emulator.

#### Known limitations
* Spark structured streaming (micro batches) for consuming change feed has been implemented but not tested end-to-end fully so is considered experimental at this point.
* No support for continuous processing (change feed) yet.
* No perf tests / optimizations have been done yet - we will iterate on perf in the next preview releases. So usage should be limited to non-production environments with this preview.

### 4.0.0-alpha.1 (2021-03-17)
* Cosmos DB Spark 3.1.1 Connector Test Release.
