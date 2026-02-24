# Multi-Tenancy Implementation Guide

> **Purpose**: Step-by-step instructions for Copilot (or a developer) to implement the multi-tenancy benchmark framework and then apply fixes one at a time, validating each fix with benchmark data before moving on.
>
> **Rule**: Never skip a validation step. Each fix must be proven with benchmark data before starting the next.

---

## Reference Documents

| Document | Path | What It Contains |
|---|---|---|
| **Analysis** | `sdk/cosmos/multi-tenancy-analysis.md` | Per-client resource inventory, bug findings (A1–A22), memory/thread breakdowns |
| **Test Plan** | `sdk/cosmos/azure-cosmos-benchmark/MULTI_TENANCY_TEST_PLAN.md` | Test scenarios (S1–S7), harness design (§4), CLI options (§4.8), result schema (§11) |
| **Benchmark Code** | `sdk/cosmos/azure-cosmos-benchmark/src/main/java/com/azure/cosmos/benchmark/` | Existing `AsyncBenchmark`, `Configuration`, workload drivers |
| **SDK Internals** | `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/` | `RxDocumentClientImpl`, `ClientTelemetry`, `GlobalEndpointManager`, `HttpClient` |

---

## Phase 1: Build the Test Framework

**Goal**: A working `MultiTenancyBenchmark` orchestrator that can run N tenants in parallel, collect resource metrics, and output results — before any SDK fixes.

### Step 1.1: Tenant Config Model

**Create**: `TenantAccountInfo.java`
- POJO for tenant JSON deserialization (see §4.3 in test plan for schema)
- Fields: `id`, `serviceEndpoint`, `masterKey`, `databaseId`, `containerId`, `overrides` (Map<String, String>)
- Include a static method to parse `tenants.json` → `List<TenantAccountInfo>`
- Support `tenantTemplate` expansion (generate N tenants from pattern)

**Create**: `tenants-sample.json`
- 3 explicit tenants + template section (disabled by default)
- Use the exact JSON from §4.3 of the test plan

**Validate**: Unit test that parses `tenants-sample.json` and produces the correct list.

### Step 1.2: Orchestrator CLI Config

**Create**: `MultiTenancyConfig.java`
- JCommander-based CLI config for orchestrator-level params
- All options from §4.8 of the test plan: `--tenantsFile`, `--scenario`, `--outputDir`, `--monitorIntervalSec`, `--churnCycles`, `--distinctQueries`, `--skipWarmup`, `--branch`, `--commitId`, `--prNumber`, `--resultSink`, etc.
- Include thread dump / heap dump options: `--threadDumpIntervalSec`, `--threadDumpOnEvent`, `--heapDumpOnEvent`, `--jfrDurationSec`

**Validate**: Parse sample CLI args and verify all fields populated.

### Step 1.3: Configuration Patches

**Modify**: `Configuration.java`
- Add `skipSystemPropertyInit` flag (boolean, default false). When true, the `AsyncBenchmark` constructor should skip all `System.setProperty()` calls.
- Add `suppressReporter` flag (boolean, default false). When true, `AsyncBenchmark` should not start its own `ScheduledReporter`.
- Add setters for `serviceEndpoint`, `masterKey`, `databaseId`, `collectionId` (needed for programmatic config from `tenants.json`, not CLI).
- The per-instance AAD fields and `setApplicationName()` are already done from previous commits.

**Modify**: `AsyncBenchmark.java`
- Guard system property setup with `if (!configuration.isSkipSystemPropertyInit())`
- Guard reporter creation with `if (!configuration.isSuppressReporter())`

**Validate**: Existing single-tenant benchmark still works unchanged (flags default to false).

### Step 1.4: Resource Monitor

**Create**: `ResourceMonitor.java`
- Collects all metrics from §4.7 of the test plan: heap, direct memory, threads (count + by-prefix breakdown), CPU, GC, FDs, active Cosmos clients, cache sizes
- `captureThreadDump(String label)` — uses `ThreadMXBean`, writes to `<outputDir>/thread-dumps/`
- `captureHeapDump(String label)` — uses `HotSpotDiagnosticMXBean`, writes to `<outputDir>/heap-dumps/`
- `snapshot()` → returns a `ResourceSnapshot` POJO with all metrics + timestamp
- Supports periodic collection (timer-based) and on-demand (lifecycle events)

**Validate**: Instantiate `ResourceMonitor`, call `snapshot()`, verify all fields populated.

### Step 1.5: Result Sink

**Create**: `ResultSink.java` (interface)
- `uploadRun(BenchmarkResult result)`
- `uploadSnapshots(String testRunId, List<ResourceSnapshot> snapshots)`
- `close()`

**Create**: `CsvResultSink.java`
- Writes CSVs per §7.2 of the test plan: `resource_snapshots.csv`, `latency_global.csv`, `test_config.json`
- Always enabled — this is the baseline output

**Create**: `CosmosResultSink.java` (optional — can stub initially)
- Writes to Cosmos DB per §9.2 schema

**Create**: `CompositeResultSink.java`
- Wraps multiple sinks, delegates to all

**Validate**: Write a `BenchmarkResult` to `CsvResultSink`, verify CSV files created with correct headers.

### Step 1.6: MultiTenancyBenchmark Orchestrator

**Create**: `MultiTenancyBenchmark.java` — the main class
- Parse CLI via `MultiTenancyConfig`
- Parse `tenants.json` via `TenantAccountInfo`
- Set system properties ONCE from `globalDefaults` (circuit breaker, PPAF)
- For each tenant:
  - Clone `Configuration` from `globalDefaults`
  - Apply tenant-specific overrides (endpoint, key, DB, container, per-tenant fields)
  - Set `skipSystemPropertyInit = true`, `suppressReporter = true`
  - Set per-tenant `applicationName` (format: `mt-bench-<tenantId>`)
  - Create the appropriate `AsyncBenchmark` subclass (`AsyncReadBenchmark`, etc.)
- Start `ResourceMonitor` (periodic snapshots)
- Capture `PRE_CREATE` thread/heap dumps if requested
- Use `ExecutorService` to run all `benchmark.run()` concurrently
- Use `CountDownLatch` for lifecycle coordination (all tenants start together)
- Capture `POST_CREATE`, `POST_WORKLOAD`, `POST_CLOSE` snapshots/dumps
- Write results via `ResultSink`
- Print summary report per §7.3

**Validate**: Run with 2–3 tenants against real Cosmos accounts (or emulator). Verify:
- [ ] All tenants get their own `CosmosAsyncClient`
- [ ] Each client has unique `userAgentSuffix` (check logs)
- [ ] Resource snapshots CSV is populated
- [ ] Summary report prints
- [ ] All clients close cleanly

### Step 1.7: Shell Scripts

**Create** the scripts from §6 and §8 of the test plan:
- `scripts/run-benchmark.sh` — with git metadata auto-detection
- `scripts/provision-benchmark-vm.sh` — with SSH key support
- `scripts/trigger-benchmark.sh` — branch/PR checkout + build + run
- `scripts/capture-diagnostics.sh` — on-demand thread/heap dumps
- `scripts/setup-benchmark-vm.sh` — VM software setup
- `scripts/setup-result-storage.sh` — Cosmos DB containers for results

**Validate**: `run-benchmark.sh` successfully launches `MultiTenancyBenchmark` with correct JVM flags.

### Step 1.8: Framework Baseline

**Run the full framework** (no SDK fixes yet) against 2–3 real Cosmos accounts:

```bash
./scripts/run-benchmark.sh SCALING tenants.json ./results/framework-baseline
```

**Expected**: The framework works end-to-end. Results show the **current state** (with all bugs present):
- Thread count grows with N clients (Bug #1: `ClientTelemetry.close()` no-op)
- Heap may be higher than expected (unbounded caches)
- This baseline is the "before" for all subsequent fixes

**✅ Framework is complete when**: You can run S1 (Idle Scaling) and S4 (Churn) and get CSV output + summary report.

---

## Phase 2: Fix and Validate (One at a Time)

**Rule**: Complete each fix cycle (implement → test → verify → commit) before starting the next. Never batch multiple fixes.

### Fix Cycle Template

```
1. Create a branch: git checkout -b fix-<action-item-id>
2. Implement the fix (reference the analysis doc section)
3. Build: mvn install -pl sdk/cosmos/azure-cosmos -am -DskipTests
4. Package benchmark: mvn package -pl sdk/cosmos/azure-cosmos-benchmark -DskipTests
5. Run the relevant baseline scenario (B1–B11)
6. Compare results with the framework baseline
7. Verify the specific metric improved AND nothing else regressed
8. Commit with message referencing the action item
9. Push and optionally upload results to Cosmos DB
```

### Fix 2.1: A1 — Fix `ClientTelemetry.close()` (P0)

**What**: `ClientTelemetry.close()` is a no-op — only logs a wrong message. Fix it to properly close `metadataHttpClient`, clear `clientTelemetryInfo` maps, set `isClosed` flag.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/ClientTelemetry.java`

**Reference**: Analysis doc §6, Bug #1

**Test scenario**: B9 (S4 — Churn, 10 clients × 100 cycles)

**What to measure**:
- Thread count after each churn cycle → should return to baseline (currently grows)
- Heap after close → should return to baseline (currently retains histogram memory)

**Pass criteria**:
- Threads after 100 cycles ≤ baseline + 2
- Heap after close ≤ baseline × 1.1

**Commands**:
```bash
./scripts/trigger-benchmark.sh --branch fix-a1-telemetry-close \
    --scenario CHURN --tenants tenants.json --result-sink CSV
# Compare with framework baseline
```

### Fix 2.2: A2 — Call `clientTelemetry.close()` from `RxDocumentClientImpl.close()` (P0)

**What**: `RxDocumentClientImpl.close()` never calls `this.clientTelemetry.close()`. Add the call.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java`

**Reference**: Analysis doc §6, Bug #1 (second part)

**Depends on**: Fix 2.1 (A1) must be done first — otherwise calling `close()` does nothing.

**Test scenario**: B9 (S4 — Churn) — same as A1, but now the close path is actually invoked.

**What to measure**: Same as A1. Results should be similar (or slightly better if A1 alone didn't fully fix the leak because `close()` wasn't called).

**Pass criteria**: Same as A1.

### Fix 2.3: A3 — Close `ThinClientStoreModel` in `RxDocumentClientImpl.close()` (P1)

**What**: `thinClientStoreModel` is created in `initThinClient()` but never closed in `close()`.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java`

**Reference**: Analysis doc §6, Bug #2

**Test scenario**: B9 (S4 — Churn) with HTTP/2 + ThinClient enabled

**What to measure**: Thread count and connection count after churn cycles with ThinClient enabled.

**Pass criteria**: No thread/connection growth over 100 cycles when ThinClient is enabled.

**Note**: Only relevant when ThinClient is enabled. Skip if not testing HTTP/2.

### Fix 2.4: A7 — Add LRU Eviction to `queryPlanCache` (P1)

**What**: `queryPlanCache` is an unbounded `ConcurrentHashMap`. Add a max-size bound with LRU eviction.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java`

**Reference**: Analysis doc §3.2.1

**Implementation options**:
- Caffeine cache (preferred — already a common dependency pattern in Azure SDKs)
- Bounded `LinkedHashMap` with `removeEldestEntry()`
- Guava `CacheBuilder`

**Default max size**: 1000 entries (covers most workloads; ~1 MB)

**Test scenario**: B8 (S3 — Query Cache Growth, 10 clients, 100→10K distinct queries)

**What to measure**:
- `queryPlanCache.size()` per client — should plateau at max size
- Heap delta between 100 queries and 10K queries — should be minimal after LRU kicks in

**Pass criteria**:
- Cache size ≤ configured max at all times
- Heap growth from 100→10K queries < 50% of pre-fix growth

### Fix 2.5: A11 — Reduce Default `maxConnections` for Multi-Client Scenarios (P2)

**What**: Default `maxConnections` is 1000 per client. With 100 clients = 100K potential connections. Consider auto-detecting multi-client scenarios and reducing the default.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/http/HttpClient.java` or `Configs.java`

**Reference**: Analysis doc §7.4

**Test scenario**: B1 (S1a — Idle Scaling, 1→200 clients) + B10 (S5 — Connection Pool Pressure)

**What to measure**:
- TCP connection count at 100 clients
- File descriptor count
- Any latency impact from smaller pool

**Pass criteria**:
- FD count at 100 clients < 50% of pre-fix value
- No latency regression >10% at 100 clients

### Fix 2.6: A15 — Enable HTTP/2 by Default for Gateway Mode (P2)

**What**: HTTP/2 is implemented but disabled by default. Consider enabling it or auto-enabling for multi-tenant scenarios.

**Reference**: Analysis doc §7.5

**Test scenario**: B6 (S2c — Point Reads, HTTP/2) vs B4 (S2a — Point Reads, HTTP/1.1)

**What to measure**:
- TCP connections: HTTP/2 should use ~30× fewer
- Latency: should be comparable or better
- Memory: should be lower (fewer connections)

**Pass criteria**:
- TCP connections at 100 clients < 5K (vs ~100K with HTTP/1.1)
- P99 latency not worse than HTTP/1.1

### Fix 2.7: A22 — OkHttp Investigation (Exploration)

**What**: Prototype an OkHttp adapter and benchmark against Reactor Netty.

**Reference**: Analysis doc §7.7, Test plan S7

**Test scenario**: B12 (S7 — OkHttp comparison)

**This is exploration, not a shipping fix**. The outcome is a recommendation document, not a code change.

---

## Phase 3: Optimization and Validation

After individual fixes are in, run the **full baseline matrix** (B1–B11) to verify no regressions and measure cumulative improvement:

```bash
./scripts/trigger-benchmark.sh --branch main --scenario SCALING --tenants tenants.json --result-sink COSMOS
./scripts/trigger-benchmark.sh --branch main --scenario CHURN --tenants tenants.json --result-sink COSMOS
# ... etc for all B1–B11
```

Compare against the Phase 1 framework baseline. Generate the comparison table from §10.3.

---

## Quick Reference: Which Scenario Tests Which Fix

| Fix | Action Item | Scenario | Run ID | Key Metric |
|---|---|---|---|---|
| A1 + A2 | ClientTelemetry close | S4 (Churn) | B9 | Threads after close |
| A3 | ThinClient close | S4 (Churn, HTTP/2) | B9 | Threads + connections after close |
| A7 | Query plan cache LRU | S3 (Cache Growth) | B8 | Cache size plateau, heap |
| A11 | Connection pool sizing | S1 (Idle) + S5 (Pool Pressure) | B1, B10 | FD count, connections |
| A15 | HTTP/2 default | S2 (Point Reads) | B4 vs B6 | TCP connections, latency |
| A22 | OkHttp investigation | S7 (HTTP Client Compare) | B12 | Threads, direct memory, connections |
