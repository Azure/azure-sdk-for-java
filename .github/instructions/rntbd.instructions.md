---
applyTo: "sdk/cosmos/**/rntbd/**"
---

# RNTBD Protocol — Class Reference for Cosmos DB Java SDK

> The acronym "RNTBD" is not formally expanded in any public documentation or source code.
> All claims in this document were verified against source code with file:line references.

## Wire Format

An RNTBD request on the wire:

```
[messageLength: 4 bytes LE]               ← written by RntbdRequest.encode()
[frame: 20 bytes]                          ← written by RntbdRequestFrame.encode()
  [resourceType:  2 bytes LE]
  [operationType: 2 bytes LE]
  [activityId:    16 bytes, MS GUID order]
[tokens: variable]                         ← written by RntbdTokenStream.encode()
  per token: [id: 2 bytes LE][type: 1 byte][value: variable]
[payloadLength: 4 bytes LE]               ← only if payload present
[payload: variable]                        ← raw bytes (e.g., JSON query spec)
```

**Quirk**: `RntbdRequestFrame.LENGTH = 24` includes the 4-byte messageLength prefix that `RntbdRequest.encode()` writes, even though `frame.encode()` only writes 20 bytes. This constant is used for computing the messageLength value itself.

## Why RNTBD Exists

HTTP+JSON header names are full UTF-8 strings repeated on every request (~40 bytes per name). RNTBD replaces each with a 2-byte short ID + 1-byte type tag + native binary value. A point read shrinks from ~800 bytes (HTTP) to ~200 bytes (RNTBD), ~4x smaller and ~10x faster to parse. At millions of ops/sec per partition this matters.

## Two Encoding Paths

- **Direct mode** (`forThinClient=false`): Used by `RntbdRequestEncoder`. All tokens in enum order. Goes over TCP to backend replica.
- **Thin client mode** (`forThinClient=true`): Used by `ThinClientStoreModel`. Ordered subset first, 3 tokens excluded (TransportRequestID, IntendedCollectionRid, ReplicaPath). RNTBD bytes become the HTTP/2 POST body to proxy (:10250).

The proxy needs HTTP headers for routing decisions *before* parsing the RNTBD body (account name, operation type, activity-id). The RNTBD body carries the full request details (auth, EPK, query features, payload). HTTP headers are a lightweight routing summary; RNTBD is the processing payload.

## Core Serialization Classes

### RntbdConstants
Protocol constants. Nested enums:
- `RntbdRequestHeader` — request token IDs (e.g., `SupportedQueryFeatures = 0x002B`, `QueryVersion = 0x002C`). Holds `thinClientHeadersInOrderList` (12 entries) and `thinClientExclusionList` (3 entries).
- `RntbdOperationType` — wire op codes (e.g., `QueryPlan = 0x0042`, `Read = 0x0003`).
- `RntbdResourceType` — wire resource codes (e.g., `Document = 0x0003`).

### RntbdToken
One typed header value: `[id:2][type:1][value:N]`.
- **Quirk**: `getValue()` lazily converts and caches internally — a getter with side effects.

### RntbdTokenStream\<T\>
Abstract container of `RntbdToken` instances. Base for `RntbdRequestHeaders` and `RntbdResponseHeaders`.
- `encode(out, isThinClient)` — thin client mode writes ordered subset first, then remaining.
- **Quirk**: Unknown token IDs during decode become `UndefinedHeader` instead of throwing.

### RntbdRequestFrame
Fixed 20-byte identity: `resourceType + operationType + activityId`.

### RntbdRequestHeaders
`extends RntbdTokenStream<RntbdRequestHeader>`. Populates RNTBD tokens from `RxDocumentServiceRequest` HTTP headers via:
1. Special-case `addXxx()` methods.
2. Generic `fillTokenFromHeader(headers, tokenSupplier, httpHeaderName)`.
- **Quirk**: ~50 header mappings, heavily mutable. Largest protocol translation surface.

### RntbdRequestArgs
Immutable bundle: `RxDocumentServiceRequest` + `activityId` + timing + `replicaPath` + `transportRequestId`.

### RntbdRequest
Complete request = `frame + headers + payload`.
- `from(RntbdRequestArgs)` — factory: creates frame, populates headers, extracts payload.
- `encode(ByteBuf, forThinClient)` — writes full wire message.
- **Quirk**: `setHeaderValue()` mutates after construction (used by ThinClientStoreModel for EPK). Payload `byte[]` not defensive-copied.

### RntbdRequestEncoder
Netty `MessageToByteEncoder<RntbdRequestRecord>`. Always uses `encode(out, false)` for direct mode.

## Response Classes

### RntbdResponseStatus
Response-side fixed header (analogous to `RntbdRequestFrame`).
- **Quirk**: Named `Status`, not `ResponseFrame`, despite serving the same structural role.

### RntbdResponseHeaders
`extends RntbdTokenStream<RntbdResponseHeader>`.
- **Quirk**: Misspelled field `storageMaxResoureQuota`. `lastStateChangeDateTime` mapped twice.

### RntbdResponse
Full response. `implements ReferenceCounted`. `decode()` returns `null` until full payload available.

### RntbdResponseDecoder
Netty `ByteToMessageDecoder`.
- **Quirk**: `static final AtomicReference<Instant> decodeStartTime` shared across all instances/channels.

## Connection Handshake Classes

### RntbdContextRequest / RntbdContext
Client → server handshake / server → client response. Sent once per channel before normal traffic. Uses `Connection`/`Connection` op/resource types.
- **Quirk**: `RntbdContext.from(...)` has a comment saying it's for test scenarios only.

### RntbdContextNegotiator
`extends CombinedChannelDuplexHandler`. One-time handshake before normal traffic.

## Transport / Endpoint Classes

### RntbdTransportClient
Top-level orchestrator (lives at `directconnectivity/RntbdTransportClient.java`). Manages endpoint provider, address selection, proactive open-connections, lifecycle.

### RntbdEndpoint (interface) / RntbdServiceEndpoint (impl)
Endpoint = channel pool + metrics + request dispatch for one backend address.
- **Quirk**: Mixed atomics and plain mutable fields. `lastRequestNanoTime` initialized to `System.nanoTime()` to avoid negative elapsed-time math.

### RntbdClientChannelPool
Channel pool with acquisition limits and fairness heuristics.
- **Quirk**: Fairness and metrics are "approximate, not guaranteed" per class comment. `availableChannels` relies on event-loop confinement for thread safety.

### RntbdClientChannelHandler
Builds the Netty pipeline: SSL → IdleState → ContextNegotiator → ResponseDecoder → RequestEncoder → RequestManager.

### RntbdRequestManager
Central pipeline handler: request routing, pending tracking, handshake, errors.
- **Quirk**: Huge mutable state machine. Correctness depends on Netty event-loop confinement.

### RntbdRequestRecord
`extends CompletableFuture<StoreResponse>`. Async lifecycle tracker with staged state machine.
- **Quirk**: Both a future and a request record — surprising dual role.

## Utility Classes

| Class | Purpose |
|-------|---------|
| `RntbdTokenType` | Token type enum + codec (Byte, Short, Long, String, SmallString, Guid, Bytes) |
| `RntbdUUID` | UUID encode/decode in MS GUID byte order |
| `RntbdFramer` / `RntbdRequestFramer` | Frame length validation / Netty LengthFieldBasedFrameDecoder |
| `RntbdRequestTimer` | Request timeout scheduling |
| `RntbdHealthCheckRequest` | Prebuilt health-check message |
| `RntbdClientChannelHealthChecker` | Channel health via timing + CPU-sensitive timeouts |
| `RntbdConnectionStateListener` | Triggers address refresh on connection failures |
| `RntbdOpenConnectionsHandler` | Proactive warm-up orchestration |
| `RntbdObjectMapper` | Jackson utilities (has static mutable class-name cache) |
| `RntbdLoop` / `LoopEpoll` / `LoopNIO` / `LoopNativeDetector` | Netty event loop abstraction |
| `RntbdMetrics` / `MetricsCompletionRecorder` | Metrics collection |
| `RntbdDurableEndpointMetrics` | Monotonic counters surviving endpoint recycling |
| `RntbdChannelAcquisitionTimeline` / `Event` / `EventType` | Acquisition diagnostics |
| `RntbdChannelState` / `Statistics` | Per-channel snapshots |
| `RntbdConnectionEvent` | Connection lifecycle enum |
| `RntbdThreadFactory` | Custom-named thread factory |
