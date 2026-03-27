# HTTP Connection Lifecycle Management

**Status**: Draft — PR [#48420](https://github.com/Azure/azure-sdk-for-java/pull/48420)
**Author**: Abhijeet Mohanty
**Tracking**: [#48251](https://github.com/Azure/azure-sdk-for-java/issues/48251)

---

## Goals

1. **DNS re-resolution** — Force periodic connection rotation so that DNS changes (failover,
   migration, scaling) are picked up within a bounded window. TCP connections never re-resolve
   DNS on their own; max lifetime is the mechanism that forces new connection creation.
   **Applies to both HTTP/1.1 and HTTP/2 connections.**

2. **Connection keepalive (HTTP/2)** — Prevent intermediate infrastructure (NAT gateways,
   firewalls, load balancers) from silently reaping idle HTTP/2 connections. HTTP/2 PING
   frames serve as application-layer keepalive, distinct from TCP keepalive which operates
   below TLS and may not be visible to L7 middleboxes. HTTP/1.1 keepalive is a separate
   future concern (see Future Work).

## Non-Goals

- **PING-based eviction** — Client-driven connection closure will only be driven by idle
  timeout and max life of connection. We do not evict connections based on stale PING ACKs.
  Degraded connections are handled by the existing response timeout retry path (6s/6s/10s
  escalation → cross-region failover). PING serves only as keepalive.

## Protocol Split (Production Evidence)

Kusto query against `ComputeRequest5M` for `legacy-conversations-prod-0` (2026-03-23):

| Operation | Protocol | Volume (6h) |
|-----------|----------|-------------|
| Read, Upsert, Query, Batch, Patch, Create | HTTP/2 | ~43.8M |
| **ChangeFeed/Incremental** | **HTTP/1.1** | ~978K |
| Read (some), Batch (some), ReadFeed | HTTP/1.1 | ~147K |

Both protocols coexist on the same account. Max lifetime must cover both pools to achieve
Goal 1 (DNS re-resolution) for all operation types including ChangeFeed.

---

## TCP Keepalive vs HTTP/2 PING

Both prevent idle connection reaping, but operate at different layers:

| | TCP Keepalive | HTTP/2 PING |
|-|--------------|-------------|
| **Layer** | TCP (below TLS) | Application (HTTP/2 frame, inside TLS) |
| **Visibility to L7 middleboxes** | ❌ Invisible — middlebox sees no TLS records | ✅ Visible — TLS record with HTTP/2 frame |
| **NAT/firewall idle timer reset** | ✅ Resets TCP-level timer | ✅ Resets both TCP and L7 timers |
| **Detects half-open TCP** | ✅ (after multiple probes) | ✅ (if ACK tracking is added) |
| **Java SDK control** | Limited — OS/JVM defaults | Full — `ChannelDuplexHandler` on parent channel |

HTTP/2 PING is the right choice because thin-client proxy connections traverse L7
infrastructure where TCP keepalive alone is insufficient.

---

## Design Choices

1. **Custom eviction predicate replaces all built-in eviction** — reactor-netty 1.2.13:
   custom `evictionPredicate` bypasses built-in `maxIdleTime` and `maxLifeTime`. We must
   re-implement idle timeout in the predicate. Applies to all connections (H1.1 and H2).

2. **Max lifetime covers both HTTP/1.1 and HTTP/2** — The eviction predicate and
   `CONNECTION_EXPIRY_NANOS` attribute apply to all connections in the pool. This ensures
   DNS re-resolution for all operation types, including ChangeFeed (HTTP/1.1).

3. **PING keepalive is HTTP/2 only** — PING is an HTTP/2 protocol frame — it cannot be
   sent on HTTP/1.1 connections. The handler is installed only when H2 is enabled
   (`isH2Enabled && PING_HEALTH_ENABLED`). HTTP/1.1 keepalive is a separate future concern
   (see Future Work).

4. **Max lifetime and PING keepalive are independent features** — Separate install paths
   in `doOnConnected`: `stampConnectionExpiry()` for lifetime (all connections),
   `installOnParentIfAbsent()` for PING (H2 only). Disabling one does not affect the other.

5. **Per-connection jitter** — Each connection gets a deterministic expiry stamped once at
   creation (`CONNECTION_EXPIRY_NANOS` channel attribute). Avoids .NET's per-pool sync-lock
   (all connections expire together) and the non-determinism of re-rolling jitter each sweep.
   Matches reactor-netty 1.3.4's `maxLifeTimeVariance` semantics for easy migration.

6. **Two-phase eviction for lifetime** — Instead of immediately closing a connection past
   its lifetime (which RST_STREAMs active H2 streams), mark as `PENDING_EVICTION_NANOS` on
   first detection, then evict when idle or after a 10s drain grace period.

7. **Eviction rate limiter** — At most 1 connection evicted per sweep cycle (dead channels
   exempt). Prevents cliff eviction when multiple connections expire in the same window.

8. **Derived sweep interval** — `clamp(min(idleTimeout, baseMaxLifetime) / 2, 1s, 5s)`.
   Always faster than the smallest eviction threshold.

9. **30-minute default (defensive)** — .NET uses 5 minutes. We start at 30 minutes with
   `[30:01, 30:30]` effective range. Can be tuned down after production validation.

---

## Architecture

The eviction predicate, max lifetime, and PING keepalive apply to **all** connections in the
pool (HTTP/1.1 and HTTP/2). The H2-specific `doOnConnected` block handles only H2 pipeline
handlers (header cleaner, H2 settings).

```
ConnectionProvider (reactor-netty 1.2.13)
│
├─ evictInBackground(derived interval)
│  │
│  └─ evictionPredicate (ALL connections — H1.1 and H2):
│       Phase 0: !channel.isActive()         → evict (dead, no rate limit)
│       ── rate limiter: max 1 per cycle ──
│       Phase 1: idleTime > 60s              → evict (idle)
│       Phase 2: nanoTime > CONNECTION_EXPIRY → two-phase eviction
│                1st sweep: mark PENDING_EVICTION_NANOS
│                next: evict if idle OR 10s grace period expired
│
├─ doOnConnected (shared — ALL connections):
│  │
│  ├─ If max lifetime enabled:
│  │    stampConnectionExpiry(channel, base + jitter)
│  │    → stamps CONNECTION_EXPIRY_NANOS attribute
│  │
│  └─ If PING keepalive enabled AND H2 enabled:
│       installOnParentIfAbsent(channel, interval)
│       → installs Http2PingHealthHandler (H2 only — PING is an HTTP/2 frame)
│       → sends PING frames every 10s (keepalive, not eviction)
│
├─ doOnConnected (H2 only — if H2 enabled):
│    Http2ResponseHeaderCleanerHandler installation
│
└─ Channel Attributes (per connection):
     CONNECTION_EXPIRY_NANOS    (stamped by stampConnectionExpiry)
     PENDING_EVICTION_NANOS    (stamped by eviction predicate)
     LAST_PING_ACK_NANOS       (stamped by PING handler, for future use)
     HANDLER_INSTALLED          (one-time PING install guard)
```

---

## Configuration

All internal — not exposed as public API. System property pattern.

| Config | System Property | Default | Purpose |
|--------|----------------|---------|---------|
| Max lifetime enabled | `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_ENABLED` | `true` | Master toggle |
| Max lifetime | `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_IN_SECONDS` | `1800` (30 min) | Base lifetime |
| Jitter range | Compile-time constant | `30s` | Per-connection offset `[1s, 30s]` |
| PING keepalive enabled | `COSMOS.HTTP2_PING_HEALTH_ENABLED` | `true` | Master toggle |
| PING interval | `COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS` | `10s` | Keepalive frequency |
| Eviction sweep | Derived | `5s` | `clamp(min(thresholds) / 2, 1s, 5s)` |
| Max evictions/cycle | Hard-coded | `1` | Rate limit (dead channels exempt) |

**Disable**: Set `COSMOS.HTTP_CONNECTION_MAX_LIFETIME_ENABLED=false` or
`COSMOS.HTTP2_PING_HEALTH_ENABLED=false`.

---

## .NET Parity

| Aspect | .NET | Java |
|--------|------|------|
| Base lifetime | 5 min | 30 min (defensive) |
| Jitter | Per-pool `[0s, 30s)` | Per-connection `[1s, 30s]` |
| PING keepalive | No | Yes |
| PING-based eviction | No | No |

---

## Testing

### Unit / Integration Tests

Test group: `manual-http-network-fault`. Linux with `tc`/`iptables`.

| Test | Validates |
|------|-----------|
| `connectionRotatedAfterMaxLifetimeExpiry` | Connection evicted after lifetime + jitter |
| `perConnectionJitterStaggersEviction` | Connections don't all expire in the same sweep |
| `connectionEvictedAfterMaxLifetimeEvenWithHealthyPings` | Lifetime eviction fires even when PINGs succeed |

CI stage: `Cosmos_Live_Test_HttpNetworkFault` on Ubuntu VMs, `MaxParallel: 1`.

### End-to-End DNS Rotation Validation

The Cosmos DB front-end DNS (`<account>-<region>.documents.azure.com:443` and `:10250`)
resolves to multiple IPs behind the same hostname. To validate that max lifetime achieves
DNS re-resolution and traffic redistribution, tests use a `FilterableDnsResolverGroup` —
a custom Netty `AddressResolverGroup` that wraps JVM resolution but dynamically filters
out blocked IPs at runtime. No OS-level hacks (no `/etc/hosts`, no `iptables`, no external
DNS server).

Wired via `HttpClient.resolver(resolverGroup)` in reactor-netty.

**Test flow:**
1. Create `FilterableDnsResolverGroup`, wire into client builder
2. Resolve endpoint → enumerate IPs (IP1, IP2)
3. Run workload, capture `remoteAddress()` from parent channel → connections on IP1
4. Mid-workload: `resolver.blockIp(IP1)` — dynamic, no restart
5. Wait for max lifetime → eviction → new connection → resolver returns IP2 only
6. Assert new connection's `remoteAddress()` is IP2
7. `resolver.unblockIp(IP1)` → next rotation may return to IP1

This validates the full chain: max lifetime → eviction → pool creates new connection →
DNS re-resolution → traffic moves to a different backend IP. The dynamic block/unblock
cycle proves the behavior is repeatable under a running workload.

---

## Future Work

- **reactor-netty 1.3.4**: Replace custom lifetime logic with native `maxLifeTime()` +
  `maxLifeTimeVariance()`. Spring Boot 4 track has 1.3.3 — track 1.3.4+ with Central team.
- **PING-based eviction**: Currently scoped to keepalive only. If production data shows
  value in evicting on stale ACKs, the `LAST_PING_ACK_NANOS` attribute is already tracked —
  adding an eviction phase is a predicate-only change.
- **HTTP/1.1 application-layer keepalive**: HTTP/1.1 has no PING equivalent. HTTP/2 PING
  frames keep connections alive through L7 middleboxes, but HTTP/1.1 connections rely solely
  on TCP keepalive — invisible to L7 proxies/load balancers. Explore sending periodic
  `OPTIONS` requests as an application-layer keepalive for HTTP/1.1 connections in sparse
  workloads. Kusto evidence (2026-03-23, `legacy-conversations-prod-0`, 6h window): HTTP/1.1
  traffic is **100% ChangeFeed/Incremental** (~388M requests) from two SDK versions —
  `4.75.0-alpha.20251008.4` (348M, 90%) and `1.1.2-openai-release` (40M, 10%). ChangeFeed
  is long-polling so rarely truly idle, making this low risk today — but worth addressing if
  future HTTP/1.1 workloads beyond ChangeFeed emerge. (Thin-client proxy uses only HTTP/2,
  so this gap does not affect thin-client scenarios.)
- **JVM DNS cache**: Default TTL is 30s (verified JDK 18). If a customer sets `-1` (cache
  forever), max lifetime still rotates connections but new ones resolve to the cached IP.
  We do not override the customer's JVM DNS setting.
