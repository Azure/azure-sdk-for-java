# Pass/Fail Thresholds

## Hard Fail

| Metric | Threshold | Verdict |
|---|---|---|
| Threads after close > baseline + 2 | Hard fail | 🔴 LEAK DETECTED |
| Heap after close > baseline × 1.1 | Hard fail | 🔴 MEMORY LEAK |

## Soft Warn

| Metric | Threshold | Verdict |
|---|---|---|
| P99 latency at N=100 > 5× P99 at N=1 | Soft warn | 🟡 INVESTIGATE |
| Throughput at N=100 < 0.7× throughput at N=1 | Soft warn | 🟡 INVESTIGATE |
| GC pause max > 200ms | Soft warn | 🟡 Tune GC |

## CSV Columns (monitor.csv)

| Column | Type | Description |
|---|---|---|
| `timestamp` | ISO 8601 | Snapshot time |
| `phase` | enum | `PRE_CREATE`, `POST_CREATE`, `POST_WORKLOAD`, `POST_CLOSE` |
| `usedHeapBytes` | long | Used heap after GC |
| `nettyDirectMemBytes` | long | Netty direct memory |
| `liveThreadCount` | int | Live thread count |
| `openFDs` | int | Open file descriptors |
| `gcCount` | int | Cumulative GC count |
| `gcTimeMs` | long | Cumulative GC time |
| `activeCosmosClients` | int | Active CosmosAsyncClient instances |
| `queryPlanCacheSize` | int | Query plan cache entries |

## Key Snapshots

- **Baseline** = first row (`PRE_CREATE`) — before any clients
- **Final** = last row (`POST_CLOSE`) — after all clients closed + GC
- **Peak** = row with highest `usedHeapBytes`

## Status Indicators

- ✅ = passed / improved (delta ≤ 0 or within threshold)
- 🟡 = marginal (<10% change)
- 🔴 = failed / regressed (>10% worse or threshold exceeded)
