# Multi-Tenancy Deep Analysis: Cosmos DB Java SDK — Gateway Mode

> **Branch**: `multi-tenancy-analysis`
> **Scope**: Hundreds of `CosmosClient` instances (one per Cosmos DB account) in a single JVM, using **Gateway mode** (thin client). Rntbd/Direct mode is excluded.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Client Initialization Chain](#2-client-initialization-chain)
3. [Per-Client Resource Inventory](#3-per-client-resource-inventory)
   - 3.1 [Reactor Netty HTTP Client & Connection Pool](#31-reactor-netty-http-client--connection-pool)
   - 3.2 [Caches](#32-caches)
   - 3.3 [Background Tasks & Threads](#33-background-tasks--threads)
   - 3.4 [HTTP Protocol Version Analysis: HTTP/1.1 vs HTTP/2](#34-http-protocol-version-analysis-http11-vs-http2)
   - 3.5 [ThinClient & HTTP/2 Architecture](#35-thinclient--http2-architecture)
   - 3.6 [Per-Client Memory Breakdown](#36-per-client-memory-breakdown)
4. [Shared (Singleton) Resources](#4-shared-singleton-resources)
5. [Resource Impact at Scale](#5-resource-impact-at-scale)
6. [Resource Leak Bugs](#6-resource-leak-bugs)
7. [Recommendations & Action Items](#7-recommendations--action-items)

---

## 1. Executive Summary

Each `CosmosClient` / `CosmosAsyncClient` in Gateway mode allocates a **full, independent set of resources**: a Reactor Netty connection pool (up to 1000 connections), background threads, multiple unbounded caches, and periodic background HTTP calls. At 100+ clients, this results in:

| Concern | Impact |
|---|---|
| **Threads** | 100+ dedicated threads (1 per client for `GlobalEndpointManager`) + shared pools |
| **Connections** | Up to N × 1000 TCP connections (HTTP/1.1); file descriptor exhaustion risk |
| **Memory** | ~2–6 MB per client (typical); up to 15+ MB heavy usage; caches are unbounded ⚠️ |
| **Background HTTP** | N requests every ~5 min (endpoint refresh) |
| **Resource leak** | ~~ClientTelemetry.close() no-op~~ **FIXED** — IMDS client is now ephemeral, no per-client leak |
| **ThinClient leak** | `ThinClientStoreModel` is **never closed** in `RxDocumentClientImpl.close()` |
| **HTTP/2** | Supported via `Http2ConnectionConfig` + `ThinClientStoreModel`, but disabled by default |

---

## 2. Client Initialization Chain

```
CosmosClientBuilder.buildAsyncClient()
  └─► new CosmosAsyncClient(builder)
       └─► AsyncDocumentClient.Builder.build()
            └─► new RxDocumentClientImpl(...)
                 ├─► constructor
                 │    ├─► httpClient()                     → creates Reactor Netty pool (or SharedGatewayHttpClient)
                 │    ├─► new GlobalEndpointManager         → background scheduler thread
                 │    ├─► new SessionContainer              → session token cache
                 │    ├─► new ClientTelemetry().init()       → IMDS HTTP client pool (5 connections)
                 │    └─► queryPlanCache = new ConcurrentHashMap<>()
                 └─► init()
                      ├─► createRxGatewayProxy()            → RxGatewayStoreModel
                      ├─► initThinClient()                  → ThinClientStoreModel (if HTTP/2 + thin client enabled)
                      ├─► globalEndpointManager.init()       → starts background refresh
                      ├─► new RxClientCollectionCache         → collection metadata cache
                      └─► new RxPartitionKeyRangeCache        → partition key range cache
```

**Key file**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/RxDocumentClientImpl.java`

---

## 3. Per-Client Resource Inventory

### 3.1 Reactor Netty HTTP Client & Connection Pool

The Cosmos SDK has its **own internal Reactor Netty HTTP client** — it does NOT use `azure-core-http-netty`. The implementation lives under `com.azure.cosmos.implementation.http`.

#### Connection Pool Creation

**File**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/http/HttpClient.java`

```java
static HttpClient createFixed(HttpClientConfig httpClientConfig) {
    ConnectionProvider.Builder fixedConnectionProviderBuilder = ConnectionProvider
        .builder(httpClientConfig.getConnectionPoolName());
    fixedConnectionProviderBuilder.maxConnections(httpClientConfig.getMaxPoolSize());
    // pendingAcquireTimeout, maxIdleTime also configured
}
```

When **HTTP/2 is enabled**, an `Http2AllocationStrategy` replaces the default strategy:

```java
if (http2CfgAccessor.isEffectivelyEnabled(http2Cfg)) {
    fixedConnectionProviderBuilder.allocationStrategy(
        Http2AllocationStrategy.builder()
            .minConnections(http2CfgAccessor.getEffectiveMinConnectionPoolSize(http2Cfg))
            .maxConnections(http2CfgAccessor.getEffectiveMaxConnectionPoolSize(http2Cfg))
            .maxConcurrentStreams(http2CfgAccessor.getEffectiveMaxConcurrentStreams(http2Cfg))
            .build()
    );
}
```

#### Default Pool Configuration

| Parameter | HTTP/1.1 Default | HTTP/2 Default | Source |
|---|---|---|---|
| Max connections | **1000** | **1000** | `Configs.getReactorNettyMaxConnectionPoolSize()` |
| Min connections (HTTP/2 only) | N/A | From `Configs` | `Configs.getHttp2MinConnectionPoolSize()` |
| Max concurrent streams (HTTP/2) | N/A | **30** | `Configs.getHttp2MaxConcurrentStreams()` |
| Max idle time | **60 seconds** | 60 seconds | `Configs.getMaxIdleConnectionTimeout()` |
| Pending acquire timeout | **45 seconds** | 45 seconds | `Configs.getConnectionAcquireTimeout()` |
| Response timeout | **60 seconds** | 60 seconds | `Configs.getResponseTimeout()` |
| Max lifetime | Unlimited | Unlimited | **NOT configured** |
| Connection metrics | Disabled | Disabled | **NOT enabled** |

#### Two Modes

Controlled by `CosmosClientBuilder.connectionSharingAcrossClientsEnabled(boolean)`:

| Mode | Behavior | File |
|---|---|---|
| **Default (disabled)** | Each client gets its own `ConnectionProvider` with 1000 max connections | `HttpClient.createFixed()` |
| **Enabled** | All clients share a **singleton** `SharedGatewayHttpClient`, reference-counted. First client's config wins. | `SharedGatewayHttpClient.java` |

**SharedGatewayHttpClient reference counting**:
- `counter.incrementAndGet()` in `getOrCreateInstance()`
- `counter.decrementAndGet()` in `close()` → pool only disposed when counter reaches 0

**⚠️ Potential issue**: `RxDocumentClientImpl.close()` calls `LifeCycleUtils.closeQuietly(this.reactorHttpClient)` which calls `close()` on the `SharedGatewayHttpClient`. If the close order is wrong or a client is closed prematurely, the shared pool could be disposed while other clients are still using it.

#### What's NOT Custom

- **EventLoopGroup**: Reactor Netty's **global default** event loop (`LoopResources.DEFAULT`, ~CPU-core threads, daemon). Shared across all clients.
- **ByteBuf allocator**: Netty's default `PooledByteBufAllocator` — shared globally.

#### Multi-Tenancy Impact

| Resource | Per Client | 100 Clients (default) | 100 Clients (shared) |
|---|---|---|---|
| `ConnectionProvider` | 1 | **100 pools** | **1 pool** |
| Max TCP connections | 1000 | **100,000** | **1,000** |
| File descriptors | ≤1000 | Risk of **ulimit exhaustion** | Bounded |
| Memory per idle connection | ~4–8 KB | Up to **400–800 MB** theoretical max | ~4–8 MB |
| Memory per active connection (with SSL) | ~32–64 KB | Varies with concurrency | Varies |

---

### 3.2 Caches — Byte-Level Memory Analysis

> Byte sizes assume 64-bit JVM with compressed oops (<32 GB heaps). Object header = 12 bytes.

---

#### 3.2.1 Query Plan Cache

**Source**: `RxDocumentClientImpl.java` — field `queryPlanCache`

```java
private ConcurrentMap<String, PartitionedQueryExecutionInfo> queryPlanCache;
// initialized as:
this.queryPlanCache = new ConcurrentHashMap<>();
```

| Property | Value |
|---|---|
| Type | `ConcurrentHashMap<String, PartitionedQueryExecutionInfo>` |
| Max size | **5,000 entries** — full-clear eviction when limit reached (not LRU). No TTL. |
| Per-client? | ✅ Yes |
| Eviction | Clear-all when size >= 5000 (Constants.QUERYPLAN_CACHE_SIZE). Only simple queries cached (excludes aggregates, distinct, group by, limit, top, offset, dcount, order by). |

**Per-entry** (only simple queries are cached): Key (SQL query text ~100 chars) ≈ 256 B + Value (`PartitionedQueryExecutionInfo` with Jackson `ObjectNode`) ≈ 200–2000 B + Node overhead ≈ 32 B → **~500 B – 2.5 KB per entry**

| Scenario | Entries | Memory |
|---|---|---|
| Typical (100 queries) | 100 | **~100 KB** |
| Heavy (500 queries) | 500 | **~1 MB** |
| Max (5K entries, cache cap) | 5,000 | **~2.5–12.5 MB** (full-clear at cap) |

---

#### 3.2.2 Collection Metadata Cache

**Source**: `RxCollectionCache.java`

```java
private final AsyncCache<String, DocumentCollection> collectionInfoByNameCache;
private final AsyncCache<String, DocumentCollection> collectionInfoByIdCache;
```

`AsyncCache` internals (`AsyncCache.java`):
```java
private final ConcurrentHashMap<TKey, AsyncLazy<TValue>> values;  // unbounded
```

| Property | Value |
|---|---|
| Type | 2× `AsyncCache` → 2× unbounded `ConcurrentHashMap` |
| Per-entry | ~2–8 KB (DocumentCollection with indexing policy, partition key def) |
| 5 containers | **~60 KB** |
| 50 containers | **~400 KB – 1.6 MB** |

---

#### 3.2.3 Partition Key Range Cache

**Source**: `RxPartitionKeyRangeCache.java`

```java
private final AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapCache;
```

`AsyncCacheNonBlocking` internals:
```java
private final ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values;  // unbounded
```

Each `CollectionRoutingMap` (`InMemoryCollectionRoutingMap`) holds:
```java
private final Map<String, ImmutablePair<PartitionKeyRange, IServerIdentity>> rangeById;
private final List<PartitionKeyRange> orderedPartitionKeyRanges;
private final List<Range<String>> orderedRanges;
private final Set<String> goneRanges;
```

**Per-PKRange**: `PartitionKeyRange` (~500–800 B) + `Range<String>` (~120 B) + map overhead (~56 B) ≈ **~900 B/range**

| Scenario | Collections × Partitions | Memory |
|---|---|---|
| Typical (5 × 50) | 250 ranges | **~225 KB** |
| Heavy (10 × 500) | 5000 ranges | **~4.5 MB** |
| Extreme (50 × 1000) | 50,000 ranges | **~45 MB** ⚠️ |

---

#### 3.2.4 Session Token Container

**Source**: `SessionContainer.java`

```java
private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>>
    collectionResourceIdToSessionTokens = new ConcurrentHashMap<>();
private final ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = new ConcurrentHashMap<>();
private final ConcurrentHashMap<Long, String> collectionResourceIdToCollectionName = new ConcurrentHashMap<>();
```

Inner map creation (per-collection):
```java
ConcurrentHashMap<String, ISessionToken> tokens =
    new ConcurrentHashMap<String, ISessionToken>(200, 0.75f, 2000);
```
- `concurrencyLevel=2000` — on Java 8+ only affects initial capacity hint, not lock segments

**Per-token**: `VectorSessionToken` (version 8B + globalLsn 8B + `UnmodifiableMap<Integer,Long>` ~80B + String ~100B) + Node overhead ≈ **~340 B/token**

Tokens accumulate **without bound**. Old partition key range IDs from splits are **never cleaned up**.

| Scenario | Collections × PKRanges | Memory |
|---|---|---|
| Typical (5 × 50) | 250 tokens | **~95 KB** |
| Heavy (10 × 200) | 2000 tokens | **~700 KB** |

---

#### 3.2.5 Location/Endpoint Cache

**Source**: `LocationCache.java`

`DatabaseAccountLocationsInfo` holds 13 fields:
```java
private UnmodifiableList<RegionalRoutingContext> writeRegionalRoutingContexts;
private UnmodifiableList<RegionalRoutingContext> readRegionalRoutingContexts;
private UnmodifiableList<String> preferredLocations;
// ... plus 10 more (bi-directional lookup maps for read/write endpoints)
```

| Size | Memory |
|---|---|
| 3 regions | **~2 KB** |
| 10 regions | **~5–8 KB** |

Bounded by the number of Azure regions (~60 max). Contains TTL-based cleanup for unavailable locations.

---

#### 3.2.6 ClientTelemetry Data Structures

> ⚠️ **Updated Feb 2026**: The telemetry subsystem has been significantly refactored since the initial analysis. The claims below reflect the **current** codebase.

**Source**: ClientTelemetry.java, ClientTelemetryInfo.java

The original analysis described three unbounded maps (systemInfoMap, cacheRefreshInfoMap, operationInfoMap) with ConcurrentDoubleHistogram values (~256 KB each). **These no longer exist.** The telemetry architecture has been refactored:

| Property | Previous (at time of initial analysis) | Current |
|---|---|---|
| Per-client histogram maps | 3× unbounded ConcurrentHashMap<ReportPayload, ConcurrentDoubleHistogram> | **Removed** — no per-client histogram maps |
| Histogram implementation | ConcurrentDoubleHistogram (precision=4, ~256 KB each) | **Micrometer-based** — uses CosmosMicrometerMetricsOptions with shared MeterRegistry |
| IMDS HTTP client pool | Persistent 5-connection ConnectionProvider per client (field: metadataHttpClient) | **Ephemeral** — HTTP client created and disposed inline during init(). Metadata cached in a static AtomicReference<AzureVMMetadata> (one fetch per JVM). |
| ClientTelemetryInfo fields | Maps + histograms + metadata | Simple String/Boolean/List fields only (machineId, clientId, processId, userAgent, connectionMode, etc.) |
| Per-client memory | ~50 KB idle, ~1–3 MB active | **~negligible** (<10 KB) |

**Multi-Tenancy Impact**: ClientTelemetry is no longer a significant per-client resource concern. The IMDS metadata fetch is a one-time JVM-level operation, and telemetry metrics flow through a shared Micrometer registry.

---

### 3.3 Background Tasks & Threads

#### 3.3.1 GlobalEndpointManager — Location Refresh

**Source**: `GlobalEndpointManager.java`

```java
private final Scheduler scheduler = Schedulers.newSingle(theadFactory);
// "cosmos-global-endpoint-mgr" daemon thread
```

| Property | Value |
|---|---|
| Thread | **1 dedicated Reactor `Scheduler`** per client (daemon) |
| Interval | `backgroundRefreshLocationTimeIntervalInMS` (from `unavailableLocationsExpirationTimeInSeconds` × 1000) |
| Cleanup | ✅ Properly disposed: `scheduler.dispose()` in `close()` |

**Multi-tenancy**: 100 clients = **100 daemon threads** + **100 HTTP calls per refresh interval**

#### 3.3.2 ClientTelemetry

> **Updated Feb 2026**: Bug #1 has been **fixed**. See [Bug #1 status](#bug-1-clienttelemetryclose-was-a-no-op--fixed) below.

**Source**: ClientTelemetry.java

| Property | Value |
|---|---|
| IMDS HTTP client | **Ephemeral**  created and disposed inline during init(). No persistent pool. |
| IMDS metadata | Cached in a **static** AtomicReference<AzureVMMetadata>  one fetch per JVM, not per client |
| close() | No-op **by design**  no per-instance resources to clean up |
| Called from RxDocumentClientImpl.close()? | Yes  now properly called |

---

### 3.4 HTTP Protocol Version Analysis: HTTP/1.1 vs HTTP/2

#### Current State: HTTP/2 is Supported but Disabled by Default

On this branch, the Cosmos SDK **fully supports HTTP/2** via the `Http2ConnectionConfig` API and `ThinClientStoreModel`. However, it is **disabled by default**.

**HTTP/2 enablement check** (in `RxDocumentClientImpl`):
```java
this.useThinClient = Configs.isThinClientEnabled()
    && this.connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY
    && this.connectionPolicy.getHttp2ConnectionConfig() != null
    && ImplementationBridgeHelpers
        .Http2ConnectionConfigHelper
        .getHttp2ConnectionConfigAccessor()
        .isEffectivelyEnabled(this.connectionPolicy.getHttp2ConnectionConfig());
```

**HTTP/2 defaults** (from `Configs.java`):

| Config | Default | System Property | Env Variable |
|---|---|---|---|
| HTTP/2 enabled | **`false`** | `COSMOS.HTTP2_ENABLED` | `COSMOS_HTTP2_ENABLED` |
| Max connection pool size | **1000** | `COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE` | `COSMOS_HTTP2_MAX_CONNECTION_POOL_SIZE` |
| Min connection pool size | From `Configs` | `COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE` | `COSMOS_HTTP2_MIN_CONNECTION_POOL_SIZE` |
| Max concurrent streams | **30** | `COSMOS.HTTP2_MAX_CONCURRENT_STREAMS` | `COSMOS_HTTP2_MAX_CONCURRENT_STREAMS` |

#### SSL/TLS & ALPN Configuration

**Source**: `Configs.java` — `sslContextInit(boolean serverCertVerificationDisabled, boolean http2Enabled)`

```java
if (http2Enabled) {
    sslContextBuilder
        .applicationProtocolConfig(
            new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2,
                ApplicationProtocolNames.HTTP_1_1
            )
        );
}
```

When HTTP/2 is enabled, ALPN advertises **H2 first, HTTP/1.1 as fallback**.

When HTTP/2 is **disabled**, `sslContextInit` is called with `http2Enabled=false` — **no ALPN**, resulting in pure HTTP/1.1.

#### Reactor Netty Protocol Configuration

**Source**: `ReactorNettyClient.configureChannelPipelineHandlers()`

```java
if (http2CfgAccessor.isEffectivelyEnabled(http2Cfg)) {
    this.httpClient = this.httpClient
        .secure(sslContextSpec -> /* ALPN-enabled SSL context */)
        .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
        .http2Settings(spec -> spec
            .initialWindowSize(1_048_576)      // 1 MB window
            .maxFrameSize(65_536)               // 64 KB frames
            .maxConcurrentStreams(effectiveMaxConcurrentStreams))
        .doOnChannelInit((observer, channel, addr) -> {
            // Custom StripWhitespaceHandler for gateway compatibility
        });
}
```

HTTP/2 settings:
- **Window size**: 1 MB (larger than default 64 KB — better throughput)
- **Max frame size**: 64 KB
- **Max concurrent streams**: From config (default 30)
- **Custom handler**: Strips prohibited whitespace from response headers (gateway-specific workaround)

#### Connection Pool: HTTP/1.1 vs HTTP/2

| Aspect | HTTP/1.1 (Default) | HTTP/2 (Enabled) |
|---|---|---|
| **Request-to-connection** | 1 request = 1 connection (head-of-line blocking) | N requests multiplexed on 1 connection via streams |
| **Connections for N concurrent requests** | N connections needed | ~⌈N/30⌉ connections (30 streams default) |
| **`maxConnections=1000` meaning** | Max 1000 concurrent in-flight requests | Max 1000 × 30 = **30,000 concurrent requests** |
| **Connection allocation** | Standard fixed pool | `Http2AllocationStrategy` with min/max connections + max concurrent streams |
| **Connection acquisition under load** | Requests queue (45s timeout) | New streams open instantly on existing connections |
| **TLS handshakes** | Per connection (up to 1000) | Per connection (but far fewer needed: ~34 for 1000 concurrent) |
| **Header compression** | None — full headers on every request | **HPACK** — headers delta-encoded after first request |
| **Multi-tenant (100 clients, default pool)** | 100 × 1000 = **100,000 TCP connections** | 100 × ~34 = **~3,400 TCP connections** |
| **File descriptor risk** | **Severe** | **Low** |

---

### 3.5 ThinClient & HTTP/2 Architecture

#### What is ThinClientStoreModel?

**Source**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/implementation/ThinClientStoreModel.java`

`ThinClientStoreModel` extends `RxGatewayStoreModel` and implements a **hybrid transport**:
- HTTP/2 is used as the **transport framing** mechanism
- Actual operation payloads are encoded in **RNTBD format** (Reactor Netty Binary Data), not standard HTTP headers
- The ThinClient proxy on the server side parses RNTBD headers for routing

Key architecture:
```
Client App → ThinClientStoreModel
                ├── Encode request as RNTBD RxDocumentServiceRequest → ByteBuf body
                ├── HTTP POST to ThinClient proxy endpoint
                │    (only User-Agent + Activity-ID as HTTP headers;
                │     everything else in RNTBD payload)
                ├── HTTP/2 multiplexing over shared TCP connections
                └── Decode response: check HTTP status → decode RNTBD → extract payload
```

**Routing**: `ThinClientStoreModel` uses `getThinClientEndpoint()` instead of the normal gateway endpoint.

**Request routing in `RxDocumentClientImpl`**: ThinClient is used for point operations, queries, batch operations, and (non-all-versions) change feed — **only** when `useThinClient` is true AND `isThinClientEndpointReady()` returns true. Other operations (database/collection management, etc.) still go through the regular `RxGatewayStoreModel`.

#### Http2ConnectionConfig

**Source**: `sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/Http2ConnectionConfig.java`

This is the **public API** for configuring HTTP/2. It is a field on `GatewayConnectionConfig` and propagated to `HttpClientConfig`.

| Field | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `null` → `false` | Whether HTTP/2 is enabled |
| `maxConnectionPoolSize` | `Integer` | `null` → `1000` | Max TCP connections in the pool |
| `minConnectionPoolSize` | `Integer` | `null` → from Configs | Min TCP connections (pre-warmed) |
| `maxConcurrentStreams` | `Integer` | `null` → `30` | Max HTTP/2 streams per connection |

All fields are **nullable** — when `null`, falls through to system property → env variable → hardcoded default in `Configs`.

The class exposes `getEffective*()` package-private methods via an `ImplementationBridgeHelpers` accessor.

#### Multi-Tenancy Impact: HTTP/2 with ThinClient

| Metric | HTTP/1.1 (100 clients) | HTTP/2 (100 clients, 30 streams) |
|---|---|---|
| Max TCP connections | **100,000** | **~3,400** (100 × ⌈1000/30⌉) |
| Effective concurrent requests | 100,000 (1 per conn) | **100 × 30,000 = 3M** (theoretical) |
| File descriptors | Extreme risk | Manageable |
| TLS handshakes at warmup | Up to 100,000 | Up to ~3,400 |
| Idle connection memory | 400–800 MB (theoretical) | ~14–27 MB |
| Per-request header overhead | ~1–2 KB (full HTTP headers) | Minimal (RNTBD binary + HPACK) |

**Recommendation**: For multi-tenant deployments, **HTTP/2 with ThinClient should be strongly considered**. The default `maxConcurrentStreams=30` is conservative; increasing it to 100–256 (typical server limits) would further reduce connection counts.

---

### 3.6 Per-Client Memory Breakdown

**Assumptions for "Typical"**: 5 containers, 50 PKRanges/container, 100 distinct queries, Session consistency, 3 regions, ~10 active connections.

| Component | Typical | Heavy (10–15 MB range) | Source |
|---|---|---|---|
| **Query plan cache** | ~100 KB | **~1 MB** (500 entries) | `RxDocumentClientImpl` — unbounded `ConcurrentHashMap` |
| **PKRange cache** | ~225 KB | **~1 MB** (1000 ranges) | `RxPartitionKeyRangeCache` — `AsyncCacheNonBlocking` |
| **Collection cache** (×2 maps) | ~60 KB | **~100 KB** | `RxCollectionCache` — 2× `AsyncCache` |
| **Session container** | ~95 KB | **~200 KB** | `SessionContainer` — nested `ConcurrentHashMap` |
| **Telemetry** | ~negligible | ~negligible | Micrometer-based; no per-client histograms (refactored) |
| **Connection pool + active conns** | ~320 KB | **~2–4 MB** (50 active × 32–64 KB) | Reactor Netty channel + SSL |
| **IMDS HTTP client** | ~0 | ~0 | Ephemeral (created/disposed in init()); metadata in static AtomicReference |
| **Thread stacks** (native) | ~512 KB–1 MB | ~512 KB–1 MB | `GlobalEndpointManager.scheduler` (1 thread) |
| **Location cache** | ~2 KB | ~5 KB | `LocationCache.DatabaseAccountLocationsInfo` |
| **Misc (fields, auth, SSL ctx)** | ~50 KB | ~100 KB | `RxDocumentClientImpl` — 50+ fields |
| | | | |
| **Total Heap** | **~600 KB – 1 MB** | **~5–10 MB** | |
| **Total (+ native/stacks)** | **~1.5–2 MB** | **~7–12 MB** | |
| **Total (+ active conn buffers peak)** | **~2–3 MB** | **~10–15 MB** ✅ | |

**10–15 MB** corresponds to a moderately heavy workload: 10–20 containers, 500+ queries, 50+ active connections, telemetry between flush cycles. For idle/minimal clients: **~1.5–2 MB**. For extreme (10K queries, 10K PKRanges): **~30+ MB** driven by unbounded caches.

---

## 4. Shared (Singleton) Resources

These are allocated once per JVM, NOT per client:

| Resource | Type | Threads | File |
|---|---|---|---|
| `CosmosSchedulers.COSMOS_PARALLEL` | Static `Scheduler` | CPU-core count threads (daemon) | `CosmosSchedulers.java` |
| `CpuMemoryMonitor` | Static executor | 1 daemon thread | `CpuMemoryMonitor.java` |
| Reactor Netty event loop | Global `LoopResources` | ~CPU-core threads (daemon) | Reactor Netty internal |
| `SharedGatewayHttpClient` (opt-in) | Static ref-counted singleton | None (uses event loop) | `SharedGatewayHttpClient.java` |
| Netty `PooledByteBufAllocator` | Global singleton | None | Netty internal |
| `AzureVMMetadata` singleton | `AtomicReference` | None | `ClientTelemetry.java` |

---

## 5. Resource Impact at Scale

### Thread Count for N Clients

| Thread Source | Per Client | 100 Clients | 500 Clients |
|---|---|---|---|
| GlobalEndpointManager scheduler | 1 | 100 | 500 |
| **Subtotal (dedicated)** | **1** | **100** | **500** |
| CosmosSchedulers.COSMOS_PARALLEL | — | CPU cores | CPU cores |
| CpuMemoryMonitor | — | 1 | 1 |
| Reactor Netty event loop | — | ~CPU cores | ~CPU cores |
| **Total** | — | **~100 + 2×CPU** | **~500 + 2×CPU** |

### Memory for N Clients

| Scenario | Per Client | 100 Clients | 500 Clients |
|---|---|---|---|
| Minimal (idle) | ~1.5 MB | ~150 MB | ~750 MB |
| Typical | ~3 MB | ~300 MB | ~1.5 GB |
| Heavy | ~10–15 MB | ~1–1.5 GB | ~5–7.5 GB |
| Extreme (unbounded caches) | ~30+ MB | ~3+ GB | ~15+ GB ⚠️ |

### Network Overhead for N Clients

| Background Call | Interval | 100 Clients | 500 Clients |
|---|---|---|---|
| DatabaseAccount refresh | ~5 min | **~20 req/min** | **~100 req/min** |
| IMDS metadata (telemetry) | Per telemetry cycle | Varies | Varies |

---

## 6. Resource Leak Bugs

### Bug #1: ClientTelemetry.close() Was a No-Op  FIXED

**Severity**: ~~RED HIGH~~  **FIXED** 

> **Updated Feb 2026**: This bug has been **resolved** in the current codebase.

**Previous state** (at time of initial analysis):
- close() only logged a misleading message ("GlobalEndpointManager closed.") and did NOT close resources
- RxDocumentClientImpl.close() never called 	his.clientTelemetry.close()
- Leaked IMDS ConnectionProvider (5-connection pool) and ClientTelemetryInfo histograms per client

**Current state**:
- close() is intentionally a no-op because there are **no per-instance resources** to clean up. The code comment explicitly states: *"Nothing to clean up  no per-instance resources. The IMDS HTTP client is created and disposed during init(), not held."*
- RxDocumentClientImpl.close() **does** call 	his.clientTelemetry.close() now
- The IMDS HTTP client is **ephemeral**  created and disposed inline during metadata fetch. Metadata is cached in a static AtomicReference<AzureVMMetadata> (one fetch per JVM)
- The ClientTelemetryInfo histogram maps (systemInfoMap, operationInfoMap, cacheRefreshInfoMap) no longer exist  telemetry uses Micrometer instead

**Impact**: No per-client resource leak from ClientTelemetry in the current codebase.

---

### Bug #2: ThinClientStoreModel Never Closed

**Severity**: 🟡 MEDIUM (only when ThinClient is enabled)

**Source**: `RxDocumentClientImpl.close()`

The `thinClientStoreModel` field is created in `initThinClient()` but **never closed** in `close()`. The `close()` method closes `globalEndpointManager`, `storeClientFactory`, `reactorHttpClient`, but not `thinClientStoreModel`.

---

### Bug #3: GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover Never Closed

**Severity**: LOW

> **Updated Feb 2026**: The original description was inverted. Corrected below.

**Source**: RxDocumentClientImpl.close()

The close() method closes globalPartitionEndpointManagerForCircuitBreaker (with a null check), but globalPartitionEndpointManagerForPerPartitionAutomaticFailover is **never closed at all**  it is simply missing from the close() method.

Two distinct fields exist:
- globalPartitionEndpointManagerForCircuitBreaker  closed with null check 
- globalPartitionEndpointManagerForPerPartitionAutomaticFailover  **never closed** 

---


## 7. Recommendations & Action Items

### 7.1 Immediate Bug Fixes

| # | Action | Priority | File |
|---|---|---|---|
| **A1** | ~~Fix ClientTelemetry.close()~~ | ~~P0~~ ✅ **FIXED** | ClientTelemetry.java — IMDS client is now ephemeral; close() is intentionally a no-op |
| **A2** | ~~Call 	his.clientTelemetry.close() from RxDocumentClientImpl.close()~~ | ~~P0~~ ✅ **FIXED** | RxDocumentClientImpl.java — close() now calls clientTelemetry.close() |
| **A3** | Close `thinClientStoreModel` in `RxDocumentClientImpl.close()` | 🟡 P1 | `RxDocumentClientImpl.java` |

### 7.2 Resource Sharing Improvements

| # | Action | Benefit | Complexity |
|---|---|---|---|
| **A4** | Make `connectionSharingAcrossClientsEnabled` the default (or auto-enable when >N clients) | Reduces N connection pools to 1 | Low |
| **A5** | Share `GlobalEndpointManager` background scheduler across clients (shared `Scheduler` with configurable parallelism) | Reduces N threads to a small pool | Medium |
| **A6** | Pool or share `QueryPlanCache` across clients targeting the same account | Reduces memory duplication | Medium |

### 7.3 Cache Optimization

| # | Action | Benefit |
|---|---|---|
| **A7** | Add LRU eviction to queryPlanCache (Caffeine or bounded LinkedHashMap) | Cache already capped at 5,000 entries with full-clear eviction. LRU would further improve by avoiding clearing all entries at once. |
| **A8** | Add max-size bounds to `AsyncCache` and `AsyncCacheNonBlocking` | Prevents OOM in extreme scenarios |
| **A9** | Clean up stale partition key range IDs in `SessionContainer` after splits | Prevents unbounded token accumulation |
| **A10** | Reduce `SessionContainer` inner map `concurrencyLevel` from 2000 to 16–64 | Marginal memory savings |

### 7.4 Connection Pool Tuning for Multi-Tenancy

| # | Action | Rationale |
|---|---|---|
| **A11** | Reduce default `maxConnections` when many clients are detected (e.g., 100 instead of 1000) | 500 × 1000 = 500K potential connections |
| **A12** | Configure `maxLifeTime()` on `ConnectionProvider` | Prevent stale connections |
| **A13** | Enable connection pool metrics (`ConnectionProvider.Builder.metrics()`) | Observability |
| **A14** | Configure explicit `evictInBackground()` interval | Control idle cleanup |

### 7.5 HTTP/2 & ThinClient for Multi-Tenancy

| # | Action | Benefit | Complexity |
|---|---|---|---|
| **A15** | Consider enabling HTTP/2 by default for Gateway mode, or auto-enable for multi-tenant scenarios | **~30× reduction** in TCP connections (100K → ~3.4K for 100 clients with 30 streams) | Medium — already implemented, just needs default change |
| **A16** | Increase default `maxConcurrentStreams` from 30 to 100–256 (typical server limit) | Further reduces connections needed | Low — config change |
| **A17** | Ensure `SharedGatewayHttpClient` properly interacts with HTTP/2 `Http2AllocationStrategy` | Shared HTTP/2 pool across all clients | Medium |
| **A18** | Reduce default HTTP/2 `maxConnectionPoolSize` from 1000 to a lower value (e.g., 50–100) since multiplexing makes large pools unnecessary | Memory + FD savings | Low — config change |

### 7.6 Documentation & API Surface

| # | Action |
|---|---|
| **A19** | Document multi-tenancy best practices: recommend `connectionSharingAcrossClientsEnabled(true)`, HTTP/2 enablement, pool sizing, expected resource consumption |
| **A20** | Consider a `CosmosClientPool` or `CosmosClientManager` API for multi-tenant scenarios |
| **A21** | Add builder-level `maxConnectionPoolSize()` (currently buried in `ConnectionPolicy` / `GatewayConnectionConfig`) |

### 7.7 HTTP Client Implementation Investigation

| # | Action | Benefit | Complexity |
|---|---|---|---|
| **A22** | Prototype an OkHttp adapter behind the existing `HttpClient` interface (`com.azure.cosmos.implementation.http.HttpClient`) and benchmark against Reactor Netty at 100 clients | Data-driven answer on whether the HTTP client is the bottleneck for multi-tenancy (threads, direct memory, connection pooling). OkHttp eliminates direct memory (heap-only), has trivially shareable `ConnectionPool`, and native HTTP/2 coalescing. | Medium — adapter code is straightforward; the question is compatibility with ThinClient channel handlers. |
