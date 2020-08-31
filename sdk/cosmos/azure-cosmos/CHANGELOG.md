# Release History

## 4.4.0-beta.2 (Unreleased)


## 4.4.0-beta.1 (2020-08-27)
### New Features
* Added new API to efficiently load many documents (via list of pk/id pairs or all documents for a set of pk values).
* Added new `deleteItem` API.
* Enabled query metrics by default.
### Key Bug Fixes
* Fixed NPE in `GatewayAddressCache`.
* Fixing query metric issue for zero item response.
* Improved performance (reduced CPU usage) for address parsing and Master-Key authentication.

## 4.3.2-beta.2 (2020-08-17)
### Key Bug Fixes
* No changes from previous version, releasing for compatibility issues with spring data modules.

## 4.3.2-beta.1 (2020-08-14)
### Key Bug Fixes
* Fixed issue in RntbdServiceEndpoint to avoid early closure of an unused TCP connection.

## 4.3.1 (2020-08-13)
### Key Bug Fixes
* Fixed issue with `GROUP BY` query, where it was returning only one page.
* Fixed user agent string format to comply with central SDK guidelines.
* Enhanced diagnostics information to include query plan diagnostics.

## 4.3.0 (2020-07-29)
### New Features
* Updated reactor-core library version to `3.3.8.RELEASE`. 
* Updated reactor-netty library version to `0.9.10.RELEASE`. 
* Updated netty library version to `4.1.51.Final`. 
* Added new overload APIs for `upsertItem` with `partitionKey`. 
* Added open telemetry tracing support. 
### Key Bug Fixes
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

## 4.2.0 (2020-07-14)
### New Features
* Added script logging enabled API to `CosmosStoredProcedureRequestOptions`.
* Updated `DirectConnectionConfig` default `idleEndpointTimeout` to 1h and default `connectTimeout` to 5s.
### Key Bug Fixes
* Fixed issue where `GatewayConnectionConfig` `idleConnectionTimeout` was overriding `DirectConnectionConfig` `idleConnectionTimeout`.
* Fixed `responseContinuationTokenLimitInKb` get and set APIs in `CosmosQueryRequestOptions`.
* Fixed issue in query and change feed when recreating the collection with same name.
* Fixed issue with top query throwing ClassCastException.
* Fixed issue with order by query throwing NullPointerException.
* Fixed issue in handling of cancelled requests in direct mode causing reactor `onErrorDropped` being called. 

## 4.1.0 (2020-06-25)
### New Features
* Added support for `GROUP BY` query.
* Increased the default value of maxConnectionsPerEndpoint to 130 in DirectConnectionConfig.
* Increased the default value of maxRequestsPerConnection to 30 in DirectConnectionConfig.
### Key Bug Fixes
* Fixed issues with order by query returning duplicate results when resuming by using continuation token. 
* Fixed issues with value query returning null values for nested object.
* Fixed null pointer exception on request manager in RntbdClientChannelPool.

## 4.0.1 (2020-06-10)
### New Features
* Renamed `QueryRequestOptions` to `CosmosQueryRequestOptions`.
* Updated `ChangeFeedProcessorBuilder` to builder pattern.
* Updated `CosmosPermissionProperties` with new container name and child resources APIs.
### Key Bug Fixes
* Fixed ConnectionPolicy `toString()` Null Pointer Exception.

## 4.0.1-beta.4 (2020-06-03)
### New Features
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
### Key Bug Fixes
* Fixed issue with parsing of query results in case of Value order by queries. 

## 4.0.1-beta.3 (2020-05-15)
### New Features
* Added autoscale/autopilot throughput provisioning support to SDK.  
* Replaced `ConnectionPolicy` with new connection configs. Exposed `DirectConnectionConfig` & `GatewayConnectionConfig` APIs through `CosmosClientBuilder` for Direct & Gateway mode connection configurations.
* Moved `JsonSerializable` & `Resource` to implementation package. 
* Added `contentResponseOnWriteEnabled` API to CosmosClientBuilder which disables full response content on write operations.
* Exposed `getETag()` APIs on response types.
* Moved `CosmosAuthorizationTokenResolver` to implementation. 
* Renamed `preferredLocations` & `multipleWriteLocations` API to `preferredRegions` & `multipleWriteRegions`. 
* Updated `reactor-core` to 3.3.5.RELEASE, `reactor-netty` to 0.9.7.RELEASE & `netty` to 4.1.49.Final versions. 
* Added support for `analyticalStoreTimeToLive` in SDK.     
### Key Bug Fixes
* Fixed socket leak issues with Direct TCP client.
* Fixed `orderByQuery` with continuation token bug.

## 4.0.1-beta.2 (2020-04-21)
### New Features
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
### Key Bug Fixes
* `ChangeFeedProcessor` bug fix for handling partition splits & when partition not found.
* `ChangeFeedProcessor` bug fix when synchronizing lease updates across different threads.

## 4.0.1-beta.1 (2020-03-10)
### New Features 
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
### Key Bug Fixes
* Fixed race condition causing `ArrayIndexOutOfBound` exception in StoreReader
