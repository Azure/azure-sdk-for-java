# ResourceThrottleRetryPolicy

## Background

- Retries primarily on errors with `429` as status code.
- Uses an exponential backoff based retry mechanism.

## Retry frequency behavior

| Status Code / Sub-status Code | Retry Window                  | Backoff multiplier          | Retry delay            | Max retries                 |
|:------------------------------|-------------------------------|-----------------------------|------------------------|-----------------------------|
| 429/*                         | User specified (Default: 30s) | User specified (Default: 1) | Part of exception body | User specified (Default: 9) |
