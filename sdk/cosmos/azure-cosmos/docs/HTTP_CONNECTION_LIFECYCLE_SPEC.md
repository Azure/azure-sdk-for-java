# HTTP/2 Connection Lifecycle Management

**Status**: Draft — PR [#48420](https://github.com/Azure/azure-sdk-for-java/pull/48420)
**Author**: Abhijeet Mohanty
**Tracking**: [#48251](https://github.com/Azure/azure-sdk-for-java/issues/48251)

---

## Goals

1. **DNS re-resolution** — Force periodic connection rotation so that DNS changes (failover,
   migration, scaling) are picked up within a bounded window. TCP connections never re-resolve
   DNS on their own; max lifetime is the mechanism that forces new connection creation.

2. **Connection keepalive** — Prevent intermediate infrastructure (NAT gateways, firewalls,
   load balancers) from silently reaping idle HTTP/2 connections. HTTP/2 PING frames serve as
   application-layer keepalive, distinct from TCP keepalive which operates below TLS and may
   not be visible to L7 middleboxes.

## Non-Goals

- **PING-based eviction** — Client-driven connection closure will only be driven by idle
  timeout and max life of connection. We do not evict connections based on stale PING ACKs.
  Degraded connections are handled by the existing response timeout retry path (6s/6s/10s
  escalation → cross-region failover). PING serves only as keepalive.

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
   re-implement idle timeout in the predicate.

2. **Max lifetime and PING keepalive are independent features** — Separate install paths
   in `doOnConnected`: `stampConnectionExpiry()` for lifetime, `installOnParentIfAbsent()`
   for PING. Disabling one does not affect the other. Both gated by explicit boolean configs.

3. **Per-connection jitter** — Each connection gets a deterministic expiry stamped once at
   creation (`CONNECTION_EXPIRY_NANOS` channel attribute). Avoids .NET's per-pool sync-lock
   (all connections expire together) and the non-determinism of re-rolling jitter each sweep.
   Matches reactor-netty 1.3.4's `maxLifeTimeVariance` semantics for easy migration.

4. **Two-phase eviction for lifetime** — Instead of immediately closing a connection past
   its lifetime (which RST_STREAMs active H2 streams), mark as `PENDING_EVICTION_NANOS` on
   first detection, then evict when idle or after a 10s drain grace period.

5. **Eviction rate limiter** — At most 1 connection evicted per sweep cycle (dead channels
   exempt). Prevents cliff eviction when multiple connections expire in the same window.

6. **Derived sweep interval** — `clamp(min(idleTimeout, baseMaxLifetime) / 2, 1s, 5s)`.
   Always faster than the smallest eviction threshold.

7. **30-minute default (defensive)** — .NET uses 5 minutes. We start at 30 minutes with
   `[30:01, 30:30]` effective range. Can be tuned down after production validation.

---

## Architecture

```
ConnectionProvider (reactor-netty 1.2.13)
│
├─ evictInBackground(derived interval)
│  │
│  └─ evictionPredicate:
│       Phase 0: !channel.isActive()         → evict (dead, no rate limit)
│       ── rate limiter: max 1 per cycle ──
│       Phase 1: idleTime > 60s              → evict (idle)
│       Phase 2: nanoTime > CONNECTION_EXPIRY → two-phase eviction
│                1st sweep: mark PENDING_EVICTION_NANOS
│                next: evict if idle OR 10s grace period expired
│
├─ doOnConnected (two independent paths):
│  │
│  ├─ If max lifetime enabled:
│  │    stampConnectionExpiry(channel, base + jitter)
│  │    → stamps CONNECTION_EXPIRY_NANOS attribute
│  │
│  └─ If PING keepalive enabled:
│       installOnParentIfAbsent(channel, interval)
│       → installs Http2PingHealthHandler on parent H2 channel
│       → sends PING frames every 10s (keepalive, not eviction)
│
└─ Channel Attributes:
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
resolves to multiple IPs behind the same hostname. To validate that max lifetime actually
achieves DNS re-resolution and traffic redistribution:

1. Resolve the endpoint to enumerate available IPs (e.g., `dig` or `nslookup`).
2. Establish connections and capture the resolved IP from the parent channel's
   `remoteAddress()`.
3. Use `iptables` to block traffic to one specific IP, forcing DNS to resolve to
   an alternate IP on the next connection.
4. Wait for max lifetime to expire and the connection to rotate.
5. Assert the new connection's `remoteAddress()` is the alternate IP.

This validates the full chain: max lifetime → eviction → pool creates new connection →
DNS re-resolution → traffic moves to a different backend IP.

---

## Future Work

- **reactor-netty 1.3.4**: Replace custom lifetime logic with native `maxLifeTime()` +
  `maxLifeTimeVariance()`. Spring Boot 4 track has 1.3.3 — track 1.3.4+ with Central team.
- **PING-based eviction**: Currently scoped to keepalive only. If production data shows
  value in evicting on stale ACKs, the `LAST_PING_ACK_NANOS` attribute is already tracked —
  adding an eviction phase is a predicate-only change.
- **JVM DNS cache**: Default TTL is 30s (verified JDK 18). If a customer sets `-1` (cache
  forever), max lifetime still rotates connections but new ones resolve to the cached IP.
  We do not override the customer's JVM DNS setting.
