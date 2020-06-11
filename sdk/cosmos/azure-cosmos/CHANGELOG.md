# Release History

## 4.0.1-beta.5 (Unreleased)
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
