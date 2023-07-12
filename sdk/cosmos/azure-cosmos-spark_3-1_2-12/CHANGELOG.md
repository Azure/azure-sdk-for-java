## Release History

### 4.21.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed
* Fixed schema reference issue for empty array - See [PR 35746](https://github.com/Azure/azure-sdk-for-java/pull/35746)

#### Other Changes

### 4.20.0 (2023-06-26)

#### Features Added
* Added `feed_detail` diagnostics mode. - See [PR 35501](https://github.com/Azure/azure-sdk-for-java/pull/35501)

### 4.19.0 (2023-06-09)

#### Features Added
* Added support for priority based throttling - See [PR 35238](https://github.com/Azure/azure-sdk-for-java/pull/35238)
* Added new configuration parameter `spark.cosmos.write.bulk.targetedPayloadSizeInBytes` to allow increasing the micro batch payload size for better efficiency when documents are often above 110 KB.  - See [PR 35379](https://github.com/Azure/azure-sdk-for-java/pull/35379)
* 
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
- Fixed LSN offset for Spark 2 -> Spark 3 offset conversion UDF function - See [PR 33757](https://github.com/Azure/azure-sdk-for-java/pull/33757)

### 4.17.0 (2023-02-17)

#### Features Added
* Added Service Principle based AAD Auth - See [PR 32393](https://github.com/Azure/azure-sdk-for-java/pull/32393) and [PR 33449](https://github.com/Azure/azure-sdk-for-java/pull/33449)
* Added capability to allow modification of throughput in Spark via `ALTER TABLE` or `ALTER DATABASE` command. - See [PR 33369](https://github.com/Azure/azure-sdk-for-java/pull/33369)

#### Bugs Fixed
- Change feed pull API is using an incorrect key value for collection lookup, which can result in using the old collection in collection recreate scenarios. - See [PR 33178](https://github.com/Azure/azure-sdk-for-java/pull/33178)

### 4.16.0 (2023-01-13)

#### Features Added
* Added an option to use raw json when applying partial updates via WriteStrategy `ItemPatch`- See [PR 32610](https://github.com/Azure/azure-sdk-for-java/pull/32610)
* Added the `spark.cosmos.read.maxIntegratedCacheStalenessInMS` configuration key
  to make `MaxIntegratedCacheStaleness` tunable for caching queries. - See [PR 32592](https://github.com/Azure/azure-sdk-for-java/pull/32592)
* Adds a new config option to determine whether offsets provided in batch mode should be ignored (instead of throwing an `IllegalStateException`) when an offset for a different container (could even be same name after recreation) is provided. If set to ignore will have same behavior as not providing any start offset (using start from settings instead). - See [PR 32962](https://github.com/Azure/azure-sdk-for-java/pull/32962)

### 4.15.0 (2022-11-16)

#### Other Changes
* Fixed shading instructions to correct dependency issues in Azure Synapse with version 4.14.0 and 4.14.1. - See [PR 31980](https://github.com/Azure/azure-sdk-for-java/pull/31980) 
* Reduced the logging noise level on CancellationExceptions from `RntbdReporter.reportIssue`. - See [PR 32175](https://github.com/Azure/azure-sdk-for-java/pull/32175)

### 4.14.1 (2022-10-07)
> [!IMPORTANT]
> We strongly recommend our customers to use version 4.14.1 and above.
#### Bugs Fixed
* Fixed incorrect RU metric reporting in micrometer metrics. - See [PR 31307](https://github.com/Azure/azure-sdk-for-java/pull/31307)

### 4.14.0 (2022-09-30)

#### Features Added
* Added new config options for Change Feed Modes, Incremental as `LatestVersion` and Full Fidelity as `AllVersionsAndDeletes` changes - See [PR 30399](https://github.com/Azure/azure-sdk-for-java/pull/30399)
* Added option to emit client-side metrics via micrometer.io MeterRegistry. - See [PR 30065](https://github.com/Azure/azure-sdk-for-java/pull/30065)

### 4.13.1 (2022-09-22)

#### Bugs Fixed
* Fixed a race condition that could result in a memory/thread leak for `BulkExecutor` instances (and their corresponding `cosmos-daemon-BulkExecutor-*` thread). This issue could occur when ingesting data into Cosmos DB via structured streaming jobs. - See [PR 31082](https://github.com/Azure/azure-sdk-for-java/pull/31082)

### 4.13.0 (2022-09-15)

#### Other Changes
* Added support to allow overriding json parsing behavior when a json document contains duplicated properties. Config entry `spark.cosmos.read.allowInvalidJsonWithDuplicateJsonProperties` can be used to not raise a hard error and use the last property instead. - See [PR 30916](https://github.com/Azure/azure-sdk-for-java/pull/30916)

### 4.12.2 (2022-08-04)

#### Breaking Changes
* Known issue introduced with Java SDK [PR 30161](https://github.com/Azure/azure-sdk-for-java/pull/30161) for incremental change feed schema when schema inference is disabled, possibly causing schema mismatch errors due to new `_lsn` column. Mitigation would be to drop that column from the dataframe: `spark.readStream.format("cosmos.oltp.changeFeed").options(**changeFeedCfg).load().drop("_lsn")`

#### Bugs Fixed
* Fixed the SerializationDateTimeConversionMode `AlwaysEpochMillisecdsWithSystemDefaultTimezone` where ZoneOffset calculation could be wrong especially for dates in the 19xx years. - See [PR 30266](https://github.com/Azure/azure-sdk-for-java/pull/30266)

#### Other Changes
* Added support to allow config different account for throughput control - See [PR 30127](https://github.com/Azure/azure-sdk-for-java/pull/30127)

### 4.12.1 (2022-07-22)

#### Bugs Fixed
* Fixed `NotFoundException` for changeFeed after split - See [PR 29982](https://github.com/Azure/azure-sdk-for-java/pull/29982)
* Fixed `IllegalArgumentException` when trying to update targetThroughput(Threshold) without process restart. - See [PR 30049](https://github.com/Azure/azure-sdk-for-java/pull/30049)

#### Other Changes
* Added a new `spark.cosmos.serialization.dateTimeConversionMode` mode called `AlwaysEpochMillisecondsWithSystemDefaultTimezone` that will assume SystemDefault time zone instead of UTC when a Date/time to be parsed has no explicit time zone. - See [PR 30001](https://github.com/Azure/azure-sdk-for-java/pull/30001)

### 4.12.0 (2022-07-14)

#### Features Added
* Added a new config option `spark.cosmos.changeFeed.batchCheckpointLocation` that allows to also proceed a change feed batch query from a checkpoint/offset - See [PR 29771](https://github.com/Azure/azure-sdk-for-java/pull/29771)
* Added an option to also restrict the memory consumption for batch change feed queries (via the `spark.cosmos.changeFeed.itemCountPerTriggerHint` config option) - See [PR 29771](https://github.com/Azure/azure-sdk-for-java/pull/29771)
* Added a UDF `CreateChangeFeedOffsetFromSpark2` that allows creating a Spark 3.* offset/checkpoint from a Spark 2.4 checkpoint - See [PR 29771](https://github.com/Azure/azure-sdk-for-java/pull/29771)

#### Bugs Fixed
* Fixed a bug preventing use patch on container not partitioned by `id`. - See [PR 29883](https://github.com/Azure/azure-sdk-for-java/pull/29883)

### 4.11.2 (2022-06-17)

#### Bugs Fixed
* Fixed a regression introduced in [PR 29152](https://github.com/Azure/azure-sdk-for-java/pull/29152) that can lead to `IllegalStateException: Latest LSN xxx must not be smaller than start LSN yyy`. - See [PR 29485](https://github.com/Azure/azure-sdk-for-java/pull/29485)

### 4.11.1 (2022-06-09)

#### Bugs Fixed
* Fixed a bug preventing usage of feedRangeFilter with change feed. - See [PR 29338](https://github.com/Azure/azure-sdk-for-java/pull/29338)

### 4.11.0 (2022-06-08)
#### Other Changes
* Updated `azure-cosmos` to version `4.31.0`.

### 4.10.1 (2022-06-01)

#### Features Added
* Added ability to disable endpoint rediscovery when using custom domain names in combination with private endpoints from a custom (on-premise) Spark environment (neither Databricks nor Synapse). - See [PR 29078](https://github.com/Azure/azure-sdk-for-java/pull/29078)
* Added a config option `spark.cosmos.serialization.dateTimeConversionMode` to allow changing date/time conversion to fall back to converting `java.sql.Date` and `java.sql.Tiemstamp` into Epoch Milliseconds like in the Cosmos DB Connector for Spark 2.4 - See [PR 29081](https://github.com/Azure/azure-sdk-for-java/pull/29081)

#### Bugs Fixed
* Fixed possible perf issue when Split results in 410 when trying to get latest LSN in Spark partitioner that could result in reprocessing change feed events (causing "hot partition2") - See [PR 29152](https://github.com/Azure/azure-sdk-for-java/pull/29152)
* Fixed a bug resulting in ChangeFeed requests using the account's default consistency model instead of falling back to eventual if `spark.cosmos.read.forceEventualConsistency` is `true` (the default config). - See [PR 29152](https://github.com/Azure/azure-sdk-for-java/pull/29152)

### 4.10.0 (2022-05-20)
#### Features Added
* Added the ability to change the target throughput control (`spark.cosmos.throughputControl.targetThroughputThreshold` or `spark.cosmos.throughputControl.targetThroughput`) when throughput control is enabled without having to also change the throughput control group name (`spark.cosmos.throughputControl.name`). - See [PR 28969](https://github.com/Azure/azure-sdk-for-java/pull/28969)

#### Bugs Fixed
* Fixed an issue with creating new Throughput control client item when `enableThroughputControlGroup` is being called multiple times with the same throughput control group. - See [PR 28905](https://github.com/Azure/azure-sdk-for-java/pull/28905)
* Fixed a possible dead-lock on static ctor for CosmosException when the runtime is using custom class loaders. - See [PR 28912](https://github.com/Azure/azure-sdk-for-java/pull/28912) and [PR 28961](https://github.com/Azure/azure-sdk-for-java/pull/28961)

#### Other Changes
* Changed 429 (Throttling) retry policy to have an upper bound for the back-off time of 5 seconds - See [PR 28764](https://github.com/Azure/azure-sdk-for-java/pull/28764)
* Improved efficiency of spark partitioning for queries with partitioning strategy `Restrictive` by skipping I/O calls to retrieve metadata (min. LSN, max. LSN, document count and total document size). - See [PR 28764](https://github.com/Azure/azure-sdk-for-java/pull/28764)
* Enabled `connectionEndpointRediscoveryEnabled` by default - See [PR 28471](https://github.com/Azure/azure-sdk-for-java/pull/28471)

### 4.9.0 (2022-04-22)

#### Bugs Fixed
* Fixed an issue preventing the `cosmos.oltp.changeFeed` DataSource to honor the `spark.cosmos.partitioning.feedRangeFilter` config parameter. - See [PR 28258](https://github.com/Azure/azure-sdk-for-java/pull/28258)
* Fixed memory leak issue related to circular reference of `CosmosDiagnostics` in `StoreResponse` and `CosmosException` - See [PR 28343](https://github.com/Azure/azure-sdk-for-java/pull/28343)

### 4.8.0 (2022-04-08)
#### Features Added
* Added an API to determine installed version of the Cosmos Spark connector (`CosmosItemsDataSource.version`/`CosmosChangeFeedDataSource.version`).See [PR 27709](https://github.com/Azure/azure-sdk-for-java/pull/27709)

#### Other Changes
* Reduced GC (Garbage Collection) pressure when executing queries returning many documents by pushing down type conversion. - See [PR 27440](https://github.com/Azure/azure-sdk-for-java/pull/27440)

### 4.7.0 (2022-03-11)
#### Features Added
* Added patch support - See [PR 27435](https://github.com/Azure/azure-sdk-for-java/pull/27435)

#### Bugs Fixed
* Fixed an issue causing errors deserializing offsets when using Cosmos change feed in Spark structured streaming jobs and upgrading from version 4.2.0 - 4.4.1 to later versions. - See [PR 27455](https://github.com/Azure/azure-sdk-for-java/pull/27455)

### 4.6.2 (2022-02-16)
#### Bugs Fixed
* Fixed an issue preventing preferred regions configured in `spark.cosmos.preferredRegionsList` from being used - See [PR 27084](https://github.com/Azure/azure-sdk-for-java/pull/27084)
* Fixed `spark.cosmos.changeFeed.itemCountPerTriggerHint` handling when using  structured streaming - there was an issue that would reduce the throughput in subsequent micro batches too aggressively. - See [PR 27101](https://github.com/Azure/azure-sdk-for-java/pull/27101)
* Fixed an issue preventing driver programs to cleanly shutting down due to active Cosmos Clients being cached. - See [PR 27137](https://github.com/Azure/azure-sdk-for-java/pull/27137)
* Fixed an issue resulting in excessive memory consumption due to unbounded prefetch of data in Spark queries (batch or structured streaming) using the `cosmos.oltp` or `cosmos.oltp.changeFeed` connector when the data gets consumed slower than retrieved from the Cosmos DB backend. - See [PR 27237](https://github.com/Azure/azure-sdk-for-java/pull/27237) and [PR 27261](https://github.com/Azure/azure-sdk-for-java/pull/27261) and [PR 27299](https://github.com/Azure/azure-sdk-for-java/pull/27299)

### 4.6.1 (2022-02-11)
#### Bugs Fixed
* Fixed a regression introduced in 4.5.0 that could result in returning incomplete query results when Executors are under high CPU load. - See [PR 26991](https://github.com/Azure/azure-sdk-for-java/pull/26991)

#### New Features
* Added support for reading from a Cosmos table without schema inference and maintaining system properties `_ts` and `_etag` when writing this data to another Cosmos container. This is helpful when moving data from one container to another without always consistent schema of the documents in the source container. - See [PR 26820](https://github.com/Azure/azure-sdk-for-java/pull/26820)
* Added support for new `spark.cosmos.write.strategy` value  `ItemOverwriteIfNotModified`, which will allow only updating documents that haven't been modified since reading them (optimistic concurrency). - See [PR 26847](https://github.com/Azure/azure-sdk-for-java/pull/26847)
* Added support for correlating queries executed via the Cosmos Spark connector with service-telemetry based on the `correlationActivityId`. - See [PR 26908](https://github.com/Azure/azure-sdk-for-java/pull/26908)
* Improved direct transport configuration in Spark to reduce number of transient failures under very high CPU/memory pressure on Spark Executors. - See [PR 27021](https://github.com/Azure/azure-sdk-for-java/pull/27021)

### 4.6.0 (2022-01-25)
#### Bugs Fixed
* Fixed an issue in schema inference logic resulting in only using the first element of an array to derive the schema. - See [PR 26568](https://github.com/Azure/azure-sdk-for-java/pull/26568)

#### New Features
* Added support for Spark 3.2. Two different maven packages will be published - but we will keep versions with further feature updates and fixes in-sync between both.
  - Spark 3.1: com.azure.cosmos.spark:azure-cosmos-spark_3-1_2-12:4.6.0
  - Spark 3.2: com.azure.cosmos.spark:azure-cosmos-spark_3-2_2-12:4.6.0

### 4.5.3 (2022-01-06)
#### Bugs Fixed
* Fixed an issue in the Java SDK that would result in NullPointerException when trying to bulk-ingest data into deleted and recreated container. - See [PR 26205](https://github.com/Azure/azure-sdk-for-java/pull/26205)

#### New Features
* Added support for writing RDDs containing ShortType or ByteType columns into Cosmos DB. - See [PR 26137](https://github.com/Azure/azure-sdk-for-java/pull/26137).

### 4.5.2 (2021-12-17)
#### Bugs Fixed
* Fixed an issue in the Java SDK that would expose request timeouts from the Gateway endpoint with StatusCode==0 instead of 408. This resulted in not retrying these transient errors in the Spark connector as expected. - See [PR 26049](https://github.com/Azure/azure-sdk-for-java/pull/26049)
* Fixed a bug where bulk responses with mixed results don't handle 400/409 for individual item operations properly. This resulted in silently ignoring these 400/409 errors - the Spark job (for example to Upsert documents) completed "successfully" (because the 400s were silently ignored) without actually upserting the documents. This fix ensures that these errors are thrown, and the Spark job fails. - See [PR 26069](https://github.com/Azure/azure-sdk-for-java/pull/26069)

### 4.5.1 (2021-12-14)
#### Bugs Fixed
* Fixed an issue that can cause hangs when bulk-ingesting data into Cosmos containers with more than 255 physical partitions - See [PR 26017](https://github.com/Azure/azure-sdk-for-java/pull/26017)
* Improved robustness of built-in retry policies for transient I/O errors when calculating Spark partitioning, do schema inference or process Catalog APIs. - See [PR 26029](https://github.com/Azure/azure-sdk-for-java/pull/26029)

### 4.5.0 (2021-12-09)
#### New Features
* Added a user defined function that can be used to calculate the "feedRange" of a partition key value. This "feedRange" can be used to determine co-located documents and to optimize query performance when the query is scoped to a single/few logical partitions. - See [PR 25889](https://github.com/Azure/azure-sdk-for-java/pull/25889).
* Extended set of provided/accepted TBLPROPERTIES within the `CosmosCatalog` to allow retrieving partition key count, provisioned throughput, partition key definition etc. natively via the `DESCRIBE TABLE EXTENDED` command. - See [PR 25853](https://github.com/Azure/azure-sdk-for-java/pull/25853).
#### Bugs Fixed
* Suppressing query plan retrieval in Spark queries (where they never would be necessary) to suppress I/O calls to the gateway and improve latency for query execution. - See [PR 25668](https://github.com/Azure/azure-sdk-for-java/pull/25668)
* Fixed a bug resulting in not being able to use views defined in the CosmosCatalog after cluster restart. Error observed in this case was `com.databricks.backend.common.rpc.DatabricksExceptions$SQLExecutionException: org.json4s.package$MappingException: unknown error`. - See [PR 25908](https://github.com/Azure/azure-sdk-for-java/pull/25908)
* Modified bulk execution settings (maxPendingActions, I/O Thread count factor and 408-retry backoff) to improve default experience in scenarios with significantly more physical partitions in Cosmos than Spark Executor Cores. - See [PR 25914](https://github.com/Azure/azure-sdk-for-java/pull/25914)
* Bulk execution improvement triggering a flush to the backend (sending the request instead of keep buffering) when the total buffered payload size exceeds the max payload size for batch instead of only relying on retry policies. - See [PR 25897](https://github.com/Azure/azure-sdk-for-java/pull/25897)
* Bulk execution improvement shortening the flush interval when the `Flux` of incoming operations signals "completion" - See [PR 25917](https://github.com/Azure/azure-sdk-for-java/pull/25917)
* Added a custom retry policy for transient failures when draining a Cosmos query or change feed query, which will automatically retry transient I/O error more aggressively than the built-in retry policy in the SDK. - See [PR 25917](https://github.com/Azure/azure-sdk-for-java/pull/25917) 

### 4.4.2 (2021-11-15)
#### Bugs Fixed
* Fixed an issue in Spark Streaming jobs processing Cosmos DB's changefeed resulting in error deserializing the offset

### 4.4.1 (2021-11-12)
#### Bugs Fixed
* Fixed an issue that can cause large delays before read or write operations start for large Cosmos DB container 

### 4.4.0 (2021-11-10)
#### New Features
* Added support for writing an RDD to Cosmos with opaque json payload to avoid risk of unwanted modification due to schema-inference/mapping - See [PR 24319](https://github.com/Azure/azure-sdk-for-java/pull/24319).
* Added an option to control how null/empty-default values are serialized to json documents in Cosmos - See [PR 24797](https://github.com/Azure/azure-sdk-for-java/pull/24797).
#### Bugs Fixed
* Fixed a regression (compared to Cosmos DB connector on Spark 2.4) that resulted in casting error `["java.lang.Integer" cannot be cast to "java.sql.Date"]` when trying to write RDD row with an Integer value if the schema's `FiledType` is Date for this column. See [PR 25156](https://github.com/Azure/azure-sdk-for-java/pull/25156) 
* Fixed issue that could result in `PoolAcquirePendingLimitException` error especially on Spark clusters which large executors (high number of cores per executor) See [PR 25047](https://github.com/Azure/azure-sdk-for-java/pull/25047)
* Improved robustness for bulk ingestion jobs to reduce the memory footprint. See [PR 25215](https://github.com/Azure/azure-sdk-for-java/pull/25215)

### 4.3.1 (2021-09-13)
#### Bugs Fixed
* Fixed issue resulting in option `spark.cosmos.read.maxItemCount` not always being honored
* Fixed issue resulting in dropping some events when using Spark Streaming when config option `spark.cosmos.changeFeed.itemCountPerTriggerHint` is configured.

### 4.3.0 (2021-08-11)
#### Configuration Changes
* Introduced a new config option `spark.cosmos.read.maxItemCount` to allow modifying the page size for query and change feed requests against the Cosmos DB backend. The previous default was 100 items per request - the new default is 1000 and can be modified via the new config option if necessary, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/23466).

#### Bugs Fixed
* Improved robustness for bulk ingestion jobs to avoid transient hangs - when the Spark job did not finish gracefully although the actual ingestion work has been finished. See [PR 22950](https://github.com/Azure/azure-sdk-for-java/pull/22950), [PR 23262](https://github.com/Azure/azure-sdk-for-java/pull/23262), [PR 23329](https://github.com/Azure/azure-sdk-for-java/pull/23329), [PR 23334](https://github.com/Azure/azure-sdk-for-java/pull/23334), [PR 23360](https://github.com/Azure/azure-sdk-for-java/pull/23360), [PR 23335](https://github.com/Azure/azure-sdk-for-java/pull/23335) and [PR 23461](https://github.com/Azure/azure-sdk-for-java/pull/23461)  

### 4.2.1-beta.1 (2021-07-15)
* Fixed Catalog api synapse integration.

### 4.2.0 (2021-06-23)
#### Configuration Changes
* Changed the default value of `spark.cosmos.read.inferSchema.forceNullableProperties` from `false` to `true` based on user feedback, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/22049).

#### Bugs Fixed
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

#### Bugs Fixed
* Fixes an issue resulting in invalid query plans when using string filter operators (StartsWith, EndsWith, Contains)

### 4.0.0 (2021-05-14)
#### Configuration Renames
* Renamed data source name `cosmos.changeFeed` to `cosmos.oltp.changeFeed`, See [PR](https://github.com/Azure/azure-sdk-for-java/pull/21184).

#### Bugs Fixed
* Fixed a bug in bulk write when using Gateway mode that could cause job failures during partition splits
* Improved the client-side throughput control algorithm to allow saturating the allowed throughput
* Added debug-level logging to help client-side throughput control investigations

### 4.0.0-beta.3 (2021-05-05)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.3` Release.
#### Configuration Renames
* Renamed data source name `cosmos.changeFeed` to `cosmos.oltp.changeFeed`, see [PR](https://github.com/Azure/azure-sdk-for-java/pull/21121).
* Configuration renamed. See [PR](https://github.com/Azure/azure-sdk-for-java/pull/21004) for list of changes. See [Configuration-Reference](https://aka.ms/azure-cosmos-spark-3-config) for more details.

#### Bugs Fixed
* Added validation for all config-settings with a name starting with "spark.cosmos."
* Fixed a bug in bulk write causing nonresponse.

### 4.0.0-beta.2 (2021-04-19)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.2` Release.

#### New Features
* The beta-2 is feature-complete now
* Spark structured streaming (micro batches) for consuming change feed
* Spark structured streaming (micro batches) support added for writes (TableCapability.STREAMING_WRITE)
* Allowing configuration of "Cosmos views" in the Spark catalog to enable direct queries against Spark catalog

#### Bugs Fixed
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
