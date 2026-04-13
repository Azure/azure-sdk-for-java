# readMany by Partition Key — Design & Implementation Plan

## Overview

New `readMany` overloads on `CosmosAsyncContainer` / `CosmosContainer` that accept a
`List<PartitionKey>` (without item-id). The SDK splits the PK values by physical
partition, generates a streaming query per physical partition, and returns results as
`CosmosPagedFlux<T>` / `CosmosPagedIterable<T>`.

An optional `SqlQuerySpec` parameter lets callers supply a custom query for projections
and additional filters. The SDK appends the auto-generated PK WHERE clause to it.

## Decisions

| Topic | Decision |
|---|---|
| API name | `readMany` — new overload distinguished by `List<PartitionKey>` parameter |
| Return type | `CosmosPagedFlux<T>` (async) / `CosmosPagedIterable<T>` (sync) |
| Custom query format | `SqlQuerySpec` — full query with parameters; SDK ANDs the PK filter |
| Partial HPK | Supported from the start; prefix PKs fan out via `getOverlappingRanges` |
| PK deduplication | Done at Spark layer only, not in the SDK |
| Spark UDF | New `GetCosmosPartitionKeyValue` UDF |
| Custom query validation | Gateway query plan; reject aggregates/ORDER BY/DISTINCT/GROUP BY/DCount/vector/fulltext |
| Max PK list size | Enforced per invocation (same effective cap as existing readMany) |

## Phase 1 — SDK Core (`azure-cosmos`)

### Step 1: New public overloads in CosmosAsyncContainer

```java
<T> CosmosPagedFlux<T> readMany(List<PartitionKey> partitionKeys, Class<T> classType)
<T> CosmosPagedFlux<T> readMany(List<PartitionKey> partitionKeys,
                                 CosmosReadManyRequestOptions requestOptions,
                                 Class<T> classType)
<T> CosmosPagedFlux<T> readMany(List<PartitionKey> partitionKeys,
                                 SqlQuerySpec customQuery,
                                 CosmosReadManyRequestOptions requestOptions,
                                 Class<T> classType)
```

All delegate to a private `readManyByPartitionKeyInternal(...)`.

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
5. If custom `SqlQuerySpec` provided → validate via query plan (Step 4).
6. Per physical partition → build `SqlQuerySpec` with PK WHERE clause (Step 5).
7. Execute queries via `createReadManyQueryAsync()`.
8. Return results as `CosmosPagedFlux<T>`.

### Step 4: Custom query validation

One-time call per invocation (existing query plan caching applies):

- `QueryPlanRetriever.getQueryPlanThroughGatewayAsync()` for the user query.
- Reject (`IllegalArgumentException`) if:
  - `queryInfo.hasAggregates()`
  - `queryInfo.hasOrderBy()`
  - `queryInfo.hasDistinct()`
  - `queryInfo.hasGroupBy()`
  - `queryInfo.hasDCount()`
  - `queryInfo.hasNonStreamingOrderBy()`
  - `partitionedQueryExecutionInfo.hasHybridSearchQueryInfo()`

### Step 5: Query construction

**Single PK (HASH):**
```sql
{baseQuery} WHERE c["{pkPath}"] IN (@pk0, @pk1, @pk2)
```

**Full HPK (MULTI_HASH):**
```sql
{baseQuery} WHERE (c["{path1}"] = @p0l1 AND c["{path2}"] = @p0l2)
              OR  (c["{path1}"] = @p1l1 AND c["{path2}"] = @p1l2)
```

**Partial HPK (prefix-only):**
```sql
{baseQuery} WHERE (c["{path1}"] = @p0l1)
              OR  (c["{path1}"] = @p1l1)
```

If the base query already has a WHERE clause:
```sql
{selectAndFrom} WHERE ({existingWhere}) AND ({pkFilter})
```

### Step 6: Bridge / accessor wiring

Expose internal method through `ImplementationBridgeHelpers`.

## Phase 2 — Spark Connector (`azure-cosmos-spark_3`)

### Step 7: New UDF — `GetCosmosPartitionKeyValue`

- Input: partition key column(s) as array.
- Output: serialized PK string.

### Step 8: PK-only serialization helper

`CosmosPartitionKeyHelper`:
- `getCosmosPartitionKeyValueString(pkValues)` — serialize.
- `tryParsePartitionKey(serialized)` — deserialize.

### Step 9: `CosmosItemsDataSource.readManyByPartitionKey`

Static entry points, deduplicates PKs at Spark level, delegates to reader.

### Step 10: `CosmosReadManyByPartitionKeyReader`

Per-Spark-partition execution, analogous to `CosmosReadManyReader`.

### Step 11: `ItemsPartitionReaderWithReadManyByPartitionKey`

Calls new SDK API with `Iterator[PartitionKey]`, iterates `CosmosPagedFlux` pages.

## Phase 3 — Testing

- Unit tests: query construction (single PK, HPK, partial HPK, custom query composition).
- Unit tests: query plan rejection (aggregates, ORDER BY, DISTINCT, etc.).
- Integration tests: end-to-end SDK + Spark UDF.
