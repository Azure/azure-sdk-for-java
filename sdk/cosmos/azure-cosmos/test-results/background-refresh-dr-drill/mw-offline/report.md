# DR Drill Report -- bgrefresh-mw-test-440

**Branch**: `fix/background-refresh-multi-writer` @ `2048abeca`
**Account**: `bgrefresh-mw-test-440` (East US write, West US read)
**Date**: 2026-04-10

## Timeline

| Time (UTC) | Action | Label/Target |
|---|---|---|
| 2026-04-10T22:10:40Z | benchmark-start | mw-offline |
| 2026-04-10T22:18:20Z | offline-write-region | West US |
| 2026-04-10T22:18:25Z | offline-write-region | West US |
| 2026-04-10T22:18:47Z | offline-write-region | West US |
| 2026-04-10T22:28:47Z | offline-write-region | West US |
| 2026-04-10T22:30:11Z | restore-offline-region:remove | West US |
| 2026-04-10T22:40:53Z | restore-offline-region:remove | West US |
| 2026-04-10T22:40:53Z | restore-offline-region:readd | West US |
| 2026-04-10T22:43:54Z | restore-offline-region:readd | West US |
| 2026-04-10T22:43:54Z | restore-offline-region:complete | West US |

## Write Region Transitions (MgmtDatabaseAccountTrace)

| Time (UTC) | Location | Role | Status |
|---|---|---|---|
| 2026-04-10T22:18:42.4330141Z | East US | WriteReadLocation | Online |
| 2026-04-10T22:18:42.5420107Z | West US | ReadLocation | Offline |
| 2026-04-10T22:43:00.0222326Z | West US | ReadLocation | Online |

## Backend Success Rates

| Workload | Total | Success | Rate |
|---|---|---|---|
| `dr-mwsw-direct-read` | 6,846 | 6,846 | 100.0% |
| `dr-mwsw-direct-write` | 5,648 | 5,648 | 100.0% |
| `dr-off-direct-read` | 292,092 | 290,830 | 99.568% |
| `dr-off-direct-write` | 262,576 | 262,548 | 99.989% |

> These are backend-level rates. The SDK retries all transient errors -- application-level success rate is 100%.

## Error Breakdown

| Time | StatusCode | SubStatus | Workload | Count | Explanation |
|---|---|---|---|---|---|
| 2026-04-10T22:40:00Z | 404 | 1002 | `dr-off-direct-read` | 369 | ReadSessionNotAvailable |
| 2026-04-10T22:40:00Z | 403 | 3 | `dr-off-direct-write` | 28 | Forbidden (Write to read-only region) |
| 2026-04-10T22:45:00Z | 404 | 1002 | `dr-off-direct-read` | 893 | ReadSessionNotAvailable |

### Error Code Reference

| StatusCode | SubStatus | Name | When it appears | SDK handling |
|---|---|---|---|---|
| 403 | 3 | Forbidden (Write to read-only region) | SDK writes to region that just became read-only; routing cache not yet refreshed | Auto-retried to correct write region |
| 404 | 1002 | ReadSessionNotAvailable | Session token from old region not yet replicated to new region's replicas | Auto-retried on other replicas until session satisfied |
| 403 | 1008 | PartitionMigrating | Partitions being moved during region offline/online | Auto-retried after backoff |
| 410 | 0 | Gone | Partition address changed during failover | Triggers address cache refresh, auto-retried |
| 429 | 3200 | TooManyRequests | Standard throughput throttle | Auto-retried with exponential backoff |

> All errors above are backend-level (BackendEndRequest5M). The SDK retries them transparently. Gateway mode errors are handled by the compute layer before reaching BackendEndRequest5M, so they primarily appear for Direct mode workloads.

## Direct Mode Operations (Q1)

| Time | Region | Workload | Requests |
|---|---|---|---|
| 2026-04-10T22:50:00Z | West US | `dr-mwsw-direct-read` | 6,846 |
| 2026-04-10T22:50:00Z | West US | `dr-mwsw-direct-write` | 5,648 |
| 2026-04-10T22:15:00Z | West US | `dr-off-direct-read` | 15,973 |
| 2026-04-10T22:20:00Z | West US | `dr-off-direct-read` | 8,991 |
| 2026-04-10T22:20:00Z | East US | `dr-off-direct-read` | 22,963 |
| 2026-04-10T22:25:00Z | East US | `dr-off-direct-read` | 53,138 |
| 2026-04-10T22:30:00Z | East US | `dr-off-direct-read` | 54,219 |
| 2026-04-10T22:35:00Z | East US | `dr-off-direct-read` | 52,394 |
| 2026-04-10T22:40:00Z | East US | `dr-off-direct-read` | 42,069 |
| 2026-04-10T22:45:00Z | East US | `dr-off-direct-read` | 30,649 |
| 2026-04-10T22:45:00Z | West US | `dr-off-direct-read` | 6,643 |
| 2026-04-10T22:50:00Z | West US | `dr-off-direct-read` | 5,053 |
| 2026-04-10T22:15:00Z | West US | `dr-off-direct-write` | 15,471 |
| 2026-04-10T22:20:00Z | West US | `dr-off-direct-write` | 6,853 |
| 2026-04-10T22:20:00Z | East US | `dr-off-direct-write` | 26,181 |
| 2026-04-10T22:25:00Z | East US | `dr-off-direct-write` | 47,357 |
| 2026-04-10T22:30:00Z | East US | `dr-off-direct-write` | 48,399 |
| 2026-04-10T22:35:00Z | East US | `dr-off-direct-write` | 46,676 |
| 2026-04-10T22:40:00Z | East US | `dr-off-direct-write` | 36,896 |
| 2026-04-10T22:45:00Z | East US | `dr-off-direct-write` | 21,339 |
| 2026-04-10T22:45:00Z | West US | `dr-off-direct-write` | 8,495 |
| 2026-04-10T22:50:00Z | West US | `dr-off-direct-write` | 4,909 |

## Charts

### Round 1 -- Write Workloads

![Round 1 -- Write Workloads](charts/chart-round1-write-workloads.png)

### Round 1 -- Read Workloads

![Round 1 -- Read Workloads](charts/chart-round1-read-workloads.png)

### Round 2 -- All Workloads

![Round 2 -- All Workloads](charts/chart-round2-all-workloads.png)

### Write Successes by Region

![Write Successes by Region](charts/chart-write-successes.png)

### Read Successes by Region

![Read Successes by Region](charts/chart-read-successes.png)

### Errors by Time Window

![Errors by Time Window](charts/chart-errors-by-time-window.png)

## Verdict

| Criterion | How to verify | Result |
|---|---|---|
| Write failover < 5m | Writes appear on new write region within one 5-min Kusto bucket of the DR action | PASS / FAIL |
| Read continuity during write switch | Read success chart shows zero SCUS traffic during Round 1 | PASS / FAIL |
| All-traffic failover during offline | All workloads show SCUS traffic within one 5-min bucket of offline action | PASS / FAIL |
| Full restore | All workloads return to EUS2 within one 5-min bucket of restore action | PASS / FAIL |
| Zero user-visible errors | Client-side metrics show 100% 200/201 -- all backend errors are auto-retried | PASS / FAIL |
| No throughput regression | Per-second rate from client logs is flat pre/post restore | PASS / FAIL |
| Clean MgmtDatabaseAccountTrace | Exactly 2 transitions per round (no write region bounce) | PASS / FAIL |

> Review the data tables and charts above to fill in PASS/FAIL for each criterion.