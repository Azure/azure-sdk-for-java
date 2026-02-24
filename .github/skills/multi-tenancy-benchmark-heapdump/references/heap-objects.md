# Heap Object to Root Cause Mapping

When comparing PRE_CLOSE and POST_CLOSE heap dumps, these retained object patterns indicate specific bugs:

## ConcurrentDoubleHistogram (org.HdrHistogram)

**Root cause**: A1 -- `ClientTelemetryInfo` histogram maps (`systemInfoMap`, `operationInfoMap`, `cacheRefreshInfoMap`) not cleared on close.

**Expected**: ~256 KB per histogram. With N clients x 3 maps = 768 KB * N retained.

**Fix**: `ClientTelemetry.close()` must null out the histogram maps.

## ConnectionProvider / ConnectionPool (reactor.netty)

**Root cause**: A1 -- IMDS `metadataHttpClient` not shut down. Each client creates a `ConnectionProvider` with 5-connection pool.

**Expected**: ~5 connections * N clients retained.

**Fix**: `ClientTelemetry.close()` must call `metadataHttpClient.shutdown()`.

## ScheduledThreadPoolExecutor / Thread

**Root cause**: A1+A2 -- `GlobalEndpointManager` scheduler not disposed, or `ClientTelemetry.close()` never called.

**Expected**: 1 scheduler thread per client lifecycle leaked.

**Fix**: Ensure `RxDocumentClientImpl.close()` calls `clientTelemetry.close()`.

## SessionContainer / SessionTokenEntry

**Root cause**: Not a leak -- session tokens are per-partition and expected to be retained while client is alive. Should be released after close.

**If retained after close**: The `CosmosAsyncClient` reference is still reachable (GC root). Check for unclosed clients.

## ConcurrentHashMap entries (queryPlanCache)

**Root cause**: A7 -- `queryPlanCache` is unbounded. Grows with distinct queries.

**Expected**: Entries should not grow beyond configured max (default 1000 with LRU fix).

**Fix**: Add LRU eviction to queryPlanCache.

## ByteBuf / PooledByteBuf (io.netty.buffer)

**Not a leak** -- Netty uses pooled direct buffers. These are recycled, not leaked. High counts are normal during active I/O and should drop after connections close.

## Cosmos SDK-specific objects to watch

| Class pattern | Normal? | If retained after close |
|---|---|---|
| `RxDocumentClientImpl` | No | Client not closed |
| `GlobalEndpointManager` | No | Should be closed |
| `ClientTelemetry` | No | Should be closed (A1) |
| `ClientTelemetryInfo` | No | Should be nulled (A1) |
| `StoreClient` | No | Should be closed |
| `CosmosAsyncClient` | No | Application leak |
| `ReactorNettyClient` | No | Should be shutdown |
| `CpuMemoryMonitor` | Singleton | OK if 1 instance |
