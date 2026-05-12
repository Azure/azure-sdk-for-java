# HTTP/2 PING Keepalive — Design Spec

**Status:** Draft  
**PR:** [#49095](https://github.com/Azure/azure-sdk-for-java/pull/49095)  
**Date:** 2026-05-12

---

## 1. Problem Statement

HTTP/2 connections between the Cosmos Java SDK and the Cosmos DB gateway traverse L7 middleboxes — NAT gateways, firewalls, Azure Load Balancers, corporate proxies. These middleboxes maintain connection tracking tables and silently reap idle connections after vendor-specific timeouts (often 4–5 minutes). When a reaped connection is reused, the client sees opaque TCP RST or timeout errors with no indication the connection was severed by a middlebox.

The SDK's existing connection pool eviction (`evictionPredicate`) handles *local* idle detection but cannot prevent a middlebox from closing a connection that the SDK considers alive.

## 2. Goal

Send periodic HTTP/2 PING frames on idle parent (TCP) channels to:
1. **Keep middlebox connection tracking entries alive**, preventing silent connection reaping.
2. **Detect broken connections** — if no PING ACK within the configured timeout, close the connection so the pool creates a fresh replacement.

This is aligned with the Rust SDK’s approach where hyper’s built-in PING kills connections on timeout and the shard health sweep replaces them.

**Non-goals:**
- Replacing reactor-netty’s native `maxIdleTime` — the SDK uses a custom `evictionPredicate` that bypasses reactor-netty’s built-in idle handling.
- Application-layer keepalive (e.g., Cosmos heartbeat RPCs).
- Sharding — Rust requires per-endpoint sharding because hyper opens only 1 H2 connection per client. Reactor-netty natively pools multiple H2 connections, so sharding is unnecessary.

## 3. Design

### 3.1 Handler: `Http2PingHandler`

A Netty `ChannelDuplexHandler` installed on the **parent (TCP) channel** of an HTTP/2 connection. It tracks activity on both inbound and outbound directions and sends a PING frame when the connection has been idle for longer than a configurable interval.

#### State

| Field | Type | Description |
|---|---|---|
| `pingIntervalNanos` | `long` | Configured idle threshold (immutable) |
| `pingTimeoutNanos` | `long` | ACK timeout — close connection if exceeded (immutable) |
| `lastActivityNanos` | `long` | `System.nanoTime()` of last read/write (event-loop-local, no sync) |
| `pingTask` | `ScheduledFuture<?>` | Periodic check handle, cancelled on removal |
| `pingOutstandingSinceNanos` | `long` | `nanoTime()` when PING was sent; 0 = no outstanding PING |
| `pingsSent` | `AtomicInteger` | Monotonic counter — also used as PING frame payload |
| `pingAcksReceived` | `AtomicInteger` | Monotonic counter of received ACKs |
| `PING_HEALTH_DEGRADED` | `AttributeKey<Boolean>` | Per-channel flag: set when ACK missed, cleared on ACK |

#### Lifecycle

```
handlerAdded()
  └─ schedule periodic check (interval = max(500ms, pingInterval/2))

channelRead(frame)
  ├─ update lastActivityNanos
  ├─ if frame is Http2PingFrame(ack=true):
  │     pingAcksReceived++, pingOutstandingSinceNanos=0
  │     clear PING_HEALTH_DEGRADED attribute (connection proved responsive)
  └─ propagate downstream

write(frame)
  ├─ update lastActivityNanos
  └─ propagate upstream

maybeSendPing()            ← periodic task
  ├─ if channel inactive → cancel + return
  ├─ if feature disabled → cancel + return
  ├─ if pingOutstandingSinceNanos != 0:
  │     if waited ≥ pingTimeout → set PING_HEALTH_DEGRADED, cancel task, ctx.close()
  │     return (at most 1 in-flight PING)
  ├─ if numActiveStreams > 0 → reset lastActivityNanos + return
  │     (active request traffic is keepalive; PING would be noise)
  ├─ idleNanos = now - lastActivityNanos
  ├─ if idleNanos < pingIntervalNanos → return
  └─ set pingOutstandingSinceNanos=now, send DefaultHttp2PingFrame(++pingsSent)
     └─ on failure: pingOutstandingSinceNanos=0 (unblock next attempt)
     └─ reset lastActivityNanos

handlerRemoved() / channelInactive()
  └─ cancel pingTask
```

#### PING concurrency and noisy-neighbour prevention

**At-most-one outstanding PING.** `pingOutstandingSinceNanos` records when the PING was sent (0 = none outstanding). If a PING is already in flight, `maybeSendPing()` returns immediately — this prevents unbounded queuing if ACKs are delayed.

**Broken connection detection.** When the periodic check finds that `pingOutstandingSinceNanos` is non-zero and the elapsed wait exceeds `pingTimeoutNanos`, the handler:
1. Sets the `PING_HEALTH_DEGRADED` channel attribute to `true`
2. Logs a WARN
3. Cancels the periodic task
4. Closes the connection via `ctx.close()`

This is aligned with the Rust SDK where hyper kills connections on PING timeout. The connection pool will create a fresh replacement on the next request. The `PING_HEALTH_DEGRADED` attribute remains set for observability — external components can read `Http2PingHandler.isConnectionHealthDegraded(channel)` for diagnostic purposes.

**Suppressed during active streams.** Before checking idle time, the handler queries `Http2FrameCodec.connection().numActiveStreams()`. If any streams are open (i.e., customer requests are in flight), the PING is skipped entirely — the request/response frames already keep the middlebox entry alive. The idle baseline is reset to `now` so that the full interval is measured from when all streams close, not from the last frame on a previous stream.

Together these two guards ensure:
- At most 1 PING frame in flight at any time
- Zero PING frames while customer requests are active
- PING traffic only during true connection idleness

#### Idle detection

Any inbound frame (`channelRead`) or outbound write (`write`) resets the idle timer. The periodic check runs at `max(500ms, pingInterval / 2)` — this bounds the worst-case send delay to 1.5× the configured interval while avoiding excessive scheduling overhead.

#### PING frame details

- **Outbound:** `DefaultHttp2PingFrame(count)` where `count` = `pingsSent` counter (auto-increment). The 8-byte payload carries the sequence number for optional correlation.
- **Inbound ACK:** `Http2PingFrame` with `ack() == true`, counted and propagated.
- **No connection closure:** Missed ACKs cause the connection to be **closed** after the configured timeout. The handler sets `PING_HEALTH_DEGRADED` and calls `ctx.close()`.

### 3.2 Installation

Installed via `Http2PingHandler.installIfAbsent(Channel, int, int)`:

```
doOnConnected(connection)
  ├─ Is HTTP/2?  → check pipeline for Http2MultiplexHandler
  ├─ Is enabled? → Configs.isHttp2PingHealthEnabled()
  ├─ Interval > 0? → Configs.getHttp2PingIntervalInSeconds()
  ├─ Timeout    → Configs.getHttp2PingTimeoutInSeconds()
  └─ installIfAbsent(channel, interval, timeout)
       ├─ resolve parent channel (channel.parent() ?? channel)
       ├─ idempotency check via PING_HANDLER_INSTALLED attribute
       └─ parent.pipeline().addLast("cosmos.http2PingHandler", handler)
```

**Why parent channel?** HTTP/2 PING is a connection-level frame (RFC 9113 §6.7). Stream-level channels don't have access to the connection frame codec. The parent TCP channel's `Http2FrameCodec` encodes/decodes PING frames.

**Idempotency:** `doOnConnected` fires once for the parent channel and once per stream. The `PING_HANDLER_INSTALLED` `AttributeKey` on the parent prevents duplicate installation. A race between concurrent stream channels is caught via `IllegalArgumentException` (benign).

### 3.3 Integration point

`ReactorNettyClient.java` → `httpClient.doOnConnected(...)` callback. The handler is installed after the HTTP/2 codec is in the pipeline but the detection is done via `Http2MultiplexHandler.class` presence in the pipeline (which is only present on H2 parent channels, not on HTTP/1.1 connections).

### 3.4 Test hook

The test (`Http2PingKeepaliveTest`) relies on the auto-installed handler and reads aggregate PING statistics via `Http2PingHandler.getGlobalPingsSent()` and `getGlobalPingAcksReceived()`. No interceptor or callback injection is required.

## 4. Configuration

| System Property | Default | Description |
|---|---|---|
| `COSMOS.HTTP2_PING_HEALTH_ENABLED` | `true` | Master switch. Set to `false` to disable PING keepalive. |
| `COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS` | `1` | Idle threshold before sending a PING frame. Aligned with Rust SDK (1s). |
| `COSMOS.HTTP2_PING_TIMEOUT_IN_SECONDS` | `2` | ACK timeout. If no ACK within this, the connection is closed. Aligned with Rust SDK (2s). |

**Aligned with Rust SDK:** Both SDKs use interval=1s, timeout=2s by default. Dead connection detected within ~3s worst-case.

**No client builder API.** Configuration is JVM-wide via system properties, consistent with the existing `COSMOS.HTTP2_ENABLED` pattern.

**Runtime disable:** The periodic task checks `Configs.isHttp2PingHealthEnabled()` on every tick. Setting the property to `false` at runtime causes the task to self-cancel on the next check.

## 5. Threading Model

- The handler runs entirely on Netty's channel event loop (single thread per channel).
- `lastActivityNanos` requires no synchronization — only accessed from the event loop thread.
- `pingsSent` / `pingAcksReceived` are `AtomicInteger` for safe cross-thread diagnostic reads (e.g., tests, metrics).
- `ScheduledFuture.cancel(false)` is non-interrupting — if the task is mid-execution, it completes and won't be rescheduled.

## 6. Timing Characteristics

| Parameter | Value (defaults) |
|---|---|
| PING interval | 1 second |
| PING ACK timeout | 2 seconds |
| Check frequency | 500 milliseconds (interval / 2) |
| Worst-case detection | ~3 seconds (idle just after a check + full timeout) |
| Minimum check frequency | 500 milliseconds (clamped) |

For a default-configured idle connection:
```
t=0s    last activity
t=0.5s  check → idle 0.5s < 1s → skip
t=1s    check → idle 1s ≥ 1s → PING sent, idle reset
t=1.5s  check → PING outstanding 0.5s < 2s → wait
t=2s    check → PING outstanding 1s < 2s → wait
t=2.5s  check → PING outstanding 1.5s < 2s → wait
t=3s    check → PING outstanding 2s ≥ 2s → CLOSE (if no ACK)
…or at t=1.1s  ACK arrives → pingOutstanding=0, PING_HEALTH_DEGRADED cleared
```

## 7. Failure Modes

| Scenario | Behavior |
|---|---|
| PING ACK not received | After timeout: `PING_HEALTH_DEGRADED` set, task cancelled, connection **closed** via `ctx.close()`. Pool creates replacement on next request. |
| PING ACK arrives late | Before timeout: `PING_HEALTH_DEGRADED` cleared, `pingOutstandingSinceNanos` reset. Connection resumes normal keepalive. |
| Connection has active streams | PING suppressed — request traffic is keepalive. Idle baseline reset. |
| Channel becomes inactive | Periodic task cancelled in `channelInactive()`. |
| Feature disabled at runtime | Task self-cancels on next check. |
| Handler already installed (race) | `IllegalArgumentException` caught and ignored. |
| `ctx.writeAndFlush()` fails | WARN logged; idle timer reset to prevent tight retry loop. |
| HTTP/1.1 connection | Handler never installed (no `Http2MultiplexHandler` in pipeline). |

## 8. Logging

| Level | Event |
|---|---|
| DEBUG | Handler installed (interval, check frequency, channel ID) |
| DEBUG | PING frame sent successfully (sequence #, channel ID) |
| DEBUG | PING ACK received, degraded flag cleared (channel ID) |
| WARN | PING frame send failure (sequence #, channel ID, exception) |
| WARN | PING ACK not received within timeout — connection closed (channel ID) |

## 9. Test Coverage

**Test:** `Http2PingKeepaliveTest` (TestNG, group `manual-http-network-fault`)

**Environment:** Docker with `--cap-add=NET_ADMIN`, real Cosmos DB account (thin-client-enabled).

| Step | What's verified |
|---|---|
| 1. Create H2 client, seed data | Connection establishment |
| 2. Set interval to 3s, do a read | Handler installation on parent channel |
| 3. Sleep 20s (idle) | PINGs fire (~6-7 expected) |
| 4. Do another read | Connection survived idle period |
| 5. Assert `pingsSent > 0` | PING transmission works |
| 6. Assert `pingAcksReceived > 0` | Server acknowledges PINGs |

**Helper:** `Http2PingFrameCounterHandler` — independent inbound handler that counts PING ACK frames for cross-validation.

## 10. Rust SDK Alignment

| Dimension | Java | Rust |
|---|---|---|
| Mechanism | Custom Netty `ChannelDuplexHandler` | hyper’s built-in `http2_keep_alive_*` |
| PING interval | 1s (configurable) | 1s (configurable) |
| ACK timeout | 2s (configurable) | 2s (configurable) |
| On missed ACK | Close connection | hyper kills connection → shard eviction |
| PING while idle | Yes (suppressed when activeStreams > 0) | Yes (`http2_keep_alive_while_idle = true`) |
| In-flight limit | 1 (explicit gate) | hyper manages internally |
| Sharding | Not needed — reactor-netty pools multiple H2 connections per endpoint | Per-endpoint shard pools (needed because hyper opens only 1 H2 connection per client) |
| TCP keepalive | Not explicitly disabled alongside H2 PING | Mutually exclusive with H2 PING |

**Deliberate differences:**

- **Active-stream suppression** (Java-only) avoids sending PING frames during request bursts. Rust’s hyper handles this internally.
- **No sharding needed** — Rust requires per-endpoint sharding because hyper's HTTP/2 client opens only 1 connection per `reqwest::Client`. Java's reactor-netty natively supports pooling multiple H2 connections per endpoint via `Http2AllocationStrategy` (configurable `minConnections`/`maxConnections`/`maxConcurrentStreams`), so the PING handler is simply installed per-connection with no shard layer.

## 11. Future Considerations

- **Metrics integration:** Expose `pingsSent` / `pingAcksReceived` / `PING_HEALTH_DEGRADED` in `CosmosDiagnostics` for observability.
- **Client builder API:** Expose `http2PingIntervalInSeconds()` and `http2PingTimeoutInSeconds()` on `CosmosClientBuilder` for per-client configuration.
- **TCP keepalive mutual exclusion:** Explicitly disable TCP keepalive when HTTP/2 PING is active (aligned with Rust).
- **Adaptive interval:** Adjust PING interval based on observed middlebox behavior.
