# Multi-Tenancy Benchmark Results Tracker

## Summary

| Run ID | Date (UTC) | Scenario | Branch | Commit | Threads | Verdict |
|--------|-----------|----------|--------|--------|---------|---------|
| R6 | 2026-02-20 20:01 | CHURN 3-cycle (90s settle) | fix-a1-telemetry-close | 8bc3caa168e | 0 growth | PASS - no leak |

---

## R6 — Multi-cycle CHURN with 90s settle (2026-02-20) — CONCLUSIVE

- **Branch**: `fix-a1-telemetry-close` (A1/A2 fix reverted, benchmark enhancements kept)
- **Commit**: `8bc3caa168e`
- **VM**: `benchuser@4.154.169.45`
- **Results dir**: `results/20260220T200151-CHURN-90s-settle/`
- **Settle time**: 90s per cycle (> 60s BoundedElastic evictor TTL)

### Per-Cycle Results

| Cycle | Threads | Thread delta | Heap (MB) | FDs |
|-------|---------|-------------|-----------|-----|
| 1 | 63 | +57 | 28 | 58 |
| 2 | 63 | +57 | 38 | 57 |
| 3 | 63 | +57 | 38 | 57 |

### Leak Verdict

| Metric | Value | Result |
|--------|-------|--------|
| Thread growth (cycle 1 to 3) | **0** | PASS |
| `transport-response-bounded-elastic` after settle | **0** (fully evicted) | PASS |
| Heap stable across cycles 2-3 | 38 MB = 38 MB | PASS |
| FDs stable | 57 = 57 | PASS |

### Conclusion

**No thread leak exists.** The thread growth seen in R4/R5 (5s settle) was entirely due to
BoundedElastic evictor TTL timing. With 90s settle (> 60s TTL), all idle workers are
reclaimed and thread count is perfectly stable at 63 across all cycles.

The `transport-response-bounded-elastic` threads that showed growth in R4 (8 to 17) and R5
(8 to 37) do NOT appear at all after 90s settle -- they were all temporary workers that
the evictor correctly cleaned up.

---

<!-- TEMPLATE: Copy below for new runs -->
<!--
## RN — SCENARIO (YYYY-MM-DD)

- **Branch**: `<branch>`
- **Commit**: `<commit>` — "<message>"
- **VM**: `<vm>`
- **Results dir**: `<dir>`

### Resource Snapshots

| Phase | Timestamp | Heap (MB) | Threads | Direct Mem (MB) | FDs | GC Count | GC Time (ms) |
|-------|-----------|-----------|---------|-----------------|-----|----------|-------------|
| PRE_CREATE (Baseline) | | | | | | | |
| POST_CREATE | | | | | | | |
| Peak | | | | | | | |
| POST_CLOSE (Final) | | | | | | | |

### Leak Check

| Metric | Value | Threshold | Result |
|--------|-------|-----------|--------|
| Thread delta (final − baseline) | | ≤ 2 | |
| Heap ratio (final / baseline) | | ≤ 1.1× | |

### Analysis

-
-->

---

## Findings & Action Items

### F1: reactor-http-epoll Threads (Shared LoopResources)

16 `reactor-http-epoll` threads appear after any Cosmos client usage and persist for the JVM lifetime.
NOT a leak -- Reactor Netty's global default event-loop pool (`LoopResources.DEFAULT`).

- Default thread count: `Runtime.getRuntime().availableProcessors()` (16 on D16s_v5 VM)
- Shared by ALL `ReactorNettyClient` instances (Gateway HTTP client, IMDS metadata client)
- Cosmos SDK does NOT customize `LoopResources` -- relies on reactor-netty defaults
- Configurable via system property: `reactor.netty.ioWorkerCount=<N>`

**Multi-tenancy concern**: 100+ clients sharing 16 event-loop threads could cause contention.

| Action | Description | Complexity |
|--------|-------------|------------|
| A23 | Benchmark event-loop contention at 100+ clients with varying `ioWorkerCount` | Low |
| A24 | Consider exposing `ioWorkerCount` as a Cosmos SDK config for multi-tenant scenarios | Low |

### F2: ClientTelemetry Cleanup Opportunities

`ClientTelemetry` has significant dead code. Still instantiated per-client, creating an
IMDS `HttpClient` pool each time even though metadata is cached statically after first call.

**Active code (6)**: constructor, `init()`, `recordValue()`, `getClientTelemetryConfig()`,
`isClientMetricsEnabled()`, `getMachineId()`

**Dead code (7)**: `close()` (was no-op with wrong log message), `blockingGetOrLoadMachineId()`,
`DEFAULT_CLIENT_TELEMETRY_ENABLED`, 4x `TCP_NEW_CHANNEL_LATENCY_*` constants

**IMDS HttpClient should be static**: `metadataHttpClient` is per-instance but
`azureVmMetaDataSingleton` is static -- only first call does HTTP. All subsequent instances
create unused `ConnectionProvider` objects.

| Action | Description | Complexity |
|--------|-------------|------------|
| A25 | Make IMDS `metadataHttpClient` a static singleton (lazy-init) | Low |
| A26 | Remove dead code: `blockingGetOrLoadMachineId()`, `TCP_NEW_CHANNEL_LATENCY_*` constants | Low |
| A27 | Fix `close()` log message (says "GlobalEndpointManager closed" -- copy-paste error) | Trivial |

### F3: transport-response-bounded-elastic Thread Growth

Only thread group that grows across CHURN cycles (8 after 1 cycle, 17 after 5 with fix, 37 without).

Reactor `BoundedElasticScheduler` creates workers on-demand, evicts after 60s TTL.
CHURN cycle settle time (5s) is shorter than evictor TTL, so workers accumulate temporarily.

**CONFIRMED NOT A LEAK (R6)**: With 90s settle (> 60s TTL), thread count is perfectly
stable at 63 across all cycles. All bounded-elastic workers are properly evicted.
The growth seen in R4/R5 was purely timing -- 5s settle was too short for the 60s evictor.

| Action | Description | Status |
|--------|-------------|--------|
| A28 | Increase CHURN settle time to 60s+ and verify bounded-elastic threads stabilize | DONE (R6) -- CONFIRMED |
| A29 | Investigate if any bounded-elastic schedulers should be disposed on client close | NOT NEEDED -- no leak |

### F4: A1/A2 Fix Impact Reassessment

**Original hypothesis**: `ClientTelemetry.close()` no-op leaked IMDS pool threads and
GlobalEndpointManager scheduler threads per client lifecycle.

**Revised understanding**:

| Resource | Actual behavior |
|----------|----------------|
| IMDS HttpClient pool | Created per-instance but unused after first client (metadata cached statically). Leaked `ConnectionProvider` wastes minor memory, no FDs or threads. |
| GlobalEndpointManager scheduler | Already properly closed via `RxDocumentClientImpl.close()`. Never broken. |
| reactor-http-epoll threads | Shared `LoopResources.DEFAULT` singleton -- same count (16) fix vs no-fix. |
| Thread count diff (80 vs 100) | Entirely in `transport-response-bounded-elastic` (17 vs 37). Likely timing-related. |

**Conclusion**: A1/A2 fix is correct for code hygiene but has minimal observable impact on
threads, heap, or FDs in CHURN testing. Real multi-tenancy concerns are:
- Event-loop contention at scale (A23)
- Unnecessary per-instance IMDS client creation (A25)
- Bounded-elastic growth during rapid churn (A28/A29)
