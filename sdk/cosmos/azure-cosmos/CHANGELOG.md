## Release History

### 4.34.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed
* Fixed issues with "id" encoding when using special characters that should be allowed in the "id" property of a document. [#29944](https://github.com/Azure/azure-sdk-for-java/pull/29944)
* Fixed `NotFoundException` for `queryChangeFeed` with staled feed range after split - See [PR 29982](https://github.com/Azure/azure-sdk-for-java/pull/29982)

#### Other Changes

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
> [!IMPORTANT]
> We strongly recommend our customers to use version 4.31.0 and above.
#### Bugs Fixed
* Fixed Store Response headers case insensitivity. - See [PR 29268](https://github.com/Azure/azure-sdk-for-java/pull/29268)

#### Other Changes
* Add `IdleStateHandler` after Ssl handshake has completed and improvement on keeping inner exceptions for creating new channels. 

### 4.30.1 (2022-06-01)
#### Other Changes
* Made CosmosPatchOperations thread-safe. Usually there is no reason to modify a CosmosPatchOperations instance concurrently form multiple threads - but making it thread-safe acts as protection in case this is done anyway - See [PR 29143](https://github.com/Azure/azure-sdk-for-java/pull/29143)
* Added system property to allow overriding proxy setting for client telemetry endpoint. - See [PR 29022](https://github.com/Azure/azure-sdk-for-java/pull/29022)
* Added additional information about the reason on Rntbd channel health check failures. - See [PR 29253](https://github.com/Azure/azure-sdk-for-java/pull/29253)

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
