# Tasks: HTTP/2 Connection Management for Gateway V2

## Part 1: Response Timeout Validation (Complete existing gaps)

### Phase 1a: Http2AllocationStrategy Deep-Dive
- [x] Research `Http2AllocationStrategy` from reactor-netty source (stream allocation, connection selection, failure handling)
- [x] Write Obsidian vault note: `Reactor Netty - Http2AllocationStrategy Stream Allocation.md`

### Phase 1b: Multi-Parent-Channel Test
- [ ] Test: `multiParentChannelConnectionReuse` — force CosmosClient to open >1 parent H2 channel to the same endpoint, inject delay on one, verify BOTH parent channels survive
- [ ] Extract full CosmosDiagnostics → evidence MD file in Obsidian

### Phase 1c: Retry ParentChannelId Allocation Test
- [ ] Test: `retryUsesConsistentParentChannelId` — under delay, verify whether each retry attempt uses the SAME parentChannelId or acquires from a different one (test Http2AllocationStrategy's round-robin vs sticky behavior)
- [ ] Extract full CosmosDiagnostics → evidence MD file in Obsidian

## Part 2: Connect/Acquire Timeout Differentiation

### Phase 2a: Implementation
- [ ] Add system property `COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS` in `Configs.java`
- [ ] In `ReactorNettyClient.send()`, apply per-request `.option(CONNECT_TIMEOUT_MILLIS, ...)` keyed on port (10250=3s, 443=45s)
- [ ] Alternatively: split `ConnectionProvider` per endpoint type if per-request option isn't supported

### Phase 2b: Testing
- [ ] Test: `connectTimeoutDifferentiation_DocumentRequest` — `CONNECTION_DELAY` 5s on GW V2 endpoint, document request fails with connect timeout
- [ ] Test: `connectTimeoutDifferentiation_MetadataRequest` — `CONNECTION_DELAY` 5s on metadata endpoint, metadata request succeeds (45s budget)
- [ ] Extract full CosmosDiagnostics → evidence MD files in Obsidian

## Part 3: H2 Parent Channel Health Checker

### Phase 3a: Channel Statistics Tracking
- [ ] Create `Http2ChannelStatistics.java` (inspired by `RntbdChannelStatistics`)
  - Fields: channelId, parentChannelId, streamSuccessCount, streamFailureCount, lastReadTime, lastWriteTime, goawayReceived, creationTime
- [ ] Surface `Http2ChannelStatistics` in `CosmosDiagnostics` for request-relevant channels only

### Phase 3b: Health Checker Implementation
- [ ] Create `Http2ChannelHealthChecker.java` (inspired by `RntbdClientChannelHealthChecker`)
  - Heuristics:
    1. Close 1 connection at a time, prioritize GOAWAY-received connections
    2. 100% stream failure rate over N consecutive streams → mark unhealthy
    3. CPU/memory utilization context (high CPU = maybe not a bad connection)
    4. H2 PING liveness probe: `.http2Settings(s -> s.pingInterval(Duration))`

### Phase 3c: Integration
- [ ] Wire `Http2ChannelHealthChecker` into `ConnectionProvider` lifecycle
- [ ] Enable `pingInterval` on `Http2Settings`

### Phase 3d: Testing
- [ ] Test: `sickConnectionEviction` — tc netem to make one parent channel permanently sick, verify health checker evicts it
- [ ] Test: `goawayPrioritization` — simulate GOAWAY, verify that connection is prioritized for closure
- [ ] Test: `healthyConnectionPreserved` — under normal operation, no false evictions
- [ ] Extract full CosmosDiagnostics → evidence MD files in Obsidian

## Git Worktree Setup
- [x] Create worktree for Part 2 branch — `azure-sdk-for-java-part2-conn-timeout` on `AzCosmos_H2ConnectAcquireTimeout` (sparse: `sdk/cosmos`)
- [x] Create worktree for Part 3 branch — `azure-sdk-for-java-part3-h2-health` on `AzCosmos_H2ChannelHealthChecker` (sparse: `sdk/cosmos`, cherry-picked `59ddc8692a2`)

## Worktree Layout
| Part | Branch | Worktree Directory | Base |
|------|--------|--------------------|------|
| 1 | `AzCosmos_HttpTimeoutPolicyChangesGatewayV2` | `azure-sdk-for-java` (main clone) | origin/main + commits |
| 2 | `AzCosmos_H2ConnectAcquireTimeout` | `azure-sdk-for-java-part2-conn-timeout` | Part 1 HEAD (`dcf8ecd8ba8`) |
| 3 | `AzCosmos_H2ChannelHealthChecker` | `azure-sdk-for-java-part3-h2-health` | Part 1 HEAD + PING research commit |
