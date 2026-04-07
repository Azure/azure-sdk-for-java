# Java SDK — Region Routing and Cross-Region Retry Design

## Overview

This document describes how the Azure Cosmos DB Java SDK routes requests to specific regions, how `preferredRegions` and `excludedRegions` affect routing, and under what conditions the SDK retries requests cross-region — potentially bypassing the configured region preferences.

## Component Graph

When an operation is executed through the Java SDK, the request flows through the following layers:

```
Public API (CosmosAsyncClient / CosmosClient)
  → RxDocumentClientImpl (creates RxDocumentServiceRequest)
    → ClientRetryPolicy (cross-region retry decisions)
      → GlobalEndpointManager.resolveServiceEndpoint()
        → LocationCache.resolveServiceEndpoint() (region resolution)
          → Transport Layer (Gateway or Direct)
            → Cosmos DB Service
```

Region routing is determined by `LocationCache.resolveServiceEndpoint()`, which uses the `preferredRegions`, `excludedRegions`, and account topology to select the target regional endpoint. The `ClientRetryPolicy` can override this by setting `usePreferredLocations = false` on retry, causing the request to bypass region preferences.

## LocationCache — Region Resolution

[`LocationCache.java`](../src/main/java/com/azure/cosmos/implementation/routing/LocationCache.java) is the core component that determines which regional endpoint a request is sent to.

### `resolveServiceEndpoint()` — The Entry Point

```java
// LocationCache.java:175-203
public RegionalRoutingContext resolveServiceEndpoint(RxDocumentServiceRequest request) {
    // 1. If a specific endpoint was already assigned (e.g., by retry policy), use it
    if (request.requestContext.regionalRoutingContextToRoute != null) {
        return request.requestContext.regionalRoutingContextToRoute;
    }

    int locationIndex = request.requestContext.locationIndexToRoute (default 0);
    boolean usePreferredLocations = request.requestContext.usePreferredLocations (default true);

    // 2. WRITE path (single-write) or usePreferredLocations=false:
    //    Route directly via availableWriteLocations — BYPASSES preferred/excluded
    if (!usePreferredLocations
        || (request.isWriteOperation() && !this.canUseMultipleWriteLocations(request))) {
        // flip-flop between 1st and 2nd write region (for manual failover)
        return availableWriteRegionalRoutingContexts[locationIndex % 2];
    }

    // 3. READ path (or WRITE on multi-write):
    //    Route through getApplicableRead/WriteRegionRoutingContexts()
    //    which HONORS preferred and excluded regions
    endpoints = isWrite
        ? getApplicableWriteRegionRoutingContexts(request)
        : getApplicableReadRegionRoutingContexts(request);
    return endpoints.get(locationIndex % endpoints.size());
}
```

**Key insight**: The `usePreferredLocations` flag is the gate. When `false`, the request bypasses ALL region filtering (preferred + excluded) and goes directly to the write region via `availableWriteLocations`.

### `getPreferredAvailableRoutingContexts()` — Endpoint List Construction

This method (line 830) builds the ordered list of regional endpoints used by `resolveServiceEndpoint()`. It is called twice during account topology refresh:

```java
// LocationCache.java:809-810
// Step 1: Build write endpoints — fallback = defaultRoutingContext (the CosmosClientBuilder endpoint)
writeRegionalRoutingContexts = getPreferredAvailableRoutingContexts(
    ..., OperationType.Write, this.defaultRoutingContext);

// Step 2: Build read endpoints — fallback = writeRegionalRoutingContexts[0]
readRegionalRoutingContexts = getPreferredAvailableRoutingContexts(
    ..., OperationType.Read, writeRegionalRoutingContexts.get(0));
```

Inside `getPreferredAvailableRoutingContexts()`, there is a critical gate at line 838:

```java
if (this.canUseMultipleWriteLocations() || expectedAvailableOperation.supports(OperationType.Read)) {
    // Lines 839-882: preferred/regional endpoint detection path
    if (preferredLocations != null && !preferredLocations.isEmpty()) {
        // Iterate preferred locations → build endpoint list in that order
    } else {
        // No preferred locations: iterate orderedLocations
        for (location : orderedLocations) {
            if (defaultRoutingContext matches this regional endpoint) {
                endpoints.clear();  // CLEAR the list
                break;              // BREAK — fall through to fallback
            }
            endpoints.add(endpoint);
        }
    }
    if (endpoints.isEmpty()) {
        endpoints.add(fallbackRoutingContext);  // Use the fallback
    }
} else {
    // Lines 883-891: simple path — just adds all orderedLocations, NO regional detection
    for (location : orderedLocations) {
        endpoints.add(endpoint);
    }
}
```

### Regional Endpoint Behavior (No `preferredRegions` Set)

When using a regional endpoint (e.g., `https://account-westus.documents.azure.com`) **without** `preferredRegions`, the behavior differs between single-write and multi-write accounts:

#### Single-write account

1. **Write endpoints**: `canUseMultipleWriteLocations()=false`, `Write.supports(Read)=false` → falls to line 883 (simple path). Just adds write locations: `[East US]`. **No regional endpoint detection.**
2. **Read endpoints**: fallback = `writeRegionalRoutingContexts[0]` = `East US`. Regional endpoint detection clears the list → fallback = `East US`.
3. **Result**: All reads go to East US (write region). ❌ Traffic not pinned to regional endpoint.

#### Multi-write account

1. **Write endpoints**: `canUseMultipleWriteLocations()=true` → enters line 838 (detection path). Regional endpoint detected → clears list → fallback = `defaultRoutingContext` = West US (the regional endpoint). `writeRegionalRoutingContexts = [West US]`.
2. **Read endpoints**: fallback = `writeRegionalRoutingContexts[0]` = `West US`. Same detection → fallback = `West US`.
3. **Result**: All traffic goes to West US. ✅ Accidentally pinned via fallback chain.

**This multi-write "fix" is accidental** — it works because the write endpoint computation produces the regional endpoint as the fallback, which cascades into the read fallback. This is not intentional SDK design.

### `getApplicableRegionRoutingContexts()` — Excluded Region Filtering

When `excludedRegions` or `excludedRegionsSupplier` is configured, `getApplicableRegionRoutingContexts()` (line 334) filters the endpoint list:

1. Remove user-excluded regions from the endpoint list
2. Remove internally-excluded regions (from per-partition circuit breaker)
3. If the filtered list is empty, add the fallback endpoint
4. For reads: fallback = `writeRegionalRoutingContexts[0]`
5. For writes: fallback = `defaultRoutingContext`

### `DatabaseAccount` Resolution

The initial `DatabaseAccount` read (account topology discovery) goes directly to the URL passed to `CosmosClientBuilder`:
- **Global endpoint**: goes to the hub region (typically the first write region)
- **Regional endpoint**: goes to that region

This call happens before `resolveServiceEndpoint()` is initialized, so it is not affected by preferred/excluded regions.

### Metadata Resource Routing (Gateway Mode)

In Gateway mode, metadata reads (`Collection`, `PartitionKeyRange`) follow the same `resolveServiceEndpoint()` path as data reads:

```
readPartitionKeyRanges() / readCollection()
  → getStoreProxy()                    // routes to gatewayProxy for metadata resources
    → RxGatewayStoreModel.performRequest()
      → globalEndpointManager.resolveServiceEndpoint(request)
        → LocationCache.resolveServiceEndpoint()
```

- `usePreferredLocations` defaults to `true` — never set to `false` for metadata reads
- No special-casing for master resources in the routing layer
- `ChangeFeedProcessor` partition split handling (`PARTITION_KEY_RANGE_IS_GONE`, `PARTITION_KEY_RANGE_IS_SPLITTING`) also uses this path — preferred/excluded regions are honored

**Exception**: `GatewayAddressCache.getMasterAddressesViaGatewayAsync()` (the `/addresses` endpoint for physical address resolution) bypasses `resolveServiceEndpoint()` entirely. This is the address resolution layer, not the data/metadata request layer.

## ClientRetryPolicy — Cross-Region Retries

[`ClientRetryPolicy.java`](../src/main/java/com/azure/cosmos/implementation/ClientRetryPolicy.java) handles cross-region retry decisions. The key mechanism is the `usePreferredLocations` flag in the `RetryContext`:

- **`usePreferredLocations = true`**: retry goes through `getApplicableRead/WriteRegionRoutingContexts()` which filters by preferred + excluded → **honors region config**
- **`usePreferredLocations = false`**: retry goes through `resolveServiceEndpoint()` line 186-198 which routes directly via `availableWriteLocations` → **bypasses all region filters**

### Exhaustive `usePreferredLocations` Trace

Every call site in `ClientRetryPolicy` that sets `usePreferredLocations`:

| Line | Trigger | `usePreferredLocations` | Cross-region retry? | Applies to |
|---|---|---|---|---|
| 120 | **403/3 WRITE_FORBIDDEN** | **`false`** | **Yes** — marks endpoint unavailable, refreshes location, retries to next write region | Both SW + MW |
| 130 | **403/1008 DATABASE_ACCOUNT_NOTFOUND** | **`false`** | **Yes** — marks endpoint unavailable for reads, retries to next endpoint | Both SW + MW (reads only) |
| 138 | **GATEWAY_ENDPOINT_UNAVAILABLE** | `true` | **Yes** — retries to next preferred endpoint | Both SW + MW |
| 140 | **Non-retriable network write** | **`false`** | **No** — `noRetry()` at line 370. Only marks endpoint unavailable and refreshes cache. | Both SW + MW |
| 253 | **404/1002 SESSION_NOT_AVAILABLE (multi-write)** | `true` | **Yes** — iterates through applicable endpoints honoring preferred/excluded | MW only |
| 262 | **404/1002 SESSION_NOT_AVAILABLE (single-write)** | **`false`** | **Yes** — retries up to 2 times, falls back to write region | SW only |
| 345 | **Gateway read timeout** | `true` | **Yes** — only for safe-to-retry reads, retries to next preferred endpoint | Both (reads only) |
| 465 | **503 SERVICE_UNAVAILABLE** | `true` | **Yes** — reads on both SW+MW. Writes: MW retries cross-region; SW does not unless PPAF enabled | Both SW + MW |

*SW = single-write, MW = multi-write*

### Paths That Bypass Preferred/Excluded AND Retry Cross-Region

| Line | Trigger | Account Type | Steady-state? |
|---|---|---|---|
| **120** | **403/3 WRITE_FORBIDDEN** | Both SW + MW | No — failover event |
| **130** | **403/1008 DATABASE_ACCOUNT_NOTFOUND** | Both SW + MW (reads only) | No — region add/online event |
| **262** | **404/1002 SESSION_NOT_AVAILABLE** | **SW only** | **Yes — most impactful** |

### Single-write vs Multi-write Summary

| Scenario | Single-write | Multi-write |
|---|---|---|
| 404/1002 SESSION_NOT_AVAILABLE | → write region (**bypasses** pref/excl) | Iterates applicable regions (**honors**) |
| 403/3 WRITE_FORBIDDEN | → write region (**bypasses**) | → write region (**bypasses**) |
| 403/1008 DB_ACCOUNT_NOTFOUND | → write region (**bypasses**, reads only) | → write region (**bypasses**, reads only) |
| GATEWAY_ENDPOINT_UNAVAILABLE | Reads: next preferred (**honors**). Writes: → write region (**bypasses**) | Next applicable (**honors**) |
| 503 SERVICE_UNAVAILABLE | Reads: next preferred (**honors**). Writes: **no cross-region retry** | Next applicable (**honors**) |
| Gateway read timeout | Next preferred (**honors**, reads only) | Next preferred (**honors**, reads only) |

## ChangeFeedProcessor Region Routing

`ChangeFeedProcessor` does not have its own region routing logic. All CFP requests flow through the standard SDK pipeline:

- **Change feed polling**: `CosmosChangeFeedRequestOptions` → `queryChangeFeed()` → `resolveServiceEndpoint()` → honors `preferredRegions`
- **Metadata reads** (Collection, PartitionKeyRange): same path → honors `preferredRegions`
- **Lease operations** (read/write/replace): same path → writes go to write region on single-write accounts
- **Partition split handling** (410/1002, 410/1007): CFP calls `getOverlappingRanges()` → `readPartitionKeyRanges()` → same Gateway path → honors `preferredRegions`

Region routing for CFP is configured entirely at the `CosmosClientBuilder` level:

```java
CosmosAsyncClient client = new CosmosClientBuilder()
    .endpoint("https://<account>.documents.azure.com:443/")
    .key(KEY)
    .preferredRegions(Arrays.asList("<target-region>"))
    .consistencyLevel(ConsistencyLevel.SESSION)
    .buildAsyncClient();
```

`ChangeFeedProcessorOptions` has no region-related settings.

`CosmosChangeFeedRequestOptions` has a per-request `setExcludedRegions()`, but this is for the manual change feed pull model (`container.queryChangeFeed()`), not for `ChangeFeedProcessor`.

## Recommendation

**Always use `preferredRegions` for predictable region routing.**

| Account Type | Configuration | Behavior |
|---|---|---|
| Single-write | `preferredRegions(["<target>"])` | Reads + metadata pinned to target. Writes go to write region. |
| Multi-write | `preferredRegions(["<target>"])` | ALL traffic (reads + writes + metadata) pinned to target. |
| Either | Regional endpoint only (no `preferredRegions`) | **Unreliable** — single-write falls back to write region; multi-write accidentally pins via fallback chain. |
| Either | `excludedRegionsSupplier(...)` | Works but less explicit than `preferredRegions`. |
| Either | `preferredRegions` + `excludedRegionsSupplier` | Redundant but harmless. Behaves same as `preferredRegions` alone. |

## Cross-SDK Comparison

The Python SDK (`azure-sdk-for-python`) implements the same `LocationCache` fallback logic:
- `effective_preferred_locations = []` for regional endpoints → falls back to write region
- Same single-write vs multi-write divergence applies
- Same `usePreferredLocations` bypass mechanism in retry policies

## Appendix: Sub-status Code Reference

| Sub-status | Constant | Description |
|---|---|---|
| 3 | `FORBIDDEN_WRITEFORBIDDEN` | Write region changed (failover) |
| 1002 | `READ_SESSION_NOT_AVAILABLE` / `PARTITION_KEY_RANGE_GONE` | Session token not available / partition split |
| 1007 | `COMPLETING_SPLIT_OR_MERGE` | Partition is splitting |
| 1008 | `DATABASE_ACCOUNT_NOTFOUND` / `COMPLETING_PARTITION_MIGRATION` | Region not available / partition migration |
| 1000 | `NAME_CACHE_IS_STALE` | Container recreated with same name |
