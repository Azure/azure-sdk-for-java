# InvalidPartitionExceptionRetryPolicy

## Background
- Retries on `GONE`/`NAME_CACHE_IS_STALE`(410/1000) scenarios.
- Could be used in conjunction with a `DocumentClientRetryPolicy` instance.
- Can be retried upto once after which if 410/1000 is received it stops retrying.
- In case, it is not a 410/1000, return the `ShouldRetryResult` instance as is or retry using 
an encapsulated `DocumentClientRetryPolicy` instance.