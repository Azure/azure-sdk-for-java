# Root Cause Mapping

When a benchmark metric fails, use this to explain why. Source: `sdk/cosmos/azure-cosmos-benchmark/docs/multi-tenancy-analysis.md`.

## Thread Count Growing

**Cause (historical)**: A1+A2 — `ClientTelemetry.close()` was a no-op. IMDS HTTP client pool and `GlobalEndpointManager` scheduler leaked per client lifecycle.

**Status**: ✅ **FIXED** — A1/A2 resolved. IMDS client is now ephemeral. `RxDocumentClientImpl.close()` calls `clientTelemetry.close()`. If threads still grow, investigate: A3 (ThinClientStoreModel never closed), Bug #3 (PPAF manager never closed).

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/ClientTelemetry.java`

## Heap Growing After Close

**Cause (historical)**: A1 — `ClientTelemetryInfo` histogram maps leaked memory.

**Status**: ✅ **FIXED** — Histogram maps no longer exist. Telemetry uses Micrometer. If heap still grows after close, investigate remaining leaks (A3, Bug #3).

## Heap Growing With Queries

**Cause**: A7 — `queryPlanCache` has a 5,000-entry cap with full-clear eviction (not LRU). Only simple queries are cached.

**Fix**: A7 would add LRU eviction to improve over the current clear-all strategy, avoiding the sawtooth pattern.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java`

## Too Many TCP Connections

**Cause**: A11 — Each client creates `ConnectionProvider` with default `maxConnections=1000`. 100 clients = 100K connections.

**Fix**: A11 reduces default for multi-client scenarios.

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/Configs.java`

## High P99 Latency

**Cause**: A15 — HTTP/1.1 head-of-line blocking.

**Fix**: A15 enables HTTP/2 for Gateway mode (multiplexing).

## Direct Memory High

**Cause**: Reactor Netty `ByteBuf` (direct/pooled) scales with active connections. Not a bug — expected behavior.
