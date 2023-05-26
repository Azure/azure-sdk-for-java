# GoneAndRetryWithRetryPolicy

## Background

- Retries primarily on `GoneException` and `RetryWithException` type exceptions.
- Uses an exponential backoff based retry mechanism.

## Status code specific forceRefresh behavior

| Status Code / Sub-status Code | Error name                                                     | forceRefreshAddressCache | forceCollectionRoutingMapRefresh | forceCollectionRoutingMapRefresh | forceNameCacheRefresh |
|:------------------------------|----------------------------------------------------------------|--------------------------|----------------------------------|----------------------------------|-----------------------|
| 410/21003                     | `GONE` / `COMPLETING_SPLIT_EXCEEDED_RETRY_LIMIT`               | `false`                  | `true`                           |                                  |                       |
| 410/21004                     | `GONE` / `COMPLETING_PARTITION_MIGRATION_EXCEEDED_RETRY_LIMIT` | `true`                   | `true`                           |                                  |                       |
| 410/21001                     | `GONE` / `NAME_CACHE_IS_STALE_EXCEEDED_RETRY_LIMIT`            | `false`                  |                                  |                                  | `true`                |
| 410/21002                     | `GONE` / `PARTITION_KEY_RANGE_GONE_EXCEEDED_RETRY_LIMIT`       | `true`                   |                                  |                                  |                       |

## Status code specific retry frequency for `GoneException`

| Status Code / Sub-status Code | Retry Window | Default Retry Window | Backoff multiplier | Max backoff | Max retries |
|:------------------------------|--------------|----------------------|--------------------|-------------|-------------|
| 410/*                         | N/A          | 30 s                 | 2                  | 15 s        | N/A         |
| 410/21001                     | N/A          | 30 s                 | 2                  | 15 s        | 2           |

## Status code specific retry frequency for `RetryWithException`

| Status Code / Sub-status Code | Retry Window | Default Retry Window | Initial backoff | Backoff multiplier | Max backoff | Max retries |
|:------------------------------|--------------|----------------------|-----------------|--------------------|-------------|-------------|
| 449/*                         | N/A          | 30 s                 | 10 ms           | 2                  | 1 s         | N/A         |

# ClearingSessionContainerClientRetryPolicy

# Background
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested retries before executing its own.
- Used for handling `NOT_FOUND` / `READ_SESSION_NOT_AVAILABLE` (404/1002).
- Triggered at most once after which it simply lets through the `ShouldRetryResult` instance.
- Responsible for clearing / removing the session tokens associated with some collection / container.

# InvalidPartitionExceptionRetryPolicy

## Background
- Retries on `GONE`/`NAME_CACHE_IS_STALE`(410/1000) scenarios.
- Could be used in conjunction with a `DocumentClientRetryPolicy` instance.
- Can be retried upto once after which if 410/1000 is received it stops retrying.
- In case, it is not a 410/1000, return the `ShouldRetryResult` instance as is or retry using
  an encapsulated `DocumentClientRetryPolicy` instance.

# PartitionKeyMismatchRetryPolicy

## Background
- Refreshes the `collectionCache` for `BADREQUEST`/`PARTITION_KEY_MISMATCH` scenarios.
- Triggered once and retries immediately.
- If it is not a `BADREQUEST`/`PARTITION_KEY_MISMATCH` scenario, then use `nextRetryPolicy`.

# PartitionKeyRangeGoneRetryPolicy

## Background
- Refreshes the `CollectionRoutingMap` instance associated with the collection in `GONE`/`PARTITION_KEY_RANGE_GONE` scenario.
- Triggered once retries immediately.
- If not a `GONE`/`PARTITION_KEY_RANGE_GONE` scenario, executes the `nextRetryPolicy`.

# RenameCollectionAwareClientRetryPolicy

## Background
- Retries on errors with `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE`(404/1002) scenarios.
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested retries before executing its own.
- Clears the session tokens associated with the collection, resolves the collection in the `collectionCache`.
- If `RenameCollectionAwareClientRetryPolicy` can't resolve the collection, just return `ShouldRetryResult` instance as is.
- If the collection can be resolved, then retry immediately.
- `RenameCollectionAwareClientRetryPolicy` executes collection resolution / session token clearance from the `sessionContainer` at most once.

## Where is it used?

# WebExceptionRetryPolicy

# Background
- Exponential backoff-based retry policy.
- `Exception` types covered by `WebExceptionRetryPolicy`:
    - `ConnectException`
    - `UnknownHostException`
    - `SSLHandshakeException`
    - `NoRouteToHostException`
    - `SSLPeerUnverifiedException`

# Retry frequency
| Retry Window | Initial backoff | Backoff multiplier | Max backoff | Max retries |
|--------------|-----------------|--------------------|-------------|-------------|
| 30s          | 1s              | 2                  | 1 s         | N/A         |

# ResourceThrottleRetryPolicy

## Background

- Retries primarily on errors with `429` as status code.
- Uses an exponential backoff based retry mechanism.
- Could use the `ThrottlingResourceOptions` which enables application developers to specify maximum retry attempts
  and the maximum duration within which a throttled request can be retried.

## Retry frequency

| Status Code / Sub-status Code | Retry Window                  | Backoff multiplier          | Retry delay            | Max retries                 |
|:------------------------------|-------------------------------|-----------------------------|------------------------|-----------------------------|
| 429/*                         | User specified (Default: 30s) | User specified (Default: 1) | Part of exception body | User specified (Default: 9) |

# SessionTokenMismatchRetryPolicy

## Background

- Retries on errors with `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE`(404/1002) scenarios.
- Uses an exponential backoff based retry mechanism.
- Used by the `ConsistencyWriter` and `ConsistencyReader`.

## Retry frequency behavior

| Status Code / Sub-status Code | Error name                               | Retry Window | Maximum Backoff | Backoff multiplier | Initial backoff | Max retries |
|:------------------------------|------------------------------------------|--------------|-----------------|--------------------|-----------------|-------------|
| 404/1002                      | `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE` | Default: 5s  | Default: 50ms   | 2                  | Default: 5ms    | N/A         |
