# Tasks: HTTP/2 Connection Management for Gateway V2

## Part 1: Response Timeout Validation (Complete existing gaps)

### Phase 1a: Http2AllocationStrategy Deep-Dive
- [x] Research `Http2AllocationStrategy` from reactor-netty source (stream allocation, connection selection, failure handling)
- [x] Write Obsidian vault note: `Reactor Netty - Http2AllocationStrategy Stream Allocation.md`

### Phase 1b: Multi-Parent-Channel Test
- [x] Test: `multiParentChannelConnectionReuse` — sends 35 concurrent reads to force >1 parent H2 channel, injects delay, verifies parent channels survive
- [ ] Extract full CosmosDiagnostics → evidence MD file in Obsidian

### Phase 1c: Retry ParentChannelId Allocation Test
- [x] Test: `retryUsesConsistentParentChannelId` — captures parentChannelId from ALL retry attempts (6s/6s/10s) via `extractAllParentChannelIds()`, verifies channels survive post-delay
- [ ] Extract full CosmosDiagnostics → evidence MD file in Obsidian

## Part 2: Connect/Acquire Timeout Differentiation

### Phase 2a: Implementation
- [x] Add system property `COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS` in `Configs.java` (default 3s)
- [x] Add `Configs.getThinClientConnectionTimeoutInSeconds()` with system property + env variable support
- [x] Add `ReactorNettyClient.resolveConnectTimeoutMs()` — per-request `.option(CONNECT_TIMEOUT_MILLIS)` keyed on port (non-443=3s, 443=45s)

### Phase 2b: Testing
- [x] Test: `connectTimeoutDifferentiation_DocumentRequest` — `CONNECTION_DELAY` 5s > 3s thin client timeout, validates SERVICE_UNAVAILABLE
- [x] Test: `connectTimeoutDifferentiation_MetadataRequest` — `CONNECTION_DELAY` 5s < 45s gateway timeout, validates success
- [ ] Extract full CosmosDiagnostics → evidence MD files in Obsidian

## Part 3: H2 Parent Channel Health Checker

### Phase 3a: Channel Statistics Tracking
- [x] Create `Http2ChannelStatistics.java` — thread-safe stats with AtomicInteger/AtomicReference
  - Fields: channelId, parentChannelId, streamSuccessCount, streamFailureCount, consecutiveFailureCount, lastReadTime, lastWriteTime, goawayReceived, creationTime, lastPingRttNanos
  - JSON serializer with `Http2ChannelStatsJsonSerializer`
- [ ] Surface `Http2ChannelStatistics` in `CosmosDiagnostics` for request-relevant channels only

### Phase 3b: Health Checker Implementation
- [x] Enhanced `Http2ConnectionHealthCheckHandler.java` with:
  - GOAWAY frame handling (`Http2GoAwayFrame`) with `recordGoaway()` on channel stats
  - Channel statistics association via `setChannelStatistics()`
  - PING RTT measurement (`pingSentNanoTime` → ACK delta → `stats.recordPingRtt()`)
  - CPU-aware `isUnhealthy()`: GOAWAY priority > consecutive failures (threshold=5) > CPU suppression (>90%)
  - `getSystemCpuLoad()` normalized to [0,1] via `OperatingSystemMXBean`

### Phase 3c: Integration
- [x] `ReactorNettyClient.doOnConnected()`: create `Http2ChannelStatistics` per parent channel, associate with health handler
- [x] `ConnectionObserver`: `updateChannelStatisticsOnSuccess()` / `OnFailure()` / `OnWrite()` on RESPONSE_RECEIVED / RESPONSE_INCOMPLETE / REQUEST_SENT
- [ ] Enable `evictInBackground` on `ConnectionProvider` using Configs interval

### Phase 3d: Testing
- [x] Test: `sickConnectionEviction` — 30s delay, PING timeout, parent channel evicted, new channel used
- [x] Test: `healthyConnectionPreserved` — wait > PING interval, ACK arrives normally, same channel reused
- [x] Test: `connectionRecoveryAfterEviction` — full eviction + recovery + stability verification
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
