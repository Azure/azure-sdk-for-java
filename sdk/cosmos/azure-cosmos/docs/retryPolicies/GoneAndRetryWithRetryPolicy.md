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
