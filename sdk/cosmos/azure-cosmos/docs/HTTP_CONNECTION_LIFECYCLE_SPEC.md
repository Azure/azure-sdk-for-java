# HTTP/2 Connection Lifecycle Management Spec

**Status**: Draft — PR [#48420](https://github.com/Azure/azure-sdk-for-java/pull/48420)
**Date**: 2026-03-23
**Author**: Abhijeet Mohanty
**Tracking Issue**: [#48251](https://github.com/Azure/azure-sdk-for-java/issues/48251)

---

## Table of Contents

1. [Goals & Motivation](#1-goals--motivation)
2. [Key Design Choices](#2-key-design-choices)
3. [Architectural Overview](#3-architectural-overview)
4. [Eviction Predicate Design](#4-eviction-predicate-design)
5. [Http2PingHealthHandler](#5-http2pinghealthhandler)
6. [Per-Connection Jitter](#6-per-connection-jitter)
7. [Configuration](#7-configuration)
8. [.NET Parity](#8-net-parity)
9. [Testing & CI](#9-testing--ci)
10. [Known Gaps & Future Work](#10-known-gaps--future-work)

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

### Goal 2: Connection Liveness

HTTP/2 connections can become silently degraded — packet black-holes, half-open TCP,
NAT/firewall timeout — without the SDK knowing. Two problems arise:

1. **Silent degradation detection**: Affects both sparse and high-throughput workloads. The next
   request discovers the dead connection via response timeout (6s/6s/10s escalation), adding
   unnecessary latency. Under high throughput, many requests can pile up on a degraded
   connection before the timeout fires.
2. **Idle connection reaping**: Primarily affects sparse workloads. Intermediate network
   infrastructure (NAT gateways, firewalls, load balancers) silently drops idle TCP connections
   after their own timeout. Without traffic on the wire, the SDK believes the connection is
   alive, but the next request hits a dead path.

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
what we want. But the SDK is on 1.2.13. See [§10](#10-known-gaps--future-work) for the upgrade
path. **Tracking item**: Integrate `maxLifeTimeVariance` when reactor-netty 1.3.4+ is available
in the SDK dependency chain (Spring Boot 4 track has 1.3.3 — follow up with Central team).

---

## 2. Key Design Choices

These choices shape the architecture. Read these first to understand why the system is
structured the way it is.

1. **Custom eviction predicate replaces all built-in eviction** — reactor-netty 1.2.13:
   *"Otherwise only the custom eviction predicate is invoked."* Since we need custom logic
   for PING health, we must re-implement idle timeout and lifetime in the predicate.

2. **Max lifetime and PING health are independent** — Disabling PING (`PING_INTERVAL=0`)
   must not disable max lifetime, and vice versa. They are installed via separate code paths
   in `doOnConnected`: `stampConnectionExpiry()` for lifetime, `installOnParentIfAbsent()`
   for PING.

3. **Per-connection jitter, not per-pool or per-evaluation** — Each connection gets a
   deterministic expiry stamped once at creation via `CONNECTION_EXPIRY_NANOS` channel
   attribute. Avoids .NET's sync-lock problem (per-pool) and the non-determinism of
   re-rolling jitter each sweep (per-evaluation).

4. **Two-phase eviction for lifetime (Phase 3)** — Instead of immediately closing a
   connection past its lifetime (which RST_STREAMs active H2 streams), the predicate marks
   it as `PENDING_EVICTION_NANOS` on first detection, then evicts when idle or after a 10s
   drain grace period. PING-stale eviction (Phase 2) remains immediate — degraded connections
   should be closed fast.

5. **Eviction rate limiter** — At most 1 connection evicted per sweep cycle (dead channels
   exempt). Prevents cliff eviction when a sustained network blip makes all PING ACKs stale
   simultaneously.

6. **Derived sweep interval** — `clamp(min(idleTimeout, pingAckTimeout, baseMaxLifetime) / 2, 1s, 5s)`.
   Adapts to configured thresholds so the sweep is always faster than the smallest eviction
   threshold.

---

## 3. Architectural Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                 ConnectionProvider (reactor-netty 1.2.13)            │
│                                                                      │
│  evictInBackground(derived interval) sweeps all connections through: │
│                                                                      │
│  evictionPredicate((connection, metadata) -> boolean)                │
│    Phase 0: !channel.isActive()            → evict (dead, no limit) │
│    ── rate limiter: max 1 eviction per cycle (Phases 1-3) ──        │
│    Phase 1: idleTime > 60s                 → evict (idle)           │
│    Phase 2: PING ACK stale > ackTimeout    → evict (immediate)     │
│    Phase 3: nanoTime > CONNECTION_EXPIRY    → two-phase eviction:   │
│             1st sweep: mark PENDING_EVICTION_NANOS, return false    │
│             next sweep: evict if idle OR 10s grace period expired   │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Per Parent H2 TCP Channel (Netty Pipeline)                         │
│                                                                      │
│    Http2FrameCodec (reactor-netty)                                  │
│      ↓                                                               │
│    Http2ResponseHeaderCleanerHandler (existing)                     │
│      ↓                                                               │
│    Http2PingHealthHandler (installed only if PING interval > 0)     │
│      ├─ Sends PING every 10s via scheduleAtFixedRate                │
│      └─ On PING ACK → stamps LAST_PING_ACK_NANOS attribute         │
│                                                                      │
│  Channel Attributes (per connection):                                │
│    CONNECTION_EXPIRY_NANOS   (stamped by stampConnectionExpiry)      │
│    LAST_PING_ACK_NANOS      (stamped by Http2PingHealthHandler)     │
│    PENDING_EVICTION_NANOS   (stamped by eviction predicate Phase 3) │
│    HANDLER_INSTALLED         (one-time PING install guard)          │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ReactorNettyClient.doOnConnected()  (two independent paths)        │
│                                                                      │
│    1. Install Http2ResponseHeaderCleanerHandler (existing)           │
│    2. If maxLifetimeSeconds > 0:                                     │
│         stampConnectionExpiry(channel, baseMaxLife, jitterRange)     │
│         → stamps CONNECTION_EXPIRY_NANOS = now + base + jitter      │
│    3. If pingIntervalSeconds > 0:                                    │
│         installOnParentIfAbsent(channel, pingInterval)              │
│         → seeds LAST_PING_ACK_NANOS = now                           │
│         → starts periodic PING schedule                              │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

**Key design principle**: Channel attributes are the sole communication mechanism between the
handler, the install paths, and the eviction predicate. No shared objects, no locks, no coupling
between components.

---

## 4. Eviction Predicate Design

Set on `ConnectionProvider.Builder.evictionPredicate()`. Evaluated by reactor-netty's background
sweep (derived interval) against every connection in the pool. **Replaces all built-in eviction**
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

### Phase 2: PING Liveness (Immediate Eviction)
```java
Long lastAckNanos = parentChannel.attr(LAST_PING_ACK_NANOS).get();
if (lastAckNanos != null && System.nanoTime() - lastAckNanos > pingAckTimeoutNanos)
    return true;
```
Reads the attribute stamped by `Http2PingHealthHandler`. If no PING ACK has arrived within
the timeout (default 30s), the connection is silently degraded. **Immediate eviction** —
degraded connections should be closed fast; active streams are already failing.

**Null-safety**: `hasAttr` check ensures HTTP/1.1 connections (no PING handler) are unaffected.
Also unaffected when PING is disabled (`PING_INTERVAL=0`) — attribute is never stamped.

### Phase 3: Per-Connection Max Lifetime (Two-Phase Eviction)

Reads `CONNECTION_EXPIRY_NANOS` stamped by `stampConnectionExpiry()` in `doOnConnected`
(independent of PING handler — see §2 design choice #2). Includes per-connection jitter.

Instead of immediately evicting (which RST_STREAMs active H2 streams), Phase 3 uses two-phase
eviction to allow active streams to drain:

```java
Long expiryNanos = parentChannel.attr(CONNECTION_EXPIRY_NANOS).get();
if (expiryNanos != null && now > expiryNanos) {
    Long pendingSince = parentChannel.attr(PENDING_EVICTION_NANOS).get();
    if (pendingSince == null) {
        // First detection — mark as pending, don't evict yet
        parentChannel.attr(PENDING_EVICTION_NANOS).set(now);
        return false;
    }
    // Already pending — evict if idle or 10s grace period expired
    if (metadata.idleTime() > 0 || now - pendingSince > 10_000_000_000L) {
        return true;
    }
    return false; // active streams — wait for next sweep
}
```

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

## 5. Http2PingHealthHandler

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

## 6. Per-Connection Jitter

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
| Effective lifetime | `[1801s, 1830s]` = `[30:01, 30:30]` with defaults |
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

## 7. Configuration

All configurations use the existing `Configs` system property pattern. Not exposed as public API.

| Config | System Property | Default | Purpose |
|--------|----------------|---------|---------|
| **Max lifetime enabled** | `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_ENABLED` | `true` | Master toggle for connection max lifetime |
| Max lifetime | `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_IN_SECONDS` | `1800` (30 min) | Base connection lifetime (defensive — 6× .NET's 5 min) |
| Jitter range | Compile-time constant | `30` seconds | Per-connection random offset `[1s, 30s]` |
| **PING health enabled** | `COSMOS.HTTP2_PING_HEALTH_ENABLED` | `true` | Master toggle for PING health probing |
| PING interval | `COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS` | `10` seconds | PING frame send frequency |
| PING ACK timeout | `COSMOS.HTTP2_PING_ACK_TIMEOUT_IN_SECONDS` | `30` seconds | Evict if no ACK within this window |
| Eviction sweep | Derived | `5` seconds | `clamp(min(idleTimeout, pingAckTimeout, baseMaxLifetime) / 2, 1s, 5s)` |
| Max evictions per cycle | Hard-coded | `1` | Rate limits Phases 1–3 to prevent cliff drain. Dead channels (Phase 0) exempt. |

**Disable lifetime eviction**: `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_ENABLED=false`
**Disable PING health**: `COSMOS.HTTP2_PING_HEALTH_ENABLED=false`

---

## 8. .NET Parity

Reference: `CosmosHttpClientCore.cs` line 151, `azure-cosmos-dotnet-v3` @ `4cbe83b1`.

| Aspect | .NET | Java (this spec) |
|--------|------|-----------------|
| Base lifetime | 5 min (300s) | 5 min (300s) ✅ |
| Jitter scope | Per-pool | Per-connection ✅ Better |
| Jitter range | `[0s, 30s)` continuous | `[1s, 30s]` discrete |
| PING health | Not implemented | ✅ `Http2PingHealthHandler` |
| Eviction mechanism | .NET `PooledConnectionLifetime` | reactor-netty `evictionPredicate` |

---

## 9. Testing & CI

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

## 10. Known Gaps & Future Work

| Gap | Impact | Mitigation / Future Work |
|-----|--------|-------------------------|
| **No pre-warming on eviction** | Brief window with 0 connections (pool size = 1). Next request pays TCP+TLS cost. | Thin-client proxy is nearby (<50ms handshake). `pendingAcquireTimeout` (45s) is the safety net. |
| **Goal 1 is reactive** | If DNS hasn't changed, rotation reconnects to same IP. No active load redistribution. | Acceptable — the goal is re-resolution after failover/migration, not active balancing. |
| **Customer `networkaddress.cache.ttl=-1`** | Max lifetime evicts connections, but new ones resolve to cached (stale) IP. Goal 1 defeated. | Do not override customer's JVM DNS setting. Document as known limitation. |
| **reactor-netty 1.3.4 upgrade** | Native `maxLifeTimeVariance()` would simplify Phase 3 and remove channel attribute. | Spring Boot 4 track has 1.3.3. Track 1.3.4+ availability with Central team. |

### 10.1 Design: Decouple Max Lifetime from PING Health

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

### 10.2 Design: Graceful Eviction (Avoid RST_STREAM on Active Streams)

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
