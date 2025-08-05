## Release History

### 4.74.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed
* Fixed 404/1002 for query when container recreated with same name. - [PR 45930](https://github.com/Azure/azure-sdk-for-java/pull/45930)
* Fixed Null Pointer Exception for query when container recreated with same name. - [PR 45930](https://github.com/Azure/azure-sdk-for-java/pull/45930)
* Fixed Null Pointer Exception for readMany when container recreated with same name. - [PR 45930](https://github.com/Azure/azure-sdk-for-java/pull/45930)

#### Other Changes

### 4.73.1 (2025-07-24)

#### Bugs Fixed
* Fixed an issue where child partition lease is getting created with null continuation token when change feed processor restart after split - See [PR 46075](https://github.com/Azure/azure-sdk-for-java/pull/46075)

#### Other Changes
* Added quicker cross region retry capability when a 410 `Lease Not Found` is returned by a partition in a Strong Consistency account. - See [PR 46071](https://github.com/Azure/azure-sdk-for-java/pull/46071)
* Added an option to override AAD audience scope through environment variable. See [PR 46237](https://github.com/Azure/azure-sdk-for-java/pull/46237).

### 4.73.0 (2025-07-18)

#### Bugs Fixed
* Fixed OpenTelemetry traces/spans to include the attributes defined in semantic convention for Azure Cosmos DB - [PR 45929](https://github.com/Azure/azure-sdk-for-java/pull/45929)

### 4.72.0 (2025-06-24)

#### Features Added
* Added `azure.cosmosdb.operation.request_charge` and `azure.cosmosdb.response.sub_status_code` trace attributes. - [PR 45753](https://github.com/Azure/azure-sdk-for-java/pull/45753) and [PR 45826](https://github.com/Azure/azure-sdk-for-java/pull/45826)

#### Bugs Fixed
* Fixed an issue returning incorrect `ReadConsistencyStrategy` in `CosmosDiagnosticsContext.getEffectiveReadConsistencyStrategy()` for change feed operations in some cases - [PR 45645](https://github.com/Azure/azure-sdk-for-java/pull/45645)

### 4.71.0 (2025-06-04)

#### Features Added
* Added `GetEffectiveReadConsistencyLevel` method on the `CosmosDiagnosticsContext`. - [PR 45414](https://github.com/Azure/azure-sdk-for-java/pull/45414)

#### Bugs Fixed
* Fixed an issue ignoring client-level diagnostic thresholds for point read operations in some cases - [PR 45488](https://github.com/Azure/azure-sdk-for-java/pull/45488)
* Fixed an issue ignoring read consistency strategy for queries, readMany operations and change feed in some cases - [PR 45552](https://github.com/Azure/azure-sdk-for-java/pull/45552)

#### Other Changes
* Improved throughput for benchmarks with high concurrency by avoiding blocking `UUID.randomUUID` calls in the hot path. - [PR 45495](https://github.com/Azure/azure-sdk-for-java/pull/45495)

### 4.71.0-beta.1 (2025-05-19)

#### Features Added
* Added Weighted RRF for Hybrid and Full Text Search queries - [PR 45328](https://github.com/Azure/azure-sdk-for-java/pull/45328)

### 4.70.0 (2025-05-16)

#### Bugs Fixed
* Fixed an issue where child partition is getting overridden with null continuation token if a split happens during the first request of a parent partition. - See [PR 45363](https://github.com/Azure/azure-sdk-for-java/pull/45363)

#### Other Changes
* Added a way to opt-in into Per-Partition Automatic Failover using enablement flag from the account metadata. - [PR 45317](https://github.com/Azure/azure-sdk-for-java/pull/45317)

### 4.68.1 (2025-05-16)
> [!IMPORTANT]
> We strongly recommend customers with an upgrade-intent to upgrade to version 4.70.0 or later to include this bug fix.

#### Bugs Fixed
* Fixed an issue where child partition is getting overridden with null continuation token if a split happens during the first request of a parent partition. - See [PR 45363](https://github.com/Azure/azure-sdk-for-java/pull/45363)

### 4.69.0 (2025-05-14)

#### Features Added
* Added Beta public API to enable http2. - See [PR 45294](https://github.com/Azure/azure-sdk-for-java/pull/45294)
* Added API to allow customers to wrap/extend `CosmosAsyncContainer` - [PR 43724](https://github.com/Azure/azure-sdk-for-java/pull/43724) and [PR 45087](https://github.com/Azure/azure-sdk-for-java/pull/45087) 
* Added Per-Partition Automatic Failover which enables failover for writes at per-partition level for Single-Write Multi-Region accounts. - [PR 44099](https://github.com/Azure/azure-sdk-for-java/pull/44099)
* Added Beta public API to allow defining the consistency behavior for read / query / change feed operations independent of the chosen account-level consistency level. **NOTE: This API is still in preview mode and can only be used when using DIRECT connection mode.** - See [PR 45161](https://github.com/Azure/azure-sdk-for-java/pull/45161)
* Added Weighted RRF for Hybrid and Full Text Search queries - [PR 45328](https://github.com/Azure/azure-sdk-for-java/pull/45328)

#### Bugs Fixed
* Fixed the fail back flow where not all partitions were failing back to original first preferred region for Per-Partition Circuit Breaker. - [PR 44099](https://github.com/Azure/azure-sdk-for-java/pull/44099)
* Fixed diagnostics issue where operations in Gateway mode hitting end-to-end timeout would not capture diagnostics correctly. - [PR 44099](https://github.com/Azure/azure-sdk-for-java/pull/44099)
* Enabled `excludeRegions` to be applied for `QueryPlan` calls. - [PR 45196](https://github.com/Azure/azure-sdk-for-java/pull/45196)
* Fixed the behavior to correctly perform partition-level failover through circuit breaker for operations enabled with threshold-based availability strategy. - [PR 45244](https://github.com/Azure/azure-sdk-for-java/pull/45244)
* Fixed an issue where `QueryPlan` calls are indefinitely retried in the same region when there are connectivity or response delays with / from the Gateway. - [PR 45267](https://github.com/Azure/azure-sdk-for-java/pull/45267)
* Fixed hang issue in `CosmosPagedIterable#handle` by preventing race conditions in underlying subscription of `Flux<FeedResponse>`. - [PR 45290](https://github.com/Azure/azure-sdk-for-java/pull/45290)

#### Other Changes
* Added the `vectorIndexShardKeys` to the vectorIndexSpec for QuantizedFlat and DiskANN vector search. - [PR 44007](https://github.com/Azure/azure-sdk-for-java/pull/44007)
* Added user agent suffixing if Per-Partition Automatic Failover or Per-Partition Circuit Breaker are enabled at client scope. - [PR 45197](https://github.com/Azure/azure-sdk-for-java/pull/45197)
* Enable threshold-based availability strategy for reads by default when Per-Partition Automatic Failover is also enabled. - [PR 45267](https://github.com/Azure/azure-sdk-for-java/pull/45267) 

### 4.68.0 (2025-03-20)

#### Bugs Fixed
* Fixed applications possibly not closing gracefully due to thread `partition-availability-staleness-check` not being a daemon thread. - [PR 44441](https://github.com/Azure/azure-sdk-for-java/pull/44441).
* Fixed an issue, which could result in missing to create lease documents when errors happen during the first initialization of the `ChangeFeedProcessor`. This would result in not processing change events for those partitions. - [PR 44648](https://github.com/Azure/azure-sdk-for-java/pull/44648).

### 4.67.0 (2025-02-20)

#### Other Changes
* Block `ChangeFeedProcessor` from starting by throwing an `IllegalStateException` when the lease container contains leases with the same lease prefix but different `ChangeFeedMode` - [PR 43798](https://github.com/Azure/azure-sdk-for-java/pull/43798).

### 4.66.1 (2025-02-08)

#### Bugs Fixed
* Fixed an issue in change feed processor where records are skipped and excessive requests are prefetched. - See [PR 43788](https://github.com/Azure/azure-sdk-for-java/pull/43788)
* Fixed small perf overhead due to NPE for readItem returning 404. - See [PR 44008](https://github.com/Azure/azure-sdk-for-java/pull/44008)
* Perform cross-region retry for `Document` reads when enclosing address requests hit request timeouts (408:10002). - See [PR 43937](https://github.com/Azure/azure-sdk-for-java/pull/43937)

#### Other Changes
* Added temporary internal-only option to enable thin client mode with system property COSMOS.THINCLIENT_ENABLED, setting the thin client endpoint with system property COSMOS.THINCLIENT_ENDPOINT, and default thin client endpoint with system property COSMOS.DEFAULT_THINCLIENT_ENDPOINT while the thin-client transport is still under development. This transport mode is not yet supported or ready to be used by external customers. Please don't use these configs in any production scenario yet. - [PR 43188](https://github.com/Azure/azure-sdk-for-java/pull/43188)
* Added a system property `COSMOS.ITEM_SERIALIZATION_INCLUSION_MODE` (environment variable `COSMOS_ITEM_SERIALIZATION_INCLUSION_MODE`) that allows customizing (`Always`, `NonNull`, `NonEmpty`, `NonDefault`) the JSON serialization inclusion mode when serializing items/documents. - See [PR 44035](https://github.com/Azure/azure-sdk-for-java/pull/44035) and [PR 44114](https://github.com/Azure/azure-sdk-for-java/pull/44114)

### 4.66.0 (2025-01-14)

#### Other Changes
* Added client vmId info to Rntbd health check logs - See [43079](https://github.com/Azure/azure-sdk-for-java/pull/43079)
* Added support to enable http2 for gateway mode with system property `COSMOS.HTTP2_ENABLED` and system variable `COSMOS_HTTP2_ENABLED`. - [PR 42947](https://github.com/Azure/azure-sdk-for-java/pull/42947)
* Added support to allow changing http2 max connection pool size with system property `COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE` and system variable `COSMOS_HTTP2_MAX_CONNECTION_POOL_SIZE`. - [PR 42947](https://github.com/Azure/azure-sdk-for-java/pull/42947)
* Added support to allow changing http2 max connection pool size with system property `COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE` and system variable `COSMOS_HTTP2_MIN_CONNECTION_POOL_SIZE`. - [PR 42947](https://github.com/Azure/azure-sdk-for-java/pull/42947)
* Added options to fine-tune settings for bulk operations. - [PR 43509](https://github.com/Azure/azure-sdk-for-java/pull/43509)
* Added the following metrics. - See [PR 43716](https://github.com/Azure/azure-sdk-for-java/pull/43716)
  *`cosmos.client.req.gw.bulkOpCountPerEvaluation`
  *`cosmos.client.req.gw.bulkOpRetriedCountPerEvaluation`
  *`cosmos.client.req.gw.bulkGlobalOpCount`
  *`cosmos.client.req.gw.bulkTargetMaxMicroBatchSize`
  *`cosmos.client.req.rntbd.bulkOpCountPerEvaluation`
  *`cosmos.client.req.rntbd.bulkOpRetriedCountPerEvaluation`
  *`cosmos.client.req.rntbd.bulkGlobalOpCount`
  *`cosmos.client.req.rntbd.bulkTargetMaxMicroBatchSize`

### 4.65.0 (2024-11-19)

#### Features Added
* Added support for Hybrid Search and Full text queries and new query features `HybridSearch` and `CountIf` in CosmosDB - See [42885](https://github.com/Azure/azure-sdk-for-java/pull/42885)
* Added `CosmosFullTextPolicy` in `CosmosContainerProperties` and `CosmosFullTextIndexes` in `IndexingPolicy` to support Full Text Search in Cosmos DB - See [PR 42278](https://github.com/Azure/azure-sdk-for-java/pull/42278)
* Added two new properties `quantizationSizeInBytes` and `indexingSearchListSize` to the `CosmosVectorIndexSpec` to support Partitioned DiskANN for vector search in Cosmos DB - See [PR 42333](https://github.com/Azure/azure-sdk-for-java/pull/42333)
* Added system property `COSMOS.LOAD_AZURE_VM_META_DATA` to allow customers to disable/enable loading Azure VM metadata for diagnostics - See [PR 42874](https://github.com/Azure/azure-sdk-for-java/pull/42874)

#### Bugs Fixed
* Fixed a Null Pointer Exception in `ContainerThroughputConrolGroupProperties` if defaultGroup is not set. - See [PR 42835](https://github.com/Azure/azure-sdk-for-java/pull/42835)
* Fixed a Null Pointer Exception in `RoutingMapProviderHelpers#getOverlappingRanges()` in case of Routing map being null - See [PR 42874](https://github.com/Azure/azure-sdk-for-java/pull/42874)
* Fixed an issue where `continuationToken` is not being updated in the lease document if only `304` has been observed since `changeFeedProcessor` startup - See [PR 43013](https://github.com/Azure/azure-sdk-for-java/pull/43013)

#### Other Changes
* Enable `JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS` by default for objectMapper. - See [PR 42520](https://github.com/Azure/azure-sdk-for-java/pull/42520)
* Added system property `COSMOS.ALLOW_UNQUOTED_CONTROL_CHARS` which allow customer to disable/enable `JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS`. - See [PR 42520](https://github.com/Azure/azure-sdk-for-java/pull/42520)
* Added system property `COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT` and `COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER` to allow user config error action on invalid UTF-8 bytes. - See [PR 42520](https://github.com/Azure/azure-sdk-for-java/pull/42520)
* Added system property `COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED` and system variable `COSMOS_HTTP_CONNECTION_WITHOUT_TLS_ALLOWED` to allow using http connections to connect to CosmosDB emulator. - See [PR 42972](https://github.com/Azure/azure-sdk-for-java/pull/42972)
  * **NOTE :** Please only use this config during local development or test environment, do not use this in prod env.
* Added system property `COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED` and system variable `COSMOS_EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED` to disable server certification validation to CosmosDB emulator. - See [PR 42972](https://github.com/Azure/azure-sdk-for-java/pull/42972)
    * **NOTE :** Please only use this config during local development or test environment, do not use this in prod env.
* Added system property `COSMOS.EMULATOR_HOST` and system variable `COSMOS_EMULATOR_HOST` to config emulator host name. - See [PR 42972](https://github.com/Azure/azure-sdk-for-java/pull/42972)

### 4.63.4 (2024-10-15)

#### Bugs Fixed
* Fixed an issue where a `NullPointerException` was thrown with circuit breaker enabled and partition split / merge scenarios. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)
* Fixed an issue when a `Batch` operation hitting end-to-end timeout would not capture diagnostics correctly. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)
* Fixed an issue where holding onto a `CosmosException` instance would hold a strong reference to a `RxDocumentClientImpl` instance preventing garbage collection of the `RxDocumentClientImpl` instance. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)

### 4.64.0 (2024-10-10)
> [!IMPORTANT]
> We strongly recommend our customers to use version 4.64.0 and above.
#### Features Added
* Added an API to retrieve diagnostics from the change feed processor context. - See [PR 41738](https://github.com/Azure/azure-sdk-for-java/pull/41738)
* Added support to allow `queryChangeFeed` to complete when all changes available when the query starts have been fetched. - See [PR 42160](https://github.com/Azure/azure-sdk-for-java/pull/42160)
* Added an utility API to help extract sub-range continuation tokens from existing changeFeed query continuation token. - See [PR 42156](https://github.com/Azure/azure-sdk-for-java/pull/42156)

#### Bugs Fixed
* Fixed an issue where a `NullPointerException` was thrown with circuit breaker enabled and partition split / merge scenarios. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)
* Fixed an issue when a `Batch` operation hitting end-to-end timeout would not capture diagnostics correctly. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)  
* Fixed an issue to avoid transient `IllegalArgumentException` due to duplicate json properties for the `uniqueKeyPolicy` property in `DocumentCollection`. - See [PR 41608](https://github.com/Azure/azure-sdk-for-java/pull/41608) and [PR 42244](https://github.com/Azure/azure-sdk-for-java/pull/42244)
* Fixed an issue where holding onto a `CosmosException` instance would hold a strong reference to a `RxDocumentClientImpl` instance preventing garbage collection of the `RxDocumentClientImpl` instance. - See [PR 42178](https://github.com/Azure/azure-sdk-for-java/pull/42178)

### 4.63.3 (2024-09-10)

#### Bugs Fixed
* Fixed an issue where `CosmosDiagnostics` being accumulated across all requests for `queryChangeFeed` - See [PR 41698](https://github.com/Azure/azure-sdk-for-java/pull/41698)
* Fixed an issue in the `CosmosAsyncContainer.queryChangeFeed` API that could result in hangs under certain conditions. - See [PR 41774](https://github.com/Azure/azure-sdk-for-java/pull/41774)
* Fixed an issue where cross region retries were not performed when preferred regions were not configured for `CosmosClient` / `CosmosAsyncClient` - See [PR 41653](https://github.com/Azure/azure-sdk-for-java/pull/41653)

#### Other Changes
* Changed diagnostic handler implementations to use weak references to `CosmosAsyncClient` to allow GC to earlier clean them up. - See [PR 41710](https://github.com/Azure/azure-sdk-for-java/pull/41710)

### 4.63.2 (2024-08-23)

#### Bugs Fixed
* Fixed a direct buffer memory leak due to not explicitly stopping the partition recovery flow in per-partition circuit breaker. - See [PR 41486](https://github.com/Azure/azure-sdk-for-java/pull/41486)
* Fixed an issue where client-level end-to-end timeout is not getting applied for `Batch` operations. - See [PR 41553](https://github.com/Azure/azure-sdk-for-java/pull/41553)

#### Other Changes
* Fixed an issue to avoid transient `IllegalArgumentException` due to duplicate json properties for the `uniqueKeyPolicy` property. - See [PR 41608](https://github.com/Azure/azure-sdk-for-java/pull/41608)

### 4.63.1 (2024-08-12)

#### Bugs Fixed
* Fixed an eager prefetch issue for order by queries to prevent unnecessary round trips. - See [PR 41348](https://github.com/Azure/azure-sdk-for-java/pull/41348)
* Fixed an issue to not fail fast for metadata resource resolution when faults are injected for Gateway routed operations. - See [PR 41428](https://github.com/Azure/azure-sdk-for-java/pull/41428)
* Fixed an issue to adhere with exception tolerance thresholds for consecutive read and write failures with circuit breaker. - See [PR 41428](https://github.com/Azure/azure-sdk-for-java/pull/41428)
* Fixed excessive retries bug when it has been identified that operations through a closed `CosmosClient` [or] `CosmosAsyncClient` are executed. - See [PR 41364](https://github.com/Azure/azure-sdk-for-java/pull/41364)

#### Other Changes
* Normalized `collectionLink` formatting. - See [PR 41248](https://github.com/Azure/azure-sdk-for-java/pull/41428)

### 4.63.0 (2024-07-26)

#### Features Added
* Added optional id validation to prevent documents with invalid char '/' in id property to be created. - See [PR 41108](https://github.com/Azure/azure-sdk-for-java/pull/41108)
* Added support for specifying a set of custom diagnostic correlation ids in the request options. - See [PR 40835](https://github.com/Azure/azure-sdk-for-java/pull/40835)
* Added support for client-driven partition-level failover for multi-write CosmosDB accounts. - See [PR 39265](https://github.com/Azure/azure-sdk-for-java/pull/39265)

#### Bugs Fixed
* Fixed an issue where `contactedRegions` shows the wrong region in a multi region account if no preferred regions are specified. - See [PR 41045](https://github.com/Azure/azure-sdk-for-java/pull/41045)
* Changed meters for client telemetry to always include all tags to respect this requirement from prometheus - See [PR 41213](https://github.com/Azure/azure-sdk-for-java/pull/41213)
* Fixed an issue where customer provided session token is not honored for the `readMany` operation. - See [PR 39265](https://github.com/Azure/azure-sdk-for-java/pull/39265)

#### Other Changes
* Added metrics and tracing for ReadMany operations. - See [PR 41042](https://github.com/Azure/azure-sdk-for-java/pull/41042)
* Added a `warn`-level log to capture when the `pkRangeId` in the user-passed session token and the `resolvedPartitionKeyRangeId` in the request doesn't match. - See [PR 41268](https://github.com/Azure/azure-sdk-for-java/pull/41268)

### 4.62.0 (2024-07-02)

#### Features Added
* Added support for changing some request options dynamically without the need of restarting the application. - See [PR 40061](https://github.com/Azure/azure-sdk-for-java/pull/40061)

#### Bugs Fixed
* Fixed a possible `NullPointerException` in the ctor of `FeedOperationState`. - See [PR 40714](https://github.com/Azure/azure-sdk-for-java/pull/40714)
* Changed to only disable `PartitionKeyRangeGoneRetryPolicy` when enable `disableSplitHandling` in `ChangeFeedRequestOptions`. - See [PR 40738](https://github.com/Azure/azure-sdk-for-java/pull/40738)

#### Other Changes
* Added diagnostic fields for `quorumAckedLSN` and `currentReplicaSetSize`. Changed `replicaStatusList` to include all replicas and more information. - See [PR 39844](https://github.com/Azure/azure-sdk-for-java/pull/39844)
* Ensured that exceptions thrown in custom serializers are being wrapped as a CosmosException with StatusCode 400. - See [PR 40797](https://github.com/Azure/azure-sdk-for-java/pull/40797) and [PR 40913](https://github.com/Azure/azure-sdk-for-java/pull/40913)
* Reduced number of logs emitted in the success case for cross partition queries. - See [PR 40932](https://github.com/Azure/azure-sdk-for-java/pull/40932)
* Reduced noisy logs about the value of the ` AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY` environment variable. - See [PR 40714](https://github.com/Azure/azure-sdk-for-java/pull/40714)

### 4.61.1 (2024-05-31)

#### Bugs Fixed
* Fixed an issue causing `IllegalArgumentException` when using `handleChanges` on change feed processor startup - See [PR 40420](https://github.com/Azure/azure-sdk-for-java/pull/40420)

### 4.61.0 (2024-05-24)

#### Features Added
* Added query statement conditionally in diagnostics and tracing. - See [PR 39990](https://github.com/Azure/azure-sdk-for-java/pull/39990)

#### Bugs Fixed
* Fixed a rare issue causing `StackOverflowError` when `RntbdRequestRecord` expires and tries to serialize `CosmosException` using default Jackson Object Mapper - See [PR 40272](https://github.com/Azure/azure-sdk-for-java/pull/40272)
* Fixed UserAgent encoding when the suffix contains non-ASCII characters. - See [PR 40293](https://github.com/Azure/azure-sdk-for-java/pull/40293)

#### Other Changes
* Added robustness improvement to avoid client-side parsing errors `java.lang.IllegalArgumentException: Unable to parse JSON` when Gateway returns duplicate `unqiueKeyPolicy` in IndexPolicy (invalid json) - See[PR 40306](https://github.com/Azure/azure-sdk-for-java/pull/40306)

### 4.60.0 (2024-05-19)

#### Features Added
* Added `cosmosVectorEmbeddingPolicy` in `cosmosContainerProperties` and `vectorIndexes` in `indexPolicy` to support vector search in CosmosDB - See[PR 39379](https://github.com/Azure/azure-sdk-for-java/pull/39379)
* Added support for non-streaming OrderBy query and a query feature `NonStreamingOrderBy` to support Vector Search queries. - See [PR 39897](https://github.com/Azure/azure-sdk-for-java/pull/39897/) 
* Added the capability to regionally scope session tokens used for operations scoped to a logical partition. - See [PR 38003](https://github.com/Azure/azure-sdk-for-java/pull/38003)

#### Bugs Fixed
* Ensured that `excludedRegions` is getting honored change feed operations. - See [PR 38003](https://github.com/Azure/azure-sdk-for-java/pull/38003) 

#### Other Changes
* Added change to throw `IllegalStateException` when change feed mode is switched from `AllVersionsAndDeletes` to `Incremental` and vice-versa for the same deployment unit for EPK-Range based leases. See [PR 38740](https://github.com/Azure/azure-sdk-for-java/pull/38740)

### 4.59.0 (2024-04-27)
#### Features Added
* Added public APIs `getCustomItemSerializer` and `setCustomItemSerializer` to allow customers to specify custom payload transformations or serialization settings. - See [PR 38997](https://github.com/Azure/azure-sdk-for-java/pull/38997) and [PR 39933](https://github.com/Azure/azure-sdk-for-java/pull/39933) 

#### Other Changes
* Load Blackbird or Afterburner into the ObjectMapper depending upon Java version and presence of modules in classpath. Make Afterburner and Blackbird optional maven dependencies. See - [PR 39689](https://github.com/Azure/azure-sdk-for-java/pull/39689)

### 4.53.5-hotfix (2024-04-25)

#### Bugs Fixed
* Fixed an issue in QuorumReader when quorum could not be selected even though 1 secondary and Primary are reachable and in sync. - See [PR 38832](https://github.com/Azure/azure-sdk-for-java/pull/38832)

### 4.58.0 (2024-04-16)
#### Other Changes
* Changed initial `targetBatchSize` to be capped by both `initialBatchSize` and `maxBatchSize` configured in `CosmosBulkExecutionOptions` - See[39500](https://github.com/Azure/azure-sdk-for-java/pull/39500)
* Ensured that `exceptionMessage` is populated even for non-cosmos Exceptions in `GatewayStatistics` - See [PR 39507](https://github.com/Azure/azure-sdk-for-java/pull/39507)
* Added partition key helper functions to `PartitionKeyBuilder` that are needed for `azure-spring-data-cosmos`. - See [PR 39213](https://github.com/Azure/azure-sdk-for-java/pull/39213)
* Added `cosmos.client.req.rntbd.actualItemCount` and `cosmos.client.req.gw.actualItemCount` metrics. - See [PR 39682](https://github.com/Azure/azure-sdk-for-java/pull/39682)

### 4.57.0 (2024-03-25)

#### Features Added
* Added public APIs `setMaxMicroBatchSize` and `getMaxMicroBatchSize` in `CosmosBulkExecutionOptions` - See [PR 39335](https://github.com/Azure/azure-sdk-for-java/pull/39335)

#### Bugs Fixed
* Suppressed exceptions when calling diagnostics handlers. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)
* Fixed an issue where no cross region retry for write operations due to channel acquisition timeout. - See [PR 39255](https://github.com/Azure/azure-sdk-for-java/pull/39255)
* Fixed incorrect container tag value in metrics. - See [PR 39322](https://github.com/Azure/azure-sdk-for-java/pull/39322)
* Fixed issue where CosmosDiagnosticsContext is null when diagnostics are sampled out. - See [PR 39352](https://github.com/Azure/azure-sdk-for-java/pull/39352)
#### Other Changes
* Changed logic to only call `System.exit()` in `DiagnosticsProvider` for `Error` scenario. Also added `System.err` for `Error` cases. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)
* Removed `System.exit()` calls from `ImplementationBridgeHelpers`. - See [PR 39387](https://github.com/Azure/azure-sdk-for-java/pull/39387)

### 4.53.4-hotfix (2024-03-15)

#### Other Changes
* Removed `System.exit()` calls from `ImplementationBridgeHelpers`. - See [PR 39215](https://github.com/Azure/azure-sdk-for-java/pull/39215)

### 4.48.3-hotfix (2024-03-15)

#### Bugs Fixed
* Fixed an issue where `sampleDiagnostics` is not being honored for `query. See [PR 37015](https://github.com/Azure/azure-sdk-for-java/pull/37015)
* Suppressed exceptions when calling diagnostics handlers. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)

### Other Changes
* Changed logic to only call `System.exit()` in `DiagnosticsProvider` for `Error` scenario. Also added `System.err` for `Error` cases. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)
* Removed `System.exit()` calls from `ImplementationBridgeHelpers`. - See [PR 39182](https://github.com/Azure/azure-sdk-for-java/pull/39182)

### 4.45.3-hotfix (2024-03-15)

#### Bugs Fixed
* Fixed an issue where `sampleDiagnostics` is not being honored for `query. See [PR 37015](https://github.com/Azure/azure-sdk-for-java/pull/37015)
* Suppressed exceptions when calling diagnostics handlers. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)

### Other Changes
* Changed logic to only call `System.exit()` in `DiagnosticsProvider` for `Error` scenario. Also added `System.err` for `Error` cases. - See [PR 39077](https://github.com/Azure/azure-sdk-for-java/pull/39077)
* Removed `System.exit()` calls from `ImplementationBridgeHelpers`. - See [PR 39184](https://github.com/Azure/azure-sdk-for-java/pull/39184)

### 4.53.3-hotfix (2024-03-07)

#### Bugs Fixed
* Suppressed exceptions when calling diagnostics handlers. - See [PR 39121](https://github.com/Azure/azure-sdk-for-java/pull/39121)

#### Other Changes
* Changed logic to only call `System.exit()` in `DiagnosticsProvider` for `Error` scenario. Also added `System.err` for `Error` cases. - See [PR 39121](https://github.com/Azure/azure-sdk-for-java/pull/39121)

### 4.56.0 (2024-02-20)

#### Features Added
* Added overloads for `CosmosAsyncContainer.readMany` and `CosmosContainr.readMany` accepting request options via `CosmosReadManyRequestOptions` to allow specifying excluded regions, diagnostics thresholds and end-to-end timeout etc. - See [PR 38821](https://github.com/Azure/azure-sdk-for-java/pull/38821)

#### Bugs Fixed
* Fixed an issue in QuorumReader when quorum could not be selected even though 1 secondary and Primary are reachable and in sync. - See [PR 38832](https://github.com/Azure/azure-sdk-for-java/pull/38832)

### 4.55.1 (2024-02-13)

#### Other Changes
* Limited max. number of threads possible to be used by BulkExecutor instances . - See [PR 38745](https://github.com/Azure/azure-sdk-for-java/pull/38745)

### 4.55.0 (2024-02-08)
* Added option to override the Http Connection Pool size in Gateway mode. Increasing the connection pool size beyond 1000 can be useful when the number of concurrent requests in Gateway mode is very high and you see a `reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException: Pending acquire queue has reached its maximum size of 2000` error. - See [PR 38305](https://github.com/Azure/azure-sdk-for-java/pull/38305)

#### Features Added
* Added payload size metrics for Gateway mode. - See [PR 38517](https://github.com/Azure/azure-sdk-for-java/pull/38517)

#### Other Changes
* Reduced CPU overhead slightly for workloads with high throughput of point operations - especially when diagnostics like traces or metrics are enabled. - See [PR 38232](https://github.com/Azure/azure-sdk-for-java/pull/38232)
* Changed to add `transportRequestChannelAcquisitionContext` in CosmosDiagnostics based on duration in `channelAcquisitionStarted` stage. By default, if `channelAcquisitionStarted` took more than 1s, `transportRequestChannelAcquisitionContext` will be added. - See [PR 38416](https://github.com/Azure/azure-sdk-for-java/pull/38416)
* Added information about the path when it is invalid in RxDocumentService ctor. - See [PR 38482](https://github.com/Azure/azure-sdk-for-java/pull/38482)
* Removed `CancellationException` callstack from `RntbdRequestRecord.toString`. - See [PR 38504](https://github.com/Azure/azure-sdk-for-java/pull/38504)
* Using customized `subStatusCodes` for client generated `InternalServerErrorException`. - See [PR 38518](https://github.com/Azure/azure-sdk-for-java/pull/38518)
* Added an option to opt-out of E2E timeout defined in CosmosClientBuilder for non-point operations via system property or environment variable. - See [PR 38388](https://github.com/Azure/azure-sdk-for-java/pull/38388)
* Using `ConnectionTimeout` as the `RNTBD` connection `acquisitionTimeout`. - See [PR 38695](https://github.com/Azure/azure-sdk-for-java/pull/38695)

### 4.53.2-hotfix (2024-02-04)

#### Other Changes
* Reduced CPU overhead slightly for workloads with high throughput of point operations - especially when diagnostics like traces or metrics are enabled. - See [PR 38232](https://github.com/Azure/azure-sdk-for-java/pull/38232)
* Changed to add `transportRequestChannelAcquisitionContext` in CosmosDiagnostics based on duration in `channelAcquisitionStarted` stage. By default, if `channelAcquisitionStarted` took more than 1s, `transportRequestChannelAcquisitionContext` will be added. - See [PR 38416](https://github.com/Azure/azure-sdk-for-java/pull/38416)
* Added an option to opt-out of E2E timeout defined in CosmosClientBuilder for non-point operations via system property or environment variable. - See [PR 38388](https://github.com/Azure/azure-sdk-for-java/pull/38388)

### 4.54.0 (2024-01-03)

#### Features Added
* Integrate `ThroughputControl` with ChangeFeedProcessor - See [PR 38052](https://github.com/Azure/azure-sdk-for-java/pull/38052)

#### Bugs Fixed
* Fixed issue where AAD/Entra ID related exceptions are not fully propagated to the caller when CosmosAsyncClient is created, causing ambiguity for user on the root cause of the error - See [PR 37977](https://github.com/Azure/azure-sdk-for-java/pull/37977) 
* Fixed a `NullPointerException` issue in `MetadataRequestRetryPolicy` when `locationEndpointToRoute` is not set. - See [PR 38094](https://github.com/Azure/azure-sdk-for-java/pull/38094)

#### Other Changes
* Reset `transitTimeoutCount` and `cancellationCount` in `RntbdClientChannelHealthChecker` when CPU load is above threshold. - See [PR 38157](https://github.com/Azure/azure-sdk-for-java/pull/38157)
* Perf-improvement avoiding extra-buffer copy for query and point operations - See [PR 38072](https://github.com/Azure/azure-sdk-for-java/pull/38072)

### 4.53.1 (2023-12-06)

#### Bugs Fixed
* Fixed high number of PKRangeFeed calls when using BulkExecution without SparkConnector - See [PR 37920](https://github.com/Azure/azure-sdk-for-java/pull/37920) 

#### Other Changes
* Changed to `DEBUG` log level in `WebExceptionRetryPolicy` for non-handled exception scenario and retry scenario - See [PR 37918](https://github.com/Azure/azure-sdk-for-java/pull/37918)

### 4.53.0 (2023-12-01)
#### Bugs Fixed
* Fixed a bug resulting in `CosmosDiagnosticsContext.getStatusCode()` always returning `0` for `readMany` operations. - See [PR 37394](https://github.com/Azure/azure-sdk-for-java/pull/37394)
* Fixed an issue where PartitionKeyRange request will not do cross region retry. - See [PR 37403](https://github.com/Azure/azure-sdk-for-java/pull/37403)
* Fixed an issue where Session consistency was not honored when the consistency level on the `CosmosClientBuilder.consistencyLevel` was not explicitly set to `ConsistencyLevel.SESSION` but the default account consistency level is session. If not enforcing session consistency is the intended behavior, you can set the `CosmsoClientBuilder.consistencyLevel` to `ConsistencyLevel.EVENTUAL`. - See [PR 37377](https://github.com/Azure/azure-sdk-for-java/pull/37377)
* Fixed an issue where client level `EndToEndOperationLatencyPolicyConfig.availabilityStrategy` is not being applied for `query` - See [PR 37511](https://github.com/Azure/azure-sdk-for-java/pull/37511)
* Fixed an issue where operation is not cancelled based on `CosmosEndToEndOperationLatencyPolicyConfig.endToEndOperationTimeout` when `429` happens - See [PR 37764](https://github.com/Azure/azure-sdk-for-java/pull/37764)
* Fixed an issue where `CosmosEndToEndOperationLatencyPolicyConfig.endToEndOperationTimeout` is not applied for `ReadMany` - See [PR 37764](https://github.com/Azure/azure-sdk-for-java/pull/37764)
* Fixed an issue with OFFSET and LIMIT query clause returning partial query results when used with DISTINCT - See [PR 37860](https://github.com/Azure/azure-sdk-for-java/pull/37860)

#### Other Changes
* Modified the event payload when diagnostic details are traced (vis Open telemetry traces). The diagnostics can exceed the max. attribute size of 8KB. This PR will split the diagnostics and trace them in multiple events (ordered by `SequenceNumber` attribute) to ensure the full diagnostics message is available in logged events. - See [PR 37376](https://github.com/Azure/azure-sdk-for-java/pull/37376)
* Added `sessionRetryCfg` to the diagnostic string and modified `proactiveInit` key name to `proactiveInitCfg` in the diagnostic string. - See [PR 36711](https://github.com/Azure/azure-sdk-for-java/pull/36711)
* Modified `429` retry backoff time when `retryAfter` is not being returned from server. For `429/3200`, SDK will retry immediately, for others SDK will backoff 100ms - See [PR 37764](https://github.com/Azure/azure-sdk-for-java/pull/37764)

### 4.52.0 (2023-10-24)
#### Features Added
* Added an option to configure the minimum retry duration for 404/1002 session not available. - See [PR 37143](https://github.com/Azure/azure-sdk-for-java/pull/37143) and [PR 37240](https://github.com/Azure/azure-sdk-for-java/pull/37240)

#### Bugs Fixed
* Fixed an issue where `emptyPageDiagnosticsEnabled` in `CosmosQueryRequestOptions` was being overridden. This caused empty page diagnostics to be logged (with INFO level) even when the flag was set to false - See [PR 37199](https://github.com/Azure/azure-sdk-for-java/pull/37199)
* Fixed an issue where the HttpTimeoutPolicy was not being used correctly - See [PR 37188](https://github.com/Azure/azure-sdk-for-java/pull/37188) 
* Fixed an issue where SDK mark region unavailable on http timeout - See [PR 37163](https://github.com/Azure/azure-sdk-for-java/pull/37163)
* Fixed an issue where SDK do `A, B, C, A` retry pattern for `404/1002` - See [PR 37040](https://github.com/Azure/azure-sdk-for-java/pull/37040)
* Fixed an issue where SDK do aggressive retry on `449` - See [PR 37040](https://github.com/Azure/azure-sdk-for-java/pull/37040)
* Fixed an issue where SDK skip cross region retry for server generated `410` for write operations - See [PR 37040](https://github.com/Azure/azure-sdk-for-java/pull/37040)
* Added 410/1002 handling for `ChangeFeedProcessor#getCurrentState` in **Latest Version**, **All Version and Deletes** changes modes. - See [PR 37107](https://github.com/Azure/azure-sdk-for-java/pull/37107)
    * **NOTE :** Here the fix is for a `ChangeFeedProcessor` instance built with either `handleLatestVersionChanges` or `handleAllVersionsAndDeletesChanges`.
* Fixed an issue where SDK does not do retry for `AddressRefresh` on `HttpTimeout` for write operations - See [PR 37286](https://github.com/Azure/azure-sdk-for-java/pull/37286)

### 4.51.0 (2023-09-29)

#### Features Added
* Added a preview API to `ChangeFeedProcessorBuilder` to process an additional `ChangeFeedProcessorContext` for handling all versions and deletes changes. - See [PR 36715](https://github.com/Azure/azure-sdk-for-java/pull/36715)
* Added public APIs to configure a `Supplier<CosmosExcludedRegions>` through `CosmosClientBuilder#excludedRegionSupplier` and `CosmosExcludedRegions` - a type which encapsulates a set of excluded regions. See [PR 36616](https://github.com/Azure/azure-sdk-for-java/pull/36616)

#### Bugs Fixed
* Fixed an issue with the threshold based availability strategy, which could result in missing diagnostics and unnecessarily high tail latency - See [PR 36508](https://github.com/Azure/azure-sdk-for-java/pull/36508) and [PR 36786](https://github.com/Azure/azure-sdk-for-java/pull/36786).
* Fixed an issue where `sampleDiagnostics` is not being honored for query. See [PR 37015](https://github.com/Azure/azure-sdk-for-java/pull/37015)
* Fixed the issue of `excludeRegions` not being honored for `CosmosBulkExecutionOptions`. - See[PR 36616](https://github.com/Azure/azure-sdk-for-java/pull/36616)
* Fixed an issue with missing diagnostics (metrics, logging) for `Cosmos(Async)Container.readMany` calls - See [PR 37009](https://github.com/Azure/azure-sdk-for-java/pull/37009)

### 4.50.0 (2023-09-25)

#### Features Added
* Added throughput control support for `gateway mode`. See [PR 36687](https://github.com/Azure/azure-sdk-for-java/pull/36687)
* Added public API to change the initial micro batch size in `CosmosBulkExecutionOptions`. The micro batch size is dynamically adjusted based on throttling rate. By default, it starts with a relatively large micro batch size, which can result in a short spike of throttled requests at the beginning of a bulk execution - reducing the initial micro batch size - for example to 1 - will start with smaller batch size and then dynamically increase it without causing the initial short spike of throttled requests. See [PR 36910](https://github.com/Azure/azure-sdk-for-java/pull/36910)

#### Bugs Fixed
* Disabled `CosmosEndToEndOperationLatencyPolicyConfig` feature in `ChangeFeedProcessor`. Setting `CosmosEndToEndOperationLatencyPolicyConfig` at `CosmosClient` level will not affect `ChangeFeedProcessor` requests in any way. See [PR 36775](https://github.com/Azure/azure-sdk-for-java/pull/36775)
* Fixed staleness issue of `COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT` system property - See [PR 36599](https://github.com/Azure/azure-sdk-for-java/pull/36599).
* Fixed an issue where `pageSize` from `byPage` is not always being honored. This only happens when the same `CosmosQueryRequestOptions` being used through different requests, and different pageSize being used. See [PR 36847](https://github.com/Azure/azure-sdk-for-java/pull/36847)
* Fixed an issue where build of `CosmosClient` and `CosmosAsyncClient` was getting blocked for the entire aggressive warmup duration even when all the connections have been opened already. - See [PR 36889](https://github.com/Azure/azure-sdk-for-java/pull/36889)
* Fixed `CosmosClient` connection warm up bug to open connections aggressively. - See [PR 36889](https://github.com/Azure/azure-sdk-for-java/pull/36889)

#### Other Changes
* Handling negative end-to-end timeouts provided more gracefully by throwing a `CosmsoException` (`OperationCancelledException`) instead of `IllegalArgumentException`. - See [PR 36507](https://github.com/Azure/azure-sdk-for-java/pull/36507)
* Reverted preserve ordering in bulk mode([PR 35892](https://github.com/Azure/azure-sdk-for-java/pull/35892)). See [PR 36638](https://github.com/Azure/azure-sdk-for-java/pull/36638)

### 4.45.2-hotfix (2023-09-18)
> [!IMPORTANT]
> We strongly recommend our customers to upgrade directly to at least 4.48.2 or above if they have been using the 4.45.2-hotfix version of `azure-cosmos`. Versions 4.46.0 - 4.48.1 will miss important fixes that have been backported to 4.45.2-hotfix.
#### Bugs Fixed
* Added capability to mark a region as unavailable when a request is cancelled due to end-to-end timeout and connection issues
  with the region in the direct connectivity mode. - See [PR 35586](https://github.com/Azure/azure-sdk-for-java/pull/35586)
* Fixed an issue where `ConnectionStateListener` tracked staled `Uris` which fails to mark the current `Uris` unhealthy properly - See [PR 36067](https://github.com/Azure/azure-sdk-for-java/pull/36067)
* Fixed an issue to update the last unhealthy timestamp for an `Uri` instance only when transitioning to `Unhealthy` from a different health status -  See [36083](https://github.com/Azure/azure-sdk-for-java/pull/36083)
* Improved the channel health check flow to deem a channel unhealthy when it sees consecutive cancellations. - See [PR 36225](https://github.com/Azure/azure-sdk-for-java/pull/36225)
* Optimized the replica validation flow to validate replica health with `Unknown` health status only when the replica is
  used by a container which is also part of the connection warm-up flow. - See [PR 36225](https://github.com/Azure/azure-sdk-for-java/pull/36225)
* Fixed possible `NullPointerException` issue if health-check flow kicks in before RNTBD context negotiation for a given channel - See [PR 36397](https://github.com/Azure/azure-sdk-for-java/pull/36397).

### 4.48.2 (2023-08-25)
#### Bugs Fixed
* Fixed possible `NullPointerException` issue if health-check flow kicks in before RNTBD context negotiation for a given channel - See [PR 36397](https://github.com/Azure/azure-sdk-for-java/pull/36397).

#### Other Changes
* Handling negative end-to-end timeouts provided more gracefully by throwing a `CosmosException` (`OperationCancelledException`) instead of `IllegalArgumentException`. - See [PR 36535](https://github.com/Azure/azure-sdk-for-java/pull/36535)

### 4.49.0 (2023-08-21)
#### Features Added
* Added a flag for allowing customers to preserve ordering in bulk mode. See [PR 35892](https://github.com/Azure/azure-sdk-for-java/pull/35892)
* Added a flag to bypass integrated cache when dedicated gateway is used. See [PR 35865](https://github.com/Azure/azure-sdk-for-java/pull/35865)
* Added new aggressive retry timeouts for in-region calls. See [PR 35987](https://github.com/Azure/azure-sdk-for-java/pull/35987)

#### Bugs Fixed
* Wired `proactiveInit` into the diagnostics to track warmed up containers, proactive connection regions and aggressive warm up duration - See [PR 36111](https://github.com/Azure/azure-sdk-for-java/pull/36111)
* Fixed possible `NullPointerException` issue if health-check flow kicks in before RNTBD context negotiation for a given channel - See [PR 36397](https://github.com/Azure/azure-sdk-for-java/pull/36397). 

#### Other Changes
* Added coverage for `ChangeFeedProcessor` in `Latest Version` change feed mode to read change feed from a custom start time for multi-write accounts. - See[PR 36257](https://github.com/Azure/azure-sdk-for-java/pull/36257)

### 4.48.1 (2023-08-09)
#### Bugs Fixed
* Fixed request start time in the `CosmosDiagnostics` for individual request responses - See [PR 35705](https://github.com/Azure/azure-sdk-for-java/pull/35705)
* Fixed an issue where `ConnectionStateListener` tracked staled `Uris` which fails to mark the current `Uris` unhealthy properly - See [PR 36067](https://github.com/Azure/azure-sdk-for-java/pull/36067)
* Gone exceptions that are not idempotent should not be retried because it is not known if they succeeded for sure. The handling of the exception in this case is left to the user. Fixed retrying write operations when a gone exception occurs in bulk mode. - See [PR 35838](https://github.com/Azure/azure-sdk-for-java/pull/35838)
* Fixed an issue to update the last unhealthy timestamp for an `Uri` instance only when transitioning to `Unhealthy` from a different health status -  See [36083](https://github.com/Azure/azure-sdk-for-java/pull/36083)

#### Other Changes
* Query metrics diagnostics changed to JSON format. - See [PR 35761](https://github.com/Azure/azure-sdk-for-java/pull/35761)
* Improved the channel health check flow to deem a channel unhealthy when it sees consecutive cancellations. - See [PR 36225](https://github.com/Azure/azure-sdk-for-java/pull/36225)
* Optimized the replica validation flow to validate replica health with `Unknown` health status only when the replica is 
used by a container which is also part of the connection warm-up flow. - See [PR 36225](https://github.com/Azure/azure-sdk-for-java/pull/36225)

### 4.48.0 (2023-07-18)
#### Bugs Fixed
* Fixed an issue with deserialization of `conflictResolutionTimestamp` for All versions and deletes change feed mode. - See [PR 35909](https://github.com/Azure/azure-sdk-for-java/pull/35909)
* Added capability to mark a region as unavailable when a request is cancelled due to end-to-end timeout and connection issues
  with the region in the direct connectivity mode. - See [PR 35586](https://github.com/Azure/azure-sdk-for-java/pull/35586)

#### Other Changes
* Added fault injection support for Gateway connection mode - See [PR 35378](https://github.com/Azure/azure-sdk-for-java/pull/35378)

### 4.37.2-hotfix (2023-07-17)
#### Bugs Fixed
* Fixed an issue with deserialization of `conflictResolutionTimestamp` for All versions and deletes change feed mode. - See [PR 35912](https://github.com/Azure/azure-sdk-for-java/pull/35912)

### 4.47.0 (2023-06-26)
#### Features Added
* Added the capability to specify region switch hints through `CosmosClientBuilder#setSessionRetryOptions` for optimizing retries for `READ_SESSION_NOT_AVAILABLE` errors. - See [PR 35292](https://github.com/Azure/azure-sdk-for-java/pull/35292)
* Added API to exclude regions on request options which helps avoid a regions from preferred regions for the request. - See [PR 35166](https://github.com/Azure/azure-sdk-for-java/pull/35166)
* Added API for providing an availability strategy to improve availability when end-end timeout is specified. - See [PR 35166](https://github.com/Azure/azure-sdk-for-java/pull/35166)
* Added Threshold based availability strategy. - See [PR 35166](https://github.com/Azure/azure-sdk-for-java/pull/35166)

#### Bugs Fixed
* Fixes the `readMany` API to not drop existing documents from the response in point-read scenarios when 
there are non-existent document IDs also passed through the API - See [PR 35513](https://github.com/Azure/azure-sdk-for-java/pull/35513)

### 4.46.0 (2023-06-09)
#### Features Added
* Added the capability to filter request-level metrics based on diagnostic thresholds. Request-level metrics usually are used to capture metrics per backend endpoint/replica - a high cardinality dimension. Filtering by diagnostic thresholds reduces the overhead - but also means request-level metrics can only be used for debugging purposes - not for monitoring purposes. So, it is important to use the unfiltered operation-level metrics for health monitoring in this case. - See [PR 35114](https://github.com/Azure/azure-sdk-for-java/pull/35114)
* Added optional tags/dimensions for PartitionId/ReplicaId as alternative to ServiceAddress for direct-mode (rntbd) request-level metrics. - See [PR 35164](https://github.com/Azure/azure-sdk-for-java/pull/35164)
* Added request level info including timeline and system usage to the `CosmosDiagnosticsContext`. - See [PR 35254](https://github.com/Azure/azure-sdk-for-java/pull/35254) and [PR 35405](https://github.com/Azure/azure-sdk-for-java/pull/35405)
* Added an optional dimension/tag `OperationSubStatusCode` for operation-level metrics. - See [PR 35334](https://github.com/Azure/azure-sdk-for-java/pull/35334)
* Added support for `ComputedProperty` in `CosmosContainerProperties` - See [PR 35046](https://github.com/Azure/azure-sdk-for-java/pull/35046)

#### Breaking Changes
* Renamed the JVM configuration - `COSMOS.DEFENSIVE_WARMUP_CONCURRENCY` to `COSMOS.OPEN_CONNECTIONS_CONCURRENCY` - See [PR 34859](https://github.com/Azure/azure-sdk-for-java/pull/34859)

#### Bugs Fixed
* Enabled connection warm-up to continue in a best-effort manner to other regions in case of address resolution errors for a particular region - See [PR 35323](https://github.com/Azure/azure-sdk-for-java/pull/35323)
* Fixed an issue with `ChangeFeedProcessor` to fetch all changes before delay based on configured `PollDelay`. - See [PR 35324](https://github.com/Azure/azure-sdk-for-java/pull/35324)

#### Other Changes
* Refactored `CosmosContainerProactiveInitConfigBuilder` to make use of `ContainerDirectConnectionMetadata` and to wire `DirectConnectionConfig` with
  JVM configuration - `COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT` - See [PR 34859](https://github.com/Azure/azure-sdk-for-java/pull/34859)
* Extending maximum retry delay in `SessionTokenMismatchRetryPolicy`. - See [PR 35360](https://github.com/Azure/azure-sdk-for-java/pull/35360)

### 4.45.1 (2023-05-19)
#### Bugs Fixed
* Fixed an issue where status code & sub-status code `408/20008` will always be populated in the CosmosDiagnostics in case of `RNTBD` request failures - See [PR 34999](https://github.com/Azure/azure-sdk-for-java/pull/34999)
* Fixed `readMany` API bug to enable swallowing of `404 Not Found` exceptions for 404/0 scenarios when `readMany` performs point-reads internally - See [PR 34966](https://github.com/Azure/azure-sdk-for-java/pull/34966)

### 4.45.0 (2023-05-12)
#### Features Added
* Added support for priority based throttling - See [PR 34121](https://github.com/Azure/azure-sdk-for-java/pull/34121)
* Added configurability for minimum connection pool size for all containers through a system property - `COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT` - See [PR 33983](https://github.com/Azure/azure-sdk-for-java/pull/33983).
* Added `CosmosContainerProactiveInitConfigBuilder:setAggressiveWarmupDuration(Duration aggressiveWarmupDuration)` public API to switch between aggressively opening connections
  in a blocking manner to defensively opening connections in a non-blocking manner after `aggressiveWarmupDuration` has elapsed - See [PR 33983](https://github.com/Azure/azure-sdk-for-java/pull/33983).
* Added end to end timeout policy for item operations. Requests will be cancelled if they have not finished before the configured timeout - See [PR 34554](https://github.com/Azure/azure-sdk-for-java/pull/34554).
* Added capability to sample diagnostics dynamically (without need to reinitialize the app or the Cosmos Client instance). - See [PR 34915](https://github.com/Azure/azure-sdk-for-java/pull/34915). 

#### Bugs Fixed
* Fixed `IllegalArgumentException` in changeFeedProcessor when `maxScaleCount` is configured - See [PR 34618](https://github.com/Azure/azure-sdk-for-java/pull/34618)
* Removed custom user agent suffix from client telemetry - See [PR 34866](https://github.com/Azure/azure-sdk-for-java/pull/34866)
* Fixed an issue where `userAgentSuffix` is not being used in `CosmosDiagnostics` - See [PR 34863](https://github.com/Azure/azure-sdk-for-java/pull/34863)
* Enabled proactive connection management to only reopen closed / reset connections to those endpoints used by containers which
  were part of the connection warm up flow - See [PR 34892](https://github.com/Azure/azure-sdk-for-java/pull/34892)

#### Other Changes
* Disabled initialization of client telemetry background threads if client telemetry is disabled - See [PR 34889](https://github.com/Azure/azure-sdk-for-java/pull/34889)
* Removed synchronized locking on generating random UUIDs - See [PR 34879](https://github.com/Azure/azure-sdk-for-java/pull/34879)
* Capture diagnostics for cancelled `RNTBD` requests - See [PR 34912](https://github.com/Azure/azure-sdk-for-java/pull/34912)
* Added support for threshold based speculative processing - See [PR 34686](https://github.com/Azure/azure-sdk-for-java/pull/34686)

### 4.44.0 (2023-04-21)
#### Bugs Fixed
* Fixed an issue where throughput control is not triggered properly when target throughput is being used - See [PR 34393](https://github.com/Azure/azure-sdk-for-java/pull/34393)
* Fixed an issue where `IllegalStateException` being thrown during replica validation - See [PR 34538](https://github.com/Azure/azure-sdk-for-java/pull/34538)

### 4.43.0 (2023-04-06)
#### Features Added
* Added option to enable automatic retries for write operations - See [34227](https://github.com/Azure/azure-sdk-for-java/pull/34227)
* Added option to enable automatic logging of Cosmos diagnostics for errors or requests exceeding latency threshold - See [33209](https://github.com/Azure/azure-sdk-for-java/pull/33209)
* Added support for OpenTelemetry traces following the Semantic profile for Cosmos DB - See [33209](https://github.com/Azure/azure-sdk-for-java/pull/33209)

#### Breaking Changes
* Changed the default structure of Open Telemetry events being emitted by the SDK to follow the semantic profile for Cosmos DB. Use the `COSMOS.USE_LEGACY_TRACING` system property to retrun to the previous event structure: `-DCOSMOS.USE_LEGACY_TRACING=true` - See [33209](https://github.com/Azure/azure-sdk-for-java/pull/33209)

### 4.42.0 (2023-03-17)
#### Features Added
* Added support for Move operation - See [PR 31078](https://github.com/Azure/azure-sdk-for-java/pull/31078)
* GA of `subpartition` functionality in SDK - See [32501](https://github.com/Azure/azure-sdk-for-java/pull/32501)
* Added ability for SDK to use partial partition keys for queries in subpartitioned containers - See [32501](https://github.com/Azure/azure-sdk-for-java/pull/32501)
* Enable `handleLatestVersionChanges` in ChangeFeedProcessor - See [33972](https://github.com/Azure/azure-sdk-for-java/pull/33972)
* Added Merge support. NOTE: to use Change Feed Processor with merge support, onboard to the new API `handleLatestVersionChanges()` in `ChangeFeedProcessorBuilder`.

#### Bugs Fixed
* Fixed `readMany` API to take in hierarchical partition keys - See [32501](https://github.com/Azure/azure-sdk-for-java/pull/32501)
* Fixed an issue in the Direct Transport metrics for acquired/closed channels which would be triggered when endpoint get closed/evicted due to exceeding idle timeouts. This would surface as stale metrics for these endpoints. - See [33969](https://github.com/Azure/azure-sdk-for-java/pull/33969) 

#### Other Changes
* Added fault injection support - See [PR 33329](https://github.com/Azure/azure-sdk-for-java/pull/33329).

### 4.41.0 (2023-02-17)
#### Features Added
* Added ability to configure proactive connection management via `CosmosClientBuilder.openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig)`. - See [PR 33267](https://github.com/Azure/azure-sdk-for-java/pull/33267)
* Added internal merge handling - See [PR 31428](https://github.com/Azure/azure-sdk-for-java/pull/31428). See [PR 32097](https://github.com/Azure/azure-sdk-for-java/pull/32097). See [PR 32078](https://github.com/Azure/azure-sdk-for-java/pull/32078). See [PR 32165](https://github.com/Azure/azure-sdk-for-java/pull/32165). See [32259](https://github.com/Azure/azure-sdk-for-java/pull/32259). See [32496](https://github.com/Azure/azure-sdk-for-java/pull/32496)
* Added more granular control of which Cosmos client-side metrics to emit, whether to collect histograms and percentiles (and which) and also which tags/dimensions to associate with individual metrics.  - See [PR 33436](https://github.com/Azure/azure-sdk-for-java/pull/33436)

#### Breaking Changes
* NOTE: the PR to provide more granular control over metrics - See [PR 33436](https://github.com/Azure/azure-sdk-for-java/pull/33436) - includes two technically breaking changes. We don't expect any customers to be impacted by this, but the PR description as well as information below provides some context and options on how to revert the behavior to previous version.
  * The API `CosmosClientTelemetryConfig.metricTagNames` has been marked deprecated in favor of `CosmosMicrometerMetricsOptions.defaultTagNames` or `CosmosMicrometerMeterOptions.suppressTagNames` - the `CosmosClientTelemetryConfig.metricTagNames` API can still be used as long as none of the new configuration APIs is used - but we recommend starting to switch over to the new APIs.
  * Capturing metrics - especially `Timer` and `DistributionSummary` with percentiles/histograms has some performance overhead. We got feedback that initially we were emitting some metrics with relatively high cardinality on tags with percentiles/histograms of questionable value (only useful in certain scenarios). So, we decided to disable collecting these metrics by default - but still allow them to be collected when enabled manually via the APIs described in [PR 33436](https://github.com/Azure/azure-sdk-for-java/pull/33436).   

#### Bugs Fixed
* Change feed pull API is using an incorrect key value for collection lookup, which can result in using the old collection in collection recreate scenarios. - See [PR 33178](https://github.com/Azure/azure-sdk-for-java/pull/33178)

#### Other Changes
* Give a meaningful name to the GlobalEndpointManager worker thread. - See [PR 33507](https://github.com/Azure/azure-sdk-for-java/pull/33507)
* Adding activity id in header of gateway address refresh call. - See [PR 33074](https://github.com/Azure/azure-sdk-for-java/pull/33074)
* Direct mode - `RNTBD` connection health check improvements in `RntbdClientChannelHealthChecker` to allow recovering quicker when existing connections get broken (without TCP close or reset, just timeouts because packets get dropped). - See [PR 33464](https://github.com/Azure/azure-sdk-for-java/pull/33464) and - See [PR 33566](https://github.com/Azure/azure-sdk-for-java/pull/33566)  

### 4.40.0 (2023-01-13)
#### Features Added
* Added `retryAfterInMs` to `StoreResult` in `CosmosDiagnostics` - See [PR 31219](https://github.com/Azure/azure-sdk-for-java/pull/31219)
* Added `CosmosDiagnostics` to `readMany` API - See [PR 32290](https://github.com/Azure/azure-sdk-for-java/pull/32290)

#### Bugs Fixed
* Fixed issue on noisy `CancellationException` log - See [PR 31882](https://github.com/Azure/azure-sdk-for-java/pull/31882)
* Fixed issue with `TracerProvider` constructor inadvertently disabling tracing when `isClientMetricsEnabled` is true - See [PR 32787](https://github.com/Azure/azure-sdk-for-java/pull/32787)
* Added improvement in handling for idle connection being closed unexpectedly - See [PR 32936](https://github.com/Azure/azure-sdk-for-java/pull/32936)

#### Other Changes
* Reduced log noisiness when bulk ingestion completes and sink is already terminated or cancelled. - See [PR 32601](https://github.com/Azure/azure-sdk-for-java/pull/32601)
* Optimized the `readMany` API to make use of point reads when a single item is requested for a given physical partition - See [PR 31723](https://github.com/Azure/azure-sdk-for-java/pull/31723)
* Added cross region retries for data plane, query plan and metadata requests failed with http timeouts - See [PR 32450](https://github.com/Azure/azure-sdk-for-java/pull/32450)

### 4.39.0 (2022-11-16)
#### Bugs Fixed
* Fixed a rare race condition for `query plan` cache exceeding the allowed size limit - See [PR 31859](https://github.com/Azure/azure-sdk-for-java/pull/31859)
* Added improvement in `RntbdClientChannelHealthChecker` for detecting continuous transit timeout. - See [PR 31544](https://github.com/Azure/azure-sdk-for-java/pull/31544)
* Fixed an issue in replica validation where addresses may have not sorted properly when replica validation is enabled. - See [PR 32022](https://github.com/Azure/azure-sdk-for-java/pull/32022)
* Fixed unicode char handling in Uris in Cosmos Http Client. - See [PR 32058](https://github.com/Azure/azure-sdk-for-java/pull/32058)
* Fixed an eager prefetch issue to lazily prefetch pages on a query - See [PR 32122](https://github.com/Azure/azure-sdk-for-java/pull/32122)

#### Other Changes
* Shaded `MurmurHash3` of apache `commons-codec` to enable removing of the `guava` dependency - CVE-2020-8908 - See [PR 31761](https://github.com/Azure/azure-sdk-for-java/pull/31761)
* Updated test dependency of `testng` to version 7.5 - See [PR 31761](https://github.com/Azure/azure-sdk-for-java/pull/31761)
* Reduced the logging noise level on CancellationExceptions from `RntbdReporter.reportIssue`. - See [PR 32175](https://github.com/Azure/azure-sdk-for-java/pull/32175)

### 4.38.1 (2022-10-21)
#### Other Changes
* Updated test dependency of apache `commons-text` to version 1.10.0 - CVE-2022-42889 - See [PR 31674](https://github.com/Azure/azure-sdk-for-java/pull/31674)
* Updated `jackson-databind` dependency to 2.13.4.2 - CVE-2022-42003 - See [PR 31559](https://github.com/Azure/azure-sdk-for-java/pull/31559)

### 4.38.0 (2022-10-12)
#### Features Added
* Added option to set throughput control group name on per-request level for batch and bulk operations. - See [PR 31362](https://github.com/Azure/azure-sdk-for-java/pull/31362)

### 4.37.1 (2022-10-07)
#### Bugs Fixed
* Fixed incorrect RU metric reporting in micrometer metrics. - See [PR 31307](https://github.com/Azure/azure-sdk-for-java/pull/31307)
* Enabled failover to preferred locations in the case of single-write/multi-read region enabled account for read in Gateway mode and for metadata requests in Direct mode. - More details about the [Bug: Cosmos DB Client gets stuck in timeout retry loop](https://github.com/Azure/azure-sdk-for-java/issues/31260#issue-1396454421). - See [PR 31314](https://github.com/Azure/azure-sdk-for-java/pull/31314)

#### Other Changes
* Added SslHandshakeTimeout minimum duration config - See [PR 31298](https://github.com/Azure/azure-sdk-for-java/pull/31298)

### 4.37.0 (2022-09-30)
#### Features Added
* Added new preview APIs to `ChangeFeedProcessor` for handling all versions and deletes changes - See [PR 30399](https://github.com/Azure/azure-sdk-for-java/pull/30399)
* Added option to emit client-side metrics via micrometer.io MeterRegistry. - See [PR 30065](https://github.com/Azure/azure-sdk-for-java/pull/30065)

#### Bugs Fixed
* Fixed a race condition that could result in a memory/thread leak for `BulkExecutor` instances (and their corresponding `cosmos-daemon-BulkExecutor-*` thread). - See [PR 31082](https://github.com/Azure/azure-sdk-for-java/pull/31082)

#### Other Changes
* Enable replica validation by default - See [PR 31159](https://github.com/Azure/azure-sdk-for-java/pull/31159)

### 4.36.0 (2022-09-15)
#### Other Changes
* Added system property to turn on replica validation - See [PR 29767](https://github.com/Azure/azure-sdk-for-java/pull/29767)
* Added improvement to avoid retry on same replica that previously failed with 410, 408 and  >= 500 status codes - See [PR 29767](https://github.com/Azure/azure-sdk-for-java/pull/29767)
* Improvement when `connectionEndpointRediscoveryEnabled` is enabled - See [PR 30281](https://github.com/Azure/azure-sdk-for-java/pull/30281)
* Added replica validation for Unknown status if `openConnectionsAndInitCaches` is used and replica validation is enabled - See [PR 30277](https://github.com/Azure/azure-sdk-for-java/pull/30277)

### 4.35.1 (2022-08-29)
#### Other Changes
* Added non-blocking async lazy cache to improve upgrade and scaling scenarios - See [PR 29322](https://github.com/Azure/azure-sdk-for-java/pull/29322)
* Improved performance of `StoreResponse` using array headers - See [PR 30596](https://github.com/Azure/azure-sdk-for-java/pull/30596)

### 4.35.0 (2022-08-19)
#### Other Changes
* Updated netty library version to `4.1.79.Final`.
* Updated `reactor-core` version to `3.4.21`.

### 4.34.0 (2022-08-05)
#### Features Added
* GA of `DedicatedGatewayRequestOptions` API. See [PR 30142](https://github.com/Azure/azure-sdk-for-java/pull/30142)

#### Other Changes
* Added `requestSessionToken` to `CosmosDiagnostics` - See [PR 29516](https://github.com/Azure/azure-sdk-for-java/pull/29516)
* Reverted changes of [PR 29944](https://github.com/Azure/azure-sdk-for-java/pull/29944) to avoid possible regression when customers use id with special characters and their account is on ComputeGateway already. - See [PR 30283](https://github.com/Azure/azure-sdk-for-java/pull/30283)
* Added changes for `changeFeed` APIs for handling all versions and deletes changes. See [PR 30161](https://github.com/Azure/azure-sdk-for-java/pull/30161)

### 4.33.1 (2022-07-22)
#### Bugs Fixed
* Fixed issues with "id" encoding when using special characters that should be allowed in the "id" property of a document. - See [PR 29944](https://github.com/Azure/azure-sdk-for-java/pull/29944)
* Fixed `NotFoundException` for `queryChangeFeed` with staled feed range after split - See [PR 29982](https://github.com/Azure/azure-sdk-for-java/pull/29982)
* Fixed `ForbiddenException` for azure instance metadata service requests if proxy is configured for client telemetry. - See [PR 30004](https://github.com/Azure/azure-sdk-for-java/pull/30004)
* Fixed a regression introduced in [PR 27440](https://github.com/Azure/azure-sdk-for-java/pull/27440) which causes an `IllegalArgumentException` for distinct queries when using POJO serialization. - See [PR 30025](https://github.com/Azure/azure-sdk-for-java/pull/30025)
* Fixed `IllegalArgumentException` when trying to update targetThroughput(Threshold) without process restart. - See [PR 30049](https://github.com/Azure/azure-sdk-for-java/pull/30049)

#### Other Changes
* Supported username and password to be used in `GatewayConnectionConfig.setProxy` . - See [PR 30004](https://github.com/Azure/azure-sdk-for-java/pull/30004)

### 4.33.0 (2022-07-14)
#### Other Changes
* Updated netty library version to `4.1.78.Final`.
* Updated `reactor-core` version to `3.4.19`.

### 4.32.1 (2022-06-30)

#### Bugs Fixed
* Added a fix for `CloneNotSupportedException` when trying to instantiate a `Cosmos(Async)Client` and using a MAC provider which would not support cloning. Instead, this should be handled gracefully (less ideal perf is expected - but functionally it should work.) - See [PR 29719](https://github.com/Azure/azure-sdk-for-java/pull/29719)

### 4.32.0 (2022-06-27)
#### Other Changes
* Remove requires `io.netty.transport.epoll` from `module-info` - See [PR 29509](https://github.com/Azure/azure-sdk-for-java/pull/29509)
* Converted from `durationInMicroSec` to `durationInMilliSecs` in `CosmosDiagnostics` - See [PR 29643](https://github.com/Azure/azure-sdk-for-java/pull/29643)

### 4.31.0 (2022-06-08)
#### Bugs Fixed
* Fixed Store Response headers case insensitivity. - See [PR 29268](https://github.com/Azure/azure-sdk-for-java/pull/29268)

#### Other Changes
* Add `IdleStateHandler` after Ssl handshake has completed and improvement on keeping inner exceptions for creating new channels. - See [PR 29253](https://github.com/Azure/azure-sdk-for-java/pull/29253)

### 4.30.1 (2022-06-01)
#### Other Changes
* Made CosmosPatchOperations thread-safe. Usually there is no reason to modify a CosmosPatchOperations instance concurrently form multiple threads - but making it thread-safe acts as protection in case this is done anyway - See [PR 29143](https://github.com/Azure/azure-sdk-for-java/pull/29143)
* Added system property to allow overriding proxy setting for client telemetry endpoint. - See [PR 29022](https://github.com/Azure/azure-sdk-for-java/pull/29022)
* Added additional information about the reason on Rntbd channel health check failures. - See [PR 29174](https://github.com/Azure/azure-sdk-for-java/pull/29174)

### 4.30.0 (2022-05-20)
#### Bugs Fixed
* Fixed bubbling of Errors in case of any `java.lang.Error` - See [PR 28620](https://github.com/Azure/azure-sdk-for-java/pull/28620)
* Fixed an issue with creating new Throughput control client item when `enableThroughputControlGroup` is being called multiple times with the same throughput control group. - See [PR 28905](https://github.com/Azure/azure-sdk-for-java/pull/28905)
* Fixed a possible dead-lock on static ctor for CosmosException when the runtime is using custom class loaders. - See [PR 28912](https://github.com/Azure/azure-sdk-for-java/pull/28912) and [PR 28961](https://github.com/Azure/azure-sdk-for-java/pull/28961) 

#### Other Changes
* Added `exceptionMessage` and `exceptionResponseHeaders` to `CosmosDiagnostics` in case of any exceptions - See [PR 28620](https://github.com/Azure/azure-sdk-for-java/pull/28620)
* Improved performance of `query plan` cache by using `ConcurrentHashMap` with a fixed size of 1000 - See [PR 28537](https://github.com/Azure/azure-sdk-for-java/pull/28537)
* Changed 429 (Throttling) retry policy to have an upper bound for the back-off time of 5 seconds - See [PR 28764](https://github.com/Azure/azure-sdk-for-java/pull/28764)
* Improved `openConnectionsAndInitCaches` by using rntbd context negotiation. - See [PR 28470](https://github.com/Azure/azure-sdk-for-java/pull/28470)
* Enable `connectionEndpointRediscoveryEnabled` by default - See [PR 28471](https://github.com/Azure/azure-sdk-for-java/pull/28471)

### 4.29.1 (2022-04-27)
#### Bugs Fixed
* Fixed AAD authentication for `CosmosPatchOperations` - See [PR 28537](https://github.com/Azure/azure-sdk-for-java/pull/28537)

### 4.29.0 (2022-04-22)
#### Features Added
* Added Beta API `continueOnInitError` in `ThroughputControlGroupConfigBuilder` - See [PR 27702](https://github.com/Azure/azure-sdk-for-java/pull/27702)

#### Bugs Fixed
* Added improvement for handling idle connection close event when `connectionEndpointRediscoveryEnabled` is enabled - See [PR 27242](https://github.com/Azure/azure-sdk-for-java/pull/27242)
* Fixed memory leak issue related to circular reference of `CosmosDiagnostics` in `StoreResponse` and `CosmosException` - See [PR 28343](https://github.com/Azure/azure-sdk-for-java/pull/28343)

### 4.28.1 (2022-04-08)
#### Other Changes
* Updated `jackson` dependency to 2.13.2 and `jackson-databind` dependency to 2.13.2.1 - CVE-2020-36518. - See [PR 27847](https://github.com/Azure/azure-sdk-for-java/pull/27847)

### 4.28.0 (2022-03-18)
#### Features Added
* Added the "VM Unique ID" - see [Accessing and Using Azure VM Unique ID](https://azure.microsoft.com/blog/accessing-and-using-azure-vm-unique-id/) - to the request diagnostics. This information helps to simplify investigating any network issues between an application hosted in Azure and the corresponding Cosmos DB service endpoint. - See [PR 27692](https://github.com/Azure/azure-sdk-for-java/pull/27692)
* Added overload of read api on ClientEncryptionKey with request options for cosmos encrytion project. - See [PR 27210](https://github.com/Azure/azure-sdk-for-java/pull/27210)

#### Bugs Fixed
* Added `decodeTime` in `CosmosDiagnostics` - See [PR 22808](https://github.com/Azure/azure-sdk-for-java/pull/22808)

#### Other Changes
* Reduced CPU usage for some String operations by switching to APIs that don't compile a pattern for each call. - See [PR 27654](https://github.com/Azure/azure-sdk-for-java/pull/27654)
* Reduced GC (Garbage Collection) pressure when executing queries returning many documents by pushing down type conversion. - See [PR 27440](https://github.com/Azure/azure-sdk-for-java/pull/27440)

### 4.27.0 (2022-03-10)
#### Bugs Fixed
* Fixed an issue in `CosmosPagedIterable` resulting in excessive memory consumption due to unbounded prefetch of pages when converting the `CosmosPagedIterable` into an `Iterator<FeedResponse<T>>`. - See [PR 27237](https://github.com/Azure/azure-sdk-for-java/pull/27237) and [PR 27299](https://github.com/Azure/azure-sdk-for-java/pull/27299)
* Fixed a `NullPointerException` in `CosmosDiagnostics isDiagnosticsCapturedInPagedFlux` - See [PR 27261](https://github.com/Azure/azure-sdk-for-java/pull/27261)
* Fixed an issue with allowing null values for add, set and replace operations in Patch API - See [PR 27501](https://github.com/Azure/azure-sdk-for-java/pull/27501)
* Fixed an issue with top query when top x is greater than the total number of items in the database - See [PR 27377](https://github.com/Azure/azure-sdk-for-java/pull/27377)
* Fixed synchronized lists and maps for order by query race condition - See [PR 27142](https://github.com/Azure/azure-sdk-for-java/pull/27142)

### 4.26.0 (2022-02-11)
#### Features Added
* Added support to resume a "multi order by query" from a continuation token - See [PR 26267](https://github.com/Azure/azure-sdk-for-java/pull/26267)
* Added `RNTBD - open connections` information in `ClientTelemetry`.
* Added Beta API to set custom `Reactor` scheduler to be used by the `ChangeFeedProcessor` implementation - See [PR 26750](https://github.com/Azure/azure-sdk-for-java/pull/26750)
* Added support for correlating queries executed via the Cosmos Spark connector with service-telemetry based on the `correlationActivityId` - See [PR 26908](https://github.com/Azure/azure-sdk-for-java/pull/26908)

#### Bugs Fixed
* Fixed an issue in `ChangeFeedProcessor` related to `leases` that were found expired - See [PR 26750](https://github.com/Azure/azure-sdk-for-java/pull/26750)
* Fixed an issue with `query plan` caching double initialization - See [PR 26825](https://github.com/Azure/azure-sdk-for-java/pull/26825)

#### Other Changes
* Added support for logging `CosmosDiagnostics` for empty pages through system property for cross partition query - See [PR 26869](https://github.com/Azure/azure-sdk-for-java/pull/26869)

### 4.26.0-beta.1 (2022-01-25)
#### Features Added
* Added support to resume a "multi order by query" from a continuation token - See [PR 26267](https://github.com/Azure/azure-sdk-for-java/pull/26267)

### 4.25.0 (2022-01-14)
#### Bugs Fixed
* Fixed `NullPointerException` in bulk mode for deleted/recreated containers.
* Added missing exception cause in case of `InternalServerException`.

### 4.24.0 (2021-12-21)
#### Features Added
* Added implementation for `CosmosAuthorizationTokenResolver`.
* Scoped session token per partition level for gateway call.

#### Bugs Fixed
* Fixed issue causing CosmosException with statusCode 0 to be thrown on connectivity issues for Gateway.
* Addressed potential race condition in `ChangeFeedProcessor` when check-pointing current state.

### 4.23.0 (2021-12-10)
#### Features Added
* Added `setMaxMicroBatchConcurrency` and `getMaxMicroBatchConcurrency` in `CosmosBulkExecutionOptions`.

#### Bugs Fixed
* Bulk execution improvement triggering a flush when total payload size exceeds the max payload size limit.
* Bulk execution improvement shortening the flush interval when the `Flux` of incoming operations signals completion.
* Fixed metadata cache refresh scenario on collection recreate for gateway mode.

### 4.22.0 (2021-12-03)
#### Features Added
* Added Beta API `getContactedRegionNames` in `CosmosDiagnostics`.

#### Bugs Fixed
* Fixed `IllegalStateException` for `getFeedRanges` when container recreated with same name.
* Made Cosmos spans CLIENT which will allow Azure Monitor to show HTTP calls nested under Cosmos spans.
* Fixed `ConcurrentModificationException` when getting `NotFoundException` with session consistency.

### 4.21.1 (2021-11-13)
#### Bugs Fixed
* Fixed an issue in `ChangeFeedProcessor` where processing stops in some rare cases because of a race condition can occur which prevents work to be promptly assigned to other instances.

### 4.21.0 (2021-11-12)
#### Features Added
* GA of `CosmosPatch`, `CosmosBatch` and `CosmosBulk` API.
* GA of `ChangeFeedProcessorState` API.
* Added `networkRequestTimeout` API for `DirectConnectionConfig`.

#### Bugs Fixed
* Override the default keep-alive config on linux to keep connections open and detect a broken connection faster.

#### Other Changes
* Removed deprecated `BulkExecutionOptions`.
* Removed deprecated `BulkExecutionThresholds`.
* Removed deprecated `BulkItemRequestOptions`.
* Removed deprecated `BulkItemRequestOptionsBase`.
* Removed deprecated `BulkOperations`.
* Removed deprecated `BulkPatchItemRequestOptions`.
* Removed deprecated `BulkProcessingOptions`.
* Removed deprecated `BulkProcessingThresholds`.
* Removed deprecated `TransactionalBatch`.
* Removed deprecated `TransactionalBatchItemRequestOptions`.
* Removed deprecated `TransactionalBatchItemRequestOptionsBase`.
* Removed deprecated `TransactionalBatchOperationResult`.
* Removed deprecated `TransactionalBatchPatchItemRequestOptions`.
* Removed deprecated `TransactionalBatchRequestOptions`.
* Removed deprecated `TransactionalBatchResponse`.

### 4.20.1 (2021-10-27)
#### Bugs Fixed
* Removed `AfterBurner` module for Java version 16+.
* Fixed `BadRequestException` issue when using `Distinct` with matched `orderBy` queries via `continuationToken`.

### 4.20.0 (2021-10-14)
#### Features Added
* Enabling `query plan` cache by default.

#### Bugs Fixed
* Fixed issue with bulk reads when `contentResponseOnWrite` is not explicitly enabled on the cosmos client.

### 4.19.1 (2021-09-24)
#### Features Added
* Added support to config retry count for `openConnectionsAndInitCaches`.

#### Bugs Fixed
* Fixed ReadMany Api on partition split.
* Removed full exception trace from 404 error on open telemetry.
* Fixed issue with onErrorDropped being called when using concatWith in QuorumReader.

### 4.20.0-beta.1 (2021-09-22)
#### Features Added
* Added support to config retry count for `openConnectionsAndInitCaches`.

### 4.19.0 (2021-09-09)
#### New Features
* Added support for distinct count queries.
* Added support for capturing `IndexMetrics` in `CosmosQueryRequestOptions`.

#### Bugs Fixed
* Added support to switch off IO thread for response processing.
* Fixed issue for resuming order by queries from continuation token that includes undefined/null.

#### Other Changes
* Renamed `BulkExecutionOptions` to `CosmosBulkExecutionOptions`.
* Renamed `BulkExecutionThresholds` to `CosmosBulkExecutionThresholdsState`.
* Renamed `BulkItemRequestOptions` to `CosmosBulkItemRequestOptions`.
* Renamed `BulkItemRequestOptionsBase` to `CosmosBulkItemRequestOptionsBase`.
* Renamed `BulkOperations` to `CosmosBulkOperations`.
* Renamed `BulkPatchItemRequestOptions` to `CosmosBulkPatchItemRequestOptions`.
* Renamed `TransactionalBatch` to `CosmosBatch`.
* Renamed `TransactionalBatchItemRequestOptions` to `CosmosBatchItemRequestOptions`.
* Renamed `TransactionalBatchItemRequestOptionsBase` to `CosmosBatchItemRequestOptionsBase`.
* Renamed `TransactionalBatchOperationResult` to `CosmosBatchOperationResult`.
* Renamed `TransactionalBatchPatchItemRequestOptions` to `CosmosBatchPatchItemRequestOptions`.
* Renamed `TransactionalBatchRequestOptions` to `CosmosBatchRequestOptions`.
* Renamed `TransactionalBatchResponse` to `CosmosBatchResponse`.
* Renamed `processBulkOperations` to `executeBulkOperations` API.
* Renamed `executeTransactionalBatch` to `executeCosmosBatch` API.
* Moved `CosmosBulkItemResponse.java` to `com.azure.cosmos.models` package.
* Moved `CosmosBulkOperationResponse.java` to `com.azure.cosmos.models` package.
* Moved `CosmosItemOperation.java` to `com.azure.cosmos.models` package.
* Moved `CosmosItemOperationType.java` to `com.azure.cosmos.models` package.
* Moved `CosmosPatchOperations.java` to `com.azure.cosmos.models` package.

### 4.19.0-beta.1 (2021-09-02)
#### Bugs Fixed
* Added support to switch off IO thread for response processing.

### 4.18.0 (2021-08-16)
#### New Features
* Integrated cosmos diagnostics with open telemetry tracer.

#### Bugs Fixed
* Added reactor netty timeline to `query plan` calls.
* Fixed serialization warning on `clientSideRequestDiagnostics`.
* Fixed an issue when `IdleEndpointTimeout` is set to 0 in `DirectConnectionConfig`.
* Added retry for `PrematureCloseException`.
* Fixed an issue where application hangs in bulk executor.
* Fixed an issue which preventing recovery from 410/0 after split.

### 4.18.0-beta.1 (2021-08-11)
#### Bugs Fixed
* Added `TransportRequestChannelAcquisitionContext` in `CosmosDiagnostics`.

### 4.17.0 (2021-07-08)
#### New Features
* Adjust `MicroBatchSize` dynamically based on throttling rate in `BulkExecutor`.

#### Bugs Fixed
* Fixed an issue with AAD authentication in `Strong` and `BoundedStaleness` in direct mode.
* Fixed an issue where `ChangeFeedProcessor` was resuming from zero continuation token for new partitions on partition splits.

### 4.16.0 (2021-06-11)
#### Bugs Fixed
* Fixed an issue on handling partition splits during bulk operations in Gateway Mode.
* Fixed an issue with `NumberFormatException` happening on requests on large containers.
* Fixed an issue with BackOff time in `ThroughputController`.
* Fixed an issue with `ThroughputControl` calculation.
* Improved behavior when `CosmosClientBuilder.userAgentSuffix` exceeds 64 characters. Now `userAgentSuffix` will be honored as long as total userAgent value is less than 256 characters or truncated to fit the 256 characters limited.
* Fixed issue when using client-side throughput control in combination with bulk upserts, previously resulting in unnecessarily upserting documents multiple times in some cases when getting throttled.

### 4.16.0-beta.1 (2021-05-20)
#### Bugs Fixed
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.15.0 (2021-05-12)
#### New Features
* Added `backendLatencyInMs` in `CosmosDiagnostics` for `DIRECT` connection mode.
* Added `retryContext` in `CosmosDiagnostics` for query operations.

#### Bugs Fixed
* Fixed ignored `HttpClient` decoder configuration issue.
* Fixed incorrect connection mode issue in `CosmosDiagnostics`.
* Fixed issue with handling collisions in the effective partition key.
* Fixed `CosmosQueryRequestOptions` NPE in `readAllItems` API.

### 4.15.0-beta.2 (2021-04-26)
#### Bugs Fixed
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.15.0-beta.1 (2021-04-07)
#### Bugs Fixed
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.14.0 (2021-04-06)
#### New Features
* General Availability for `readMany()` API in `CosmosAsyncContainer` and `CosmosContainer`.
* General Availability for `handle()` API in `CosmosPagedFlux` and `CosmosPagedIterable`.
* Upgraded Jackson to patch version 2.12.2.
* Exposed `getDocumentUsage` and `getDocumentCountUsage()` APIs in `FeedResponse` to retrieve document count metadata.

#### Bugs Fixed
* Allowed `CosmosPagedFlux#handle()` and `CosmosPagedIterable#handle()` API for chaining.
* Removed `AfterBurner` module usage from `CosmosException` causing the warning logs.
* Fixed issue of duplicate processing of items on the same Change Feed Processor instance.
* Return `RequestTimeoutException` on client side timeout for write operations.

### 4.13.1 (2021-03-22)
#### Bugs Fixed
* Fixed issue preventing recovery from 410 status code and 0 sub status code due to stale Gateway caches when threads in parallel scheduler are starved.
* Fixed warning caused because of afterburner module usage in `CosmosDiagnostics`.
* Query performance improvements.

### 4.13.0 (2021-03-11)
> [!IMPORTANT] 
> This release updates `reactor-core` and `reactor-netty` major versions to `2020.0.4 (Europium)` release train.
#### New Features
* Updated `reactor-core` version to 3.4.3.
* Updated `reactor-netty` version to 1.0.4.
* Added `Diagnostics` for queries.

#### Bugs Fixed
* Fixed `OrderBy` for mixed and undefined types for cross partition queries.
* Fixed `readAllItems` with resourceToken.
* Fixed issue with `resourceToken` usage in `Gateway` connection mode.
* Fixed issues with point operations with permissions in `Gateway` connection mode.

### 4.12.0 (2021-02-09)
#### New Features
* Added connection endpoint rediscovery feature to help reduce and spread-out high latency spikes.
* Added changeFeed pull model beta API.
* Added support for resuming query from a pre split continuation token after partition split.
* Optimized query execution time by caching `query plan` for single partition queries with filters and orderby.

#### Bugs Fixed
* Fixed telemetry deserialization issue.
* Skip session token for `query plan`, trigger and UDF.
* Improved session timeout 404/1002 exception handling.

### 4.11.0 (2021-01-15)
#### New Features
* Added Beta API for Patch support.
* Updated reactor-core library version to `3.3.12.RELEASE`.
* Updated reactor-netty library version to `0.9.15.RELEASE`.
* Updated netty library version to `4.1.54.Final`.

#### Bugs Fixed
* Fixed RntbdServiceEnpoint close issue.
* Improved the latency and throughput for writes when multiplexing.

### 4.10.0 (2020-12-14)
#### New Features
* Added Conflict API support.

### 4.9.0 (2020-12-11)
#### New Features
* Added Beta API for Bulk Operations.
* Added `getRegionsContacted` API in `CosmosDiagnostics`.
* Added Diagnostics for `CosmosStoredProcedureResponse`.
* Added trouble shooting guide links to `CosmosException`.

#### Bugs Fixed
* Adding automatic retries on client-side transient failures on writes while possible with still being idempotent.
* Fixed NPE on `getDiagnostics` for `CosmosStoredProcedureResponse`.
* Fixed empty `resourceAddress` in `CosmosException`.

### 4.8.0 (2020-10-27)
#### New Features
* Added `contentResponseOnWriteEnabled` feature to `CosmosItemRequestOptions`.

#### Bugs Fixed
* Fixed an issue which may affect query behaviour when resuming from a continuation token.

### 4.7.1 (2020-10-21)
#### Bugs Fixed
* Improved the 449 retry policy to force back-off on initial retry and start with shorter back-offs.

### 4.7.0 (2020-10-17)
#### New Features
* Added Beta API for transactional batches.

#### Bugs Fixed
* Fixed an error parsing query metrics on locales with ',' as floating-point delimiter.
* Stopped excessive regional fail-overs when retrieving responses with invalid json from Gateway.
* Fixed an error resulting in certain queries unnecessarily being expected in the Gateway even when using Direct transport.
* Reduced logging noise level by handling SSLException on channel closure.
* Improved efficiency of retry logic for "404 - ReadSession not available" errors.

### 4.6.0 (2020-09-30)
#### New Features
* Added new API to support AAD role-based access control in Cosmos. This is a preview feature which needs to be enabled at the account settings.
* Added handler API(beta) to `CosmosPagedFlux`/`CosmosPagedIterable` to be invoked on every response.

### 4.5.2 (2020-09-29)
#### Bugs Fixed
* Increased robustness of query execution and fetching metadata cache in case of intermittent connectivity issues.

### 4.5.1 (2020-09-25)
#### Bugs Fixed
* Added preview implementation for ChangeFeedProcessor which allows for a more detailed view of the current state.
* Fixed Multiple partition supervisor tasks running simultaneously if leaseAcquireInterval is smaller than leaseRenewInterval.
* Improved Diagnostics for Rntbd connectivity.
* Stopped onError Dropped events from leaking into default reactor hook.

### 4.5.0 (2020-09-16)
#### New Features
* Increased robustness of the Rntbd stack in case of intermittent connectivity issues.
* Improved latency in case of intermittent connectivity issues to individual backend replicas for multi-region accounts avoiding initiation of unnecessary regional fail-overs.

### 4.4.0 (2020-09-12)
#### Bugs Fixed
* Fixed RequestTimeoutException when enabling `netty-tcnative-boringssl` dependency.
* Fixed memory leak issue on `Delete` operations in `GATEWAY` mode.
* Fixed a leak in `CosmosClient` instantiation when endpoint uri is invalid.
* Improved `CPU History` diagnostics.

### 4.4.0-beta.1 (2020-08-27)
#### New Features
* Added new API to efficiently load many documents (via list of pk/id pairs or all documents for a set of pk values).
* Added new `deleteItem` API.
* Enabled query metrics by default.
#### Bugs Fixed
* Fixed NPE in `GatewayAddressCache`.
* Fixing query metric issue for zero item response.
* Improved performance (reduced CPU usage) for address parsing and Master-Key authentication.

### 4.3.2-beta.2 (2020-08-17)
#### Bugs Fixed
* No changes from previous version, releasing for compatibility issues with spring data modules.

### 4.3.2-beta.1 (2020-08-14)
#### Bugs Fixed
* Fixed issue in RntbdServiceEndpoint to avoid early closure of an unused TCP connection.

### 4.3.1 (2020-08-13)
#### Bugs Fixed
* Fixed issue with `GROUP BY` query, where it was returning only one page.
* Fixed user agent string format to comply with central SDK guidelines.
* Enhanced diagnostics information to include `query plan` diagnostics.

### 4.3.0 (2020-07-29)
#### New Features
* Updated reactor-core library version to `3.3.8.RELEASE`. 
* Updated reactor-netty library version to `0.9.10.RELEASE`. 
* Updated netty library version to `4.1.51.Final`. 
* Added new overload APIs for `upsertItem` with `partitionKey`. 
* Added open telemetry tracing support. 
#### Bugs Fixed
* Fixed issue where SSLException gets thrown in case of cancellation of requests in GATEWAY mode.
* Fixed resource throttle retry policy on stored procedures execution.
* Fixed issue where SDK hangs in log level DEBUG mode. 
* Fixed periodic spikes in latency in Direct mode. 
* Fixed high client initialization time issue. 
* Fixed http proxy bug when customizing client with direct mode and gateway mode. 
* Fixed potential NPE in users passes null options. 
* Added timeUnit to `requestLatency` in diagnostics string.
* Removed duplicate uri string from diagnostics string. 
* Fixed diagnostics string in proper JSON format for point operations.
* Fixed issue with `.single()` operator causing the reactor chain to blow up in case of Not Found exception. 

### 4.2.0 (2020-07-14)
#### New Features
* Added script logging enabled API to `CosmosStoredProcedureRequestOptions`.
* Updated `DirectConnectionConfig` default `idleEndpointTimeout` to 1h and default `connectTimeout` to 5s.
#### Bugs Fixed
* Fixed issue where `GatewayConnectionConfig` `idleConnectionTimeout` was overriding `DirectConnectionConfig` `idleConnectionTimeout`.
* Fixed `responseContinuationTokenLimitInKb` get and set APIs in `CosmosQueryRequestOptions`.
* Fixed issue in query and change feed when recreating the collection with same name.
* Fixed issue with top query throwing ClassCastException.
* Fixed issue with order by query throwing NullPointerException.
* Fixed issue in handling of cancelled requests in direct mode causing reactor `onErrorDropped` being called. 

### 4.1.0 (2020-06-25)
#### New Features
* Added support for `GROUP BY` query.
* Increased the default value of maxConnectionsPerEndpoint to 130 in DirectConnectionConfig.
* Increased the default value of maxRequestsPerConnection to 30 in DirectConnectionConfig.
#### Bugs Fixed
* Fixed issues with order by query returning duplicate results when resuming by using continuation token. 
* Fixed issues with value query returning null values for nested object.
* Fixed null pointer exception on request manager in RntbdClientChannelPool.

### 4.0.1 (2020-06-10)
#### New Features
* Renamed `QueryRequestOptions` to `CosmosQueryRequestOptions`.
* Updated `ChangeFeedProcessorBuilder` to builder pattern.
* Updated `CosmosPermissionProperties` with new container name and child resources APIs.
#### Bugs Fixed
* Fixed ConnectionPolicy `toString()` Null Pointer Exception.

### 4.0.1-beta.4 (2020-06-03)
#### New Features
* Added more samples & enriched docs to `CosmosClientBuilder`. 
* Updated `CosmosDatabase` & `CosmosContainer` APIs with throughputProperties for autoscale/autopilot support. 
* Renamed `CosmosClientException` to `CosmosException`. 
* Replaced `AccessCondition` & `AccessConditionType` by `ifMatchETag()` & `ifNoneMatchETag()` APIs. 
* Merged all `Cosmos*AsyncResponse` & `CosmosResponse` types to a single `CosmosResponse` type.
* Renamed `CosmosResponseDiagnostics` to `CosmosDiagnostics`.  
* Wrapped `FeedResponseDiagnostics` in `CosmosDiagnostics`. 
* Removed `jackson` dependency from azure-cosmos & relying on azure-core. 
* Replaced `CosmosKeyCredential` with `AzureKeyCredential` type. 
* Added `ProxyOptions` APIs to `GatewayConnectionConfig`. 
* Updated SDK to use `Instant` type instead of `OffsetDateTime`. 
* Added new enum type `OperationKind`. 
* Renamed `FeedOptions` to `QueryRequestOptions`. 
* Added `getETag()` & `getTimestamp()` APIs to `Cosmos*Properties` types. 
* Added `userAgent` information in `CosmosException` & `CosmosDiagnostics`. 
* Updated new line character in `Diagnostics` to System new line character. 
* Removed `readAll*` APIs, use query select all APIs instead.
* Added `ChangeFeedProcessor` estimate lag API.   
#### Bugs Fixed
* Fixed issue with parsing of query results in case of Value order by queries. 

### 4.0.1-beta.3 (2020-05-15)
#### New Features
* Added autoscale/autopilot throughput provisioning support to SDK.  
* Replaced `ConnectionPolicy` with new connection configs. Exposed `DirectConnectionConfig` & `GatewayConnectionConfig` APIs through `CosmosClientBuilder` for Direct & Gateway mode connection configurations.
* Moved `JsonSerializable` & `Resource` to implementation package. 
* Added `contentResponseOnWriteEnabled` API to CosmosClientBuilder which disables full response content on write operations.
* Exposed `getETag()` APIs on response types.
* Moved `CosmosAuthorizationTokenResolver` to implementation. 
* Renamed `preferredLocations` & `multipleWriteLocations` API to `preferredRegions` & `multipleWriteRegions`. 
* Updated `reactor-core` to 3.3.5.RELEASE, `reactor-netty` to 0.9.7.RELEASE & `netty` to 4.1.49.Final versions. 
* Added support for `analyticalStoreTimeToLive` in SDK.     
#### Bugs Fixed
* Fixed socket leak issues with Direct TCP client.
* Fixed `orderByQuery` with continuation token bug.

### 4.0.1-beta.2 (2020-04-21)
#### New Features
* `CosmosClientException` extends `AzureException`. 
* Removed `maxItemCount` & `requestContinuationToken` APIs from `FeedOptions` instead using `byPage()` APIs from `CosmosPagedFlux` & `CosmosPagedIterable`.
* Introduced `CosmosPermissionProperties` on public surface for `Permission` APIs.
* Removed `SqlParameterList` type & replaced with `List`
* Fixed multiple memory leaks in Direct TCP client. 
* Added support for `DISTINCT` queries. 
* Removed external dependencies on `fasterxml.uuid, guava, commons-io, commons-collection4, commons-text`.  
* Moved `CosmosPagedFlux` & `CosmosPagedIterable` to `utils` package. 
* Updated netty to 4.1.45.Final & project reactor to 3.3.3 version.
* Updated public rest contracts to `Final` classes.
* Added support for advanced Diagnostics for point operations.
#### Bugs Fixed
* `ChangeFeedProcessor` bug fix for handling partition splits & when partition not found.
* `ChangeFeedProcessor` bug fix when synchronizing lease updates across different threads.

### 4.0.1-beta.1 (2020-03-10)
#### New Features 
* Updated package to `com.azure.cosmos`
* Added `models` package for model / rest contracts
* Added `utils` package for `CosmosPagedFlux` & `CosmosPagedIterable` types. 
* Updated public APIs to use `Duration` across the SDK.
* Added all rest contracts to `models` package.
* `RetryOptions` renamed to `ThrottlingRetryOptions`.
* Added `CosmosPagedFlux` & `CosmosPagedIterable` pagination types for query APIs. 
* Added support for sharing TransportClient across multiple instances of CosmosClients using a new API in the `CosmosClientBuilder#connectionSharingAcrossClientsEnabled(true)`
* Query Optimizations by removing double serialization / deserialization. 
* Response Headers optimizations by removing unnecessary copying back and forth. 
* Optimized `ByteBuffer` serialization / deserialization by removing intermediate String instantiations.
#### Bugs Fixed
* Fixed race condition causing `ArrayIndexOutOfBound` exception in StoreReader
