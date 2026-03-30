# Thin Client E2E Test Matrix — Gateway V2 QueryPlan Support

**Branch**: `AzCosmos_GatewayV2_QueryPlanSupport`  
**PR**: [#47759](https://github.com/Azure/azure-sdk-for-java/pull/47759)  
**Test methodology**: Every query runs through both a **Gateway HTTP/1 client** (Compute Gateway, server-side EPK) and a **Thin Client HTTP/2 client** (Proxy, client-side EPK conversion). Tests assert: (1) thin client endpoint used, (2) result counts match, (3) document contents/order match.

---

## 1. Query Tests (`ThinClientQueryE2ETest`) — 50 tests

### Filtering (WHERE clause)
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testSelectAll` | `SELECT * FROM c` | Full scan |
| `testWhereEquality` | `SELECT * FROM c WHERE c.category = 'electronics'` | Equality filter |
| `testWhereEqualityParameterized` | `SELECT * FROM c WHERE c.category = @cat` | Parameterized query |
| `testWhereRangeGreaterThan` | `SELECT * FROM c WHERE c.age > 30` | Range (>) |
| `testWhereRangeLessThanOrEqual` | `SELECT * FROM c WHERE c.price <= 25.00` | Range (<=) |
| `testWhereRangeBetween` | `SELECT * FROM c WHERE c.age >= 18 AND c.age <= 40` | Range (between) |
| `testWhereIn` | `SELECT * FROM c WHERE c.category IN ('electronics', 'toys')` | IN operator |
| `testWhereCompoundAndOr` | `SELECT * FROM c WHERE c.status = 'active' AND (c.category = 'electronics' OR c.category = 'books')` | Compound AND/OR |
| `testWhereNotEqual` | `SELECT * FROM c WHERE c.status != 'inactive'` | Not equal |
| `testWhereBooleanField` | `SELECT * FROM c WHERE c.isActive = true` | Boolean filter |
| `testWhereIsDefined` | `SELECT * FROM c WHERE IS_DEFINED(c.address)` | IS_DEFINED |
| `testWhereStartsWith` | `SELECT * FROM c WHERE STARTSWITH(c.category, 'elec')` | STARTSWITH |
| `testWhereContains` | `SELECT * FROM c WHERE CONTAINS(c.category, 'ook')` | CONTAINS |
| `testWhereArrayContains` | `SELECT * FROM c WHERE ARRAY_CONTAINS(c.scores, 50)` | ARRAY_CONTAINS |
| `testWhereNestedProperty` | `SELECT * FROM c WHERE c.address.city = 'Seattle'` | Nested property |

### Projection
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testSelectSpecificFields` | `SELECT c.id, c.category, c.price FROM c` | Field projection |
| `testSelectComputedAlias` | `SELECT c.id, c.price * 1.1 AS taxedPrice FROM c` | Computed alias |

### ORDER BY
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testOrderByAsc` | `SELECT * FROM c ORDER BY c.age` | ORDER BY ASC |
| `testOrderByDesc` | `SELECT * FROM c ORDER BY c.price DESC` | ORDER BY DESC |

### DISTINCT
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testDistinctValue` | `SELECT DISTINCT VALUE c.category FROM c` | DISTINCT VALUE (string) |
| `testDistinctValueBoolean` | `SELECT DISTINCT VALUE c.isActive FROM c` | DISTINCT VALUE (boolean) |

### TOP
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testTop` | `SELECT TOP 3 * FROM c` | TOP |
| `testTopWithOrderBy` | `SELECT TOP 5 * FROM c ORDER BY c.price DESC` | TOP + ORDER BY |

### Aggregates
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testCount` | `SELECT VALUE COUNT(1) FROM c` | COUNT |
| `testSum` | `SELECT VALUE SUM(c.price) FROM c` | SUM |
| `testAvg` | `SELECT VALUE AVG(c.age) FROM c` | AVG |
| `testMin` | `SELECT VALUE MIN(c.price) FROM c` | MIN |
| `testMax` | `SELECT VALUE MAX(c.age) FROM c` | MAX |

### GROUP BY
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testGroupByCount` | `SELECT c.category, COUNT(1) as cnt FROM c GROUP BY c.category` | GROUP BY + COUNT |
| `testGroupBySumAvg` | `SELECT c.category, SUM(c.price) as total, AVG(c.price) as avg FROM c GROUP BY c.category` | GROUP BY + SUM + AVG |

### OFFSET / LIMIT
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testOffsetLimit` | `SELECT * FROM c ORDER BY c.idx OFFSET 3 LIMIT 4` | OFFSET + LIMIT |

### JOIN (self-join on arrays)
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testJoinScoresArray` | `SELECT c.id, s AS score FROM c JOIN s IN c.scores` | JOIN (int array) |
| `testJoinWithFilter` | `SELECT c.id, s AS score FROM c JOIN s IN c.scores WHERE s >= 50` | JOIN + WHERE |
| `testJoinTagsArray` | `SELECT c.id, t AS tag FROM c JOIN t IN c.tags` | JOIN (string array) |

### EXISTS subquery
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testExistsSubquery` | `SELECT * FROM c WHERE EXISTS (SELECT VALUE s FROM s IN c.scores WHERE s > 60)` | EXISTS |
| `testExistsSubqueryWithStringMatch` | `SELECT * FROM c WHERE EXISTS (SELECT VALUE t FROM t IN c.tags WHERE t = 'on-sale')` | EXISTS + string match |
| `testExistsAliasInProjection` | `SELECT c.id, EXISTS (...) AS hasHighScore FROM c` | EXISTS in projection |

### LIKE
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testLikePrefix` | `SELECT * FROM c WHERE c.category LIKE 'elec%'` | LIKE prefix |
| `testLikeSuffix` | `SELECT * FROM c WHERE c.category LIKE '%ing'` | LIKE suffix |
| `testLikeContains` | `SELECT * FROM c WHERE c.category LIKE '%ook%'` | LIKE contains |

### Cross-Partition
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testCrossPartitionSelectAll` | `SELECT * FROM c ORDER BY c.idx` | Cross-partition (no PK filter) |
| `testCrossPartitionWhereFilter` | `SELECT * FROM c WHERE c.category = 'electronics' ORDER BY c.idx` | Cross-partition + filter |

### Multi-Range (creates dedicated container with multiple PKs)
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testMultiRangePartitionKeyInClause` | `SELECT * FROM c WHERE c.mypk IN (pk1, pk3, pk5)` | Multi-range IN |
| `testMultiRangePartitionKeyOrClause` | `SELECT * FROM c WHERE c.mypk = 'pk-or-1' OR c.mypk = 'pk-or-3'` | Multi-range OR |
| `testMultiRangeManyPartitionKeys` | `SELECT * FROM c WHERE c.mypk IN (pk1..pk10)` | Multi-range (10 PKs) |

### Continuation Token
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testContinuationTokenDraining` | `SELECT * FROM c` (page size 2) | Pagination / continuation tokens |

### Error Handling
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testInvalidQueryReturnsBadRequest` | `SELEC * FORM c` (invalid) | 400 BadRequest validation |

### Vector Search (requires `EnableNoSQLVectorSearch` capability)
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testVectorSearchGatewayVsThinClient` | `SELECT TOP 5 c.id, VectorDistance(c.embedding, [...]) AS score FROM c ORDER BY VectorDistance(...)` | VectorDistance + FLAT index |
| `testFullTextSearchGatewayVsThinClient` | `SELECT TOP 10 * FROM c WHERE FullTextContains(c.text, 'mountain')` | FullTextContains |
| `testHybridSearchGatewayVsThinClient` | `SELECT TOP 3 * FROM c ORDER BY RANK RRF(VectorDistance(...), FullTextScore(...))` | Hybrid RRF (vector + full-text) |

---

## 2. Point Operations (`ThinClientPointOperationE2ETest`) — 3 tests

| Test | Operation | Coverage |
|------|-----------|----------|
| `testThinClientDocumentPointOperations` | Create, Read, Replace, Upsert, Patch, Delete | Full CRUD + Patch lifecycle |
| `testThinClientBulk` | Bulk create + bulk read | Bulk operations |
| `testThinClientBatch` | Transactional batch (create + read) | CosmosBatch |

---

## 3. Change Feed (`ThinClientChangeFeedE2ETest`) — 3 tests

| Test | FeedRange | Coverage |
|------|-----------|----------|
| `testThinClientIncrementalChangeFeed` | `FeedRange.forLogicalPartition(pk)` | Incremental change feed (via batch insert) |
| `testThinClientChangeFeedFullRange` | `FeedRange.forFullRange()` | Cross-partition change feed |
| `testThinClientChangeFeedPartitionKey` | `FeedRange.forLogicalPartition(pk)` | Single-PK feed with exact count + PK validation |

---

## 4. Stored Procedures (`ThinClientStoredProcedureE2ETest`) — 3 tests

| Test | Operation | Coverage |
|------|-----------|----------|
| `testThinClientStoredProcedure` | Create + execute sproc | Sproc creates a document, verifies execution |
| `testStoredProcedureExecutionWithoutPartitionKeyThrows` | Execute without PK | Validates 400 error |
| `testThinClientStoredProcedureWithPartitionKeyNone` | Execute with `PartitionKey.NONE` | Non-partitioned sproc execution |

---

## Test Infrastructure

- **Test data**: 10 diverse documents seeded per partition (categories, prices, ages, nested objects, arrays, tags, booleans)
- **Shared container**: `/mypk` partition key, reused across query tests
- **Comparison method**: Gateway (HTTP/1 → Compute Gateway) vs Thin Client (HTTP/2 → Proxy), assert identical results
- **Endpoint validation**: Every test asserts thin client used `:10250` endpoint, gateway used `:443`

## Known Blockers (Account-side)

| Blocker | Tests Affected |
|---------|---------------|
| Container creation 408 timeout on INT account | Multi-range tests (3), FullTextSearch (1) |
| `EnableNoSQLVectorSearch` not enabled | VectorSearch (1), HybridSearch (1) |
| `queryplandotnet` account unreachable | Diagnostic test (1) |
