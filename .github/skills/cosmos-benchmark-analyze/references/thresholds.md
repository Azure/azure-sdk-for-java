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

## CSV Columns (monitor.csv from monitor.sh)

| Column | Type | Description |
|---|---|---|
| `timestamp` | ISO 8601 | Snapshot time (UTC) |
| `threads` | int | Live thread count (from /proc/PID/task) |
| `fds` | int | Open file descriptors (from /proc/PID/fd) |
| `rss_kb` | int | Resident set size in KB |
| `cpu_pct` | float | CPU usage percentage |
| `heap_used_kb` | long | Used heap (S1U+EU+OU from jstat) |
| `heap_max_kb` | long | Max heap capacity (S0C+S1C+EC+OC from jstat) |
| `gc_count` | int | Cumulative GC count (YGC+FGC+CGC) |
| `gc_time_ms` | long | Cumulative GC time in ms |

## Key Snapshots

Identify snapshots using lifecycle events from the benchmark log file (pattern: `[LIFECYCLE] <event> timestamp=<ISO>`):

- **Baseline** = first monitor.csv row after `PRE_CREATE` lifecycle event
- **Peak** = row with highest `heap_used_kb`
- **Final** = last monitor.csv row (after `POST_CLOSE` lifecycle event + settle time)

## Computed Metrics

- `thread_delta = final.threads - baseline.threads`
- `heap_ratio = final.heap_used_kb / baseline.heap_used_kb`

## Status Indicators

- ✅ = passed / improved (delta ≤ 0 or within threshold)
- 🟡 = marginal (<10% change)
- 🔴 = failed / regressed (>10% worse or threshold exceeded)
