# Proposal: HTTP/2 Connection Management for Gateway V2 (Thin Client)

## Problem

The Cosmos Java SDK's Gateway V2 (thin client) transport uses HTTP/2 multiplexing for document operations. Three gaps exist in the current connection management:

1. **Aggressive response timeouts (6s/6s/10s) need validation** — Phase 1 proved that stream-level timeouts don't kill the parent channel, but two test scenarios are missing:
   - Multi-parent-channel environments (>1 TCP connection to the same endpoint)
   - Whether retries (via `Http2AllocationStrategy`) reuse the same parentChannelId or open new ones

2. **No connect/acquire timeout differentiation** — Both Gateway V1 (metadata, port 443) and Gateway V2 (document, port 10250) use the same 45s connect timeout. Document requests against thin-client endpoints should fail fast (1–3s) while metadata requests retain the 45s budget.

3. **Sick parent channel detection** — If a parent H2 connection accepts streams but never responds (and the e2e timeout is shorter than the response timeout), the pool keeps handing out the same broken connection. No health-check mechanism exists at the H2 parent channel level.

## Solution

### Part 1: Complete response timeout validation
- Add tests for multi-parent-channel and retry-allocation-strategy scenarios
- Deep-dive `Http2AllocationStrategy` behavior under stream failures

### Part 2: Per-request connect/acquire timeout
- In `ReactorNettyClient.send()`, apply different `CONNECT_TIMEOUT_MILLIS` based on whether the request targets port 10250 (GW V2) vs port 443 (GW V1)
- System property: `COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS` (default: 3s for document, unchanged for metadata)
- Test via `CONNECTION_DELAY` fault injection

### Part 3: H2 parent channel health checker
- Implement `Http2ChannelHealthChecker` inspired by `RntbdClientChannelHealthChecker`
- Track per-channel statistics (`Http2ChannelStatistics`) inspired by `RntbdChannelStatistics`
- Use H2 PING (`pingInterval`) for connection liveness probing
- Prioritize GOAWAY-receiving channels for closure
- Monitor CPU/memory utilization as context for health decisions
- Surface relevant channel statistics in `CosmosDiagnostics`

## Execution Order

Part 1 → Part 2 → Part 3 (sequential, each builds on the previous)

## Success Criteria

- All tests pass within 180s per test
- Full CosmosDiagnostics evidence extracted for each scenario
- Minimum files changed per part
- Conceptual learnings documented in Obsidian vault
