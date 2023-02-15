## Cosmos DB Java SDK – Timeout configurations and Retry configurations

### Timeout config - Gateway

| OperationType      | Network Request Timeout | Connection Timeout |
| -----------------  |------------------------ |------------------- |
| QueryPlan          | 5s                      | 45s                |
| AddressRefresh     | 5s                      | 45s                |
| Other Http calls   | 60s                     | 45s                |


### Timeout config - Direct
| OperationType      | Network Request Timeout | Connection Timeout |
| -----------------  |:----------------------- |:------------------ |
| All Tcp calls      | 5s                      | 5s                 |

- `Network Request Timeout`: can be changed through `directConnectionConfig.setNetworkRequestTimeout`. Allowed value [5, 10].
- `Connection Timeout`: can be changed through `directConnectionConfig.setConnectionTimeout`


### Retry config
`Note: the following config only tracks what would happen within a region.`

| StatusCode      | SubStatusCode | FirstRetryWithDelay | InitialBackoff               | MaxBackoff  | BackoffStrategy  | MaxRetryAttempts   | MaxRetryTimeout                         | Other notes                                   |
|-----------------| ---------------|--------------------| ---------------------------- | ----------- | ---------------- | ------------------ | --------------------------------------- | --------------------------------------------- |
| 410             | 0              | NO                 | 1s                           | 15s         | Exponential      | N/A                | 60s - Strong/Bounded, 30s - Others      |                                               |
| 410             | 1007           | NO                 | 1s                           | 15s         | Exponential      | N/A                | 60s - Strong/Bounded, 30s - Others      |                                               |
| 410             | 1008           | NO                 | 1s                           | 15s         | Exponential      | N/A                | 60s - Strong/Bounded, 30s - Others      |                                               |
| 449             | 0              | YES                | 10ms + random salt [0, 5)    | 1s          | Exponential      | N/A                | 60s - Strong/Bounded, 30s - Others      |                                               |
| 429             | *              | `x-ms-retry-after` | `x-ms-retry-after`           | 5s          | N/A              | 9 (by default)     | 30s (by default)                        | Configurable through `ThrottlingRetryOption`  |
| 404             | 1002           | NO                 | 5ms                          | 50ms        | Exponential      | N/A                | 5s                                      |                                               |
| 410             | 1000           | NO                 | N/A                          | N/A         | N/A              | 1                  | N/A                                     |                                               |
| 410             | 1002           | NO                 | N/A                          | N/A         | N/A              | 1                  | N/A                                     | Only applies to `Query`, `ChangeFeed`         |
| 400             | 1001           | NO                 | N/A                          | N/A         | N/A              | 1                  | N/A                                     |                                               |