# Multi-Tenancy Baseline Test Plan

> **Goal**: Establish reproducible baseline measurements of CPU, memory, connections, threads, latency, and throughput for multi-tenant `CosmosClient` usage in Gateway mode, so that every optimization from `multi-tenancy-analysis.md` can be validated with before/after data.

---

## Table of Contents

1. [Test Dimensions & Metrics](#1-test-dimensions--metrics)
2. [Test Scenarios](#2-test-scenarios)
3. [Infrastructure & Environment](#3-infrastructure--environment)
4. [Test Harness Design](#4-test-harness-design)
5. [Observability: OpenTelemetry, Micrometer & Application Insights](#5-observability-opentelemetry-micrometer--application-insights)
6. [Azure VM Test Environment](#6-azure-vm-test-environment)
7. [Metric Collection Strategy](#7-metric-collection-strategy)
8. [Triggering Tests from Branches & PRs](#8-triggering-tests-from-branches--prs)
9. [External Result Storage](#9-external-result-storage)
10. [Execution Runbook](#10-execution-runbook)
11. [Result Schema & Comparison](#11-result-schema--comparison)
12. [Copilot Analysis Agent](#12-copilot-analysis-agent)

---

## 1. Test Dimensions & Metrics

### 1.1 Independent Variables (What We Vary)

| Variable | Values | Why |
|---|---|---|
| **Number of clients** | 1, 10, 50, 100, 200, 500 | Core scaling dimension |
| **Connection sharing** | `off` (default), `on` (`connectionSharingAcrossClientsEnabled`) | A3 from analysis |
| **HTTP version** | HTTP/1.1 (default), HTTP/2 + ThinClient | A15 from analysis |
| **Connection pool size** | 1000 (default), 100, 50 | A11 from analysis |
| **HTTP client impl** | Reactor Netty (default), OkHttp (investigation) | Compare connection pooling, thread model, memory footprint across HTTP client implementations |
| **Workload** | Idle (clients created, no ops), Point reads, Queries, Mixed | Isolate creation cost vs runtime cost |
| **Client lifecycle** | Long-lived, Churn (create/close cycles) | Detect leaks (A1, A2, A3) |

### 1.2 Dependent Variables (What We Measure)

| Category | Metric | Unit | Collection Method |
|---|---|---|---|
| **Memory — Heap** | Used heap after GC | bytes | `Runtime.getRuntime()` after `System.gc()` |
| **Memory — Heap** | Heap delta per client | bytes | (heap at N clients − heap at 0) / N |
| **Memory — Direct** | Netty direct memory | bytes | `PlatformDependent.usedDirectMemory()` |
| **Memory — Direct** | Buffer pool stats | bytes | `ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)` |
| **Threads** | Live thread count | count | `Thread.activeCount()` or `ThreadMXBean` |
| **Threads** | Thread name breakdown | map | `Thread.getAllStackTraces().keySet()` grouped by prefix |
| **CPU** | Process CPU % | percent | `OperatingSystemMXBean.getProcessCpuLoad()` |
| **CPU** | System CPU % | percent | `CpuMemoryMonitor` / `CpuMemoryReader` |
| **Connections** | Active TCP connections | count | `ConnectionProvider` metrics (if enabled) OR `ss`/`netstat` |
| **Connections** | Idle TCP connections | count | Same |
| **Connections** | File descriptors used | count | `/proc/self/fd` (Linux) or `lsof -p` |
| **Latency** | P50, P99, P99.9, Max | ms | Codahale `Timer` with `HdrHistogramResetOnSnapshotReservoir` |
| **Throughput** | Operations/sec | ops/s | Codahale `Meter` |
| **Cache sizes** | Query plan cache entries | count | `RxDocumentClientImpl.getQueryPlanCache().size()` |
| **Client count** | Active client count | count | `RxDocumentClientImpl.activeClientsCnt` (via diagnostics) |
| **GC** | GC pause count + time | count/ms | `GarbageCollectorMXBean` |

### 1.3 Key Ratios to Compute

| Ratio | Formula | Target |
|---|---|---|
| Marginal heap per client | `Δheap / Δclients` | Should be stable |
| Marginal threads per client | `Δthreads / Δclients` | Should be ≤1 (ideal: 0 with shared pools) |
| Latency degradation | `P99(N clients) / P99(1 client)` | Should be <2× at 100 clients |
| Throughput scaling | `ops/s(N clients) / ops/s(1 client)` | Should scale near-linearly |
| Connection efficiency | `TCP connections / concurrent requests` | HTTP/1.1: ~1.0, HTTP/2: ~0.03 |

---

## 2. Test Scenarios

### S1: Idle Client Scaling (Resource Footprint)

**Purpose**: Measure the pure resource cost of creating N clients with zero workload.

```
For N in [1, 10, 50, 100, 200, 500]:
    1. Record baseline: heap, direct mem, threads, FDs
    2. Create N CosmosAsyncClient instances (Gateway mode, distinct endpoints)
    3. Wait 30s for background tasks to stabilize
    4. Force GC, record: heap, direct mem, threads, FDs, thread-name breakdown
    5. Wait 5 min (allow background refresh + telemetry cycles)
    6. Record again (detect growth from background activity)
    7. Close all clients
    8. Force GC, wait 10s, record final state (detect leaks)
```

**Variants**:
- S1a: `connectionSharingAcrossClientsEnabled = false` (default)
- S1b: `connectionSharingAcrossClientsEnabled = true`
- S1c: HTTP/2 + ThinClient enabled

**Expected outputs**:
- Chart: heap vs N (should be linear; slope = per-client cost)
- Chart: thread count vs N
- Table: thread-name-prefix breakdown at N=100

---

### S2: Point Read Throughput & Latency Scaling

**Purpose**: Measure how latency/throughput degrade as client count increases, under fixed total concurrency.

```
Total concurrency = 200 (fixed)
For N in [1, 10, 50, 100]:
    Per-client concurrency = 200 / N
    1. Create N clients, each with its own container (pre-loaded with 1000 docs)
    2. Warmup: 5000 ops (not measured)
    3. Measure: 100,000 ops (or 5 min, whichever first)
       - Round-robin across clients
       - Per-client: Codahale Timer (latency) + Meter (throughput)
       - Global: aggregate Timer + Meter
    4. Record: P50/P99/P99.9, ops/s, heap, threads, connections
    5. Close all clients
```

**Variants**:
- S2a: Default (HTTP/1.1, no sharing)
- S2b: Connection sharing enabled
- S2c: HTTP/2 + ThinClient
- S2d: Reduced pool size (100 instead of 1000)

---

### S3: Query Cache Growth (Unbounded Cache Detection)

**Purpose**: Measure `queryPlanCache` growth and verify the 5,000-entry cap with full-clear eviction; measure memory impact.

```
Clients = 10
For query_count in [100, 500, 1000, 5000, 10000]:
    1. Create 10 clients
    2. Execute `query_count` distinct queries (parameterized: "SELECT * FROM c WHERE c.field{i} = @v")
    3. Record: queryPlanCache.size() per client, total heap
    4. Compute: cache memory = heap(10K queries) - heap(100 queries)
```

**Expected**: Linear growth up to 5,000 entries, then full-clear eviction resets to 0. Only simple queries cached. Validates A7 (LRU would improve over clear-all).

---

### S4: Client Lifecycle Churn (Leak Detection)

**Purpose**: Detect resource leaks when clients are repeatedly created and destroyed.

```
For cycle in [1..100]:
    1. Create 10 clients
    2. Execute 100 point reads per client
    3. Close all 10 clients
    4. Force GC
    5. Record: heap, threads, direct mem, FDs
```

**Expected without fix**: Thread count and/or memory should increase over cycles (proving resource leaks). Note: A1/A2 (ClientTelemetry) leaks have been **fixed**. Remaining: A3 (ThinClientStoreModel), Bug #3 (PPAF manager).
**Expected with fix**: Thread count and memory should return to baseline each cycle.

---

### S5: Connection Pool Pressure (HTTP/1.1 vs HTTP/2)

**Purpose**: Measure TCP connection count and acquisition latency under high concurrency.

```
Clients = 50
Concurrency per client = 50 (total = 2500 concurrent)
Pool size = 100 (intentionally smaller than concurrency)

1. Create 50 clients with maxConnectionPoolSize=100
2. Blast 50K point reads at max rate
3. Record: connection pool metrics, pending acquire count, acquire latency, request latency
4. Compare HTTP/1.1 vs HTTP/2 (with 30 streams per connection)
```

**Expected**: HTTP/1.1 will show connection pool exhaustion (pending acquire timeouts). HTTP/2 should handle the load with far fewer connections.

---

### S6: Long-Running Stability (24-Hour Soak)

**Purpose**: Detect slow leaks, cache growth, and degradation over time.

```
Clients = 50
Duration = 24 hours
Workload = mixed (70% reads, 20% queries, 10% writes) at moderate rate (1000 ops/s total)

Every 5 minutes, record:
    - Heap, direct memory, threads, FDs, GC stats
    - Per-client: queryPlanCache.size(), session token count
    - Latency P50/P99
    - Throughput
```

**Expected**: Should be flat. Any upward trend indicates a leak or unbounded cache.

---

### S7: HTTP Client Implementation Comparison (Reactor Netty vs OkHttp)

**Purpose**: Investigate whether replacing the Cosmos SDK's internal Reactor Netty HTTP client with OkHttp yields improvements in connection pooling, thread usage, or memory footprint for multi-tenant scenarios.

**Background**: The Cosmos SDK uses its **own internal Reactor Netty HTTP client** (`com.azure.cosmos.implementation.http.ReactorNettyClient`) — not `azure-core-http-netty`. The `HttpClient` interface has only one implementation today. OkHttp has fundamentally different connection pooling architecture:

| Aspect | Reactor Netty (current) | OkHttp (investigation) |
|---|---|---|
| **Connection pool model** | `ConnectionProvider` with configurable max connections per host; event-loop-based I/O | `ConnectionPool` with keep-alive; thread-per-request dispatch |
| **Thread model** | Shared event loop (`LoopResources.DEFAULT`, ~CPU-core threads) — all clients share | `Dispatcher` with configurable `ExecutorService` — can be shared or per-client |
| **HTTP/2 support** | Via `Http2AllocationStrategy` + ALPN | Native HTTP/2 with connection coalescing |
| **Connection reuse** | Per-`ConnectionProvider`; separate pool per client by default | Per-`OkHttpClient`; can share `ConnectionPool` across instances |
| **Idle cleanup** | `maxIdleTime` on `ConnectionProvider` | `keepAliveDuration` on `ConnectionPool` |
| **SSL/TLS** | Netty `SslContext` (OpenSSL or JDK) | JDK `SSLContext` (default) |
| **Backpressure** | Reactive (Mono/Flux) — naturally non-blocking | Blocking or callback-based; needs wrapper for reactive |
| **Direct memory** | Uses Netty `ByteBuf` (direct/pooled) — can be large | JDK byte arrays (heap) — no direct memory concerns |
| **Multi-tenant thread count** | ~CPU cores (shared event loop) + 1 per client (`GlobalEndpointManager`) | Depends on `Dispatcher` config; default max 64 concurrent requests |

**Investigation approach** (not a code-shipping change — exploration only):

```
Phase 1: Characterize (no code changes needed)
    1. Run S1 (Idle Scaling) + S2 (Point Reads) with Reactor Netty (current baseline)
    2. Record: threads, connections, heap, direct memory, FDs per client count
    3. Analyze: which resources are dominated by Reactor Netty vs SDK logic?

Phase 2: Prototype (experimental branch)
    1. Implement OkHttp adapter behind the existing HttpClient interface
    2. OkHttp-specific config:
       - Shared ConnectionPool across all CosmosClients
       - Shared Dispatcher with bounded thread pool
       - HTTP/2 enabled with connection coalescing
    3. Re-run S1 + S2 with OkHttp adapter
    4. Compare: threads, connections, heap, direct memory, FDs

Phase 3: Evaluate
    - Is the difference significant enough to justify maintaining two HTTP backends?
    - Does OkHttp eliminate any of the multi-tenancy pain points identified in the analysis?
    - Are there compatibility concerns (reactive bridge, RNTBD/ThinClient, custom handlers)?
```

**Key questions to answer**:
1. **Direct memory**: Reactor Netty uses Netty `ByteBuf` (direct/pooled). At 100 clients, does direct memory become a significant portion of total memory? OkHttp would eliminate this entirely.
2. **Connection pool sharing**: OkHttp's `ConnectionPool` is trivially shareable across `OkHttpClient` instances (they can reference the same pool object). Is this simpler than `SharedGatewayHttpClient`'s reference-counting approach?
3. **Thread count**: Does OkHttp's dispatcher model result in fewer threads at 100+ clients compared to Reactor Netty's event loop + per-client `GlobalEndpointManager`?
4. **HTTP/2 connection coalescing**: OkHttp can share a single HTTP/2 connection across different hostnames if they resolve to the same IP and share a TLS certificate. Does this help for multi-account scenarios?
5. **Compatibility**: The `ThinClientStoreModel` uses Reactor Netty-specific channel handlers (`StripWhitespaceHandler`). Would an OkHttp adapter work with ThinClient, or only with standard Gateway mode?

**Expected outcome**: A data-driven recommendation on whether to pursue an OkHttp backend, keep Reactor Netty with optimizations, or both. This is **not** about replacing Reactor Netty — it's about understanding the tradeoffs.

---

### S8: Protocol × Workload × Sharing Baseline Matrix (Recurring)

**Purpose**: Establish a reproducible baseline across all protocol modes, workload types, and connection sharing configurations. **This test is re-run after every major change/fix** to produce before/after comparison data.

#### Dimensions

| Dimension | Values | Count |
|-----------|--------|-------|
| **Protocol** | HTTP/1.1 (default), HTTP/2, ThinClient (HTTP/2 + RNTBD) | 3 |
| **Workload** | ReadThroughput, ReadLatency, WriteThroughput, WriteLatency, QueryOrderby | 5 |
| **Connection Sharing** | Isolated (default), Shared (`connectionSharingAcrossClientsEnabled=true`) | 2 |
| **Total** | | **30** |

#### Protocol Enablement (JVM System Properties)

| Protocol | System Properties |
|----------|------------------|
| HTTP/1.1 | *(none — default)* |
| HTTP/2 | `-DCOSMOS.HTTP2_ENABLED=true` |
| ThinClient | `-DCOSMOS.HTTP2_ENABLED=true -DCOSMOS.THINCLIENT_ENABLED=true` |

#### Workloads

| ID | Operation | Type | Measures | Query Plan Cacheable? |
|----|-----------|------|----------|----------------------|
| `read` | `ReadThroughput` | Point read | ops/sec | N/A |
| `readlat` | `ReadLatency` | Point read | P50/P99/P99.9/Max latency | N/A |
| `write` | `WriteThroughput` | Point write | ops/sec | N/A |
| `writelat` | `WriteLatency` | Point write | P50/P99/P99.9/Max latency | N/A |
| `query` | `QueryOrderby` | `SELECT * FROM c ORDER BY c._ts` | ops/sec | ❌ No (ORDER BY excluded from cache) |

#### Connection Sharing Modes

| ID | `connectionSharingAcrossClientsEnabled` | Pool Behavior |
|----|---------------------------------------|---------------|
| `isolated` | `false` (default) | Each client owns its own `ConnectionProvider` (up to 1,000 conns each) |
| `shared` | `true` | All clients share a singleton `SharedGatewayHttpClient` (ref-counted) |

#### Full Test Matrix (30 Scenarios)

| # | Scenario ID | Protocol | Workload | Sharing |
|---|-------------|----------|----------|---------|
| 1 | `http11-read-isolated` | HTTP/1.1 | ReadThroughput | isolated |
| 2 | `http11-read-shared` | HTTP/1.1 | ReadThroughput | shared |
| 3 | `http11-readlat-isolated` | HTTP/1.1 | ReadLatency | isolated |
| 4 | `http11-readlat-shared` | HTTP/1.1 | ReadLatency | shared |
| 5 | `http11-write-isolated` | HTTP/1.1 | WriteThroughput | isolated |
| 6 | `http11-write-shared` | HTTP/1.1 | WriteThroughput | shared |
| 7 | `http11-writelat-isolated` | HTTP/1.1 | WriteLatency | isolated |
| 8 | `http11-writelat-shared` | HTTP/1.1 | WriteLatency | shared |
| 9 | `http11-query-isolated` | HTTP/1.1 | QueryOrderby | isolated |
| 10 | `http11-query-shared` | HTTP/1.1 | QueryOrderby | shared |
| 11 | `http2-read-isolated` | HTTP/2 | ReadThroughput | isolated |
| 12 | `http2-read-shared` | HTTP/2 | ReadThroughput | shared |
| 13 | `http2-readlat-isolated` | HTTP/2 | ReadLatency | isolated |
| 14 | `http2-readlat-shared` | HTTP/2 | ReadLatency | shared |
| 15 | `http2-write-isolated` | HTTP/2 | WriteThroughput | isolated |
| 16 | `http2-write-shared` | HTTP/2 | WriteThroughput | shared |
| 17 | `http2-writelat-isolated` | HTTP/2 | WriteLatency | isolated |
| 18 | `http2-writelat-shared` | HTTP/2 | WriteLatency | shared |
| 19 | `http2-query-isolated` | HTTP/2 | QueryOrderby | isolated |
| 20 | `http2-query-shared` | HTTP/2 | QueryOrderby | shared |
| 21 | `thin-read-isolated` | ThinClient | ReadThroughput | isolated |
| 22 | `thin-read-shared` | ThinClient | ReadThroughput | shared |
| 23 | `thin-readlat-isolated` | ThinClient | ReadLatency | isolated |
| 24 | `thin-readlat-shared` | ThinClient | ReadLatency | shared |
| 25 | `thin-write-isolated` | ThinClient | WriteThroughput | isolated |
| 26 | `thin-write-shared` | ThinClient | WriteThroughput | shared |
| 27 | `thin-writelat-isolated` | ThinClient | WriteLatency | isolated |
| 28 | `thin-writelat-shared` | ThinClient | WriteLatency | shared |
| 29 | `thin-query-isolated` | ThinClient | QueryOrderby | isolated |
| 30 | `thin-query-shared` | ThinClient | QueryOrderby | shared |

#### Fixed Parameters (all 30 scenarios)

| Parameter | Value | Notes |
|-----------|-------|-------|
| **Tenants** | 50 | 50 distinct Cosmos DB accounts (`cosmosdbmulti12101`–`cosmosdbmulti121050`) |
| **Concurrency per tenant** | 20 | Total aggregate: 50 × 20 = 1,000 concurrent requests |
| **Operations per tenant** | 1,000,000 | Per scenario run |
| **Pre-created documents** | 1,000 per container | ~1 KB each |
| **Connection pool size** | 1,000 per client | Default |
| **Connection mode** | GATEWAY | All protocols use Gateway mode |
| **Consistency** | SESSION | Default |

#### Metrics Collected Per Scenario

| Category | Metrics | Source | Status |
|----------|---------|--------|--------|
| **Threads** | Peak thread count, thread name breakdown | `monitor.sh` (`/proc/53228/task`) | ✅ Available |
| **Memory** | Heap used/max (KB), RSS (KB) | `jstat -gc`, `ps` | ✅ Available |
| **File Descriptors** | Open FD count | `/proc/53228/fd` | ✅ Available |
| **GC** | GC count, GC time (ms) | `jstat -gc` | ✅ Available |
| **CPU** | Process CPU % | `ps -o %cpu` | ✅ Available |
| **Network** | Socket summary | `ss -s` | ✅ Available |
| **Throughput** | ops/sec | Codahale `Meter` | ✅ Available |
| **Latency** | P50/P99/P99.9/Max (ms) | Codahale `Timer` | ✅ Available |
| **Cosmos op latency** | `cosmos.client.op.latency` | Micrometer (SDK, DEFAULT) | ✅ Available |
| **Cosmos op RUs** | `cosmos.client.op.RUs` | Micrometer (SDK, DEFAULT) | ✅ Available |
| **GW request latency** | `cosmos.client.req.gw.latency` | Micrometer (SDK, DEFAULT) | ✅ Available |
| **GW request count** | `cosmos.client.req.gw.requests` | Micrometer (SDK, DEFAULT) | ✅ Available |
| **Connection pool** | active/idle/pending connections | Reactor Netty `ConnectionProvider.metrics()` | ❌ Not enabled — needs SDK change |
| **H2 streams** | active streams per connection, utilization | Reactor Netty H2 metrics | ❌ Not available — needs SDK change |
| **Shared pool refs** | clients sharing pool | `SharedGatewayHttpClient.counter` | ❌ Not exposed as meter |

#### Execution

```bash
# Full 30-scenario baseline (50 tenants, 1M ops each, ~8-10 hours total)
bash sdk/cosmos/azure-cosmos-benchmark/scripts/run-baseline-matrix.sh \
    sdk/cosmos/azure-cosmos-benchmark/test-setup \
    ~/results

# Quick validation (10 tenants, default 1M ops, ~3-4 hours)
bash sdk/cosmos/azure-cosmos-benchmark/scripts/run-baseline-matrix.sh \
    sdk/cosmos/azure-cosmos-benchmark/test-setup \
    ~/results \
    --tenants 10

# Quick validation with fewer ops (10 tenants, 100K ops, ~1 hour)
bash sdk/cosmos/azure-cosmos-benchmark/scripts/run-baseline-matrix.sh \
    sdk/cosmos/azure-cosmos-benchmark/test-setup \
    ~/results \
    --tenants 10 --operations 100000
```

Each scenario runs as a **separate JVM invocation** with its own system properties and GC log. A per-scenario `tenants-override.json` is generated with the correct operation, connection sharing flag, and operation count. There is a **30-second cool-down** between scenarios.

#### Output Structure

```
~/results/<timestamp>-baseline/
├── matrix-info.json              # Git branch, commit, tenant count, dimensions
├── summary.csv                   # Peak metrics for all 30 scenarios
├── http11-read-isolated/
│   ├── scenario-info.json        # Protocol, workload, sharing, JVM opts, timing
│   ├── tenants-override.json     # Generated tenants config with overrides
│   ├── benchmark.log             # Benchmark output (throughput, latency)
│   ├── monitor.csv               # Resource snapshots every 10s
│   ├── gc.log                    # G1GC log
│   └── ss-summary.txt            # Socket stats
├── http11-read-shared/
│   └── ...
├── http11-readlat-isolated/
│   └── ...
└── ... (30 directories total)
```

#### Key Comparisons

**1. Connection sharing impact** (same protocol + workload, isolated vs shared):
- Does shared pool reduce FDs and thread count?
- Does shared pool affect throughput or latency?
- Expected: fewer FDs/connections, similar or better throughput

**2. Protocol impact** (same workload + sharing, across protocols):
- HTTP/2 vs HTTP/1.1: connection count reduction via multiplexing
- ThinClient vs HTTP/2: binary framing overhead vs throughput gain
- Expected: H2 ~30× fewer connections, similar or better latency

**3. Latency vs throughput operations** (ReadThroughput vs ReadLatency):
- ReadLatency measures per-op P99; ReadThroughput measures aggregate ops/sec
- Same underlying `AsyncReadBenchmark` class, different metric focus

**4. Read vs write** (same protocol + sharing):
- Write operations have higher RU cost → may hit throttling sooner
- Write latency may be higher due to replication

#### When to Re-Run

Re-run this matrix after:
- Any fix from the action items (A1–A22)
- SDK version upgrades
- Connection pool or threading changes
- HTTP/2 or ThinClient configuration changes

Compare results using the `summary.csv` from each run.

---

## 3. Infrastructure & Environment

### 3.1 Cosmos DB Setup

| Resource | Configuration |
|---|---|
| **Accounts** | Use emulator for unit tests; real accounts for perf (≥10 accounts for multi-tenant simulation) |
| **Throughput** | 10,000 RU/s per container (autopilot or provisioned) |
| **Consistency** | Session (default) |
| **Regions** | Single region for baseline; multi-region for S6 |
| **Documents** | 1000 pre-loaded docs per container, ~1 KB each |
| **Partition key** | `/id` (single logical partition per doc) |

### 3.2 Client JVM

| Setting | Value |
|---|---|
| **JDK** | 21 (LTS) |
| **Heap** | `-Xmx8g -Xms8g` (fixed, avoid resize noise) |
| **GC** | `-XX:+UseG1GC` (default) |
| **GC logging** | `-Xlog:gc*:file=gc.log:time,uptime` |
| **Direct memory** | `-XX:MaxDirectMemorySize=2g` |
| **Assertions** | `-ea` (enabled) |
| **JFR** | `-XX:StartFlightRecording=...` for CPU/allocation profiling |
| **Cosmos properties** | Set via system properties per scenario variant |

### 3.3 Machine

**Recommended**: Azure VM in the same region as Cosmos accounts — see [Section 6: Azure VM Test Environment](#6-azure-vm-test-environment) for detailed setup.

| Requirement | Spec |
|---|---|
| **CPU** | ≥16 cores (`Standard_D16s_v5`) |
| **RAM** | ≥64 GB |
| **OS** | Ubuntu 22.04 LTS |
| **Network** | Same Azure region, accelerated networking enabled |

---

## 4. Test Harness Design

### 4.1 Design Philosophy: Reuse `AsyncBenchmark` Per Tenant

Rather than building a standalone harness, we reuse the existing `AsyncBenchmark` machinery. Each tenant (Cosmos account) gets its own `AsyncBenchmark` instance, driven by a JSON config file describing all accounts.

**Why reuse `AsyncBenchmark`**:
- Gets Codahale metrics (Timer, Meter, reporters) for free
- Gets warmup phase, concurrency control (`Semaphore`), result upload (`CosmosTotalResultReporter`) for free
- Gets Micrometer → Application Insights bridge for free (via `Configuration.createAzureMonitorMeterRegistry()`)
- Proven, production-quality workload drivers (read, write, query, mixed)
- Reduces code to write — just orchestration, not workload logic

**Challenges with multiple `AsyncBenchmark` instances** (and mitigations):

| Challenge | Root Cause | Mitigation |
|---|---|---|
| System property mutation | `AsyncBenchmark` constructor sets JVM-global properties for circuit breaker/PPAF | Set once in orchestrator before creating any instances; skip in per-tenant init |
| Static `ManagedIdentityCredential` | Shared across all instances | ~~Fixed~~ — `CREDENTIAL` is now per-instance, built from `Configuration.buildTokenCredential()` |
| Each instance creates its own DB/container | Constructor calls `createDatabaseAndContainerIfNotExist` | Desired — each tenant needs its own container |
| Each instance starts its own reporter | Codahale `ScheduledReporter` per instance | Use one global reporter + per-tenant `MetricRegistry` with prefixed names |
| `run()` blocks until completion | Designed for single-instance use | Run each `AsyncBenchmark.run()` on its own thread via `ExecutorService` |

### 4.2 Architecture

```
MultiTenancyBenchmark (orchestrator — main class)
  │
  ├── Reads tenants.json → List<TenantAccountInfo>
  │
  ├── For each tenant account:
  │     ├── Create Configuration clone with account-specific endpoint/key
  │     ├── new AsyncReadBenchmark(tenantConfig)  ─or─  AsyncQueryBenchmark, etc.
  │     └── Submit benchmark.run() to ExecutorService
  │
  ├── ResourceMonitor (shared, samples all JVM metrics periodically)
  │
  ├── Shared Micrometer MeterRegistry → Application Insights
  │     └── Each AsyncBenchmark's ClientTelemetryConfig uses this registry
  │
  ├── Shared Codahale MetricRegistry (aggregated view)
  │     └── Per-tenant metrics prefixed: "tenant-0.latency", "tenant-1.latency"
  │
  └── ResultRecorder (CSV + Application Insights + optional Cosmos upload)
```

### 4.3 Tenant Configuration: `tenants.json`

Instead of CLI parameters for individual accounts, users provide a JSON file that describes all tenant accounts and per-tenant overrides:

```json
{
    "globalDefaults": {
        "connectionMode": "GATEWAY",
        "consistencyLevel": "SESSION",
        "concurrency": 20,
        "numberOfOperations": 100000,
        "operation": "ReadThroughput",
        "numberOfPreCreatedDocuments": 1000,
        "connectionSharingAcrossClientsEnabled": false,
        "http2Enabled": false,
        "http2MaxConcurrentStreams": 30,
        "maxConnectionPoolSize": 1000,
        "warmupDurationInSeconds": 30,
        "applicationName": "mt-bench"
    },
    "tenants": [
        {
            "id": "tenant-0",
            "serviceEndpoint": "https://account0.documents.azure.com:443/",
            "masterKey": "key0==",
            "databaseId": "benchdb",
            "containerId": "benchcol",
            "overrides": {
                "concurrency": 50
            }
        },
        {
            "id": "tenant-1",
            "serviceEndpoint": "https://account1.documents.azure.com:443/",
            "masterKey": "key1==",
            "databaseId": "benchdb",
            "containerId": "benchcol"
        },
        {
            "id": "tenant-2-managed-identity",
            "serviceEndpoint": "https://account2.documents.azure.com:443/",
            "databaseId": "benchdb",
            "containerId": "benchcol",
            "overrides": {
                "isManagedIdentityRequired": "true",
                "aadManagedIdentityClientId": "client-id-for-tenant-2",
                "aadTenantId": "tenant-id-for-tenant-2"
            }
        }
    ],
    "tenantTemplate": {
        "enabled": false,
        "count": 100,
        "endpointPattern": "https://account{i}.documents.azure.com:443/",
        "keyEnvVarPattern": "COSMOS_KEY_{i}",
        "databaseId": "benchdb",
        "containerIdPattern": "benchcol-{i}"
    }
}
```

**Features**:
- `globalDefaults`: Maps directly to `Configuration` fields — every tenant inherits these
- `tenants[]`: Explicit list of accounts with per-tenant `overrides` for any `Configuration` field
- `tenantTemplate`: For scaling tests with many accounts — generates N tenants from a pattern (endpoint, key from env var, container name). Avoids listing 100+ accounts by hand.

**How it maps to `Configuration`**:

```java
// Pseudocode in MultiTenancyBenchmark
for (TenantAccountInfo tenant : tenants) {
    Configuration tenantConfig = Configuration.fromDefaults();  // base from globalDefaults
    tenantConfig.setServiceEndpoint(tenant.serviceEndpoint);
    tenantConfig.setMasterKey(tenant.masterKey);
    tenantConfig.setDatabaseId(tenant.databaseId);
    tenantConfig.setContainerId(tenant.containerId);
    tenantConfig.applyOverrides(tenant.overrides);              // per-tenant tweaks

    // Set per-tenant applicationName so each CosmosClient has a unique userAgentSuffix.
    // This enables server-side Kusto queries to differentiate traffic by tenant.
    // Format: "mt-bench-<tenantId>" (e.g., "mt-bench-tenant-0")
    String baseName = tenantConfig.getApplicationName();        // from globalDefaults or override
    String tenantSuffix = StringUtils.isNotEmpty(baseName)
        ? baseName + "-" + tenant.id
        : "mt-bench-" + tenant.id;
    tenantConfig.setApplicationName(tenantSuffix);

    AsyncBenchmark<?> benchmark = createBenchmark(tenantConfig); // AsyncReadBenchmark etc.
    executor.submit(() -> benchmark.run());
}
```

**Server-side verification via Kusto**: The `userAgentSuffix` appears in the Cosmos DB server-side request logs as part of the `UserAgent` header. This enables per-tenant traffic analysis from the server perspective:

```kql
// Kusto query on Cosmos DB server-side logs to verify per-tenant traffic
CDBDataPlaneRequests
| where TimeGenerated > ago(1h)
| where UserAgent contains "mt-bench-"
| extend tenantId = extract("mt-bench-([\\w-]+)", 1, UserAgent)
| summarize requestCount=count(), avgRUs=avg(RequestCharge), p99LatencyMs=percentile(DurationMs, 99)
    by tenantId, OperationName, bin(TimeGenerated, 1m)
| order by tenantId, TimeGenerated
```

### 4.4 Key Design Decisions (Revised)

| Decision | Choice | Rationale |
|---|---|---|
| **Reuse `AsyncBenchmark`?** | **Yes** — each tenant gets its own instance running on a dedicated thread | Reuses proven workload drivers, Codahale metrics, warmup, `CosmosTotalResultReporter`. Avoids reimplementing the workload loop. |
| **Config input** | **JSON file** (`tenants.json`) with `globalDefaults` + per-tenant overrides | Cleaner than 100+ CLI params; supports both explicit accounts and template-based generation; easy to version-control test configs |
| **`AsyncBenchmark` thread model** | Each `benchmark.run()` on its own thread in a fixed `ExecutorService` | `run()` blocks (it's a loop with `Semaphore`). Parallelism = number of tenants. |
| **Metrics isolation** | Each tenant gets its own `MetricRegistry` (Codahale) with prefixed metric names, plus a global aggregate registry | Prevents metric name collisions; enables both per-tenant and aggregate views |
| **Micrometer / App Insights** | Shared `MeterRegistry` across all tenants; SDK-level metrics auto-tagged by account endpoint | Application Insights handles multi-dimensional metrics natively; custom dimensions identify each tenant |
| **Reporter** | One shared `CosmosTotalResultReporter` consuming the global aggregate `MetricRegistry` | Avoids N reporters writing to the same Cosmos container concurrently |
| **System properties** | Set once by orchestrator before any `AsyncBenchmark` constructor; patched to skip in constructor | Prevents N instances from clobbering each other's system properties |
| **Connection sharing** | Configurable per-run in `globalDefaults.connectionSharingAcrossClientsEnabled` | Tests A4 from analysis |
| **Resource monitoring** | Separate `ResourceMonitor` thread, independent of `AsyncBenchmark` | `AsyncBenchmark` doesn't track JVM-level resources (heap, threads, FDs) |

### 4.5 Design Comparison: Reuse `AsyncBenchmark` vs. Per-Tenant Full-Control Architecture

Before diving into specific limitations, it's worth comparing the two fundamental design approaches. The current design (§4.1–4.4) reuses `AsyncBenchmark` as-is, with each tenant getting its own instance. An alternative "Full-Control" design would give each tenant its own independently-configured `CosmosAsyncClient` built directly by the orchestrator, bypassing `AsyncBenchmark`'s constructor entirely.

#### 4.5.1 Side-by-Side Comparison

| Aspect | **Current Design: Reuse `AsyncBenchmark`** | **Full-Control Design: Orchestrator Builds Clients** |
|---|---|---|
| **Per-tenant `CosmosAsyncClient`** | Yes — each `AsyncBenchmark` instance builds its own client in the constructor | Yes — orchestrator builds each client with tenant-specific `CosmosClientBuilder` settings |
| **System properties (`COSMOS.*`)** | 🔴 **JVM-global** — `AsyncBenchmark` constructor calls `System.setProperty()` for circuit breaker, PPAF, min pool size. Last writer wins. Must be patched to skip or set-once. | ✅ **Avoided** — orchestrator sets system properties once (or uses builder-level API where available). No per-instance mutation. |
| **`TokenCredential` (managed identity)** | ✅ **Per-instance** — `CREDENTIAL` is now built per-instance via `Configuration.buildTokenCredential()`, using instance-level AAD fields (`-aadLoginEndpoint`, `-aadTenantId`, `-aadManagedIdentityClientId`). Falls back to system property / env var if not set. | ✅ **Per-tenant** — each tenant can have its own `TokenCredential` (different managed identity client IDs, different tenants). |
| **AAD config (`loginUri`, `tenantId`, `managedIdentityId`)** | ✅ **Per-instance** — `Configuration` now has instance-level fields (`-aadLoginEndpoint`, `-aadTenantId`, `-aadManagedIdentityClientId`) with `getInstanceAad*()` getters. Each tenant's `Configuration` can specify its own AAD identity via `tenants.json` overrides. Static methods retained for backward compatibility. | ✅ **Per-tenant** — each `TenantAccountInfo` can specify its own `aadLoginEndpoint`, `aadTenantId`, `managedIdentityClientId`. Orchestrator builds `DefaultAzureCredentialBuilder` per tenant. |
| **`MeterRegistry` (Micrometer)** | 🟡 **Lazy singleton per `Configuration` instance** — `azureMonitorMeterRegistry()` and `graphiteMeterRegistry()` are `synchronized` and cache on first call. Each tenant's `Configuration` creates its own registry, but all read the same system properties / env vars for App Insights connection string. | ✅ **Shared or per-tenant** — orchestrator creates one shared `AzureMonitorMeterRegistry` and wraps it with per-tenant common tags (`tenantId`). Or creates separate registries per tenant if needed. |
| **`TelemetryConfiguration.getActive()`** | 🔴 **Global singleton** — `azureMonitorMeterRegistry()` calls `TelemetryConfiguration.getActive().setRoleName(...)`. This is process-global — last writer wins. | ✅ **Set once** — orchestrator sets role name once before creating any registry. |
| **Codahale `MetricRegistry`** | 🟡 **Per-instance** — each `AsyncBenchmark` has its own `metricsRegistry`. Good isolation. But metric names (`#Successful Operations`, `Latency`) collide in aggregated views. | ✅ **Per-tenant with prefixes** — orchestrator creates registries with prefixed names (`tenant-0.latency`, `tenant-1.latency`) + a global aggregate. |
| **`ScheduledReporter`** | 🟡 **Per-instance** — each `AsyncBenchmark` starts its own `ConsoleReporter`/`CsvReporter`/`GraphiteReporter`. N instances = N reporters writing concurrently (noisy console, file contention). | ✅ **Single shared reporter** — orchestrator runs one reporter against the global aggregate registry. Per-tenant data is in CSV or App Insights via tags. |
| **Connection mode config** | ✅ **Per-instance** — each `Configuration` has its own `connectionMode`, `maxConnectionPoolSize`. Applied to that tenant's `CosmosClientBuilder`. | ✅ **Per-tenant** — same, via `tenants.json` overrides. |
| **Circuit breaker enabled/disabled** | 🔴 **Cannot differ per tenant** — controlled via `System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", ...)` which is JVM-global. Tenant A wants circuit breaker on, Tenant B wants it off → impossible. | 🟡 **Still JVM-global** — the Cosmos SDK reads this from system properties internally. No builder-level API exists today. But orchestrator can at least set it once consistently, and the test plan can test one value at a time across runs. |
| **PPAF (Per-Partition Automatic Failover)** | 🔴 **Cannot differ per tenant** — `COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED` is a system property. Same JVM-global issue. | 🟡 **Same SDK limitation** — but orchestrator avoids the N-writers race. |
| **`connectionSharingAcrossClientsEnabled`** | ✅ **Builder-level** — `CosmosClientBuilder.connectionSharingAcrossClientsEnabled(true)`. Per-tenant in both designs. | ✅ **Same** — per-tenant via `CosmosClientBuilder`. |
| **HTTP/2 + ThinClient** | ✅ **Builder-level** (when available) — per-tenant in both designs. | ✅ **Same** — per-tenant via builder. |
| **Preferred regions** | ✅ **Per-instance** — `Configuration.getPreferredRegionsList()` is per-instance. | ✅ **Per-tenant** — each tenant can have different preferred regions. |
| **Consistency level** | ✅ **Per-instance** — `Configuration.getConsistencyLevel()` applied to builder. | ✅ **Per-tenant** — same. |
| **Database / Container creation** | ✅ **Per-instance** — each `AsyncBenchmark` constructor creates its own DB/container. Desired for multi-tenant isolation. | ✅ **Per-tenant** — orchestrator creates DB/containers (can parallelize). |
| **Workload driver (`performWorkload`)** | ✅ **Proven** — reuses `AsyncReadBenchmark`, `AsyncQueryBenchmark`, etc. Zero new workload code. | 🔴 **Must reimplement** — or extract workload logic from `AsyncBenchmark` subclasses into reusable components. Significant effort. |
| **Concurrency control (`Semaphore`)** | ✅ **Per-instance** — each `AsyncBenchmark` has its own `Semaphore(concurrency)`. | Must be reimplemented or extracted. |
| **Warmup / skip-warmup** | ✅ **Built-in** — `skipWarmUpOperations`, `warmupMode` logic. | Must be reimplemented. |
| **Result upload (`CosmosTotalResultReporter`)** | ✅ **Built-in** — handles Cosmos result upload. | Must be reimplemented or extracted. |
| **Env var fallback (`tryGetValuesFromSystem`)** | 🔴 **Process-global** — reads from env vars like `SERVICE_END_POINT`, `MASTER_KEY`. All tenants would read the same env vars. Not useful for multi-tenant. | ✅ **Irrelevant** — tenant configs come from `tenants.json`, not env vars. |
| **Code to write** | 🟢 **Minimal** — orchestrator + resource monitor + JSON config reader. ~500–800 LOC. | 🔴 **Substantial** — must reimplement or extract workload loop, semaphore, metrics, warmup, reporter, result upload. ~2000–3000 LOC. |
| **Risk** | 🟡 Moderate — must carefully patch `AsyncBenchmark` to avoid global-state conflicts. Fragile if `AsyncBenchmark` changes upstream. | 🟡 Moderate — more code = more bugs, but cleaner architecture. No fragile patches. |

#### 4.5.2 What Can vs. Cannot Be Per-Tenant Today

Based on the analysis of [AsyncBenchmark.java](sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/AsyncBenchmark.java) and [Configuration.java](sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/Configuration.java):

**✅ Already per-tenant via `CosmosClientBuilder` (works in both designs)**:
- `endpoint` / `key` / `credential`
- `preferredRegions`
- `consistencyLevel`
- `connectionMode` (DIRECT vs GATEWAY)
- `maxConnectionPoolSize` (via `GatewayConnectionConfig`)
- `connectionSharingAcrossClientsEnabled`
- `contentResponseOnWriteEnabled`
- `userAgentSuffix`
- `regionScopedSessionCapturingEnabled` (via bridge accessor)
- `clientTelemetryConfig` (diagnostics thresholds, meter registry, diagnostics handler)
- Proactive connection management (`openConnectionsAndInitCaches`)

**🔴 JVM-global today — cannot differ per tenant in a single JVM**:
- `COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG` — system property, read by SDK internals
- `COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED` — system property
- `COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED` — system property
- `COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF` — system property
- `COSMOS.E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF` — system property
- `COSMOS.STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS` — system property
- `COSMOS.ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS` — system property
- `COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT` — system property
- AAD login endpoint, tenant ID, managed identity ID — static methods reading system properties
- `TelemetryConfiguration.getActive()` — Application Insights global singleton
- Environment variable-based config (`SERVICE_END_POINT`, `MASTER_KEY`, etc.) — process-global

**🟡 Currently global but fixable in Full-Control design**:
- `MeterRegistry` — lazy singleton in `Configuration`, but can be shared with per-tenant tags in the orchestrator

**✅ Recently fixed (moved from global to per-instance)**:
- `TokenCredential` — was a static field in `AsyncBenchmark`; now built per-instance via `Configuration.buildTokenCredential()` with instance-level AAD fields (`-aadLoginEndpoint`, `-aadTenantId`, `-aadManagedIdentityClientId`)
- AAD config (`loginUri`, `tenantId`, `managedIdentityId`) — `Configuration` now has instance-level getters (`getInstanceAadLoginEndpoint()`, etc.) that fall back to the static system-property/env-var values if not set per-instance

#### 4.5.3 Recommendation

**Use the Current Design (Reuse `AsyncBenchmark`) with targeted patches**:

1. **It's 3–4× less code** — the workload loop, semaphore, metrics, warmup, and result upload are battle-tested.
2. **The JVM-global system properties are test-level variables, not per-tenant variables** — in the baseline matrix (§8.1), circuit breaker and PPAF are held constant across all tenants within a run. We vary them *between runs*, not *within a run*. So set-once-per-run is sufficient.
3. **Per-tenant config independence is achieved via `tenants.json` overrides** — each tenant gets its own `Configuration` instance with its own endpoint, key, connection pool size, concurrency, consistency level, etc. The only things that can't differ per-tenant are SDK-internal system properties, which is an SDK limitation, not a harness limitation.
4. **If a future test requires per-tenant circuit breaker or PPAF differences**, that's a signal to file an SDK feature request for builder-level APIs for these settings, rather than working around it in the benchmark harness.

The Full-Control design is the right long-term architecture if the benchmark evolves into a production multi-tenant simulator, but for baseline measurement it's over-engineering.

### 4.6 Handling `AsyncBenchmark` Limitations (Patches for Current Design)

#### Problem 1: System Property Conflicts

`AsyncBenchmark` constructor sets JVM-global system properties (circuit breaker, PPAF). With N instances, the last one wins.

**Solution**: Extract system property setup into `MultiTenancyBenchmark` orchestrator. Set once before creating any `AsyncBenchmark`. Add a `Configuration` flag (e.g., `skipSystemPropertyInit`) that tenant configs set to `true`.

```java
// In MultiTenancyBenchmark (orchestrator), before creating any AsyncBenchmark:
if (globalDefaults.isPartitionLevelCircuitBreakerEnabled()) {
    System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", "...");
    // ... other circuit breaker properties
}
if (globalDefaults.isPerPartitionAutomaticFailoverRequired()) {
    System.setProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED", "true");
    // ... other PPAF properties
}

// Then for each tenant:
tenantConfig.setSkipSystemPropertyInit(true); // new flag
AsyncBenchmark<?> benchmark = createBenchmark(tenantConfig);
```

#### Problem 2: ~~Static `TokenCredential` Singleton~~ (Fixed)

The `CREDENTIAL` field was `private static final` and initialized at class-load time with a single managed identity.

**Solution (implemented)**: Removed the static `CREDENTIAL` field from all benchmark classes (`AsyncBenchmark`, `SyncBenchmark`, `AsyncEncryptionBenchmark`, `AsyncCtlWorkload`). Credential is now built per-instance via `Configuration.buildTokenCredential()`. Added instance-level AAD fields to `Configuration` (`-aadLoginEndpoint`, `-aadTenantId`, `-aadManagedIdentityClientId`) with `getInstanceAad*()` getters that fall back to the existing static/system-property values when not set. Each tenant in `tenants.json` can now specify its own AAD identity via overrides.

#### Problem 3: Per-Instance Database/Container Creation

Each `AsyncBenchmark` creates its database/container and pre-populates docs in the constructor. This is **desired** for multi-tenancy — each tenant should have its own container.

**Caution**: With 100+ tenants, constructors run sequentially (each does sync HTTP calls to create DB/container + populate docs). Budget ~5–10 seconds per tenant = ~8–16 minutes for 100 tenants. Consider parallelizing construction.

#### Problem 4: `run()` Blocks

`AsyncBenchmark.run()` is a blocking loop. With N tenants, we need N threads.

**Solution**: 
```java
ExecutorService tenantExecutor = Executors.newFixedThreadPool(numTenants);
List<Future<?>> futures = tenants.stream()
    .map(benchmark -> tenantExecutor.submit(() -> benchmark.run()))
    .collect(Collectors.toList());

// Wait for all tenants to finish
for (Future<?> f : futures) { f.get(); }
```

#### Problem 5: Lifecycle Coordination

All tenants should start workload approximately simultaneously (after all are constructed and warmed up).

**Solution**: Use a `CountDownLatch` — each tenant's `run()` waits on the latch before starting the workload loop. The orchestrator counts down after all tenants are constructed and warmed up.

#### Problem 6: Reporter Noise

Each `AsyncBenchmark` starts its own `ScheduledReporter`. With 100 tenants, that's 100 console reporters printing every 10 seconds.

**Solution**: Add a `Configuration` flag `suppressReporter` for tenant configs. The orchestrator runs a single shared reporter against the global aggregate `MetricRegistry`.

### 4.7 ResourceMonitor — What to Collect

```java
public class ResourceMonitor {
    // Collected every monitorIntervalSec and on-demand (pre/post lifecycle events)

    // ── Heap ──
    long usedHeapBytes;        // Runtime.totalMemory() - Runtime.freeMemory()
    long maxHeapBytes;         // Runtime.maxMemory()
    // For accurate post-GC measurement, call System.gc() before snapshot (only at lifecycle events)

    // ── Direct Memory ──
    long nettyDirectMemBytes;  // io.netty.util.internal.PlatformDependent.usedDirectMemory()
    long jdkDirectPoolBytes;   // BufferPoolMXBean "direct" — memoryUsed
    long jdkMappedPoolBytes;   // BufferPoolMXBean "mapped" — memoryUsed

    // ── Threads ──
    int liveThreadCount;                   // ThreadMXBean.getThreadCount()
    int daemonThreadCount;                 // ThreadMXBean.getDaemonThreadCount()
    Map<String, Integer> threadsByPrefix;  // Thread.getAllStackTraces().keySet() grouped by name prefix
    // Key prefixes to track: "cosmos-global-endpoint-mgr", "reactor-http-nio", "parallel",
    //                         "cosmos-parallel", "boundedElastic", "pool-", "ScheduledExecutor"

    // ── CPU ──
    double processCpuLoad;     // OperatingSystemMXBean.getProcessCpuLoad()
    double systemCpuLoad;      // OperatingSystemMXBean.getCpuLoad()

    // ── GC ──
    long gcCount;              // Σ GarbageCollectorMXBean.getCollectionCount()
    long gcTimeMs;             // Σ GarbageCollectorMXBean.getCollectionTime()

    // ── File Descriptors (Linux) ──
    long openFileDescriptors;  // UnixOperatingSystemMXBean.getOpenFileDescriptorCount()
    long maxFileDescriptors;   // UnixOperatingSystemMXBean.getMaxFileDescriptorCount()

    // ── Cosmos-Specific ──
    int activeCosmosClients;            // RxDocumentClientImpl.activeClientsCnt (via diagnostics)
    Map<String, Integer> cacheSizes;    // queryPlanCache, collectionCache, pkRangeCache per client
}
```

### 4.8 Test Configuration (CLI)

The orchestrator `MultiTenancyBenchmark` has its own minimal CLI. Workload-level config is in `tenants.json`.

| Option | Type | Default | Description |
|---|---|---|---|
| `--tenantsFile` | String | **required** | Path to `tenants.json` |
| `--scenario` | enum | `SCALING` | `SCALING`, `CHURN`, `CACHE_GROWTH`, `POOL_PRESSURE`, `SOAK` |
| `--monitorIntervalSec` | int | 10 | Resource monitor sampling interval |
| `--outputDir` | String | `./results` | Directory for CSVs and summary |
| `--churnCycles` | int | 100 | For `CHURN` scenario: number of create/destroy cycles |
| `--distinctQueries` | int | 100 | For `CACHE_GROWTH` scenario |
| `--appInsightsConnectionString` | String | null | Override for App Insights (or use env `APPLICATIONINSIGHTS_CONNECTION_STRING`) |
| `--skipWarmup` | boolean | false | Skip warmup phase (for idle scaling tests) |
| `--threadDumpIntervalSec` | int | 0 (disabled) | If >0, capture a thread dump every N seconds to `<outputDir>/thread-dumps/` |
| `--threadDumpOnEvent` | String | null | Comma-separated lifecycle events to trigger thread dump: `PRE_CREATE`, `POST_CREATE`, `POST_WORKLOAD`, `POST_CLOSE` |
| `--heapDumpOnEvent` | String | null | Comma-separated lifecycle events to trigger heap dump (via `jmap`): `POST_CREATE`, `POST_WORKLOAD`, `POST_CLOSE` |
| `--jfrDurationSec` | int | 300 | Duration of JFR recording (0 = disabled) |
| `--branch` | String | auto-detect | Git branch name (auto-detected from repo; override for detached HEAD) |
| `--commitId` | String | auto-detect | Git commit SHA (auto-detected; override for non-git builds) |
| `--prNumber` | String | null | GitHub PR number (for before/after tracking across PRs) |
| `--resultSink` | enum | `CSV` | Where to store results: `CSV`, `COSMOS`, `KUSTO`, `ALL` |
| `--resultCosmosEndpoint` | String | null | Cosmos DB endpoint for result storage (if `--resultSink` includes `COSMOS`) |
| `--resultCosmosDatabase` | String | `benchresults` | Database name for result storage |
| `--resultCosmosContainer` | String | `runs` | Container name for result storage |
| `--resultKustoCluster` | String | null | Kusto cluster URI (if `--resultSink` includes `KUSTO`) |
| `--resultKustoDatabase` | String | `BenchmarkResults` | Kusto database name |

---

## 5. Observability: OpenTelemetry, Micrometer & Application Insights

### 5.1 Existing Telemetry Stack in the Benchmark

The benchmark module already has a two-layer metrics stack:

| Layer | Library | What It Captures | Export Target |
|---|---|---|---|
| **Benchmark-level** | Codahale/Dropwizard Metrics 4.1 | Throughput (`Meter`), latency (`Timer` with HdrHistogram), success/failure counts | Graphite, CSV, Console, `CosmosTotalResultReporter` (→ Cosmos DB) |
| **SDK-level** | Micrometer 1.15 | Cosmos operation latency, request charge, request count, direct/gateway-level metrics | Azure Monitor (App Insights), Graphite |

These are set up in `AsyncBenchmark` constructor and `Configuration`:
- `Configuration.createAzureMonitorMeterRegistry()` — creates an `AzureMonitorMeterRegistry` if `APPLICATIONINSIGHTS_CONNECTION_STRING` or `AZURE_INSTRUMENTATION_KEY` is set
- The registry is attached to the `CosmosAsyncClient` via `CosmosClientTelemetryConfig.metricsOptions(new CosmosMicrometerMetricsOptions(registry))`

### 5.2 What the SDK Emits via Micrometer (and thus to App Insights)

The Cosmos SDK emits these metrics when a Micrometer `MeterRegistry` is attached:

| Metric Name | Type | Tags | Description |
|---|---|---|---|
| `cosmos.client.op.latency` | Timer | `operation`, `statusCode`, `region`, `endpoint` | End-to-end operation latency |
| `cosmos.client.op.calls` | Counter | same | Operation count |
| `cosmos.client.op.RUs` | DistributionSummary | same | Request Units consumed |
| `cosmos.client.req.gw.latency` | Timer | `endpoint`, `statusCode` | Gateway request latency |
| `cosmos.client.req.gw.requests` | Counter | same | Gateway request count |
| `cosmos.client.system.avgCpuLoad` | Gauge | — | Process CPU |
| `cosmos.client.req.rntbd.*` | various | — | RNTBD metrics (not relevant for Gateway) |

**Key insight for multi-tenancy**: The `endpoint` tag naturally distinguishes tenants. When all tenants share one `MeterRegistry` → App Insights, you can slice metrics by `endpoint` to compare per-tenant performance.

### 5.3 Enhancing Telemetry for Multi-Tenancy Tests

#### 5.3.1 Add Custom Dimensions

Add a custom `tenantId` tag to all SDK metrics so App Insights can filter/group by tenant:

```java
// When building each tenant's CosmosAsyncClient:
MeterRegistry sharedRegistry = ...; // shared AzureMonitorMeterRegistry
MeterRegistry tenantRegistry = new MeterRegistry(sharedRegistry) {
    // Add common tags for this tenant
};
tenantRegistry.config().commonTags("tenantId", tenant.id);

// Attach to client
clientTelemetryConfig.metricsOptions(
    new CosmosMicrometerMetricsOptions(tenantRegistry));
```

#### 5.3.2 Resource Monitor Metrics → Micrometer

Export `ResourceMonitor` data points as Micrometer gauges so they flow to App Insights alongside SDK metrics:

```java
Gauge.builder("multitenancy.heap.used", monitor, ResourceMonitor::getUsedHeapBytes)
    .register(sharedRegistry);
Gauge.builder("multitenancy.threads.live", monitor, ResourceMonitor::getLiveThreadCount)
    .register(sharedRegistry);
Gauge.builder("multitenancy.fd.open", monitor, ResourceMonitor::getOpenFileDescriptors)
    .register(sharedRegistry);
Gauge.builder("multitenancy.clients.active", monitor, ResourceMonitor::getActiveCosmosClients)
    .register(sharedRegistry);
```

#### 5.3.3 Connection Pool Metrics via Reactor Netty

Reactor Netty supports connection pool metrics via Micrometer. Currently **not enabled** in the Cosmos SDK. To capture them:

```java
// Option A: Enable in Reactor Netty globally (affects all pools in JVM)
Metrics.observeHttpClient(true); // reactor.netty.Metrics

// Option B: Per-ConnectionProvider (requires SDK code change)
ConnectionProvider.builder("cosmos-pool")
    .metrics(true, () -> new MicrometerChannelMetricsRecorder("cosmos.pool", Protocol.HTTP11))
    .build();
```

This emits: `reactor.netty.connection.provider.active.connections`, `reactor.netty.connection.provider.idle.connections`, `reactor.netty.connection.provider.pending.acquire`.

### 5.4 Application Insights for Results Comparison

#### 5.4.1 Why App Insights

| Capability | Benefit for Multi-Tenancy Tests |
|---|---|
| **Multi-dimensional queries** | Slice latency by `tenantId`, `operation`, `statusCode`, `region` |
| **Time-series charts** | Visualize heap/threads/connections over time — detect trends and leaks |
| **Before/after comparison** | Compare two test runs by time range or custom `testRunId` dimension |
| **Alerting** | Set alerts on metric thresholds (e.g., thread count > 500) |
| **Log Analytics (KQL)** | Write complex queries: "P99 latency per tenant where clients > 50" |

#### 5.4.2 Setup

1. **Create an App Insights resource** in the same Azure region as the test VM
2. **Set the connection string** via environment variable:
   ```bash
   export APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=xxx;..."
   export APPLICATIONINSIGHTS_ROLE_NAME="multi-tenancy-benchmark"
   export APPLICATIONINSIGHTS_ROLE_INSTANCE="vm-eastus-1"
   ```
3. The existing `Configuration.createAzureMonitorMeterRegistry()` will pick it up automatically
4. Add a custom `testRunId` dimension to all metrics for run isolation:
   ```java
   registry.config().commonTags(
       "testRunId", runId,          // e.g., "B4-20260218T143022Z"
       "scenario", scenario.name(), // e.g., "S2a"
       "branch", gitBranch          // e.g., "multi-tenancy-analysis"
   );
   ```

#### 5.4.3 Sample KQL Queries for Comparison

**Per-tenant P99 latency comparison**:
```kql
customMetrics
| where name == "cosmos.client.op.latency" and timestamp > ago(1h)
| extend tenantId = tostring(customDimensions["tenantId"])
| summarize p99=percentile(value, 99) by tenantId, bin(timestamp, 1m)
| render timechart
```

**Before/after heap comparison**:
```kql
customMetrics
| where name == "multitenancy.heap.used"
| extend testRunId = tostring(customDimensions["testRunId"])
| where testRunId in ("B4-before", "B4-after")
| summarize avg(value) by testRunId, bin(timestamp, 30s)
| render timechart
```

**Thread leak detection over churn cycles**:
```kql
customMetrics
| where name == "multitenancy.threads.live" and customDimensions["scenario"] == "CHURN"
| summarize maxThreads=max(value) by bin(timestamp, 10s)
| render timechart
```

#### 5.4.4 Server-Side Verification via Cosmos DB Kusto

Because each tenant's `CosmosAsyncClient` is built with a unique `userAgentSuffix` (format: `mt-bench-<tenantId>`), you can verify traffic from the server's perspective using Cosmos DB internal Kusto logs:

**Per-tenant request volume and RU consumption**:
```kql
CDBDataPlaneRequests
| where TimeGenerated > ago(1h)
| where UserAgent contains "mt-bench-"
| extend tenantId = extract("mt-bench-([\\w-]+)", 1, UserAgent)
| summarize
    totalRequests = count(),
    totalRUs = sum(RequestCharge),
    avgLatencyMs = avg(DurationMs),
    p99LatencyMs = percentile(DurationMs, 99)
    by tenantId, OperationName
| order by totalRequests desc
```

**Connection distribution per tenant (verify connection sharing)**:
```kql
CDBDataPlaneRequests
| where TimeGenerated > ago(1h)
| where UserAgent contains "mt-bench-"
| extend tenantId = extract("mt-bench-([\\w-]+)", 1, UserAgent)
| summarize distinctConnections = dcount(ClientIpAddress) by tenantId
| order by distinctConnections desc
```

**Throttling (429) by tenant — detect hotspots**:
```kql
CDBDataPlaneRequests
| where TimeGenerated > ago(1h)
| where UserAgent contains "mt-bench-"
| where StatusCode == 429
| extend tenantId = extract("mt-bench-([\\w-]+)", 1, UserAgent)
| summarize throttledRequests = count() by tenantId, bin(TimeGenerated, 1m)
| render timechart
```

---

## 6. Azure VM Test Environment

### 6.1 Why Azure VM

| Reason | Detail |
|---|---|
| **Network proximity** | Same-region as Cosmos DB accounts → <1ms network latency, no cross-region noise |
| **Consistent environment** | Reproducible CPU/memory/network across runs (vs developer laptop variance) |
| **Linux access** | `/proc/self/fd`, `ss`, `perf`, `async-profiler` — essential for FD/connection/CPU profiling |
| **High FD limits** | Azure VMs already have sufficient FD limits by default (`ulimit -n 65536`+) |
| **Scale** | Can provision 16–64 core VMs for high-concurrency tests without thermal throttling |
| **Automation** | Script VM creation, test execution, result collection, VM teardown |

### 6.2 Recommended VM Configuration

| Setting | Value | Why |
|---|---|---|
| **VM Size** | `Standard_D16s_v5` (16 vCPUs, 64 GB RAM) | Enough headroom for 200+ clients without CPU bottleneck |
| **OS** | Ubuntu 22.04 LTS | Stable, well-supported, `/proc` filesystem for monitoring |
| **Region** | Same as Cosmos DB accounts | Minimize network latency |
| **Disk** | Premium SSD (P30, 1 TB) | Fast I/O for GC logs, CSV output, JFR recordings |
| **Accelerated Networking** | **Enabled** | Lower network latency, higher throughput |
| **Proximity Placement Group** | Optional | If multiple VMs needed for distributed tests |
| **Spot instance** | OK for non-critical runs | Cost savings for long soak tests |

### 6.3 VM Setup Script

```bash
#!/bin/bash
# setup-benchmark-vm.sh — Run once after VM creation

# ── JDK + networking tools ──
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk maven git net-tools iproute2 sysstat

# ── Async-profiler (CPU/allocation profiling) ──
wget -O /tmp/async-profiler.tar.gz \
  https://github.com/async-profiler/async-profiler/releases/download/v3.0/async-profiler-3.0-linux-x64.tar.gz
sudo tar -xzf /tmp/async-profiler.tar.gz -C /opt/
echo 'export PATH=$PATH:/opt/async-profiler-3.0-linux-x64/bin' >> ~/.bashrc

# ── Clone SDK ──
git clone https://github.com/Azure/azure-sdk-for-java.git ~/azure-sdk-for-java
cd ~/azure-sdk-for-java
git checkout multi-tenancy-analysis

# ── Build benchmark module ──
mvn install -pl sdk/cosmos/azure-cosmos -am -DskipTests
mvn package -pl sdk/cosmos/azure-cosmos-benchmark -DskipTests

echo "Setup complete. Set APPLICATIONINSIGHTS_CONNECTION_STRING and create tenants.json."
```

### 6.4 Running Tests on Azure VM

```bash
#!/bin/bash
# run-benchmark.sh — Run a benchmark scenario with auto-detected git metadata
#
# Usage:
#   ./run-benchmark.sh <scenario> <tenants-file> [output-dir] [--branch <branch>] [--pr <number>] [--result-sink <CSV|COSMOS|KUSTO|ALL>]

set -euo pipefail

SCENARIO=${1:-SCALING}
TENANTS_FILE=${2:-tenants.json}
OUTPUT_DIR=${3:-./results/$(date +%Y%m%dT%H%M%S)-${SCENARIO}}
BRANCH=""
PR_NUMBER=""
RESULT_SINK="CSV"
EXTRA_ARGS=""

shift 3 2>/dev/null || true
while [[ $# -gt 0 ]]; do
    case $1 in
        --branch)      BRANCH="$2"; shift ;;
        --pr)          PR_NUMBER="$2"; shift ;;
        --result-sink) RESULT_SINK="$2"; shift ;;
        *)             EXTRA_ARGS="$EXTRA_ARGS $1" ;;
    esac
    shift
done

mkdir -p "$OUTPUT_DIR"

# Auto-detect git metadata if not provided
if [[ -z "$BRANCH" ]]; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
fi
COMMIT_ID=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
COMMIT_MSG=$(git log -1 --pretty=%s 2>/dev/null || echo "")

echo "=== Benchmark Run ==="
echo "  Scenario:  $SCENARIO"
echo "  Branch:    $BRANCH"
echo "  Commit:    $COMMIT_ID"
echo "  PR:        ${PR_NUMBER:-none}"
echo "  Sink:      $RESULT_SINK"
echo "  Output:    $OUTPUT_DIR"

# Save git metadata for result correlation
cat > "${OUTPUT_DIR}/git-info.json" <<EOF
{
    "branch": "$BRANCH",
    "commitId": "$COMMIT_ID",
    "commitMessage": "$(echo "$COMMIT_MSG" | sed 's/"/\\"/g')",
    "prNumber": "${PR_NUMBER:-null}",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

# JVM flags
JVM_OPTS="-Xmx8g -Xms8g \
  -XX:+UseG1GC \
  -XX:MaxDirectMemorySize=2g \
  -Xlog:gc*:file=${OUTPUT_DIR}/gc.log:time,uptime,level \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${OUTPUT_DIR}/ \
  -XX:StartFlightRecording=duration=300s,filename=${OUTPUT_DIR}/flight.jfr,settings=profile"

# App Insights (set in env or here)
export APPLICATIONINSIGHTS_ROLE_NAME="multi-tenancy-benchmark"
export APPLICATIONINSIGHTS_ROLE_INSTANCE="$(hostname)"

# Run
java $JVM_OPTS \
  -jar sdk/cosmos/azure-cosmos-benchmark/target/azure-cosmos-benchmark-*-jar-with-dependencies.jar \
  --scenario "$SCENARIO" \
  --tenantsFile "$TENANTS_FILE" \
  --outputDir "$OUTPUT_DIR" \
  --branch "$BRANCH" \
  --commitId "$COMMIT_ID" \
  ${PR_NUMBER:+--prNumber "$PR_NUMBER"} \
  --resultSink "$RESULT_SINK" \
  --monitorIntervalSec 10 \
  $EXTRA_ARGS \
  2>&1 | tee "${OUTPUT_DIR}/benchmark.log"

# Collect system metrics snapshot
ss -s > "${OUTPUT_DIR}/ss-summary.txt"
cat /proc/self/status > "${OUTPUT_DIR}/proc-status.txt" 2>/dev/null || true

echo "Results in: $OUTPUT_DIR"
```

### 6.5 Azure VM Provisioning Script

The provisioning script supports two modes: **use an existing VM** or **create a new one**. Authentication uses **SSH key pairs** (Azure default). Password auth is not supported.

**SSH key options** (pick one):
| Option | What it does |
|---|---|
| `--ssh-key <pub-key-path>` | Use a **pre-existing** key pair. Provide the `.pub` file; the private key is derived by stripping the `.pub` suffix. |
| `--create-key [<path>]` | **Generate a new** key pair. Default path: `~/.ssh/cosmos-bench-<vm-name>`. The key is created with `ssh-keygen` and used for the VM. |
| *(neither)* | Falls back to `az vm create --generate-ssh-keys`, which reuses `~/.ssh/id_rsa` if it exists or creates it if not. |

```bash
#!/bin/bash
# provision-benchmark-vm.sh — Create a new VM or connect to an existing one
#
# Authentication: Uses SSH key pairs (not passwords).
#
# SSH Key Modes:
#   --ssh-key <pub-key-path>    Use a pre-existing key pair (provide .pub file)
#   --create-key [<path>]       Generate a new key pair (default: ~/.ssh/cosmos-bench-<vm-name>)
#   (neither)                   Falls back to az vm create --generate-ssh-keys (~/.ssh/id_rsa)
#
# Usage:
#   ./provision-benchmark-vm.sh --new --location eastus [--ssh-key <pub>|--create-key [path]] [options]
#   ./provision-benchmark-vm.sh --existing --ip <ip> --user <user> --key <private-key-path>
#   ./provision-benchmark-vm.sh --existing --rg <rg-name> --vm-name <vm-name> --key <private-key-path>

set -euo pipefail

MODE=""
LOCATION="eastus"
RG="rg-cosmos-benchmark"
VM_NAME="vm-benchmark-01"
VM_SIZE="Standard_D16s_v5"
VM_IP=""
SSH_USER="benchuser"
SSH_PRIVATE_KEY=""           # Path to private key for connecting (e.g., ~/.ssh/id_rsa)
SSH_PUBLIC_KEY=""            # Path to public key for VM creation (e.g., ~/.ssh/id_rsa.pub)
CREATE_KEY=false             # Whether to generate a new key pair
CREATE_KEY_PATH=""           # Optional: custom path for the generated key
DISK_SIZE=256
SETUP_AFTER_CREATE=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --new)          MODE="new" ;;
        --existing)     MODE="existing" ;;
        --location)     LOCATION="$2"; shift ;;
        --rg)           RG="$2"; shift ;;
        --vm-name)      VM_NAME="$2"; shift ;;
        --size)         VM_SIZE="$2"; shift ;;
        --ip)           VM_IP="$2"; shift ;;
        --user)         SSH_USER="$2"; shift ;;
        --key)          SSH_PRIVATE_KEY="$2"; shift ;;
        --ssh-key)      SSH_PUBLIC_KEY="$2"; shift ;;
        --create-key)
            CREATE_KEY=true
            # Check if next arg is a path (not another flag)
            if [[ $# -gt 1 && ! "$2" =~ ^-- ]]; then
                CREATE_KEY_PATH="$2"; shift
            fi
            ;;
        --disk-size)    DISK_SIZE="$2"; shift ;;
        --skip-setup)   SETUP_AFTER_CREATE=false ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

# Helper: build SSH command with key if provided
ssh_cmd() {
    local cmd="ssh"
    if [[ -n "$SSH_PRIVATE_KEY" ]]; then
        cmd="$cmd -i $SSH_PRIVATE_KEY"
    fi
    echo "$cmd"
}

scp_cmd() {
    local cmd="scp"
    if [[ -n "$SSH_PRIVATE_KEY" ]]; then
        cmd="$cmd -i $SSH_PRIVATE_KEY"
    fi
    echo "$cmd"
}

# Generate a new SSH key pair if requested
generate_ssh_key() {
    local key_path="$1"
    local key_dir
    key_dir=$(dirname "$key_path")

    mkdir -p "$key_dir"

    if [[ -f "$key_path" ]]; then
        echo "  SSH key already exists at $key_path — reusing it."
    else
        echo "  Generating new SSH key pair: $key_path"
        ssh-keygen -t rsa -b 4096 -f "$key_path" -N "" -C "cosmos-benchmark-${VM_NAME}" -q
        echo "  Created: $key_path (private) + ${key_path}.pub (public)"
    fi

    chmod 600 "$key_path"
    chmod 644 "${key_path}.pub"

    SSH_PRIVATE_KEY="$key_path"
    SSH_PUBLIC_KEY="${key_path}.pub"
}

if [[ "$MODE" == "new" ]]; then
    echo "=== Creating new VM: $VM_NAME in $RG ($LOCATION) ==="
    az group create --name "$RG" --location "$LOCATION" 2>/dev/null || true

    # Resolve SSH key: --create-key > --ssh-key > fallback to --generate-ssh-keys
    SSH_KEY_ARGS=""
    if [[ "$CREATE_KEY" == "true" ]]; then
        # Generate a new key pair
        if [[ -z "$CREATE_KEY_PATH" ]]; then
            CREATE_KEY_PATH="$HOME/.ssh/cosmos-bench-${VM_NAME}"
        fi
        generate_ssh_key "$CREATE_KEY_PATH"
        SSH_KEY_ARGS="--ssh-key-value $SSH_PUBLIC_KEY"
    elif [[ -n "$SSH_PUBLIC_KEY" ]]; then
        # Use the provided pre-existing public key
        if [[ ! -f "$SSH_PUBLIC_KEY" ]]; then
            echo "ERROR: Public key not found: $SSH_PUBLIC_KEY"
            exit 1
        fi
        SSH_KEY_ARGS="--ssh-key-value $SSH_PUBLIC_KEY"
        # Derive private key path (convention: remove .pub suffix)
        if [[ -z "$SSH_PRIVATE_KEY" && "$SSH_PUBLIC_KEY" == *.pub ]]; then
            SSH_PRIVATE_KEY="${SSH_PUBLIC_KEY%.pub}"
            if [[ ! -f "$SSH_PRIVATE_KEY" ]]; then
                echo "ERROR: Private key not found at derived path: $SSH_PRIVATE_KEY"
                echo "  Provide it explicitly: --key <private-key-path>"
                exit 1
            fi
            echo "  Using private key: $SSH_PRIVATE_KEY (derived from public key path)"
        fi
    else
        # Fallback: let az cli handle it (reuses ~/.ssh/id_rsa or creates it)
        SSH_KEY_ARGS="--generate-ssh-keys"
        if [[ -z "$SSH_PRIVATE_KEY" ]]; then
            SSH_PRIVATE_KEY="$HOME/.ssh/id_rsa"
        fi
    fi

    az vm create \
      --resource-group "$RG" \
      --name "$VM_NAME" \
      --image Ubuntu2204 \
      --size "$VM_SIZE" \
      --accelerated-networking true \
      --admin-username "$SSH_USER" \
      $SSH_KEY_ARGS \
      --authentication-type ssh \
      --os-disk-size-gb "$DISK_SIZE" \
      --storage-sku Premium_LRS

    az vm open-port --resource-group "$RG" --name "$VM_NAME" --port 22
    VM_IP=$(az vm show -g "$RG" -n "$VM_NAME" -d --query publicIps -o tsv)
    echo "VM created. IP: $VM_IP"
    echo "  SSH key: $SSH_PRIVATE_KEY"

    if [[ "$SETUP_AFTER_CREATE" == "true" ]]; then
        echo "=== Running setup script on new VM ==="
        $(ssh_cmd) -o StrictHostKeyChecking=no "${SSH_USER}@${VM_IP}" 'bash -s' < scripts/setup-benchmark-vm.sh
    fi

elif [[ "$MODE" == "existing" ]]; then
    if [[ -z "$SSH_PRIVATE_KEY" ]]; then
        # Try default key location
        if [[ -f "$HOME/.ssh/id_rsa" ]]; then
            SSH_PRIVATE_KEY="$HOME/.ssh/id_rsa"
            echo "  Using default SSH key: $SSH_PRIVATE_KEY"
        else
            echo "ERROR: --key <private-key-path> is required for --existing mode."
            echo "  Example: $0 --existing --ip 10.0.0.1 --user benchuser --key ~/.ssh/my_key"
            exit 1
        fi
    fi

    if [[ ! -f "$SSH_PRIVATE_KEY" ]]; then
        echo "ERROR: Private key not found: $SSH_PRIVATE_KEY"
        exit 1
    fi

    if [[ -z "$VM_IP" ]]; then
        # Resolve IP from Azure resource
        VM_IP=$(az vm show -g "$RG" -n "$VM_NAME" -d --query publicIps -o tsv)
    fi
    echo "=== Using existing VM at ${SSH_USER}@${VM_IP} ==="
    echo "  SSH key: $SSH_PRIVATE_KEY"
    echo "Verifying connectivity..."
    $(ssh_cmd) -o ConnectTimeout=10 "${SSH_USER}@${VM_IP}" 'echo "VM reachable. JDK: $(java -version 2>&1 | head -1)"'
else
    echo "Usage:"
    echo "  $0 --new --location <region> [--ssh-key <pub-key>|--create-key [path]]"
    echo "  $0 --existing --ip <ip> --user <user> --key <private-key-path>"
    echo "  $0 --existing --rg <rg> --vm-name <name> --key <private-key-path>"
    exit 1
fi

# Output connection info for other scripts
echo "$VM_IP" > .vm-ip
echo "$SSH_USER" > .vm-user
echo "$SSH_PRIVATE_KEY" > .vm-key
echo "=== Ready: $(ssh_cmd) ${SSH_USER}@${VM_IP} ==="

# After tests — tear down to save costs (only for new VMs)
# az group delete --name "$RG" --yes --no-wait
```

**Usage examples**:

```bash
# Create a new VM — generate a dedicated key pair (recommended for isolation)
./scripts/provision-benchmark-vm.sh --new --location eastus --create-key

# Create a new VM — generate key at a custom path
./scripts/provision-benchmark-vm.sh --new --location eastus --create-key ~/.ssh/my-bench-key

# Create a new VM — use a pre-existing key pair
./scripts/provision-benchmark-vm.sh --new --location eastus --ssh-key ~/.ssh/bench_key.pub

# Create a new VM — fallback: reuse ~/.ssh/id_rsa (or create it if missing)
./scripts/provision-benchmark-vm.sh --new --location eastus --size Standard_D16s_v5

# Use an existing VM by IP with SSH key
./scripts/provision-benchmark-vm.sh --existing --ip 20.84.100.42 --user benchuser --key ~/.ssh/id_rsa

# Use an existing Azure VM by resource name
./scripts/provision-benchmark-vm.sh --existing --rg rg-cosmos-benchmark --vm-name vm-benchmark-01 --key ~/.ssh/bench_key

# Create a new VM but skip auto-setup (run setup-benchmark-vm.sh manually later)
./scripts/provision-benchmark-vm.sh --new --location eastus --create-key --skip-setup
```

**Note**: The script persists connection info to `.vm-ip`, `.vm-user`, and `.vm-key` files. Other scripts (`run-benchmark.sh`, `trigger-benchmark.sh`) can read these to SSH into the VM without re-specifying credentials.

### 6.6 Managed Identity for Cosmos Access (Optional)

Instead of putting master keys in `tenants.json`, use Azure Managed Identity:

1. **Assign system-assigned identity** to the VM:
   ```bash
   az vm identity assign --resource-group rg-cosmos-benchmark --name vm-benchmark-01
   ```

2. **Grant Cosmos DB RBAC** to the VM identity for each account:
   ```bash
   VM_PRINCIPAL=$(az vm show -g rg-cosmos-benchmark -n vm-benchmark-01 --query identity.principalId -o tsv)
   az cosmosdb sql role assignment create \
     --account-name account0 \
     --resource-group rg-cosmos \
     --role-definition-id "00000000-0000-0000-0000-000000000002" \
     --scope "/" \
     --principal-id "$VM_PRINCIPAL"
   ```

3. **In `tenants.json`**, omit `masterKey` and set `"useManagedIdentity": true`

4. `AsyncBenchmark` already supports managed identity via `Configuration.isUseManagedIdentity()`

### 6.7 Thread Dump & Heap Dump Capture

Dumps are essential for diagnosing thread leaks, deadlocks, and memory issues. The benchmark harness supports both **automated** (event-driven / periodic) and **on-demand** capture.

#### 6.7.1 Automated Capture via CLI Options

| CLI Option | Trigger | Output |
|---|---|---|
| `--threadDumpIntervalSec 30` | Every 30 seconds during workload | `<outputDir>/thread-dumps/threads-<timestamp>.txt` |
| `--threadDumpOnEvent POST_CREATE,POST_CLOSE` | At specific lifecycle events | Same directory, named by event |
| `--heapDumpOnEvent POST_WORKLOAD,POST_CLOSE` | At specific lifecycle events | `<outputDir>/heap-dumps/heap-<event>-<timestamp>.hprof` |

**Lifecycle events**:
- `PRE_CREATE` — before any `CosmosAsyncClient` is created
- `POST_CREATE` — after all N clients created + settle time
- `POST_WORKLOAD` — after workload completes (before client close)
- `POST_CLOSE` — after all clients closed + settle time

#### 6.7.2 Implementation in `ResourceMonitor`

```java
public class ResourceMonitor {
    // ... existing fields ...

    /**
     * Captures a thread dump to the output directory.
     * Uses ThreadMXBean for in-process capture (no external tools needed).
     */
    public void captureThreadDump(String label) {
        Path dumpFile = outputDir.resolve("thread-dumps")
            .resolve(String.format("threads-%s-%s.txt", label, Instant.now().toString().replace(':', '-')));
        Files.createDirectories(dumpFile.getParent());

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(dumpFile))) {
            writer.printf("Thread Dump [%s] at %s%n", label, Instant.now());
            writer.printf("Total threads: %d (daemon: %d)%n%n",
                threadMXBean.getThreadCount(), threadMXBean.getDaemonThreadCount());

            for (ThreadInfo info : threadInfos) {
                writer.println(info.toString());
            }

            // Also write thread-name-prefix summary
            Map<String, Long> byPrefix = Arrays.stream(threadInfos)
                .collect(Collectors.groupingBy(
                    ti -> ti.getThreadName().replaceAll("-?\\d+$", ""),
                    Collectors.counting()));
            writer.println("=== Thread Name Prefix Summary ===");
            byPrefix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> writer.printf("  %-50s %d%n", e.getKey(), e.getValue()));
        }

        logger.info("Thread dump captured: {}", dumpFile);
    }

    /**
     * Captures a heap dump via jmap (requires JDK, not just JRE).
     * Falls back to HotSpotDiagnosticMXBean if jmap is not available.
     */
    public void captureHeapDump(String label) {
        Path dumpFile = outputDir.resolve("heap-dumps")
            .resolve(String.format("heap-%s-%s.hprof", label, Instant.now().toString().replace(':', '-')));
        Files.createDirectories(dumpFile.getParent());

        try {
            // Use HotSpotDiagnosticMXBean (works without jmap)
            com.sun.management.HotSpotDiagnosticMXBean diagnosticMXBean =
                ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
            diagnosticMXBean.dumpHeap(dumpFile.toString(), true /* live objects only */);
            logger.info("Heap dump captured: {} (size: {} MB)",
                dumpFile, Files.size(dumpFile) / (1024 * 1024));
        } catch (Exception e) {
            logger.error("Failed to capture heap dump: {}", e.getMessage());
        }
    }
}
```

#### 6.7.3 On-Demand Capture via Scripts

For ad-hoc debugging during a running benchmark (e.g., SSH into VM mid-test):

```bash
#!/bin/bash
# capture-diagnostics.sh — Capture thread/heap dumps of a running benchmark
#
# Usage:
#   ./capture-diagnostics.sh [--threads] [--heap] [--jfr <duration>] [--output-dir <dir>]

set -euo pipefail

CAPTURE_THREADS=false
CAPTURE_HEAP=false
JFR_DURATION=""
OUTPUT_DIR="./diagnostics/$(date +%Y%m%dT%H%M%S)"

while [[ $# -gt 0 ]]; do
    case $1 in
        --threads)     CAPTURE_THREADS=true ;;
        --heap)        CAPTURE_HEAP=true ;;
        --jfr)         JFR_DURATION="$2"; shift ;;
        --output-dir)  OUTPUT_DIR="$2"; shift ;;
        --all)         CAPTURE_THREADS=true; CAPTURE_HEAP=true; JFR_DURATION="60" ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

mkdir -p "$OUTPUT_DIR"

# Find the benchmark JVM PID
BENCH_PID=$(jps -l | grep -E 'azure-cosmos-benchmark|MultiTenancyBenchmark' | awk '{print $1}')
if [[ -z "$BENCH_PID" ]]; then
    echo "ERROR: No running benchmark JVM found."
    echo "Running Java processes:"
    jps -l
    exit 1
fi
echo "Found benchmark PID: $BENCH_PID"

# Thread dump
if [[ "$CAPTURE_THREADS" == "true" ]]; then
    THREAD_FILE="$OUTPUT_DIR/thread-dump-$(date +%H%M%S).txt"
    echo "Capturing thread dump..."
    jstack "$BENCH_PID" > "$THREAD_FILE" 2>&1
    echo "  Thread dump: $THREAD_FILE ($(wc -l < "$THREAD_FILE") lines)"

    # Also capture thread count summary
    echo "" >> "$THREAD_FILE"
    echo "=== Thread Name Prefix Summary ===" >> "$THREAD_FILE"
    grep '"' "$THREAD_FILE" | sed 's/"\([^"]*\)".*/\1/' | sed 's/-[0-9]*$//' | sort | uniq -c | sort -rn >> "$THREAD_FILE"
fi

# Heap dump
if [[ "$CAPTURE_HEAP" == "true" ]]; then
    HEAP_FILE="$OUTPUT_DIR/heap-dump-$(date +%H%M%S).hprof"
    echo "Capturing heap dump (this may take a minute)..."
    jmap -dump:live,format=b,file="$HEAP_FILE" "$BENCH_PID"
    HEAP_SIZE_MB=$(du -m "$HEAP_FILE" | awk '{print $1}')
    echo "  Heap dump: $HEAP_FILE (${HEAP_SIZE_MB} MB)"
fi

# JFR recording
if [[ -n "$JFR_DURATION" ]]; then
    JFR_FILE="$OUTPUT_DIR/recording-$(date +%H%M%S).jfr"
    echo "Starting JFR recording for ${JFR_DURATION}s..."
    jcmd "$BENCH_PID" JFR.start duration="${JFR_DURATION}s" filename="$JFR_FILE" settings=profile
    echo "  JFR will be saved to: $JFR_FILE (after ${JFR_DURATION}s)"
fi

# Quick process stats
echo ""
echo "=== Process Stats ==="
echo "Threads: $(ls /proc/$BENCH_PID/task 2>/dev/null | wc -l)"
echo "FDs:     $(ls /proc/$BENCH_PID/fd 2>/dev/null | wc -l)"
echo "RSS:     $(awk '/VmRSS/{print $2" "$3}' /proc/$BENCH_PID/status 2>/dev/null)"
echo "Heap:    $(jcmd $BENCH_PID GC.heap_info 2>/dev/null | head -5)"

echo ""
echo "Diagnostics saved to: $OUTPUT_DIR"
```

#### 6.7.4 Output Structure

With diagnostics enabled, the results directory looks like:

```
results/20260218T143022-SCALING/
  ├── benchmark.log
  ├── gc.log
  ├── flight.jfr
  ├── resource_snapshots.csv
  ├── thread-dumps/
  │     ├── threads-PRE_CREATE-2026-02-18T14-30-22Z.txt
  │     ├── threads-POST_CREATE-2026-02-18T14-31-05Z.txt
  │     ├── threads-periodic-2026-02-18T14-31-35Z.txt
  │     ├── threads-periodic-2026-02-18T14-32-05Z.txt
  │     ├── threads-POST_WORKLOAD-2026-02-18T14-35-22Z.txt
  │     └── threads-POST_CLOSE-2026-02-18T14-35-35Z.txt
  ├── heap-dumps/
  │     ├── heap-POST_WORKLOAD-2026-02-18T14-35-22Z.hprof
  │     └── heap-POST_CLOSE-2026-02-18T14-35-35Z.hprof
  └── ...
```

---

## 7. Metric Collection Strategy

### 7.1 Snapshot Schedule

| Event | When | What |
|---|---|---|
| **Pre-creation baseline** | Before any client is created | Full `ResourceMonitor` snapshot |
| **Post-creation** | After all N clients created + 30s settle | Full snapshot (after `System.gc()`) |
| **Periodic (during workload)** | Every `monitorIntervalSec` | Full snapshot (no forced GC) |
| **Post-workload** | After workload completes | Full snapshot (after `System.gc()`) |
| **Post-close** | After all clients closed + 10s | Full snapshot (after `System.gc()`) — detect leaks |

### 7.2 Output Format

**CSV files** (one per metric category):

```
results/
  ├── resource_snapshots.csv       # timestamp, heap, direct_mem, threads, cpu, gc, fds
  ├── thread_breakdown.csv         # timestamp, thread_prefix, count
  ├── latency_global.csv           # timestamp, p50, p99, p999, max, count, rate
  ├── latency_per_client.csv       # timestamp, client_id, p50, p99, p999, max, count, rate
  ├── cache_sizes.csv              # timestamp, client_id, query_plan_size, collection_cache_size, pk_range_cache_size
  ├── connection_pool.csv          # timestamp, client_id, active_conns, idle_conns, pending_acquire
  └── test_config.json             # full config dump for reproducibility
```

### 7.3 Per-Test Summary Report

At the end of each test run, generate a summary:

```
═══════════════════════════════════════════════════════════════
 Multi-Tenancy Baseline Report
 Scenario:     S2a (Point Reads, HTTP/1.1, no sharing)
 Clients:      100
 Duration:     300s
 Total Ops:    523,412
═══════════════════════════════════════════════════════════════
 THROUGHPUT:   1,744.7 ops/s (global)
 LATENCY:      P50=2.1ms  P99=12.4ms  P99.9=45.2ms  Max=312ms
─────────────────────────────────────────────────────────────
 HEAP:         Baseline=128MB  Peak=1,247MB  Per-client=11.2MB
 DIRECT MEM:   Peak=342MB
 THREADS:      Baseline=18  Peak=218  Per-client=2.0
 TCP CONNS:    Peak=4,312  Per-client=43.1
 FILE DESCS:   Peak=4,428
 GC PAUSES:    Count=847  Total=2,341ms  Max=45ms
─────────────────────────────────────────────────────────────
 CACHE (avg per client):
   QueryPlan:  142 entries  (~140 KB est.)
   PKRange:    250 entries  (~225 KB est.)
   Session:    250 tokens   (~85 KB est.)
─────────────────────────────────────────────────────────────
 LEAK CHECK:   Heap after close: 134MB (delta from baseline: +6MB)
               Threads after close: 19 (delta: +1)
═══════════════════════════════════════════════════════════════
```

---

## 8. Triggering Tests from Branches & PRs

### 8.1 Motivation

As we iterate on multi-tenancy optimizations, each fix lands on a branch or PR. We need to:

1. **Run the same benchmark against different code versions** — compare branch A (before fix) vs branch B (after fix)
2. **Tag results with branch/commit/PR** — so we can query "show me all B9 runs for PR #12345"
3. **Automate the build-and-run cycle** — one command: checkout → build → benchmark → upload results

### 8.2 `trigger-benchmark.sh` — One-Command Branch Testing

```bash
#!/bin/bash
# trigger-benchmark.sh — Checkout a branch/PR, build, and run benchmark
#
# Usage:
#   ./trigger-benchmark.sh --branch <branch-name> --scenario <scenario> --tenants <file> [options]
#   ./trigger-benchmark.sh --pr <pr-number> --scenario <scenario> --tenants <file> [options]
#   ./trigger-benchmark.sh --compare <branch-a> <branch-b> --scenario <scenario> --tenants <file>

set -euo pipefail

SDK_DIR="${SDK_DIR:-$HOME/azure-sdk-for-java}"
BRANCH=""
PR_NUMBER=""
COMPARE_A=""
COMPARE_B=""
SCENARIO="SCALING"
TENANTS_FILE="tenants.json"
RESULT_SINK="CSV"
SKIP_BUILD=false
EXTRA_ARGS=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --branch)       BRANCH="$2"; shift ;;
        --pr)           PR_NUMBER="$2"; shift ;;
        --compare)      COMPARE_A="$2"; COMPARE_B="$3"; shift 2 ;;
        --scenario)     SCENARIO="$2"; shift ;;
        --tenants)      TENANTS_FILE="$2"; shift ;;
        --result-sink)  RESULT_SINK="$2"; shift ;;
        --sdk-dir)      SDK_DIR="$2"; shift ;;
        --skip-build)   SKIP_BUILD=true ;;
        *)              EXTRA_ARGS="$EXTRA_ARGS $1" ;;
    esac
    shift
done

build_and_run() {
    local ref="$1"
    local label="$2"

    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "  Building and running: $label ($ref)"
    echo "════════════════════════════════════════════════════════"

    cd "$SDK_DIR"

    # Checkout
    if [[ "$ref" =~ ^[0-9]+$ ]]; then
        # It's a PR number — fetch the PR head
        echo "Fetching PR #${ref}..."
        git fetch origin pull/${ref}/head:pr-${ref}
        git checkout pr-${ref}
    else
        echo "Checking out branch: $ref"
        git fetch origin "$ref"
        git checkout "$ref"
        git pull origin "$ref" 2>/dev/null || true
    fi

    COMMIT_ID=$(git rev-parse --short HEAD)
    BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)
    echo "  Commit: $COMMIT_ID"

    # Build (skip if requested — e.g., re-running with same binary)
    if [[ "$SKIP_BUILD" == "false" ]]; then
        echo "Building azure-cosmos + benchmark module..."
        mvn install -pl sdk/cosmos/azure-cosmos -am -DskipTests -q
        mvn package -pl sdk/cosmos/azure-cosmos-benchmark -DskipTests -q
        echo "Build complete."
    fi

    # Run
    local OUTPUT_DIR="./results/$(date +%Y%m%dT%H%M%S)-${label}-${SCENARIO}"
    ./scripts/run-benchmark.sh "$SCENARIO" "$TENANTS_FILE" "$OUTPUT_DIR" \
        --branch "$BRANCH_NAME" \
        ${PR_NUMBER:+--pr "$PR_NUMBER"} \
        --result-sink "$RESULT_SINK" \
        $EXTRA_ARGS

    echo "  Results: $OUTPUT_DIR"
    echo "$OUTPUT_DIR" >> .last-benchmark-runs
}

if [[ -n "$COMPARE_A" && -n "$COMPARE_B" ]]; then
    # Compare mode: run both branches sequentially
    build_and_run "$COMPARE_A" "before"
    build_and_run "$COMPARE_B" "after"

    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "  Both runs complete. Compare results:"
    echo "  $(tail -2 .last-benchmark-runs)"
    echo "════════════════════════════════════════════════════════"

elif [[ -n "$PR_NUMBER" ]]; then
    build_and_run "$PR_NUMBER" "pr-${PR_NUMBER}"

elif [[ -n "$BRANCH" ]]; then
    build_and_run "$BRANCH" "$BRANCH"

else
    echo "Usage: $0 --branch <name> | --pr <number> | --compare <branch-a> <branch-b>"
    exit 1
fi
```

### 8.3 Usage Examples

```bash
# Run benchmark on a specific branch
./scripts/trigger-benchmark.sh --branch multi-tenancy-fix-telemetry-close \
    --scenario CHURN --tenants tenants.json --result-sink COSMOS

# Run benchmark on a PR
./scripts/trigger-benchmark.sh --pr 12345 \
    --scenario SCALING --tenants tenants.json --result-sink ALL

# Compare two branches (before/after a fix)
./scripts/trigger-benchmark.sh \
    --compare main multi-tenancy-fix-telemetry-close \
    --scenario CHURN --tenants tenants.json --result-sink COSMOS

# Re-run without rebuilding (same binary, different scenario)
./scripts/trigger-benchmark.sh --branch main --scenario SOAK --tenants tenants.json --skip-build
```

### 8.4 Git Metadata in Results

Every benchmark run automatically captures git metadata in `<outputDir>/git-info.json` and passes `--branch`, `--commitId`, `--prNumber` to the orchestrator. These flow into:

| Destination | How | Used For |
|---|---|---|
| **CSV results** | `test_config.json` includes `branch`, `commitId`, `prNumber` | Local comparison |
| **Cosmos DB** | Fields in the result document (§9.1) | Query: "all B9 runs on branch X" |
| **Kusto** | Columns in the `BenchmarkRuns` table | Dashboard: "P99 latency by branch over time" |
| **App Insights** | Common tags on all metrics | KQL: `customDimensions["branch"]` |

### 8.5 Before/After Comparison Workflow

The typical workflow for validating a fix:

```
1. ./scripts/trigger-benchmark.sh --compare main <fix-branch> \
       --scenario CHURN --tenants tenants.json --result-sink COSMOS

2. Results are uploaded to Cosmos DB with branch/commit tags

3. Query results:
   SELECT c.testRunId, c.branch, c.leakCheck.threadsAfterClose, c.latency.p99Ms
   FROM c
   WHERE c.scenario = 'S4' AND c.branch IN ('main', 'fix-telemetry-close')
   ORDER BY c.timestamp DESC

4. Or in Kusto:
   BenchmarkRuns
   | where scenario == "S4"
   | where branch in ("main", "fix-telemetry-close")
   | project branch, commitId, threadsAfterClose, p99LatencyMs, timestamp
   | order by timestamp desc
```

---

## 9. External Result Storage

### 9.1 Why External Storage

Local CSV files are fine for individual runs, but multi-run comparison across branches/PRs requires:

| Need | Local CSV | Cosmos DB | Kusto (ADX) |
|---|---|---|---|
| **Single-run analysis** | ✅ Easy | ✅ Works | ✅ Works |
| **Cross-run comparison** | 🔴 Manual diff | ✅ SQL queries | ✅ KQL queries |
| **Time-series trending** | 🔴 Not built-in | 🟡 Possible with App Insights | ✅ Native time-series |
| **Dashboarding** | 🔴 None | 🟡 Power BI connector | ✅ ADX Dashboards native |
| **Alerting on regressions** | 🔴 None | 🟡 Via Azure Functions | ✅ KQL alerts |
| **Ingestion from JVM** | 🟡 File I/O | ✅ Cosmos SDK (already in repo) | 🟡 Kusto Java SDK |
| **Query language** | N/A | SQL (familiar) | KQL (powerful for analytics) |
| **Cost** | Free | ~$25/mo (400 RU/s) | ~$150/mo (Dev/Test SKU) |
| **Retention** | Disk space | Unlimited (TTL optional) | Configurable |

### 9.2 Option A: Cosmos DB Result Storage (Recommended for Phase 1)

**Why start here**:
- The benchmark already has `CosmosTotalResultReporter` — proven write path
- The Cosmos SDK is already a dependency — zero new JARs
- SQL API queries are familiar and sufficient for before/after comparisons
- Low cost: a single 400 RU/s container costs ~$25/month
- Results are durable, queryable, and globally accessible

#### 9.2.1 Result Container Schema

One Cosmos container stores all benchmark runs. Partition key: `/scenario`.

```json
{
    "id": "B9-CHURN-fix-telemetry-close-abc1234-20260218T143022Z",
    "scenario": "S4",
    "testRunId": "B9",
    "timestamp": "2026-02-18T14:30:22Z",

    "git": {
        "branch": "fix-telemetry-close",
        "commitId": "abc1234",
        "commitMessage": "Fix ClientTelemetry thread leak on close",
        "prNumber": 12345
    },

    "environment": {
        "vmName": "vm-benchmark-01",
        "vmSize": "Standard_D16s_v5",
        "region": "eastus",
        "jdkVersion": "21.0.2",
        "osVersion": "Ubuntu 22.04"
    },

    "config": {
        "numClients": 10,
        "churnCycles": 100,
        "connectionSharing": false,
        "httpVersion": "1.1",
        "poolSize": 1000,
        "concurrency": 200,
        "workload": "CHURN"
    },

    "throughput": { "opsPerSec": 1744.7, "totalOps": 523412 },
    "latency": { "p50Ms": 2.1, "p99Ms": 12.4, "p999Ms": 45.2, "maxMs": 312.0 },
    "resources": {
        "heapBaselineMB": 128, "heapPeakMB": 1247, "heapPerClientMB": 11.2,
        "directMemPeakMB": 342,
        "threadsBaseline": 18, "threadsPeak": 218, "threadsPerClient": 2.0,
        "tcpConnectionsPeak": 4312, "fileDescriptorsPeak": 4428,
        "gcCount": 847, "gcTotalMs": 2341
    },
    "caches": { "avgQueryPlanEntries": 142, "avgPkRangeEntries": 250, "avgSessionTokens": 250 },
    "leakCheck": {
        "heapAfterCloseMB": 134, "threadsAfterClose": 19,
        "heapDeltaFromBaselineMB": 6, "threadDeltaFromBaseline": 1
    },

    "passFail": {
        "threadLeakPassed": true,
        "memoryLeakPassed": true,
        "latencyDegradationPassed": true,
        "overallPassed": true
    },

    "ttl": -1
}
```

#### 9.2.2 Time-Series Snapshots Container (Optional)

For the periodic `ResourceMonitor` snapshots (every 10 seconds), store in a separate container with partition key `/testRunId` and TTL of 30 days (these are high-volume, low-value after analysis):

```json
{
    "id": "B9-abc1234-20260218T143032Z-snapshot",
    "testRunId": "B9-abc1234-20260218T143022Z",
    "timestamp": "2026-02-18T14:30:32Z",
    "heapUsedBytes": 1308622848,
    "directMemBytes": 358612992,
    "liveThreads": 218,
    "processCpuPct": 42.3,
    "gcCount": 12,
    "gcTimeMs": 45,
    "openFDs": 4428,
    "ttl": 2592000
}
```

#### 9.2.3 Setup Script

```bash
#!/bin/bash
# setup-result-storage.sh — Create Cosmos DB containers for benchmark results

RESOURCE_GROUP="rg-cosmos-benchmark"
ACCOUNT_NAME="cosmos-bench-results"
LOCATION="eastus"

# Create account (serverless for low-volume; provisioned for dashboarding)
az cosmosdb create \
  --name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --default-consistency-level Session \
  --locations regionName="$LOCATION" failoverPriority=0

# Create database
az cosmosdb sql database create \
  --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --name benchresults

# Create runs container (partitioned by scenario)
az cosmosdb sql container create \
  --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --database-name benchresults \
  --name runs \
  --partition-key-path /scenario \
  --throughput 400

# Create snapshots container (partitioned by testRunId, TTL enabled)
az cosmosdb sql container create \
  --account-name "$ACCOUNT_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --database-name benchresults \
  --name snapshots \
  --partition-key-path /testRunId \
  --throughput 400 \
  --default-ttl 2592000

echo "Result storage ready. Set these in your environment:"
echo "  export RESULT_COSMOS_ENDPOINT=$(az cosmosdb show -n $ACCOUNT_NAME -g $RESOURCE_GROUP --query documentEndpoint -o tsv)"
echo "  export RESULT_COSMOS_KEY=$(az cosmosdb keys list -n $ACCOUNT_NAME -g $RESOURCE_GROUP --query primaryMasterKey -o tsv)"
```

#### 9.2.4 Comparison Queries (SQL API)

**Compare thread leak across branches**:
```sql
SELECT c.git.branch, c.git.commitId, c.git.prNumber,
       c.leakCheck.threadsAfterClose, c.leakCheck.threadDeltaFromBaseline,
       c.resources.threadsPeak, c.timestamp
FROM c
WHERE c.scenario = 'S4'
  AND c.git.branch IN ('main', 'fix-telemetry-close')
ORDER BY c.timestamp DESC
```

**P99 latency trend across commits on a branch**:
```sql
SELECT c.git.commitId, c.latency.p99Ms, c.config.numClients, c.timestamp
FROM c
WHERE c.scenario = 'S2a'
  AND c.git.branch = 'multi-tenancy-analysis'
ORDER BY c.timestamp ASC
```

**All runs for a specific PR**:
```sql
SELECT c.testRunId, c.scenario, c.passFail.overallPassed,
       c.latency.p99Ms, c.resources.threadsPeak, c.timestamp
FROM c
WHERE c.git.prNumber = 12345
ORDER BY c.scenario, c.timestamp DESC
```

### 9.3 Option B: Kusto (Azure Data Explorer) Result Storage (For Dashboarding & Trending)

**When to use Kusto instead of / in addition to Cosmos**:
- You want **time-series dashboards** that update automatically with each run
- You need **anomaly detection** on metrics (Kusto has built-in `series_decompose_anomalies`)
- You want to **join benchmark results with server-side CDBDataPlaneRequests** logs (both in Kusto)
- You have many runs (1000+) and need **sub-second analytical queries** over them

#### 9.3.1 Kusto Table Schema

```kql
// Create the benchmark runs table
.create table BenchmarkRuns (
    testRunId: string,
    scenario: string,
    timestamp: datetime,
    branch: string,
    commitId: string,
    prNumber: int,
    commitMessage: string,
    vmName: string,
    vmSize: string,
    region: string,
    numClients: int,
    connectionSharing: bool,
    httpVersion: string,
    poolSize: int,
    concurrency: int,
    workload: string,
    opsPerSec: real,
    totalOps: long,
    p50Ms: real,
    p99Ms: real,
    p999Ms: real,
    maxMs: real,
    heapBaselineMB: real,
    heapPeakMB: real,
    heapPerClientMB: real,
    directMemPeakMB: real,
    threadsBaseline: int,
    threadsPeak: int,
    threadsPerClient: real,
    tcpConnectionsPeak: int,
    fileDescriptorsPeak: int,
    gcCount: long,
    gcTotalMs: long,
    threadsAfterClose: int,
    heapAfterCloseMB: real,
    threadDeltaFromBaseline: int,
    overallPassed: bool
)

// Create the time-series snapshots table
.create table BenchmarkSnapshots (
    testRunId: string,
    timestamp: datetime,
    heapUsedBytes: long,
    directMemBytes: long,
    liveThreads: int,
    processCpuPct: real,
    gcCount: long,
    gcTimeMs: long,
    openFDs: int
)

// Retention policy: keep runs forever, snapshots for 90 days
.alter table BenchmarkSnapshots policy retention softdelete = 90d
```

#### 9.3.2 Ingestion from Java

Use the [Kusto Java SDK](https://github.com/Azure/azure-kusto-java) for direct ingestion:

```java
public class KustoResultSink implements ResultSink {
    private final IngestClient ingestClient;
    private final String database;

    public KustoResultSink(String clusterUri, String database) {
        ConnectionStringBuilder csb = ConnectionStringBuilder
            .createWithAzCliCredentials(clusterUri);
        this.ingestClient = IngestClientFactory.createClient(csb);
        this.database = database;
    }

    public void uploadRun(BenchmarkResult result) {
        // Convert to CSV row and ingest
        String csv = resultToCsvRow(result);
        StreamSourceInfo source = new StreamSourceInfo(
            new ByteArrayInputStream(csv.getBytes()));
        IngestionProperties props = new IngestionProperties(
            database, "BenchmarkRuns");
        props.setDataFormat(IngestionProperties.DataFormat.CSV);
        ingestClient.ingestFromStream(source, props);
    }

    public void uploadSnapshots(String testRunId, List<ResourceSnapshot> snapshots) {
        // Batch upload periodic snapshots
        String csv = snapshotsToCsv(testRunId, snapshots);
        StreamSourceInfo source = new StreamSourceInfo(
            new ByteArrayInputStream(csv.getBytes()));
        IngestionProperties props = new IngestionProperties(
            database, "BenchmarkSnapshots");
        props.setDataFormat(IngestionProperties.DataFormat.CSV);
        ingestClient.ingestFromStream(source, props);
    }
}
```

#### 9.3.3 Kusto Dashboard Queries

**P99 latency trend by branch (time-series chart)**:
```kql
BenchmarkRuns
| where scenario == "S2a" and numClients == 100
| project timestamp, branch, p99Ms
| render timechart with (title="P99 Latency at 100 Clients by Branch")
```

**Thread leak detection — delta from baseline over time**:
```kql
BenchmarkRuns
| where scenario == "S4"
| project timestamp, branch, commitId, threadDeltaFromBaseline, overallPassed
| order by timestamp asc
| render timechart with (title="Thread Delta After Churn by Branch")
```

**Anomaly detection — flag runs where P99 regressed significantly**:
```kql
let baseline = toscalar(
    BenchmarkRuns
    | where scenario == "S2a" and branch == "main" and numClients == 100
    | summarize percentile(p99Ms, 50)
);
BenchmarkRuns
| where scenario == "S2a" and numClients == 100
| where p99Ms > baseline * 2
| project timestamp, branch, commitId, prNumber, p99Ms, baseline_p99Ms = baseline
| order by timestamp desc
```

**Join with server-side Cosmos metrics (if both in same Kusto cluster)**:
```kql
BenchmarkRuns
| where scenario == "S2a" and timestamp > ago(7d)
| join kind=inner (
    CDBDataPlaneRequests
    | where UserAgent contains "mt-bench-"
    | summarize serverP99 = percentile(DurationMs, 99) by bin(TimeGenerated, 5m)
) on $left.timestamp == $right.TimeGenerated
| project timestamp, branch, clientP99 = p99Ms, serverP99
| render timechart with (title="Client vs Server P99")
```

### 9.4 Recommendation: Start with Cosmos DB, Add Kusto for Dashboarding

| Phase | Storage | When |
|---|---|---|
| **Phase 1** (now) | **CSV + Cosmos DB** | Initial baseline collection. Cosmos gives queryable cross-run comparison with zero new dependencies. |
| **Phase 2** (after baseline matrix complete) | **Add Kusto** | When you have 50+ runs and want trending dashboards, anomaly detection, and server-side correlation. |
| **Ongoing** | **Both** (via `--resultSink ALL`) | Cosmos for low-latency lookups ("show me PR #12345 results"). Kusto for analytical queries ("P99 trend over 6 months"). |

### 9.5 `ResultSink` Architecture

```java
public interface ResultSink {
    /** Upload a complete benchmark run summary. */
    void uploadRun(BenchmarkResult result);

    /** Upload periodic resource snapshots (optional — high volume). */
    void uploadSnapshots(String testRunId, List<ResourceSnapshot> snapshots);

    /** Flush and close connections. */
    void close();
}

// Implementations:
//   CsvResultSink       — writes to local CSV files (always enabled)
//   CosmosResultSink    — writes to Cosmos DB runs + snapshots containers
//   KustoResultSink     — ingests to Kusto BenchmarkRuns + BenchmarkSnapshots tables
//   CompositeResultSink — wraps multiple sinks (for --resultSink ALL)
```

The orchestrator creates the appropriate sink(s) based on `--resultSink`:

```java
ResultSink sink;
switch (config.getResultSink()) {
    case CSV:    sink = new CsvResultSink(outputDir); break;
    case COSMOS: sink = new CompositeResultSink(
                     new CsvResultSink(outputDir),
                     new CosmosResultSink(cosmosEndpoint, cosmosKey, database, container)); break;
    case KUSTO:  sink = new CompositeResultSink(
                     new CsvResultSink(outputDir),
                     new KustoResultSink(kustoCluster, kustoDatabase)); break;
    case ALL:    sink = new CompositeResultSink(
                     new CsvResultSink(outputDir),
                     new CosmosResultSink(...),
                     new KustoResultSink(...)); break;
}
```

---

## 10. Execution Runbook

### 10.1 Baseline Matrix

Run these scenarios in order. Each produces a result set for comparison.

| Run ID | Scenario | Clients | Sharing | HTTP | Pool | Workload | Duration |
|---|---|---|---|---|---|---|---|
| B1 | S1a | 1,10,50,100,200 | off | 1.1 | 1000 | Idle | 5 min settle |
| B2 | S1b | 1,10,50,100,200 | **on** | 1.1 | 1000 | Idle | 5 min settle |
| B3 | S1c | 1,10,50,100,200 | off | **2.0** | 1000 | Idle | 5 min settle |
| B4 | S2a | 1,10,50,100 | off | 1.1 | 1000 | Point reads | 5 min |
| B5 | S2b | 1,10,50,100 | **on** | 1.1 | 1000 | Point reads | 5 min |
| B6 | S2c | 1,10,50,100 | off | **2.0** | 1000 | Point reads | 5 min |
| B7 | S2d | 1,10,50,100 | off | 1.1 | **100** | Point reads | 5 min |
| B8 | S3 | 10 | off | 1.1 | 1000 | Queries (100→10K) | Until done |
| B9 | S4 | 10×100 cycles | off | 1.1 | 1000 | Churn + reads | 100 cycles |
| B10 | S5 | 50 | off | 1.1 | **100** | High concurrency | 5 min |
| B11 | S5 | 50 | off | **2.0** | **100** | High concurrency | 5 min |
| B12 | S7 | 1,10,50,100 | off | 1.1 | 1000 | Idle + Point reads (OkHttp) | 5 min |

**Note**: B12 (OkHttp investigation) is **optional** — run only after the OkHttp adapter prototype exists on an experimental branch.

#### S8 Protocol × Workload × Sharing Baseline Matrix (B13–B42)

**Run after each major fix** using `run-baseline-matrix.sh`. 30 scenarios = 3 protocols × 5 workloads × 2 sharing modes. All use 50 tenants × 20 concurrency × 1M ops.

| Run ID | Protocol | Workload | Sharing | JVM Props |
|---|---|---|---|---|
| B13 | HTTP/1.1 | ReadThroughput | isolated | — |
| B14 | HTTP/1.1 | ReadThroughput | shared | — |
| B15 | HTTP/1.1 | ReadLatency | isolated | — |
| B16 | HTTP/1.1 | ReadLatency | shared | — |
| B17 | HTTP/1.1 | WriteThroughput | isolated | — |
| B18 | HTTP/1.1 | WriteThroughput | shared | — |
| B19 | HTTP/1.1 | WriteLatency | isolated | — |
| B20 | HTTP/1.1 | WriteLatency | shared | — |
| B21 | HTTP/1.1 | QueryOrderby | isolated | — |
| B22 | HTTP/1.1 | QueryOrderby | shared | — |
| B23 | HTTP/2 | ReadThroughput | isolated | `HTTP2_ENABLED` |
| B24 | HTTP/2 | ReadThroughput | shared | `HTTP2_ENABLED` |
| B25 | HTTP/2 | ReadLatency | isolated | `HTTP2_ENABLED` |
| B26 | HTTP/2 | ReadLatency | shared | `HTTP2_ENABLED` |
| B27 | HTTP/2 | WriteThroughput | isolated | `HTTP2_ENABLED` |
| B28 | HTTP/2 | WriteThroughput | shared | `HTTP2_ENABLED` |
| B29 | HTTP/2 | WriteLatency | isolated | `HTTP2_ENABLED` |
| B30 | HTTP/2 | WriteLatency | shared | `HTTP2_ENABLED` |
| B31 | HTTP/2 | QueryOrderby | isolated | `HTTP2_ENABLED` |
| B32 | HTTP/2 | QueryOrderby | shared | `HTTP2_ENABLED` |
| B33 | ThinClient | ReadThroughput | isolated | `HTTP2+THIN` |
| B34 | ThinClient | ReadThroughput | shared | `HTTP2+THIN` |
| B35 | ThinClient | ReadLatency | isolated | `HTTP2+THIN` |
| B36 | ThinClient | ReadLatency | shared | `HTTP2+THIN` |
| B37 | ThinClient | WriteThroughput | isolated | `HTTP2+THIN` |
| B38 | ThinClient | WriteThroughput | shared | `HTTP2+THIN` |
| B39 | ThinClient | WriteLatency | isolated | `HTTP2+THIN` |
| B40 | ThinClient | WriteLatency | shared | `HTTP2+THIN` |
| B41 | ThinClient | QueryOrderby | isolated | `HTTP2+THIN` |
| B42 | ThinClient | QueryOrderby | shared | `HTTP2+THIN` |

**Execution**: `bash scripts/run-baseline-matrix.sh test-setup ~/results`

**Quick validation (10 tenants)**: `bash scripts/run-baseline-matrix.sh test-setup ~/results --tenants 10`

### 10.2 Before/After Comparison

After implementing a fix (e.g., A3: close `ThinClientStoreModel`):

1. Re-run the **S8 baseline matrix** (`run-baseline-matrix.sh`) — produces 9 scenarios
2. Compare `summary.csv` from the new run against the previous baseline
3. For targeted fixes, also re-run the relevant single scenario (e.g., B9 for leak detection)
4. The fix passes if the metric matches the expected post-fix behavior

#### S8 Matrix Comparison Template

| Scenario | Metric | Baseline | After Fix | Δ | Pass? |
|---|---|---|---|---|---|
| `http11-read` | Peak threads | | | | |
| `http11-read` | Peak heap (MB) | | | | |
| `http11-read` | Throughput (ops/s) | | | | |
| `http2-read` | Peak threads | | | | |
| `thin-read` | Peak threads | | | | |
| `http11-query` | Peak threads | | | | |
| ... | ... | | | | |

### 10.3 Comparison Table Template

| Metric | Baseline (before fix) | After Fix | Δ | Pass? |
|---|---|---|---|---|
| Threads after 100 churn cycles | 218 | 18 | −200 | ✅ |
| Heap after close (MB) | 342 | 134 | −208 MB | ✅ |
| P99 latency at 100 clients (ms) | 45.2 | 43.1 | −2.1 ms | ✅ |

---

## 11. Result Schema & Comparison

### 11.1 Cosmos DB Result Document (for `CosmosTotalResultReporter` upload)

```json
{
    "id": "B4-100clients-http11-nosharing-20260218T143022Z",
    "testRunId": "B4",
    "scenario": "S2a",
    "timestamp": "2026-02-18T14:30:22Z",
    "config": {
        "numClients": 100,
        "connectionSharing": false,
        "httpVersion": "1.1",
        "poolSize": 1000,
        "concurrency": 200,
        "workload": "POINT_READ"
    },
    "throughput": {
        "opsPerSec": 1744.7,
        "totalOps": 523412
    },
    "latency": {
        "p50Ms": 2.1,
        "p99Ms": 12.4,
        "p999Ms": 45.2,
        "maxMs": 312.0
    },
    "resources": {
        "heapBaselineMB": 128,
        "heapPeakMB": 1247,
        "heapPerClientMB": 11.2,
        "directMemPeakMB": 342,
        "threadsBaseline": 18,
        "threadsPeak": 218,
        "threadsPerClient": 2.0,
        "tcpConnectionsPeak": 4312,
        "fileDescriptorsPeak": 4428,
        "gcCount": 847,
        "gcTotalMs": 2341
    },
    "caches": {
        "avgQueryPlanEntries": 142,
        "avgPkRangeEntries": 250,
        "avgSessionTokens": 250
    },
    "leakCheck": {
        "heapAfterCloseMB": 134,
        "threadsAfterClose": 19,
        "heapDeltaFromBaselineMB": 6,
        "threadDeltaFromBaseline": 1
    },
    "branch": "multi-tenancy-analysis",
    "commitId": "abc1234"
}
```

### 11.2 Automated Pass/Fail Criteria

| Metric | Threshold | Action on Fail |
|---|---|---|
| Threads after close ≤ baseline + 2 | Hard fail | Leak detected |
| Heap after close ≤ baseline × 1.1 | Hard fail | Memory leak |
| P99 latency at N=100 ≤ 5× P99 at N=1 | Soft warn | Investigate contention |
| Throughput at N=100 ≥ 0.7× throughput at N=1 | Soft warn | Investigate bottleneck |
| Query plan cache size capped at 5,000 with full-clear eviction | Info | Validates existing cap; LRU (A7) would improve eviction |
| GC pause max ≤ 200ms | Soft warn | Tune GC or reduce allocation |

---

## 12. Copilot Analysis Agent

### 12.1 Design Philosophy

The shell scripts (§6–§8) are the **reliable, deterministic execution layer**. The Copilot agent is a **thin analysis layer on top** — it doesn't replace any scripts, it consumes their output and provides natural-language interpretation.

```
┌─────────────────────────────────────────────────────┐
│  Copilot Analysis Agent (conversational layer)      │
│                                                     │
│  "Run CHURN on my branch and tell me if the leak    │
│   is fixed"                                         │
│                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Trigger Skill│  │ Analyze Skill│  │ Query Skill│  │
│  │ (wraps       │  │ (interprets  │  │ (Cosmos /  │  │
│  │  scripts)    │  │  results)    │  │  Kusto)    │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬─────┘  │
└─────────┼─────────────────┼─────────────────┼────────┘
          │                 │                 │
  ┌───────▼───────┐  ┌──────▼───────┐  ┌──────▼──────┐
  │ Shell Scripts  │  │ Result Docs  │  │ Cosmos DB / │
  │ (trigger,run,  │  │ (CSV, JSON)  │  │ Kusto       │
  │  provision)    │  │              │  │             │
  └────────────────┘  └──────────────┘  └─────────────┘
```

### 12.2 Skills / Tools

The agent exposes 3 skills, implementable as MCP tools or Copilot chat participants:

#### Skill 1: `bench/trigger` — Run a Benchmark

**When to use**: User wants to run a benchmark from a conversation.

| Parameter | Required | Description |
|---|---|---|
| `branch` or `pr` | Yes (one of) | What code to benchmark |
| `scenario` | Yes | `SCALING`, `CHURN`, `CACHE_GROWTH`, `POOL_PRESSURE`, `SOAK` |
| `tenants` | No (default: `tenants.json`) | Tenant config file |
| `compare` | No | If set, runs both `main` and the specified branch |
| `resultSink` | No (default: `COSMOS`) | Where to store results |

**What it does**: Shells out to `trigger-benchmark.sh` with the right flags. Returns the test run ID when complete.

**Example conversation**:
```
User:  Run the churn leak test on my PR #12345
Agent: Running scenario CHURN on PR #12345...
       ⏳ Building azure-cosmos + benchmark module...
       ⏳ Executing 100 churn cycles with 10 clients...
       ✅ Complete. Test run ID: B9-pr12345-abc1234-20260218T143022Z
       Results uploaded to Cosmos DB. Want me to analyze them?
```

#### Skill 2: `bench/analyze` — Interpret Results (Primary Value)

This is the **highest-value skill** — it turns raw metrics into actionable conclusions.

| Parameter | Required | Description |
|---|---|---|
| `runId` | Yes (or `latest`) | Test run ID to analyze |
| `compareWith` | No | Another run ID or `baseline` (auto-picks latest main run for same scenario) |

**What it does**:
1. Reads the result document from Cosmos DB (or local CSV)
2. Applies the pass/fail criteria from §11.2
3. If `compareWith` is provided, computes deltas
4. Uses the multi-tenancy analysis doc as context to **explain** anomalies

**Example conversation**:
```
User:  Analyze the latest CHURN run on my branch vs main

Agent: 📊 Comparing B9 runs:
       Branch: fix-telemetry-close (abc1234) vs main (def5678)

       ✅ Thread leak: FIXED
          main:  218 threads after 100 cycles (+200 from baseline)
          yours: 19 threads after 100 cycles (+1 from baseline)
          This confirms proper resource cleanup on client close —
          Note: A1/A2 (ClientTelemetry IMDS leak) is already fixed in current codebase.

       ✅ Heap after close: 134 MB (was 342 MB on main)
          The 208 MB reduction is consistent with eliminating the leaked
          ClientTelemetry overhead (now negligible with Micrometer-based telemetry).

       🟡 P99 latency: 43.1ms (was 45.2ms on main) — marginal improvement
          This is expected — the telemetry leak doesn't directly affect
          request-path latency.

       Overall: ✅ PASSED — safe to merge.
```

**Context the agent uses for interpretation**:
- [multi-tenancy-analysis.md](sdk/cosmos/multi-tenancy-analysis.md) — maps metric anomalies to root causes (e.g., "threads growing = Bug #2: ThinClientStoreModel never closed, Bug #3: PPAF manager never closed")
- §11.2 pass/fail thresholds — determines ✅/🔴 status
- §3.6 per-client memory breakdown — validates whether heap numbers make sense

#### Skill 3: `bench/query` — Ad-Hoc Result Queries

**When to use**: User wants to explore historical data.

| Parameter | Required | Description |
|---|---|---|
| `question` | Yes | Natural-language question about benchmark results |

**What it does**: Translates the question into a Cosmos SQL or Kusto KQL query, executes it, and summarizes the results.

**Example conversations**:
```
User:  Show me all CHURN runs on the fix-telemetry-close branch
Agent: Found 3 runs:
       | Run ID | Commit  | Threads After Close | P99 (ms) | Passed |
       |--------|---------|---------------------|----------|--------|
       | B9-001 | abc1234 | 19                  | 43.1     | ✅     |
       | B9-002 | abc1234 | 18                  | 42.8     | ✅     |
       | B9-003 | def5678 | 22                  | 44.0     | ✅     |
```

```
User:  Has P99 latency regressed on any branch in the last 2 weeks?
Agent: Checking BenchmarkRuns for scenario S2a, numClients=100...
       No regressions found. P99 range: 11.8–13.2ms across 14 runs.
       Baseline (main median): 12.1ms. All within 1.1× threshold.
```

### 12.3 Implementation Phases

| Phase | Approach | What You Get | Effort | Status |
|---|---|---|---|---|
| **Phase 1** | **Copilot Skills** (`.github/skills/multi-tenancy-benchmark-*`) — 5 self-contained skills that teach Copilot how to analyze, compare, run, look up, and check status of benchmarks. Copilot reads CSVs, applies thresholds, and explains results using its built-in file/terminal tools. | `multi-tenancy-benchmark-analyze`, `multi-tenancy-benchmark-compare`, `multi-tenancy-benchmark-run`, `multi-tenancy-benchmark-which`, `multi-tenancy-benchmark-status` | Done | ✅ Implemented |
| **Phase 2** | **MCP Tools** — register `bench_trigger`, `bench_analyze`, `bench_query` on Azure SDK MCP server for deterministic computation. | Copilot auto-selects tools; structured input/output | Medium | Planned |
| **Phase 3** | **VS Code Chat Participant Extension** — `@bench /analyze`, `@bench /compare` as a first-class chat participant with rich rendering, file watchers. | `@bench /analyze ./results/...` in Copilot chat | High | Future |

**Progression**: Phase 1 (Skills) teaches Copilot domain knowledge via markdown — no code needed, each skill is self-contained with reference files. Phase 2 (MCP) adds structured tools for deterministic analysis. Phase 3 (Extension) adds custom UI and auto-triggers.

### 12.4 Copilot Skills (Phase 1 — Current)

Five self-contained skills in `.github/skills/` provide benchmark capabilities to Copilot. Each skill has a `SKILL.md` with instructions and optional `references/` files with domain data (thresholds, root causes, fix mappings). Copilot loads skills on demand and uses its built-in file/terminal tools to execute.

#### Available Skills

| Skill | Directory | What It Does |
|---|---|---|
| `multi-tenancy-benchmark-analyze` | `.github/skills/multi-tenancy-benchmark-analyze/` | Read `resource_snapshots.csv`, apply pass/fail thresholds, explain anomalies with root causes |
| `multi-tenancy-benchmark-compare` | `.github/skills/multi-tenancy-benchmark-compare/` | Compare two result directories, compute deltas, render ✅/🔴/🟡 table, identify which fix was validated |
| `multi-tenancy-benchmark-run` | `.github/skills/multi-tenancy-benchmark-run/` | Trigger a benchmark scenario via shell scripts (local or remote VM) |
| `multi-tenancy-benchmark-which` | `.github/skills/multi-tenancy-benchmark-which/` | Look up which scenario/run-ID/metric for a given fix (A1–A22) |
| `multi-tenancy-benchmark-status` | `.github/skills/multi-tenancy-benchmark-status/` | List recent runs, check VM status, build status, tenants config |

#### Skill Structure

Each skill follows the skill-creator pattern:

```
multi-tenancy-benchmark-analyze/
├── SKILL.md              # Instructions: workflow, output format
└── references/
    ├── thresholds.md     # Pass/fail criteria, CSV column definitions
    └── root-causes.md    # Anomaly explanations (A1–A22)
```

#### Example Usage

```
User:  Analyze the results in ./results/20260218T143022-CHURN/

Copilot: 📊 Benchmark Results: ./results/20260218T143022-CHURN/
         Branch: fix-a1-telemetry-close  Commit: abc1234

         HEAP:    Baseline=128MB  Peak=1247MB  After close=134MB
         THREADS: Baseline=18  Peak=218  After close=19

         ✅ Thread leak: delta=1 (threshold: ≤2)
            Resource cleanup on client close is working correctly. (Note: A1+A2 already fixed.)
         ✅ Memory leak: ratio=1.05 (threshold: ≤1.1)

         Overall: ✅ PASSED — safe to merge.
```

```
User:  Which test should I run for fix A7?

Copilot: Fix A7 — LRU eviction for queryPlanCache
         Scenario:   CACHE_GROWTH (S3)
         Run ID:     B8
         Key metric: Cache size plateau, heap
         Pass:       Cache ≤ max, heap growth <50%
         File:       sdk/cosmos/azure-cosmos/src/.../RxDocumentClientImpl.java

         Run command:
           bash sdk/cosmos/azure-cosmos-benchmark/scripts/run-benchmark.sh CACHE_GROWTH tenants.json ./results/B8-a7
```

#### Why Skills

- **Self-contained**: Each skill bundles its reference data (thresholds, root causes) — no external dependencies
- **On-demand**: Only loaded when Copilot determines the skill is relevant
- **Progressive disclosure**: SKILL.md is concise; detailed data lives in `references/`
- **No code**: Pure markdown — anyone can edit analysis rules
- **Composable**: User can chain naturally: ask `multi-tenancy-benchmark-which` → then `multi-tenancy-benchmark-run` → then `multi-tenancy-benchmark-analyze`

### 12.5 MCP Tools Approach (Phase 2 — When Workflow Stabilizes)

Register tools on the existing Azure SDK MCP server (`eng/common/mcp/`):

```json
{
  "tools": [
    {
      "name": "bench_trigger",
      "description": "Trigger a multi-tenancy benchmark run on a branch or PR",
      "inputSchema": {
        "type": "object",
        "properties": {
          "branch": { "type": "string" },
          "pr": { "type": "integer" },
          "scenario": { "type": "string", "enum": ["SCALING","CHURN","CACHE_GROWTH","POOL_PRESSURE","SOAK"] },
          "compare": { "type": "boolean", "default": false }
        }
      }
    },
    {
      "name": "bench_analyze",
      "description": "Analyze benchmark results and compare with baseline",
      "inputSchema": {
        "type": "object",
        "properties": {
          "runId": { "type": "string", "description": "Test run ID or 'latest'" },
          "compareWith": { "type": "string", "description": "Another run ID or 'baseline'" }
        },
        "required": ["runId"]
      }
    },
    {
      "name": "bench_query",
      "description": "Query historical benchmark results from Cosmos DB or Kusto",
      "inputSchema": {
        "type": "object",
        "properties": {
          "question": { "type": "string", "description": "Natural language question about benchmark history" }
        },
        "required": ["question"]
      }
    }
  ]
}
```

Tool implementations call the existing shell scripts and Cosmos/Kusto queries — the tools are **thin wrappers**, not reimplementations.

---

## Appendix: Files to Create/Modify

| File | Purpose |
|---|---|
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/MultiTenancyBenchmark.java` | Orchestrator: reads `tenants.json`, creates N `AsyncBenchmark` instances on thread pool, coordinates lifecycle |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/MultiTenancyConfig.java` | CLI config for orchestrator-level params (`--tenantsFile`, `--scenario`, `--outputDir`) |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/TenantAccountInfo.java` | POJO for tenant JSON deserialization (endpoint, key, overrides) |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/ResourceMonitor.java` | JVM/OS metric collection (heap, threads, FDs, CPU, GC, caches) |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/ResultSink.java` | Interface for result output (CSV, Cosmos, Kusto) |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/CsvResultSink.java` | CSV file writer (always enabled) |
| `sdk/cosmos/azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/CosmosResultSink.java` | Cosmos DB result writer |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/KustoResultSink.java` | Kusto (ADX) result ingestion |
| `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/CompositeResultSink.java` | Wraps multiple sinks for `--resultSink ALL` |
| `sdk/cosmos/azure-cosmos-benchmark/tenants-sample.json` | Sample `tenants.json` with 3 accounts + template for scaling |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/setup-benchmark-vm.sh` | Azure VM provisioning + setup script |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/provision-benchmark-vm.sh` | Create new VM or connect to existing one (supports `--new` and `--existing` modes) |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/run-benchmark.sh` | Run a single benchmark scenario with JVM flags + JFR + GC logging |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/run-baseline-matrix.sh` | Run the full B1–B11 baseline matrix sequentially |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/capture-diagnostics.sh` | On-demand thread dump, heap dump, and JFR capture for a running benchmark |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/trigger-benchmark.sh` | Checkout branch/PR → build → run benchmark → upload results (one command) |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/setup-result-storage.sh` | Create Cosmos DB account + containers for benchmark result storage |
| `sdk/cosmos/azure-cosmos-benchmark/scripts/compare-results.py` | Compare two result sets and generate delta report |
| `.github/skills/multi-tenancy-benchmark-analyze/` | Copilot skill: analyze benchmark results, apply pass/fail thresholds, explain anomalies |
| `.github/skills/multi-tenancy-benchmark-compare/` | Copilot skill: compare two result directories, compute deltas, identify validated fix |
| `.github/skills/multi-tenancy-benchmark-run/` | Copilot skill: trigger benchmark scenario via shell scripts (local or remote VM) |
| `.github/skills/multi-tenancy-benchmark-which/` | Copilot skill: look up scenario/run-ID/metric for a given fix (A1–A22) |
| `.github/skills/multi-tenancy-benchmark-status/` | Copilot skill: list recent runs, VM status, build status |
| `sdk/cosmos/azure-cosmos-benchmark/IMPLEMENTATION_GUIDE.md` | Step-by-step implementation instructions: Phase 1 (framework), Phase 2 (fix-and-validate one at a time), Phase 3 (full validation) |
| **Modified**: `Configuration.java` | Add `skipSystemPropertyInit` flag; add instance-level AAD fields (`aadLoginEndpoint`, `aadTenantId`, `aadManagedIdentityClientId`) with `getInstanceAad*()` getters and `buildTokenCredential()` method; add `setApplicationName()` setter for per-tenant `userAgentSuffix` |
| **Modified**: `AsyncBenchmark.java` | Remove static `CREDENTIAL` field; build credential per-instance from `Configuration.buildTokenCredential()` |
| **Modified**: `SyncBenchmark.java` | Same as `AsyncBenchmark.java` |
| **Modified**: `AsyncEncryptionBenchmark.java` | Same as `AsyncBenchmark.java` |
| **Modified**: `AsyncCtlWorkload.java` | Same as `AsyncBenchmark.java` |
