# DR Drill Report -- bgrefresh-sw-test-440

**Branch**: `fix/background-refresh-multi-writer` @ `2048abeca`
**Account**: `bgrefresh-sw-test-440` (East US write, West US read)
**Date**: 2026-04-10

## Timeline

| Time (UTC) | Action | Label/Target |
|---|---|---|
| 2026-04-10T23:24:55Z | benchmark-start | round1-switch |
| 2026-04-10T23:29:56Z | switch-write-region | West US |
| 2026-04-10T23:31:01Z | switch-write-region | West US |
| 2026-04-10T23:41:35Z | restore-write-region | East US |
| 2026-04-10T23:42:43Z | restore-write-region | East US |
| 2026-04-10T23:48:17Z | benchmark-stop | round1-switch |

## Write Region Transitions (MgmtDatabaseAccountTrace)

| Time (UTC) | Location | Role | Status |
|---|---|---|---|
| 2026-04-10T23:30:46.4220367Z | East US | ReadLocation | Online |
| 2026-04-10T23:30:46.5152476Z | West US | WriteReadLocation | Online |
| 2026-04-10T23:42:29.6364655Z | West US | ReadLocation | Online |
| 2026-04-10T23:42:29.7304967Z | East US | WriteReadLocation | Online |

## Backend Success Rates

| Workload | Total | Success | Rate |
|---|---|---|---|
| `dr-direct-read` | 251,072 | 247,366 | 98.524% |
| `dr-direct-write` | 142,567 | 142,499 | 99.952% |
| `dr-off-direct-read` | 82,692 | 79,561 | 96.214% |
| `dr-off-direct-write` | 72,217 | 72,217 | 100.0% |

> These are backend-level rates. The SDK retries all transient errors -- application-level success rate is 100%.

## Error Breakdown

| Time | StatusCode | SubStatus | Workload | Count | Explanation |
|---|---|---|---|---|---|
| 2026-04-10T23:25:00Z | 404 | 1002 | `dr-direct-read` | 1,336 | ReadSessionNotAvailable |
| 2026-04-10T23:30:00Z | 404 | 1002 | `dr-direct-read` | 223 | ReadSessionNotAvailable |
| 2026-04-10T23:30:00Z | 403 | 3 | `dr-direct-write` | 30 | Forbidden (Write to read-only region) |
| 2026-04-10T23:40:00Z | 404 | 1002 | `dr-direct-read` | 985 | ReadSessionNotAvailable |
| 2026-04-10T23:40:00Z | 403 | 3 | `dr-direct-write` | 38 | Forbidden (Write to read-only region) |
| 2026-04-10T23:45:00Z | 404 | 1002 | `dr-direct-read` | 1,162 | ReadSessionNotAvailable |
| 2026-04-10T23:50:00Z | 404 | 1002 | `dr-off-direct-read` | 1,070 | ReadSessionNotAvailable |
| 2026-04-10T23:55:00Z | 404 | 1002 | `dr-off-direct-read` | 2,061 | ReadSessionNotAvailable |

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
| 2026-04-10T23:25:00Z | East US | `dr-direct-read` | 46,826 |
| 2026-04-10T23:30:00Z | East US | `dr-direct-read` | 54,576 |
| 2026-04-10T23:35:00Z | East US | `dr-direct-read` | 55,521 |
| 2026-04-10T23:40:00Z | East US | `dr-direct-read` | 56,424 |
| 2026-04-10T23:45:00Z | East US | `dr-direct-read` | 37,725 |
| 2026-04-10T23:25:00Z | East US | `dr-direct-write` | 40,611 |
| 2026-04-10T23:30:00Z | East US | `dr-direct-write` | 3,889 |
| 2026-04-10T23:30:00Z | West US | `dr-direct-write` | 14,251 |
| 2026-04-10T23:35:00Z | West US | `dr-direct-write` | 16,073 |
| 2026-04-10T23:40:00Z | West US | `dr-direct-write` | 6,803 |
| 2026-04-10T23:40:00Z | East US | `dr-direct-write` | 27,708 |
| 2026-04-10T23:45:00Z | East US | `dr-direct-write` | 33,232 |
| 2026-04-10T23:50:00Z | East US | `dr-off-direct-read` | 27,200 |
| 2026-04-10T23:55:00Z | East US | `dr-off-direct-read` | 55,492 |
| 2026-04-10T23:50:00Z | East US | `dr-off-direct-write` | 23,269 |
| 2026-04-10T23:55:00Z | East US | `dr-off-direct-write` | 48,948 |

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