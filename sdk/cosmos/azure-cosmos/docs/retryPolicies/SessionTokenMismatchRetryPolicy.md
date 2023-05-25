# SessionTokenMismatchRetryPolicy

## Background

- Retries primarily on errors with `404/1002` scenarios.
- Uses an exponential backoff based retry mechanism.
- Used by the `ConsistencyWriter` and `ConsistencyReader`.

## Retry frequency behavior

| Status Code / Sub-status Code | Error name                               | Retry Window | Maximum Backoff | Backoff multiplier | Initial backoff | Max retries |
|:------------------------------|------------------------------------------|--------------|-----------------|--------------------|-----------------|-------------|
| 404/1002                      | `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE` | Default: 5s  | Default: 50ms   | 2                  | Default: 5ms    | N/A         |
