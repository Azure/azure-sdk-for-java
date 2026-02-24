# Root Cause Mapping

When a benchmark metric fails, use this to explain why. Source: `sdk/cosmos/azure-cosmos-benchmark/docs/multi-tenancy-analysis.md`.

## Thread Count Growing

**Cause (historical)**: A1+A2 — `ClientTelemetry.close()` was a no-op.

**Status**: ✅ **FIXED** — IMDS client is now ephemeral. Remaining leak candidates: A3 (ThinClientStoreModel), Bug #3 (PPAF manager).

## Heap Growing After Close

**Cause (historical)**: A1 — `ClientTelemetryInfo` histogram maps leaked.

**Status**: ✅ **FIXED** — Histogram maps no longer exist; telemetry uses Micrometer.

## Heap Growing With Queries

**Cause**: A7 — `queryPlanCache` has 5,000-entry cap with full-clear eviction (not LRU). Only simple queries cached.

## Too Many TCP Connections

**Cause**: A11 — Per-client `ConnectionProvider` with `maxConnections=1000`. 100 clients = 100K connections.

## High P99 Latency

**Cause**: A15 — HTTP/1.1 head-of-line blocking.

## Direct Memory High

**Cause**: Reactor Netty `ByteBuf` scales with active connections. Expected behavior.
