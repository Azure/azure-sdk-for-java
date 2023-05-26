-------
Index
-------
1. [GoneAndRetryWithRetryPolicy](#goneandretrywithretrypolicy)
2. [ClearingSessionContainerClientRetryPolicy](#clearingsessioncontainerclientretrypolicy)
3. [InvalidPartitionExceptionRetryPolicy](#invalidpartitionexceptionretrypolicy)
4. [PartitionKeyMismatchRetryPolicy](#partitionkeymismatchretrypolicy)
5. [PartitionKeyRangeGoneRetryPolicy](#partitionkeyrangegoneretrypolicy)
6. [RenameCollectionAwareClientRetryPolicy](#renamecollectionawareclientretrypolicy)
7. [WebExceptionRetryPolicy](#webexceptionretrypolicy)
8. [SessionTokenMismatchRetryPolicy](#sessiontokenmismatchretrypolicy)
9. [OpenConnectionsAndInitCachesRetryPolicy](#openconnectionsandinitcachesretrypolicy)

# GoneAndRetryWithRetryPolicy

## Background

- Triggered on the following exceptions:
  - `GoneException`
  - `PartitionIsMigratingException`
  - `PartitionKeyRangeIsSplittinException`
  - On certain `InvalidPartitionException` scenarios.
  - `RetryWithException`
- Uses an exponential backoff-based retry mechanism.

## Status code specific forceRefresh behavior

| Status Code / Sub-status Code | Error name                                                     | forceRefreshAddressCache | forceCollectionRoutingMapRefresh | forceCollectionRoutingMapRefresh | forceNameCacheRefresh |
|:------------------------------|----------------------------------------------------------------|--------------------------|----------------------------------|----------------------------------|-----------------------|
| 410/21003                     | `GONE` / `COMPLETING_SPLIT_EXCEEDED_RETRY_LIMIT`               | `false`                  | `true`                           |                                  |                       |
| 410/21004                     | `GONE` / `COMPLETING_PARTITION_MIGRATION_EXCEEDED_RETRY_LIMIT` | `true`                   | `true`                           |                                  |                       |
| 410/21001                     | `GONE` / `NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT`            | `false`                  |                                  |                                  | `true`                |
| 410/21002                     | `GONE` / `PARTITION_KEY_RANGE_GONE_EXCEEDED_RETRY_LIMIT`       | `true`                   |                                  |                                  |                       |

## Status code specific retry frequency for `GoneException`

| Status Code / Sub-status Code | Retry Window                                          | Default Retry Window | Backoff multiplier | Max backoff | Max retries |
|:------------------------------|-------------------------------------------------------|----------------------|--------------------|-------------|-------------|
| 410/21002;410/21003;410/21004 | 60s if account-consistency is Strong / 30s for others | 30s                  | 2                  | 15 s        | N/A         |
| 410/21001                     | 60s if account-consistency is Strong / 30s for others | 30s                  | 2                  | 15 s        | 2           |

## Status code specific retry frequency for `RetryWithException`

| Status Code / Sub-status Code | Retry Window                    | Default Retry Window | Initial backoff                            | Backoff multiplier | Max backoff | Max retries |
|:------------------------------|---------------------------------|----------------------|--------------------------------------------|--------------------|-------------|-------------|
| 449/*                         | 60s for Strong / 30s for others | 30s                  | 10ms  + Some integral salt from 0ms to 5ms | 2                  | 1 s         | N/A         |

## Usage
- Used with every request sent in the direct connectivity mode. It is plugged with a request in the `ReplicatedResourceClient` class.

# ClearingSessionContainerClientRetryPolicy

## Background
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested `shouldRetry` invocations before triggering its own.
- Triggered for `NOT_FOUND` / `READ_SESSION_NOT_AVAILABLE` (404/1002) scenarios.
- Triggered at most once after which it simply returns the `ShouldRetryResult` instance.
- When triggered it clears the session tokens associated with some collection / container.

## Usage
- Plugged in when performing a metadata request for a `DocumentCollection` instance. 

# InvalidPartitionExceptionRetryPolicy

## Background
- Triggered for `GONE` / `NAME_CACHE_IS_STALE`(410/1000) scenarios.
- When triggered, it refreshes the `collectionCache` and retries immediately.
- Can be retried upto once after which if 410/1000 is received it stops retrying.
- In case, it is a 410/1000 after a retry, return the `ShouldRetryResult.error` instance.
- In case it is not a 410/1000 as is or invoke a subsequent `shouldRetry` using on an encapsulated `DocumentClientRetryPolicy` instance.

## Usage
- Used when trying to obtain feed ranges, or for queries.
- Used by `ChangeFeedFetcher` when split handling is enabled.

# PartitionKeyMismatchRetryPolicy

## Background
- Refreshes the `collectionCache` for `BADREQUEST`/`PARTITION_KEY_MISMATCH`(400/1001) scenarios when triggered.
- Triggered once and retries immediately upto once.
- If it is not a `BADREQUEST`/`PARTITION_KEY_MISMATCH` scenario, then do a subsequent `shouldRetry` invocation on `nextRetryPolicy`.

## Usage
- Used for create / replace / upsert scenarios. 

# PartitionKeyRangeGoneRetryPolicy

## Background
- Refreshes the `CollectionRoutingMap` instance associated with the collection in `GONE`/`PARTITION_KEY_RANGE_GONE` (410/1002) scenario.
- Triggered once and retries immediately.
- If it is not a `GONE`/`PARTITION_KEY_RANGE_GONE` scenario, then do a subsequent `shouldRetry` invocation on `nextRetryPolicy`.

## Usage
- Used by `ChangeFeedFetcher` when split handling is enabled.
- Used when trying to execute a query for resource types that can be spread b/w multiple physical partitions (`Document`, `Attachment`, `Conflict`)

# RenameCollectionAwareClientRetryPolicy

## Background
- Triggered on `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE`(404/1002) scenarios.
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested `shouldRetry` invocations first.
- Clears the session tokens associated with the collection, resolves the collection in the `collectionCache`.
- If `RenameCollectionAwareClientRetryPolicy` can't resolve the collection, just return `ShouldRetryResult` instance as is.
- If the collection can be resolved, then retry immediately.
- `RenameCollectionAwareClientRetryPolicy` executes collection resolution / session token clearance from the `sessionContainer` at most once.

## Usage
- Plugged in with most operations defined in `RxDocumentClientImpl`.
- Used with the `ClientRetryPolicy`.

# WebExceptionRetryPolicy

## Background
- Exponential backoff-based retry policy.
- `Exception` types covered by `WebExceptionRetryPolicy`:
    - `ConnectException`
    - `UnknownHostException`
    - `SSLHandshakeException`
    - `NoRouteToHostException`
    - `SSLPeerUnverifiedException`

## Retry frequency
| Retry Window | Initial backoff | Backoff multiplier | Max backoff | Max retries |
|--------------|-----------------|--------------------|-------------|-------------|
| 30s          | 1s              | 2                  | 1 s         | N/A         |

## Usage
- Used when making requests in the gateway connectivity mode.
- Used by fault injection when performing address resolution requests.

# ResourceThrottleRetryPolicy

## Background

- Retries primarily on errors with `429` as status code.
- Uses an exponential backoff based retry mechanism.
- Could use the `ThrottlingResourceOptions` which enables application developers to specify maximum retry attempts
  and the maximum duration within which a throttled request can be retried.

## Retry frequency

| Status Code / Sub-status Code | Retry Window / Wait time      | Backoff multiplier          | Retry delay            | Max retries                 |
|:------------------------------|-------------------------------|-----------------------------|------------------------|-----------------------------|
| 429/*                         | User specified (Default: 30s) | User specified (Default: 1) | Part of exception body | User specified (Default: 9) |

## Usage
- Used internally by other retry policies such as:
  - `ClientRetryPolicy`
  - `FaultInjectionRuleProcessorRetryPolicy`
  - `OpenConnectionAndInitCachesRetryPolicy`
  - `BulkOperationRetryPolicy`

# SessionTokenMismatchRetryPolicy

## Background

- Retries on errors with `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE`(404/1002) scenarios.
- Uses an exponential backoff based retry mechanism.

## Retry frequency behavior

| Status Code / Sub-status Code | Error name                               | Retry Window / Wait time | Maximum Backoff | Backoff multiplier | Initial backoff | Max retries |
|:------------------------------|------------------------------------------|--------------------------|-----------------|--------------------|-----------------|-------------|
| 404/1002                      | `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE` | Default: 5s              | Default: 50ms   | 2                  | Default: 5ms    | N/A         |

## Usage
- Used by `ConsistencyReader` when the targeted consistency level is Session consistency.
- Used by `ConsistencyWriter`.

# OpenConnectionsAndInitCachesRetryPolicy

## Background
- Triggered in the case of network-related failures which has resulted in a `GATEWAY_ENDPOINT_READ_TIMEOUT`.
- Triggered upto twice for fetching addresses after which retries happen in a throttled manner using the 
encapsulated `ResourceThrottleRetryPolicy` instance.

## Usage
- Used in the open connections / connection warm up flow to fetch addresses from the gateway.