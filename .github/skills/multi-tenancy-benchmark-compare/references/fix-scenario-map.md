# Fix to Scenario Mapping

| Fix | Description | Scenario | Run ID | Key Metric | Pass Criteria |
|---|---|---|---|---|---|
| A1 | ~~Fix `ClientTelemetry.close()`~~ ✅ **FIXED** | CHURN (S4) | B9 | Threads + heap after close | Threads ≤ baseline+2, heap ≤ 1.1× |
| A2 | ~~Call `clientTelemetry.close()` from `RxDocumentClientImpl`~~ ✅ **FIXED** | CHURN (S4) | B9 | Same as A1 | Same as A1 |
| A3 | Close `ThinClientStoreModel` | CHURN (S4, HTTP/2) | B9 | Threads + connections | No growth over 100 cycles |
| A7 | LRU eviction for `queryPlanCache` (currently has 5K full-clear cap) | CACHE_GROWTH (S3) | B8 | Cache size plateau, heap | Cache ≤ max, no sawtooth pattern |
| A11 | Reduce default `maxConnections` | SCALING + POOL_PRESSURE | B1, B10 | FD count, connections | FDs <50%, no latency regression >10% |
| A15 | HTTP/2 default for Gateway | SCALING (S2) | B4 vs B6 | TCP connections, latency | Connections <5K, P99 not worse |
| A22 | OkHttp investigation | SCALING (S7) | B12 | Threads, direct memory | Exploration only |

## Source Files

| Fix | File |
|---|---|
| A1 | `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/ClientTelemetry.java` |
| A2, A3, A7 | `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java` |
| A11, A15 | `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/Configs.java` |

## Full Reference

For A4–A6, A8–A10, A12–A14, A16–A21, read:
- `sdk/cosmos/azure-cosmos-benchmark/docs/multi-tenancy-analysis.md`
- `sdk/cosmos/azure-cosmos-benchmark/IMPLEMENTATION_GUIDE.md`
