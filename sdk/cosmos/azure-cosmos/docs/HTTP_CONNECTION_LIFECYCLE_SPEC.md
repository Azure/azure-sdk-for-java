# HTTP/2 Connection Lifecycle Management Spec

**Status**: Draft — PR [#48420](https://github.com/Azure/azure-sdk-for-java/pull/48420)
**Date**: 2026-03-23
**Author**: Abhijeet Mohanty
**Tracking Issue**: [#48251](https://github.com/Azure/azure-sdk-for-java/issues/48251)

---

## Table of Contents

1. [Goals & Motivation](#1-goals--motivation)
2. [Architectural Overview](#2-architectural-overview)
3. [Eviction Predicate Design](#3-eviction-predicate-design)
4. [Http2PingHealthHandler](#4-http2pinghealthhandler)
5. [Per-Connection Jitter](#5-per-connection-jitter)
6. [Configuration](#6-configuration)
7. [.NET Parity](#7-net-parity)
8. [Testing & CI](#8-testing--ci)
9. [Known Gaps & Future Work](#9-known-gaps--future-work)

---

## 1. Goals & Motivation

### Goal 1: DNS Re-Resolution for Load Redistribution

When DNS entries change (failover, migration, backend scaling), long-lived HTTP/2 connections
continue routing traffic to the original IP indefinitely. TCP connections hold a socket to the
resolved IP — they never re-resolve DNS. Without forced connection rotation, the SDK can pin
to a stale IP for the lifetime of the process.

**Max lifetime** forces periodic connection closure. When the pool creates a replacement, DNS
is resolved fresh. JVM DNS cache TTL is 30s by default (live-verified on JDK 18 via
`InetAddressCachePolicy.get()`), well under our 300s max lifetime.

### Goal 2: Connection Liveness in Sparse Workloads

HTTP/2 connections can become silently degraded — packet black-holes, half-open TCP,
NAT/firewall timeout — without the SDK knowing. In sparse workloads, two problems arise:

1. **Silent degradation detection**: The next request discovers the dead connection via response
   timeout (6s/6s/10s escalation), adding unnecessary latency.
2. **Idle connection reaping**: Intermediate network infrastructure (NAT gateways, firewalls,
   load balancers) silently drops idle TCP connections after their own timeout. Without traffic
   on the wire, the SDK believes the connection is alive, but the next request hits a dead path.

**PING health probing** addresses both: periodic PING frames keep the connection alive in the
eyes of intermediate infrastructure (keepalive), and ACK tracking detects when a connection has
gone silent (liveness). When no ACK arrives within the configured timeout, the eviction predicate
evicts the connection before any request hits the dead path.

### Why Not reactor-netty's Built-In `maxLifeTime`?

reactor-netty 1.2.13 (our version) has `maxLifeTime(Duration)`, but:

1. **No per-connection jitter** — all connections share a uniform lifetime. Connections created
   around the same time expire together, causing a thundering-herd reconnection spike.
2. **Bypassed by custom `evictionPredicate`** — reactor-netty docs: *"Otherwise only the custom
   eviction predicate is invoked."* Since we need a custom predicate for PING health, the
   built-in `maxLifeTime` and `maxIdleTime` handling is replaced entirely.

reactor-netty 1.3.4 introduces `maxLifeTimeVariance(double)` for per-connection jitter — exactly
what we want. But the SDK is on 1.2.13. See [§9](#9-known-gaps--future-work) for the upgrade
path.

---

## 2. Architectural Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                 ConnectionProvider (reactor-netty 1.2.13)            │
│                                                                      │
│  evictInBackground(5s) sweeps all connections through:               │
│                                                                      │
│  evictionPredicate((connection, metadata) -> boolean)                │
│    Phase 0: !channel.isActive()            → evict (dead)           │
│    Phase 1: idleTime > 60s                 → evict (idle)           │
│    Phase 2: PING ACK stale > ackTimeout    → evict (degraded)      │
│    Phase 3: nanoTime > CONNECTION_EXPIRY    → evict (max lifetime)  │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Per Parent H2 TCP Channel (Netty Pipeline)                         │
│                                                                      │
│    Http2FrameCodec (reactor-netty)                                  │
│      ↓                                                               │
│    Http2ResponseHeaderCleanerHandler (existing)                     │
│      ↓                                                               │
│    Http2PingHealthHandler (NEW)                                     │
│      ├─ Sends PING every 10s via scheduleAtFixedRate                │
│      ├─ On PING ACK → stamps LAST_PING_ACK_NANOS attribute         │
│      └─ On install  → stamps CONNECTION_EXPIRY_NANOS attribute      │
│                                                                      │
│  Channel Attributes (per connection):                                │
│    LAST_PING_ACK_NANOS      (read by Phase 2)                      │
│    CONNECTION_EXPIRY_NANOS   (read by Phase 3)                      │
│    HANDLER_INSTALLED         (one-time install guard)               │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ReactorNettyClient.doOnConnected()                                 │
│    1. Install Http2ResponseHeaderCleanerHandler (existing)           │
│    2. Install Http2PingHealthHandler.installOnParentIfAbsent()      │
│       → stamps CONNECTION_EXPIRY_NANOS = now + base + jitter        │
│       → seeds LAST_PING_ACK_NANOS = now                             │
│       → starts periodic PING schedule                                │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

**Key design principle**: The `Http2PingHealthHandler` writes channel attributes; the eviction
predicate reads them. The two components communicate only via `AttributeKey<Long>` on the
channel — no shared objects, no locks, no coupling.

---

## 3. Eviction Predicate Design

Set on `ConnectionProvider.Builder.evictionPredicate()`. Evaluated by reactor-netty's background
sweep every 5 seconds against every connection in the pool. **Replaces all built-in eviction**
(idle, lifetime) — confirmed from reactor-netty 1.2.13 source.

### Phase 0: Dead Channel
```java
if (!connection.channel().isActive()) return true;
```
TCP closed or RST received. Fastest path — no attribute lookups. **Exempt from rate limiting** —
dead channels are already unusable.

### Phase 1: Idle Timeout
```java
if (maxIdleTimeMs > 0 && metadata.idleTime() > maxIdleTimeMs) return true;
```
Re-implements reactor-netty's built-in `maxIdleTime` behavior (60s default), which is
bypassed when a custom eviction predicate is set.

### Phase 2: PING Liveness
```java
Long lastAckNanos = parentChannel.attr(LAST_PING_ACK_NANOS).get();
if (lastAckNanos != null && System.nanoTime() - lastAckNanos > pingAckTimeoutNanos)
    return true;
```
Reads the attribute stamped by `Http2PingHealthHandler`. If no PING ACK has arrived within
the timeout (default 30s), the connection is silently degraded.

**Null-safety**: `hasAttr` check ensures HTTP/1.1 connections (no PING handler) are unaffected.

### Phase 3: Per-Connection Max Lifetime
```java
Long expiryNanos = parentChannel.attr(CONNECTION_EXPIRY_NANOS).get();
if (expiryNanos != null && System.nanoTime() > expiryNanos) return true;
```
Reads the attribute stamped once at connection creation (§5). Includes per-connection jitter.

### Eviction Rate Limiter

Phases 1–3 are governed by a rate limiter that caps evictions to **1 connection per sweep
cycle**. This prevents cliff eviction — e.g., a sustained network blip (> PING ACK timeout)
makes all connections' PING ACKs go stale simultaneously, and without rate limiting, one sweep
would evict the entire pool.

```java
AtomicInteger evictedThisCycle = new AtomicInteger(0);
AtomicLong cycleStartNanos = new AtomicLong(System.nanoTime());

// At the top of the predicate (after Phase 0):
long now = System.nanoTime();
if (now - cycleStartNanos.get() > sweepIntervalNanos) {
    cycleStartNanos.compareAndSet(cycleStart, now);
    evictedThisCycle.set(0);
}
if (evictedThisCycle.get() >= maxEvictionsPerCycle) {
    return false; // already evicted enough this cycle
}
```

- **Phase 0 is exempt**: Dead channels are already unusable — no benefit in keeping them.
- **Cycle reset**: The counter resets when `nanoTime` advances past the sweep interval.
  Uses `compareAndSet` to handle concurrent predicate evaluations safely.
- **Effect**: With 7 connections, worst-case full pool drain takes 7 × sweepInterval
  (e.g., 7 × 5s = 35s) instead of one sweep. Each evicted connection is replaced by the
  next request, maintaining steady pool size.

### Derived Sweep Interval

The sweep interval is derived from the configured thresholds to ensure timely detection:

```
sweepInterval = clamp(min(idleTimeout, pingAckTimeout, baseMaxLifetime) / 2, 1s, 5s)
```

| Config values (defaults) | min threshold | sweep interval |
|--------------------------|--------------|---------------|
| idle=60s, ping=30s, life=300s | 30s | 5s (30/2 = 15, clamped to 5) |
| idle=60s, ping=3s, life=300s | 3s | 1s (3/2 = 1, clamped to 1) |
| idle=60s, ping=10s, life=300s | 10s | 5s (10/2 = 5) |

---

## 4. Http2PingHealthHandler

`ChannelDuplexHandler` installed on the parent H2 TCP channel. One instance per connection.

### PING Send
- Static payload: `0xC0_5D_B0_01` (no per-PING correlation needed — liveness only)
- Uses `channel().writeAndFlush()` (not `ctx.writeAndFlush()`) to traverse the full pipeline
  including `Http2FrameCodec` for proper frame encoding
- Scheduled via `channel.eventLoop().scheduleAtFixedRate()` — no external executor

### PING ACK Reception
- `channelRead` intercepts `Http2PingFrame` with `ack=true`
- Stamps `LAST_PING_ACK_NANOS = System.nanoTime()` on the channel
- Always propagates the frame via `super.channelRead()` — does not consume

### Installation
- `installOnParentIfAbsent(channel, pingIntervalMs, baseMaxLifetimeMs, jitterRangeMs)`
- Navigates to parent channel (`channel.parent() != null ? channel.parent() : channel`)
- `HANDLER_INSTALLED` attribute provides atomic one-time guard
- Stamps `CONNECTION_EXPIRY_NANOS` at install time (§5)
- Seeds `LAST_PING_ACK_NANOS = nanoTime()` to prevent immediate eviction of new connections

---

## 5. Per-Connection Jitter

### Problem

Uniform max lifetime causes thundering-herd reconnection. .NET's per-pool jitter (`5m + random(0-30s)`) means all connections within one client expire together.

### Solution

At handler installation, jitter is drawn once per connection and stamped as a channel attribute:

```java
long jitterMs = ThreadLocalRandom.current().nextLong(1000, jitterRangeMs + 1);
long expiryNanos = System.nanoTime() + (baseMaxLifetimeMs + jitterMs) * 1_000_000L;
targetChannel.attr(CONNECTION_EXPIRY_NANOS).set(expiryNanos);
```

| Property | Value |
|----------|-------|
| Jitter range | `[1s, 30s]` |
| Effective lifetime | `[301s, 330s]` with defaults |
| Scope | Per-connection (one random draw at creation) |
| Determinism | Deterministic for the connection's lifetime |
| Thread safety | `ThreadLocalRandom` — lock-free, safe for concurrent event loops |

### Comparison

| | .NET (per-pool) | Per-evaluation (rejected) | **Per-connection (chosen)** |
|-|----------------|--------------------------|---------------------------|
| Thundering herd | ❌ All connections expire together | ✅ Probabilistic stagger | ✅ Deterministic stagger |
| Deterministic | ✅ Per-pool | ❌ Re-rolled each sweep | ✅ Per-connection |
| reactor-netty 1.3.4 alignment | N/A | Must rewrite predicate | Drop attribute, use `maxLifeTimeVariance()` |

---

## 6. Configuration

All configurations use the existing `Configs` system property pattern. Not exposed as public API.

| Config | System Property | Default | Purpose |
|--------|----------------|---------|---------|
| Max lifetime | `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_IN_SECONDS` | `300` (5 min) | Base connection lifetime |
| Jitter range | Compile-time constant | `30` seconds | Per-connection random offset `[1s, 30s]` |
| PING interval | `COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS` | `10` seconds | PING frame send frequency |
| PING ACK timeout | `COSMOS.HTTP2_PING_ACK_TIMEOUT_IN_SECONDS` | `30` seconds | Evict if no ACK within this window |
| Eviction sweep | Derived | `5` seconds | `clamp(min(idleTimeout, pingAckTimeout, baseMaxLifetime) / 2, 1s, 5s)` |
| Max evictions per cycle | Hard-coded | `1` | Rate limits Phases 1–3 to prevent cliff drain. Dead channels (Phase 0) exempt. |

**Disable lifetime eviction**: `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_IN_SECONDS=0`
**Disable PING health**: `COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS=0`

---

## 7. .NET Parity

Reference: `CosmosHttpClientCore.cs` line 151, `azure-cosmos-dotnet-v3` @ `4cbe83b1`.

| Aspect | .NET | Java (this spec) |
|--------|------|-----------------|
| Base lifetime | 5 min (300s) | 5 min (300s) ✅ |
| Jitter scope | Per-pool | Per-connection ✅ Better |
| Jitter range | `[0s, 30s)` continuous | `[1s, 30s]` discrete |
| PING health | Not implemented | ✅ `Http2PingHealthHandler` |
| Eviction mechanism | .NET `PooledConnectionLifetime` | reactor-netty `evictionPredicate` |

---

## 8. Testing & CI

### Test Cases

Test group: `manual-http-network-fault`. Requires Linux with `tc` and `iptables`.

| Test | Goal | Mechanism | Key Assertion |
|------|------|-----------|---------------|
| `connectionRotatedAfterMaxLifetimeExpiry` | Goal 1 | `MAX_LIFETIME=15s`, poll parentChannelId | parentChannelId changes |
| `perConnectionJitterStaggersEviction` | Jitter | 100 concurrent requests × 3 waves | Not all parents evicted simultaneously |
| `degradedConnectionEvictedByPingHealthCheck` | Goal 2 | `iptables -j DROP` blackhole on port 10250 | Recovery read uses new parentChannelId |
| `connectionEvictedAfterMaxLifetimeEvenWithHealthyPings` | Lifetime > PING | `MAX_LIFETIME=15s`, `ACK_TIMEOUT=60s` | Lifetime eviction fires despite healthy PINGs |

### CI Pipeline

New stage `Cosmos_Live_Test_HttpNetworkFault` in `tests.yml`:
- **Platform**: Ubuntu VMs (native, not Docker)
- **PreSteps**: Install `iproute2`, `iptables`, load `sch_netem` kernel module
- **Sequential execution**: `MaxParallel: 1` (tests manipulate `tc`/`iptables`)
- **Timeout**: 30 minutes

---

## 9. Known Gaps & Future Work

| Gap | Impact | Mitigation / Future Work |
|-----|--------|-------------------------|
| **No pre-warming on eviction** | Brief window with 0 connections (pool size = 1). Next request pays TCP+TLS cost. | Thin-client proxy is nearby (<50ms handshake). `pendingAcquireTimeout` (45s) is the safety net. |
| **Goal 1 is reactive** | If DNS hasn't changed, rotation reconnects to same IP. No active load redistribution. | Acceptable — the goal is re-resolution after failover/migration, not active balancing. |
| **Customer `networkaddress.cache.ttl=-1`** | Max lifetime evicts connections, but new ones resolve to cached (stale) IP. Goal 1 defeated. | Do not override customer's JVM DNS setting. Document as known limitation. |
| **reactor-netty 1.3.4 upgrade** | Native `maxLifeTimeVariance()` would simplify Phase 3 and remove channel attribute. | Spring Boot 4 track has 1.3.3. Track 1.3.4+ availability with Central team. |

### 9.1 Design: Decouple Max Lifetime from PING Health

**Problem**: `CONNECTION_EXPIRY_NANOS` is stamped inside `Http2PingHealthHandler.installOnParentIfAbsent`.
If PING is disabled (`COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS=0`), the handler is never installed,
and max lifetime silently stops working. These are conceptually independent features coupled
through a single install path.

**Proposed fix**: Separate the concerns in `ReactorNettyClient.doOnConnected()`:

```java
// Always stamp per-connection expiry if max lifetime is configured — independent of PING.
int maxLifetimeSeconds = Configs.getHttpConnectionMaxLifetimeInSeconds();
if (maxLifetimeSeconds > 0) {
    Http2PingHealthHandler.stampConnectionExpiry(
        connection.channel(),
        maxLifetimeSeconds * 1000L,
        Configs.HTTP_CONNECTION_MAX_LIFETIME_JITTER_IN_SECONDS * 1000L);
}

// Install PING handler only if PING interval is configured — independent of max lifetime.
int pingIntervalSeconds = Configs.getHttp2PingIntervalInSeconds();
if (pingIntervalSeconds > 0) {
    Http2PingHealthHandler.installOnParentIfAbsent(
        connection.channel(), pingIntervalSeconds * 1000L);
}
```

`stampConnectionExpiry` becomes a static method that only handles the expiry attribute:

```java
public static void stampConnectionExpiry(Channel channel, long baseMaxLifetimeMs, long jitterRangeMs) {
    Channel target = channel.parent() != null ? channel.parent() : channel;
    if (!target.hasAttr(CONNECTION_EXPIRY_NANOS)) {
        long jitterMs = ThreadLocalRandom.current().nextLong(1000, jitterRangeMs + 1);
        target.attr(CONNECTION_EXPIRY_NANOS).set(
            System.nanoTime() + (baseMaxLifetimeMs + jitterMs) * 1_000_000L);
    }
}
```

`installOnParentIfAbsent` drops the lifetime parameters and only handles PING installation.

**Independence matrix after fix**:

| PING enabled | Max lifetime enabled | Behavior |
|-------------|---------------------|----------|
| ✅ | ✅ | All 4 phases active (current default) |
| ❌ | ✅ | Phase 0 (dead) + Phase 1 (idle) + Phase 3 (lifetime). No Phase 2 (PING). |
| ✅ | ❌ | Phase 0 (dead) + Phase 1 (idle) + Phase 2 (PING). No Phase 3 (lifetime). |
| ❌ | ❌ | No eviction predicate set at all (current behavior). |

### 9.2 Design: Graceful Eviction (Avoid RST_STREAM on Active Streams)

**Problem**: When the eviction predicate returns `true`, reactor-netty closes the parent TCP
connection. Active H2 streams on that connection receive `RST_STREAM`. For lifetime eviction
(Phase 3), per-connection jitter makes this unlikely during peak traffic — the connection
expires during a naturally idle moment. But for PING-stale eviction (Phase 2), the connection
may have active streams that are already experiencing timeouts due to the degraded network path.

**Key question**: Does reactor-netty's H2 pool evaluate the eviction predicate against
connections that currently have active streams? In HTTP/2, a parent connection can simultaneously
be "available for new streams" and "serving existing streams." If the background sweep evaluates
all connections (including those with active streams), eviction can interrupt in-flight requests.

**Proposed fix: Two-phase eviction via `PENDING_EVICTION_NANOS` attribute**

Instead of returning `true` immediately, stamp a `PENDING_EVICTION_NANOS` attribute and
return `false`. On subsequent sweeps, if the connection is still pending eviction AND has been
idle (from the pool's perspective), then return `true`.

```java
// Phase 2/3 hits a threshold — mark for pending eviction instead of immediate evict
if (shouldEvict) {
    Channel ch = connection.channel();
    if (!ch.hasAttr(PENDING_EVICTION_NANOS)) {
        ch.attr(PENDING_EVICTION_NANOS).set(System.nanoTime());
        return false; // don't evict yet — let active streams drain
    }

    // Already pending — check if enough time has passed for streams to drain
    long pendingSince = ch.attr(PENDING_EVICTION_NANOS).get();
    long gracePeriodNanos = EVICTION_DRAIN_GRACE_PERIOD.toNanos(); // e.g., 10s
    if (System.nanoTime() - pendingSince > gracePeriodNanos) {
        return true; // grace period expired — evict regardless
    }

    // Within grace period — only evict if connection is idle
    if (metadata.idleTime() > 0) {
        return true; // no active streams — safe to evict now
    }

    return false; // active streams — wait for next sweep
}
```

**Trade-offs**:

| Aspect | Immediate eviction (current) | Two-phase eviction (proposed) |
|--------|-------|-------------|
| Active stream impact | RST_STREAM on all active streams | Streams complete naturally (up to grace period) |
| Detection-to-eviction latency | 1 sweep cycle | 1–2 sweep cycles + up to grace period |
| Complexity | Simple boolean | Additional attribute + grace period logic |
| PING-stale connections | Evicted fast (good — they're degraded) | Delayed eviction (bad — active streams on degraded connection continue failing) |

**Recommendation**: Implement two-phase eviction for **Phase 3 (lifetime) only**. Phase 2
(PING-stale) should continue with immediate eviction — if the connection is degraded, active
streams are already failing, and keeping them alive provides no benefit.

```
Phase 0 (dead):       → immediate evict (always)
Phase 1 (idle):       → immediate evict (no active streams by definition)
Phase 2 (PING-stale): → immediate evict (connection is degraded, streams are already failing)
Phase 3 (lifetime):   → two-phase: mark pending → evict when idle or grace period expires
```
