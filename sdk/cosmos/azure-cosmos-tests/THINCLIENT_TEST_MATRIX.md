# Thin Client E2E Test Matrix — Gateway V2 QueryPlan Support

---

## 1. Query Tests (`ThinClientQueryE2ETest`) — 80 tests

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
| `testWhereCompoundAndOr` | `SELECT * FROM c WHERE c.status = 'active' AND (...)` | Compound AND/OR |
| `testWhereNotEqual` | `SELECT * FROM c WHERE c.status != 'inactive'` | Not equal |
| `testWhereBooleanField` | `SELECT * FROM c WHERE c.isActive = true` | Boolean filter |
| `testWhereIsDefined` | `SELECT * FROM c WHERE IS_DEFINED(c.address)` | IS_DEFINED |
| `testWhereStartsWith` | `SELECT * FROM c WHERE STARTSWITH(c.category, 'elec')` | STARTSWITH |
| `testWhereContains` | `SELECT * FROM c WHERE CONTAINS(c.category, 'ook')` | CONTAINS |
| `testWhereArrayContains` | `SELECT * FROM c WHERE ARRAY_CONTAINS(c.scores, 50)` | ARRAY_CONTAINS |
| `testWhereNestedProperty` | `SELECT * FROM c WHERE c.address.city = 'Seattle'` | Nested property |
| `testBetween` | `SELECT * FROM c WHERE c.age BETWEEN 18 AND 40` | BETWEEN keyword |

### Projection
| Test | SQL | Query Feature |
|------|-----|---------------|
| `testSelectSpecificFields` | `SELECT c.id, c.category, c.price FROM c` | Field projection |
| `testSelectComputedAlias` | `SELECT c.id, c.price * 1.1 AS taxedPrice FROM c` | Computed alias |
| `testSelectValueObject` | `SELECT VALUE { name: c.category, loc: c.address.city } FROM c` | VALUE with JSON object |
| `testSelectValueScalar` | `SELECT VALUE c.category FROM c` | VALUE scalar |

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

### String Functions
| Test | SQL | Function |
|------|-----|----------|
| `testStringConcat` | `SELECT CONCAT(c.category, '-', c.status) AS label FROM c` | CONCAT |
| `testStringEndsWith` | `SELECT * FROM c WHERE ENDSWITH(c.category, 'ics')` | ENDSWITH |
| `testStringLower` | `SELECT LOWER(c.category) AS lowerCat FROM c` | LOWER |
| `testStringUpper` | `SELECT UPPER(c.status) AS upperStatus FROM c` | UPPER |
| `testStringLength` | `SELECT c.category, LENGTH(c.category) AS len FROM c` | LENGTH |
| `testStringSubstring` | `SELECT SUBSTRING(c.category, 0, 4) AS prefix FROM c` | SUBSTRING |
| `testStringReplace` | `SELECT REPLACE(c.category, 'o', '0') AS replaced FROM c` | REPLACE |
| `testStringIndexOf` | `SELECT INDEX_OF(c.category, 'o') AS pos FROM c` | INDEX_OF |
| `testStringLeft` | `SELECT LEFT(c.category, 3) AS l FROM c` | LEFT |
| `testStringReverse` | `SELECT REVERSE(c.category) AS rev FROM c` | REVERSE |
| `testStringTrim` | `SELECT TRIM(c.status) AS trimmed FROM c` | TRIM |
| `testRegexMatch` | `SELECT * FROM c WHERE RegexMatch(c.category, '^elec.*')` | RegexMatch |

### Type Checking Functions
| Test | SQL | Function |
|------|-----|----------|
| `testIsArray` | `SELECT c.id, IS_ARRAY(c.scores) AS isArr FROM c` | IS_ARRAY |
| `testIsBool` | `SELECT c.id, IS_BOOL(c.isActive) AS isBool FROM c` | IS_BOOL |
| `testIsNull` | `SELECT * FROM c WHERE IS_NULL(c.nonExistentField)` | IS_NULL |
| `testIsNumber` | `SELECT c.id, IS_NUMBER(c.age) AS isNum FROM c` | IS_NUMBER |
| `testIsString` | `SELECT c.id, IS_STRING(c.category) AS isStr FROM c` | IS_STRING |
| `testIsObject` | `SELECT c.id, IS_OBJECT(c.address) AS isObj FROM c` | IS_OBJECT |

### Math Functions
| Test | SQL | Function |
|------|-----|----------|
| `testMathAbs` | `SELECT ABS(c.age - 30) AS diff FROM c` | ABS |
| `testMathCeilingFloor` | `SELECT CEILING(c.price) AS ceil, FLOOR(c.price) AS flr FROM c` | CEILING, FLOOR |
| `testMathRound` | `SELECT ROUND(c.price) AS rounded FROM c` | ROUND |
| `testMathPower` | `SELECT POWER(c.age, 2) AS ageSq FROM c` | POWER |
| `testMathSqrt` | `SELECT SQRT(c.price) AS sqrtPrice FROM c` | SQRT |

### Array Functions
| Test | SQL | Function |
|------|-----|----------|
| `testArrayLength` | `SELECT c.id, ARRAY_LENGTH(c.scores) AS len FROM c` | ARRAY_LENGTH |
| `testArraySlice` | `SELECT c.id, ARRAY_SLICE(c.tags, 0, 1) AS firstTag FROM c` | ARRAY_SLICE |

### Conditional Functions
| Test | SQL | Function |
|------|-----|----------|
| `testIif` | `SELECT c.id, IIF(c.age >= 18, 'adult', 'minor') AS ageGroup FROM c` | IIF |

### Date/Time Functions
| Test | SQL | Function |
|------|-----|----------|
| `testGetCurrentDateTime` | `SELECT VALUE GetCurrentDateTime()` | GetCurrentDateTime |

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
- **Comparison method**: Direct TCP vs Thin Client (HTTP/2 → Proxy), assert identical results
- **Endpoint validation**: Every test asserts thin client used `:10250` endpoint, gateway used `:443`
