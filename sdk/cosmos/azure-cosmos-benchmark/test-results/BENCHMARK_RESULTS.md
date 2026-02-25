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

> **Updated Feb 2026**: Deep investigation confirmed the IMDS HttpClient is now **ephemeral** --
> created and disposed inline during `init()`, not held as a persistent field. The metadata
> is cached in a static `AtomicReference<AzureVMMetadata>` (one fetch per JVM). The
> `ClientTelemetryInfo` histogram maps no longer exist -- telemetry uses Micrometer.

**Original finding (R1-R5)**: `ClientTelemetry` appeared to create an IMDS `HttpClient` pool
per-instance. Deep code review (Feb 2026) showed this was **refactored** -- the HTTP client
is ephemeral and properly disposed.

**Remaining cleanup opportunities**:

| Action | Description | Status |
|--------|-------------|--------|
| A25 | ~~Make IMDS `metadataHttpClient` a static singleton~~ | **RESOLVED** -- already ephemeral in current code |
| A26 | Remove dead code: `blockingGetOrLoadMachineId()`, `TCP_NEW_CHANNEL_LATENCY_*` constants | Low priority |
| A27 | ~~Fix `close()` log message~~ | **RESOLVED** -- `close()` is now intentionally a no-op with correct comment |

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

> **Updated Feb 2026**: Deep investigation confirmed A1/A2 bugs are **FIXED** in the current
> codebase. The IMDS client is ephemeral, `close()` is properly called from
> `RxDocumentClientImpl.close()`, and telemetry uses Micrometer (no histogram maps).

**Original hypothesis**: `ClientTelemetry.close()` no-op leaked IMDS pool threads and
GlobalEndpointManager scheduler threads per client lifecycle.

**Deep investigation findings (Feb 2026)**:

| Resource | Original Claim | Actual (Current Code) |
|----------|---------------|----------------------|
| IMDS HttpClient pool | Leaked 5-connection pool per client | **FIXED** -- ephemeral, created and disposed inline in `init()` |
| `ClientTelemetryInfo` histograms | ~256 KB `ConcurrentDoubleHistogram` leaked per client | **FIXED** -- maps no longer exist; telemetry uses Micrometer |
| `RxDocumentClientImpl.close()` calling `clientTelemetry.close()` | Never called | **FIXED** -- now properly called |
| `close()` log message | Wrong: "GlobalEndpointManager closed" | **FIXED** -- correct comment, intentional no-op |
| GlobalEndpointManager scheduler | Leaked per client | **Never broken** -- always properly closed |
| reactor-http-epoll threads | Leaked | **Not a leak** -- shared `LoopResources.DEFAULT` singleton (16 threads) |
| Thread count diff (80 vs 100) | A1/A2 leak | **Timing** -- `transport-response-bounded-elastic` evictor TTL (60s) |

**Additional bugs still present**:
- **Bug #2**: `ThinClientStoreModel` never closed in `RxDocumentClientImpl.close()`
- **Bug #3**: `globalPartitionEndpointManagerForPerPartitionAutomaticFailover` never closed

**Additional findings**:
- Query plan cache has 5,000-entry cap with full-clear eviction (not unbounded as originally claimed)
- `connectionSharingAcrossClientsEnabled` was silently dropped in benchmark harness (now wired)
- Reactor Netty connection pool metrics are NOT enabled in the SDK (gap)
- HTTP/2 stream-level metrics do NOT exist (gap)

### F5: connectionSharingAcrossClientsEnabled Was Dead Config

**Found during baseline matrix preparation (Feb 2026)**: The `tenants.json` field
`connectionSharingAcrossClientsEnabled` was silently dropped -- no field, no switch case,
no setter in `TenantWorkloadConfig`. The value hit the `default:` branch in `applyField()`
and was discarded.

**Fixed in branch `wireConnectionSharingInBenchmark`**:
- Added field, getter, setter to `TenantWorkloadConfig`
- Added switch case in `applyField()`
- Added `-connectionSharingAcrossClientsEnabled` CLI parameter to `Configuration`
- Applied on `CosmosClientBuilder` in `AsyncBenchmark`
- Wired through `fromConfiguration()` for legacy CLI path

### F6: Metrics Gaps for Multi-Tenancy

| Metric | Status | What is Needed |
|--------|--------|---------------|
| Cosmos SDK Micrometer metrics (44 meters) | Available | `CosmosMicrometerMetricsOptions` already wired in orchestrator |
| Reactor Netty connection pool (active/idle/pending) | **NOT enabled** | Call `.metrics(true)` on `ConnectionProvider.Builder` in `HttpClient.createFixed()` |
| HTTP/2 stream-level (active streams, utilization) | **NOT available** | Need `.metrics(true)` on Reactor Netty HttpClient or custom Netty H2 instrumentation |
| SharedGatewayHttpClient ref count | **NOT exposed** | Register `counter` as Micrometer Gauge |
