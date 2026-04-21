# readManyByPartitionKeys — Design & Implementation

## Overview

New `readManyByPartitionKeys` methods on `CosmosAsyncContainer` / `CosmosContainer` that accept a
`List<PartitionKey>` (without item-id). The SDK splits the PK values by physical
partition, generates batched streaming queries per physical partition, and returns results as
`CosmosPagedFlux<T>` / `CosmosPagedIterable<T>`.

An optional `SqlQuerySpec` parameter lets callers supply a custom query for projections
and additional filters. The SDK appends the auto-generated PK WHERE clause to it.

## Decisions

| Topic                   | Decision                                                                                                                                                                   |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| API name                | `readManyByPartitionKeys` — distinct name to avoid ambiguity with existing `readMany(List<CosmosItemIdentity>)`                                                            |
| Return type             | `CosmosPagedFlux<T>` (async) / `CosmosPagedIterable<T>` (sync)                                                                                                             |
| Custom query format     | `SqlQuerySpec` — full query with parameters; SDK ANDs the PK filter                                                                                                        |
| Partial HPK             | Supported from the start; prefix PKs fan out via `getOverlappingRanges`                                                                                                    |
| PK deduplication        | Done at Spark layer only, not in the SDK                                                                                                                                   |
| Spark UDF               | New `GetCosmosPartitionKeyValue` UDF                                                                                                                                       |
| Custom query validation | Gateway query plan via the standard SDK query-plan retrieval path; reject aggregates/ORDER BY/DISTINCT/GROUP BY/DCount/OFFSET/LIMIT/non-streaming ORDER BY/vector/fulltext |
| PK list size            | No hard upper-bound enforced; SDK batches internally per physical partition (default 100 PKs per batch, configurable via `COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE`)          |
| Eager validation        | Null and empty PK list rejected eagerly (not lazily in reactive chain)                                                                                                     |
| Telemetry               | Separate span name `readManyByPartitionKeyItems.<containerId>` (distinct from existing `readManyItems`)                                                                    |
| Query construction      | Table alias auto-detected from FROM clause; string literals and subqueries handled correctly                                                                               |

## Phase 1 — SDK Core (`azure-cosmos`)

### Step 1: New public overloads in CosmosAsyncContainer

```java
<T> CosmosPagedFlux<T> readManyByPartitionKeys(List<PartitionKey> partitionKeys, Class<T> classType)
<T> CosmosPagedFlux<T> readManyByPartitionKeys(List<PartitionKey> partitionKeys,
                                               CosmosReadManyRequestOptions requestOptions,
                                               Class<T> classType)
<T> CosmosPagedFlux<T> readManyByPartitionKeys(List<PartitionKey> partitionKeys,
                                               SqlQuerySpec customQuery,
                                               CosmosReadManyRequestOptions requestOptions,
                                               Class<T> classType)
```

All delegate to a private `readManyByPartitionKeyInternalFunc(...)`.

**Eager validation:** The 4-arg method validates `partitionKeys` is non-null and non-empty before constructing the reactive pipeline, throwing `IllegalArgumentException` synchronously.

### Step 2: Sync wrappers in CosmosContainer

Same signatures returning `CosmosPagedIterable<T>`, delegating to the async container.

### Step 3: Internal orchestration (RxDocumentClientImpl)

1. Resolve collection metadata + PK definition from cache.
2. Fetch routing map from `partitionKeyRangeCache`.
3. For each `PartitionKey`:
   - Compute effective partition key (EPK).
   - Full PK → `getRangeByEffectivePartitionKey()` (single range).
   - Partial HPK → compute EPK prefix range → `getOverlappingRanges()` (multiple ranges).
     **Note:** partial HPK intentionally fans out to multiple physical partitions.
4. Group PK values by `PartitionKeyRange`.
5. Per physical partition → split PKs into batches of `maxPksPerPartitionQuery` (configurable, default 100).
6. Per batch → build `SqlQuerySpec` with PK WHERE clause (Step 5).
7. Interleave batches across physical partitions in round-robin order so that bounded concurrency prefers different physical partitions over sequential batches of the same partition.
8. Execute queries via `queryForReadMany()` with bounded concurrency (`Math.min(batchCount, cpuCount)`).
9. Return results as `CosmosPagedFlux<T>`.

### Step 4: Custom query validation

One-time call per invocation using the same query-plan retrieval path and cacheability rules as regular SDK queries.

- `QueryPlanRetriever.getQueryPlanThroughGatewayAsync()` for the user query.
- Reject (`IllegalArgumentException`) if:
  - `queryInfo.hasGroupBy()` — checked first (takes precedence over aggregates since `hasAggregates()` also returns true for GROUP BY queries)
  - `queryInfo.hasAggregates()`
  - `queryInfo.hasOrderBy()`
  - `queryInfo.hasDistinct()`
  - `queryInfo.hasDCount()`
  - `queryInfo.hasOffset()`
  - `queryInfo.hasLimit()`
  - `queryInfo.hasNonStreamingOrderBy()`
  - `partitionedQueryExecutionInfo.hasHybridSearchQueryInfo()`
  - query plan details are unavailable (`queryInfo == null`)

### Step 5: Query construction

Query construction is implemented in `ReadManyByPartitionKeyQueryHelper`. The helper:

- Extracts the table alias from the FROM clause (handles `FROM c`, `FROM root r`, `FROM x WHERE ...`)
- Handles string literals in queries (parens/keywords inside `'...'` are correctly skipped)
- Recognizes SQL keywords: WHERE, ORDER, GROUP, JOIN, OFFSET, LIMIT, HAVING
- Uses parameterized queries (`@__rmPk_` prefix) to prevent SQL injection

**Single PK (HASH):**

```sql
{baseQuery} WHERE {alias}["{pkPath}"] IN (@__rmPk_0, @__rmPk_1, @__rmPk_2)
```

**Full HPK (MULTI_HASH):**

```sql
{baseQuery} WHERE ({alias}["{path1}"] = @__rmPk_0 AND {alias}["{path2}"] = @__rmPk_1)
              OR  ({alias}["{path1}"] = @__rmPk_2 AND {alias}["{path2}"] = @__rmPk_3)
```

**Partial HPK (prefix-only):**

```sql
{baseQuery} WHERE ({alias}["{path1}"] = @__rmPk_0)
              OR  ({alias}["{path1}"] = @__rmPk_1)
```

If the base query already has a WHERE clause:

```sql
{selectAndFrom} WHERE ({existingWhere}) AND ({pkFilter})
```

### Step 6: Interface wiring

New method `readManyByPartitionKeys` added directly to `AsyncDocumentClient` interface, implemented in `RxDocumentClientImpl`. New `fetchQueryPlanForValidation` static method added to `DocumentQueryExecutionContextFactory` for custom query validation.

### Step 7: Configuration

New configurable batch size via system property `COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE` or environment variable `COSMOS_READ_MANY_BY_PK_MAX_BATCH_SIZE` (default: 100, minimum: 1). Follows existing `Configs` patterns.

## Phase 2 — Spark Connector (`azure-cosmos-spark_3`)

### Step 8: New UDF — `GetCosmosPartitionKeyValue`

- Input: partition key value (single value or Seq for hierarchical PKs).
- Output: serialized PK string in format `pk([...json...])`.
- **Null handling:** Null input is serialized as a JSON-null partition key component. If callers need `PartitionKey.NONE` semantics they must use the schema-matched path with `spark.cosmos.read.readManyByPk.nullHandling=None`, which is only supported for single-path partition keys.

### Step 9: PK-only serialization helper

`CosmosPartitionKeyHelper`:

- `getCosmosPartitionKeyValueString(pkValues: List[Object]): String` — serialize to `pk([...])` format.
- `tryParsePartitionKey(serialized: String): Option[PartitionKey]` — deserialize; returns `None` for malformed input including invalid JSON (wrapped in `scala.util.Try`).
- When `spark.cosmos.read.readManyByPk.nullHandling=None` is used, hierarchical partition keys with null components are rejected with a clear error because `PartitionKey.NONE` cannot be used with multiple paths.

### Step 10: `CosmosItemsDataSource.readManyByPartitionKeys`

Static entry points that accept a DataFrame and Cosmos config. PK extraction supports two modes:

1. **UDF-produced column**: DataFrame contains `_partitionKeyIdentity` column (from `GetCosmosPartitionKeyValue` UDF).
2. **Schema-matched columns**: DataFrame columns match the container's PK paths.

Nested partition key paths are not resolved automatically from DataFrame columns and must use the UDF-produced `_partitionKeyIdentity` column.

Falls back with `IllegalArgumentException` if neither mode is possible.

### Step 11: `CosmosReadManyByPartitionKeyReader`

Orchestrator that resolves schema, initializes and broadcasts client state to executors, then maps each Spark partition to an `ItemsPartitionReaderWithReadManyByPartitionKey`. The wrapper iterator closes the reader deterministically on exhaustion, on failures, and via Spark task-completion callbacks.

### Step 12: `ItemsPartitionReaderWithReadManyByPartitionKey`

Spark `PartitionReader[InternalRow]` that:

- Deduplicates PKs via `LinkedHashMap` (by PK string representation).
- Passes the pre-built `CosmosReadManyRequestOptions` (with throughput control, diagnostics, custom serializer) to the SDK.
- Uses `TransientIOErrorsRetryingIterator` for retry handling.
- Short-circuits empty PK lists to avoid SDK rejection.

## Phase 3 — Testing

### Unit tests

- Query construction: single PK, HPK full/partial, custom query composition, table alias detection.
- Query plan rejection: aggregates, ORDER BY, DISTINCT, GROUP BY (with and without aggregates), DCOUNT.
- String literal handling: WHERE/parentheses inside string constants.
- Keyword detection: WHERE, ORDER, GROUP, JOIN, OFFSET, LIMIT, HAVING.
- PK serialization/deserialization roundtrip (including malformed JSON handling).
- `findTopLevelWhereIndex` edge cases: subqueries, string literals, case insensitivity.

### Integration tests

- End-to-end SDK: single PK basic, projections, filters, empty results, HPK full/partial, request options propagation.
- Batch size validation: temporarily lowered batch size to exercise batching/interleaving logic.
- Null/empty PK list rejection (eager validation).
- Spark connector: `ItemsPartitionReaderWithReadManyByPartitionKey` with known PK values and non-existent PKs.
- Spark public API: nested partition key containers require `_partitionKeyIdentity` and succeed when populated via `GetCosmosPartitionKeyValue`.
- `CosmosPartitionKeyHelper`: single/HPK roundtrip, case insensitivity, malformed input.

## Phase 4 — Continuation Token Support for Retry Safety

### Motivation

The Spark `TransientIOErrorsRetryingReadManyByPartitionKeyIterator` replays from a
continuation token on transient I/O failures. For this to work, `readManyByPartitionKeys`
must (a) emit a meaningful continuation token on each `FeedResponse`, and (b) accept a
continuation token that resumes where the previous attempt left off.

Because the SDK batches PK values across multiple physical partitions, the continuation
token must capture not only the backend query continuation within a single batch but also
which batches remain. Additionally, batches must be processed sequentially so that a
continuation token unambiguously identifies a position in the result stream.

### Composite continuation token

A new internal class `ReadManyByPartitionKeyContinuationToken` captures the resume state:

| Field                 | Type                  | Description                                                                                                                                                                                  |
|-----------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `remainingBatches`    | `List<Range<String>>` | EPK ranges (minInclusive, maxExclusive) of batches not yet started. Uses EPK ranges — **not** PkRangeIds — so the token survives partition splits. Reuses the existing `Range<String>` type. |
| `currentBatch`        | `Range<String>`       | EPK range of the batch currently being processed.                                                                                                                                            |
| `backendContinuation` | `String` (nullable)   | Backend query continuation within the current batch. `null` means "start from the beginning of this batch".                                                                                  |

EPK ranges are represented as `Range<String>` (the existing SDK type used throughout the
routing layer) with `isMinInclusive = true` and `isMaxExclusive = false`.

**Serialization:** JSON → Base64, keeping the token opaque to callers. Example decoded:

```json
{
  "remainingBatches": [
    {"min": "05C1E0", "max": "0BF333"},
    {"min": "0BF333", "max": "FF"}
  ],
  "currentBatch": {"min": "", "max": "05C1E0"},
  "backendContinuation": "eyJDb21wb3NpdGVUb2tlbi..."
}
```

### Sequential batch execution

Batches are sorted by `minInclusive` EPK (lexicographic) and processed one at a time
via `Flux.concat` instead of `Flux.merge`. Each `FeedResponse` carries the composite
continuation token reflecting the current position:

```text
FeedResponse 1:  remaining=[B2, B3], current=B1, backend=null
FeedResponse 2:  remaining=[B2, B3], current=B1, backend=<tok1>
  (B1 exhausted)
FeedResponse 3:  remaining=[B3],     current=B2, backend=null
FeedResponse 4:  remaining=[B3],     current=B2, backend=<tok1>
  (B2 exhausted)
FeedResponse 5:  remaining=[],       current=B3, backend=null
FeedResponse 6:  remaining=[],       current=B3, backend=<tok1>
  (done — final FeedResponse has null continuation token)
```

Sequential execution is always used — batches are never interleaved concurrently.
This ensures every `FeedResponse` carries a usable continuation token regardless of
whether the caller plans to resume or not.

### API changes

**`CosmosReadManyRequestOptionsImpl`:** Add `requestContinuation` field with
getter/setter. The impl already extends `CosmosQueryRequestOptionsBase`.

**`RxDocumentClientImpl.readManyByPartitionKeys`:**

- **With continuation token:** Deserialize the composite token. Restore batch list from
  `currentBatch` + `remainingBatches` (no PK→routing-map computation needed). Pass
  `backendContinuation` to the `CosmosQueryRequestOptions` for the first batch. Process
  remaining batches in EPK order via `Flux.concat`.
- **Without continuation:** Compute batches as today, map each `PartitionKeyRange` to its
  EPK range, sort by `minInclusive`, execute via `Flux.concat`, stamp each `FeedResponse`
  with the composite token.

**`CosmosAsyncContainer.readManyByPartitionKeyInternalFunc`:** Pass continuation token
from request options through to `RxDocumentClientImpl`.

### Spark reader integration

**`ItemsPartitionReaderWithReadManyByPartitionKey`:** The `fluxFactory` lambda applies
the continuation token: when non-null, set it on a cloned `CosmosReadManyRequestOptions`
before calling `readManyByPartitionKeys`.

**`TransientIOErrorsRetryingReadManyByPartitionKeyIterator`:** No changes needed — the
existing retry loop already captures `lastContinuationToken` from each `FeedResponse`
and passes it to `fluxFactory` on retry. Once the factory applies the token, retry
works correctly.

### EPK range stability across partition splits

The token uses EPK ranges, not PkRangeIds. If a split occurs between retries:

- The query engine handles EPK ranges spanning multiple new physical partitions transparently.
- A stale backend continuation is rejected by the backend; the SDK's
  `StaleResourceRetryPolicy` retries with a fresh routing map while the EPK range ensures
  correct targeting.
