## Release History

### 4.20.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 4.19.0 (2021-09-09)
#### New Features
* Added support for distinct count queries.
* Added support for capturing `IndexMetrics` in `CosmosQueryRequestOptions`.

#### Key Bug Fixes
* Added support to switch off IO thread for response processing.
* Fixed issue for resuming order by queries from continuation token that includes undefined/null.

### 4.19.0-beta.1 (2021-09-02)
#### Key Bug Fixes
* Added support to switch off IO thread for response processing.

### 4.18.0 (2021-08-16)
> [!IMPORTANT]
> We strongly recommend our customers to use version 4.18.0 and above.
#### New Features
* Integrated cosmos diagnostics with open telemetry tracer.

#### Key Bug Fixes
* Added reactor netty timeline to query plan calls.
* Fixed serialization warning on `clientSideRequestDiagnostics`.
* Fixed an issue when `IdleEndpointTimeout` is set to 0 in `DirectConnectionConfig`.
* Added retry for `PrematureCloseException`.
* Fixed an issue where application hangs in bulk executor.
* Fixed an issue which preventing recovery from 410/0 after split.

### 4.18.0-beta.1 (2021-08-11)
#### Key Bug Fixes
* Added `TransportRequestChannelAcquisitionContext` in `CosmosDiagnostics`.

### 4.17.0 (2021-07-08)
#### New Features
* Adjust `MicroBatchSize` dynamically based on throttling rate in `BulkExecutor`.

#### Key Bug Fixes
* Fixed an issue with AAD authentication in `Strong` and `BoundedStaleness` in direct mode.
* Fixed an issue where `ChangeFeedProcessor` was resuming from zero continuation token for new partitions on partition splits.

### 4.16.0 (2021-06-11)
#### Key Bug Fixes
* Fixed an issue on handling partition splits during bulk operations in Gateway Mode.
* Fixed an issue with `NumberFormatException` happening on requests on large containers.
* Fixed an issue with BackOff time in `ThroughputController`.
* Fixed an issue with `ThroughputControl` calculation.
* Improved behavior when `CosmosClientBuilder.userAgentSuffix` exceeds 64 characters. Now `userAgentSuffix` will be honored as long as total userAgent value is less than 256 characters or truncated to fit the 256 characters limited.
* Fixed issue when using client-side throughput control in combination with bulk upserts, previously resulting in unnecessarily upserting documents multiple times in some cases when getting throttled.

### 4.16.0-beta.1 (2021-05-20)
#### Key Bug Fixes
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.15.0 (2021-05-12)
#### New Features
* Added `backendLatencyInMs` in `CosmosDiagnostics` for `DIRECT` connection mode.
* Added `retryContext` in `CosmosDiagnostics` for query operations.

#### Key Bug Fixes
* Fixed ignored `HttpClient` decoder configuration issue.
* Fixed incorrect connection mode issue in `CosmosDiagnostics`.
* Fixed issue with handling collisions in the effective partition key.
* Fixed `CosmosQueryRequestOptions` NPE in `readAllItems` API.

### 4.15.0-beta.2 (2021-04-26)
#### Key Bug Fixes
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.15.0-beta.1 (2021-04-07)
#### Key Bug Fixes
* No changes from previous version, releasing for compatibility issues with cosmos encryption modules.

### 4.14.0 (2021-04-06)
#### New Features
* General Availability for `readMany()` API in `CosmosAsyncContainer` and `CosmosContainer`.
* General Availability for `handle()` API in `CosmosPagedFlux` and `CosmosPagedIterable`.
* Upgraded Jackson to patch version 2.12.2.
* Exposed `getDocumentUsage` and `getDocumentCountUsage()` APIs in `FeedResponse` to retrieve document count metadata.

#### Key Bug Fixes
* Allowed `CosmosPagedFlux#handle()` and `CosmosPagedIterable#handle()` API for chaining.
* Removed `AfterBurner` module usage from `CosmosException` causing the warning logs.
* Fixed issue of duplicate processing of items on the same Change Feed Processor instance.
* Return `RequestTimeoutException` on client side timeout for write operations.

### 4.13.1 (2021-03-22)
#### Key Bug Fixes
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

#### Key Bug Fixes
* Fixed `OrderBy` for mixed and undefined types for cross partition queries.
* Fixed `readAllItems` with resourceToken.
* Fixed issue with `resourceToken` usage in `Gateway` connection mode.
* Fixed issues with point operations with permissions in `Gateway` connection mode.

### 4.12.0 (2021-02-09)
#### New Features
* Added connection endpoint rediscovery feature to help reduce and spread-out high latency spikes.
* Added changeFeed pull model beta API.
* Added support for resuming query from a pre split continuation token after partition split.
* Optimized query execution time by caching query plan for single partition queries with filters and orderby.

#### Key Bug Fixes
* Fixed telemetry deserialization issue.
* Skip session token for query plan, trigger and UDF.
* Improved session timeout 404/1002 exception handling.

### 4.11.0 (2021-01-15)
#### New Features
* Added Beta API for Patch support.
* Updated reactor-core library version to `3.3.12.RELEASE`.
* Updated reactor-netty library version to `0.9.15.RELEASE`.
* Updated netty library version to `4.1.54.Final`.

#### Key Bug Fixes
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

#### Key Bug Fixes
* Adding automatic retries on client-side transient failures on writes while possible with still being idempotent.
* Fixed NPE on `getDiagnostics` for `CosmosStoredProcedureResponse`.
* Fixed empty `resourceAddress` in `CosmosException`.

### 4.8.0 (2020-10-27)
#### New Features
* Added `contentResponseOnWriteEnabled` feature to `CosmosItemRequestOptions`.

#### Key Bug Fixes
* Fixed an issue which may affect query behaviour when resuming from a continuation token.

### 4.7.1 (2020-10-21)
#### Key Bug Fixes
* Improved the 449 retry policy to force back-off on initial retry and start with shorter back-offs.

### 4.7.0 (2020-10-17)
#### New Features
* Added Beta API for transactional batches.

#### Key Bug Fixes
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
#### Key Bug Fixes
* Increased robustness of query execution and fetching metadata cache in case of intermittent connectivity issues.

### 4.5.1 (2020-09-25)
#### Key Bug Fixes
* Added preview implementation for ChangeFeedProcessor which allows for a more detailed view of the current state.
* Fixed Multiple partition supervisor tasks running simultaneously if leaseAcquireInterval is smaller than leaseRenewInterval.
* Improved Diagnostics for Rntbd connectivity.
* Stopped onError Dropped events from leaking into default reactor hook.

### 4.5.0 (2020-09-16)
#### New Features
* Increased robustness of the Rntbd stack in case of intermittent connectivity issues.
* Improved latency in case of intermittent connectivity issues to individual backend replicas for multi-region accounts avoiding initiation of unnecessary regional fail-overs.

### 4.4.0 (2020-09-12)
#### Key Bug Fixes
* Fixed RequestTimeoutException when enabling `netty-tcnative-boringssl` dependency.
* Fixed memory leak issue on `Delete` operations in `GATEWAY` mode.
* Fixed a leak in `CosmosClient` instantiation when endpoint uri is invalid.
* Improved `CPU History` diagnostics.

### 4.4.0-beta.1 (2020-08-27)
#### New Features
* Added new API to efficiently load many documents (via list of pk/id pairs or all documents for a set of pk values).
* Added new `deleteItem` API.
* Enabled query metrics by default.
#### Key Bug Fixes
* Fixed NPE in `GatewayAddressCache`.
* Fixing query metric issue for zero item response.
* Improved performance (reduced CPU usage) for address parsing and Master-Key authentication.

### 4.3.2-beta.2 (2020-08-17)
#### Key Bug Fixes
* No changes from previous version, releasing for compatibility issues with spring data modules.

### 4.3.2-beta.1 (2020-08-14)
#### Key Bug Fixes
* Fixed issue in RntbdServiceEndpoint to avoid early closure of an unused TCP connection.

### 4.3.1 (2020-08-13)
#### Key Bug Fixes
* Fixed issue with `GROUP BY` query, where it was returning only one page.
* Fixed user agent string format to comply with central SDK guidelines.
* Enhanced diagnostics information to include query plan diagnostics.

### 4.3.0 (2020-07-29)
#### New Features
* Updated reactor-core library version to `3.3.8.RELEASE`. 
* Updated reactor-netty library version to `0.9.10.RELEASE`. 
* Updated netty library version to `4.1.51.Final`. 
* Added new overload APIs for `upsertItem` with `partitionKey`. 
* Added open telemetry tracing support. 
#### Key Bug Fixes
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
#### Key Bug Fixes
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
#### Key Bug Fixes
* Fixed issues with order by query returning duplicate results when resuming by using continuation token. 
* Fixed issues with value query returning null values for nested object.
* Fixed null pointer exception on request manager in RntbdClientChannelPool.

### 4.0.1 (2020-06-10)
#### New Features
* Renamed `QueryRequestOptions` to `CosmosQueryRequestOptions`.
* Updated `ChangeFeedProcessorBuilder` to builder pattern.
* Updated `CosmosPermissionProperties` with new container name and child resources APIs.
#### Key Bug Fixes
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
#### Key Bug Fixes
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
#### Key Bug Fixes
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
#### Key Bug Fixes
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
#### Key Bug Fixes
* Fixed race condition causing `ArrayIndexOutOfBound` exception in StoreReader
