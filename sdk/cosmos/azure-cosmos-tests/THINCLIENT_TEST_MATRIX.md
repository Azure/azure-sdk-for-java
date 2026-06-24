# Thin Client (Gateway V2) QueryPlan — E2E Test Specification

**Primary suite:** `sdk/cosmos/azure-cosmos-tests/.../rx/ThinClientQueryE2ETest.java` (84 `@Test` methods)
**Sibling suites:** `ThinClientPointOperationE2ETest` (3), `ThinClientChangeFeedE2ETest` (3), `ThinClientStoredProcedureE2ETest` (3)
**TestNG group:** `thinclient` · **Status:** reverse-engineered from the committed test code

> This document is the human-readable test-design spec reconstructed from the test code so it can be reviewed independently of the implementation. It states **what is being proven, how, and what is intentionally not covered** — i.e. exactly what a reviewer needs to sign off on the test strategy.

---

## 1. Objective

Prove that **routing query execution (and its prerequisite QueryPlan retrieval) through the Gateway V2 "thin client" proxy (`:10250`) produces results that are functionally identical to the established Direct (TCP, `:443`) path**, across the full SQL surface area that the QueryPlan/feature-flag contract advertises.

The thin client changes two things the tests must defend:
1. **Request routing** — a new RNTBD `QueryPlan` operation (`0x0042`) plus `SupportedQueryFeatures` / `QueryVersion` headers are emitted to the proxy.
2. **Response deserialization** — the proxy returns `queryRanges` as **PartitionKeyInternal JSON arrays** (not EPK hex), which the client must convert to sorted EPK ranges before building the pipeline.

If either is wrong, results diverge from Direct, or the wrong endpoint is hit, or an error is mis-mapped (e.g. `statusCode 0`). The suite is designed to catch all three classes.

---

## 2. System Under Test vs. Baseline (the oracle)

| Role | Client | Transport | Endpoint |
|------|--------|-----------|----------|
| **Baseline / oracle** | `directClient` | Direct TCP | `:443` |
| **SUT** | `thinClient` | Gateway V2 (HTTP/2 → proxy) | `:10250` |

**Oracle model — differential testing.** Each functional test runs the *same* query/options against **both** clients over the **same** physical container and asserts the SUT output matches the baseline. The Direct path is the trusted reference implementation; the thin client must match it exactly. This avoids hand-maintained expected-result fixtures and means every test is automatically a parity test.

Both clients address the **same** container (seeded once via Direct), so any divergence is attributable to the thin-client path, not to data differences.

---

## 3. Data Model (fixture)

Seeded once per class (`@BeforeClass`), 10 documents, single logical partition `commonPk` (`/mypk`):

| Field | Type | Values / shape |
|-------|------|----------------|
| `id` | string | `tcdoc-<i>-<uuid8>` |
| `mypk` | string | `commonPk` (shared) |
| `category` | string | electronics / books / clothing / toys (skewed distribution) |
| `status` | string | active / inactive |
| `age` | int | 8–61 (incl. boundary values 17, 18-adjacent, 42, 61) |
| `price` | double | 7.50–549.99 (drives SUM/AVG/MIN/MAX tolerance tests) |
| `idx` | int | 0–9 (stable ORDER BY key) |
| `isActive` | bool | derived from `status` |
| `address` | object | `{ city: Seattle|Portland, zip }` (nested-property tests) |
| `scores` | int[] | `[i*10, i*10+5]` (JOIN / ARRAY_CONTAINS / EXISTS) |
| `tags` | string[] | category + conditional `on-sale` / `featured` (JOIN / EXISTS string match) |

Design intent: the fixture is deliberately heterogeneous (nested objects, arrays of two types, booleans, skewed categorical distribution, numeric range with decimals) so a **single shared container** exercises filtering, projection, aggregation, JOIN, EXISTS, and type-checking without per-test seeding. Tests needing a different topology (multi-partition, vector, full-text, hybrid) create and tear down their **own** container.

Cleanup: `@AfterClass` bulk-deletes seeded docs and closes both clients; per-test containers use `safeDeleteContainer` in a `finally`.

---

## 4. Assertion Contracts (the heart of the spec)

Every comparison helper enforces **endpoint provenance first**, then result equivalence. A reviewer should focus here — these helpers define what "identical" means.

### 4.1 Endpoint provenance — applied in *every* test
```
for (CosmosDiagnostics d : tcResult.diagnostics) assertThinClientEndpointUsed(d);
```
Asserts the thin-client pages actually traversed `:10250`. This guards against a silent fallback to Direct/Gateway V1 that would make a parity test pass for the wrong reason. The invalid-query test asserts the same on the **exception** diagnostics.

### 4.2 Document-set equality — `assertSameDocumentIds(gw, tc, ordered, desc)`
- **Count** must match first.
- **`ORDER BY` queries** (`isOrderedQuery` = query text contains `ORDER BY`): IDs compared **in sequence** — order is significant.
- **All other queries**: IDs compared as **sorted sets** — cross-partition / cross-page ordering is undefined and must *not* be asserted as sequence.

This split is the key correctness nuance: it asserts ordering exactly where the contract guarantees it and not where it doesn't (avoiding false failures while still catching real ordering bugs in ORDER BY).

### 4.3 Scalar aggregates — `assertScalarValueEquals` (COUNT/SUM/AVG/MIN/MAX)
Numeric values compared within `NUMERIC_TOLERANCE = 1e-6`; falls back to string equality for non-numerics. Prevents false mismatches from float formatting differences between the two serialization paths while still catching real aggregate errors.

### 4.4 GROUP BY — `assertGroupByDirectAndThinClientMatch(query, groupField)`
Rows compared as a **set keyed by `groupField`**, with full-row deep-equality via `jsonEqualsWithTolerance` (recursive, numeric-tolerant). GROUP BY output order is not guaranteed, so set semantics are correct; deep tolerant compare still validates every aggregate in the row.

### 4.5 Drain model — `drainQuery` / `byPage().toIterable()`
All helpers **fully drain** the feed (iterate every page, accumulate results + per-page diagnostics). This means continuation-token handling is exercised implicitly on *every* test, not just the dedicated draining test.

---

## 5. Test Matrix — `ThinClientQueryE2ETest` (84)

Unless noted, each test calls `assertDirectAndThinClientMatch(...)` (§4.1–4.2) with `partitionedOptions()` (single-PK).

| # | Category | Tests | Count | Notable assertion |
|---|----------|-------|-------|-------------------|
| 1 | **Filtering (WHERE)** | SelectAll, WhereEquality, WhereEqualityParameterized, WhereRangeGreaterThan, WhereRangeLessThanOrEqual, WhereRangeBetween, WhereIn, WhereCompoundAndOr, WhereNotEqual, WhereBooleanField, WhereIsDefined, WhereStartsWith, WhereContains, WhereArrayContains, WhereNestedProperty, Between | 16 | set-equality |
| 2 | **Projection** | SelectSpecificFields, SelectComputedAlias, SelectValueObject, SelectValueScalar | 4 | set-equality |
| 3 | **ORDER BY** | OrderByAsc, OrderByDesc | 2 | **sequence**-equality |
| 4 | **DISTINCT** | DistinctValue, DistinctValueBoolean | 2 | set-equality |
| 5 | **TOP** | Top, TopWithOrderBy | 2 | seq for ORDER BY variant |
| 6 | **Aggregates** | Count, Sum, Avg, Min, Max | 5 | scalar ±1e-6 |
| 7 | **GROUP BY** | GroupByCount, GroupBySumAvg | 2 | keyed-set + tolerant deep-equal |
| 8 | **OFFSET/LIMIT** | OffsetLimit | 1 | **sequence** (has ORDER BY) |
| 9 | **JOIN (self, arrays)** | JoinScoresArray, JoinWithFilter, JoinTagsArray | 3 | set-equality |
| 10 | **EXISTS subquery** | ExistsSubquery, ExistsSubqueryWithStringMatch, ExistsAliasInProjection | 3 | set-equality |
| 11 | **LIKE** | LikePrefix, LikeSuffix, LikeContains | 3 | set-equality |
| 12 | **String functions** | Concat, EndsWith, Lower, Upper, Length, Substring, Replace, IndexOf, Left, Reverse, Trim, RegexMatch | 12 | set-equality |
| 13 | **Type-check functions** | IsArray, IsBool, IsNull, IsNumber, IsString, IsObject | 6 | set-equality |
| 14 | **Math functions** | MathAbs, MathCeilingFloor, MathRound, MathPower, MathSqrt | 5 | set-equality |
| 15 | **Array functions** | ArrayLength, ArraySlice | 2 | set-equality |
| 16 | **Conditional** | Iif | 1 | set-equality |
| 17 | **Date/Time** | GetCurrentDateTime | 1 | scalar |
| 18 | **Cross-partition** | CrossPartitionSelectAll, CrossPartitionWhereFilter | 2 | no PK filter; ORDER BY idx → sequence |
| 19 | **Multi-range (own container, 24k RU)** | MultiRangeIN(3/5), MultiRangeOR(2/3), MultiRangeMany(10/10) | 3 | exact count + sorted-ID equality across N physical partitions |
| 20 | **Continuation draining** | ContinuationTokenDraining | 1 | see §6.1 |
| 21 | **Error handling** | InvalidQueryReturnsBadRequest | 1 | see §6.2 |
| 22 | **Vector / Full-Text / Hybrid** | VectorSearch, FullTextSearch, FullTextScoreRanking, HybridSearch | 4 | see §6.3 |
| 23 | **readManyByPartitionKeys (validation QueryPlan path)** | NoCustomQuery, WithCustomQuery, WithParameterizedCustomQuery | 3 | see §6.4 |

**Total: 84.**

---

## 6. Special-case tests (hardened — F1–F5)

These five were strengthened beyond simple parity because they guard the specific failure modes the thin-client change introduces. A reviewer should weigh these most heavily.

### 6.1 `testContinuationTokenDraining` (F-drain)
Drains the thin client with **page size 3** to force multiple continuations, against a fully-drained Direct baseline.
Asserts: (a) `pageCount > 1` (continuations genuinely occurred — not a single page that vacuously passes), (b) drained count == baseline, (c) sorted-ID set == baseline, (d) **no duplicate IDs** across pages (`HashSet.size() == list.size()`). Directly covers the "lower page size and drain" scenario and proves continuation tokens round-trip correctly through the proxy without dropping or duplicating rows.

### 6.2 `testInvalidQueryReturnsBadRequest` (F-error)
Sends `SELEC * FORM c`. Asserts **`statusCode == 400`** (explicitly, with a message naming the `statusCode 0` thin-client decode regression as the thing being guarded), and asserts the failing request used the thin-client endpoint. This is the regression test for the production fix in `RxGatewayStoreModel.validateOrThrow` (non-JSON / NUL-padded proxy error frame must surface as a real 400, never 0).

### 6.3 Vector / Full-Text / Hybrid (F-search)
- **Vector**: TOP-5 `VectorDistance` ORDER BY, **cosine** and **euclidean** variants; asserts size==5, **position-by-position ID equality** with Direct, top result is the planted nearest neighbor, and score > 0.99 for the cosine match.
- **FullTextContains**: asserts non-empty baseline and sorted-ID parity.
- **FullTextScore ranking** (`ORDER BY RANK FullTextScore`): asserts **exact ranked order** parity.
- **Hybrid** (`ORDER BY RANK RRF(VectorDistance, FullTextScore)`): asserts **exact ranked order** parity for TOP-3.

These prove the streaming/non-streaming ORDER BY and HybridSearch features advertised in `SupportedQueryFeatures` actually produce correct *ordering* through the proxy — the highest-risk area, since hybrid/non-streaming plans are where the advertised feature set and the deserialized `queryRanges` interact.

### 6.4 `readManyByPartitionKeys` (F-validation)
Exercises the **validation QueryPlan path** that bifurcates between Compute Gateway (V1) and thin client based on whether `DocumentCollection` metadata is available:
- **NoCustomQuery**: no QueryPlan fetched, but per-batch reads must still hit `:10250`.
- **WithCustomQuery**: custom projection+filter triggers QueryPlan validation; asserts parity **and** projection shape (`id`,`category`,`status` present, `status=='active'`).
- **WithParameterizedCustomQuery**: `@cat` binding; asserts parity and every row `category=='electronics'`.
This is the test that proves the routing rule `useGatewayMode = (partitionKeyDefinition == null)` behaves correctly for both branches.

---

## 7. Query-feature coverage vs. the advertised contract

`SupportedQueryFeatures` advertised by the client (per `QueryPlanRetriever`): Aggregate, CompositeAggregate, MultipleOrderBy, MultipleAggregates, OrderBy, OffsetAndLimit, Distinct, GroupBy, Top, DCount, NonValueAggregate, NonStreamingOrderBy, HybridSearch, WeightedRankFusion.

| Advertised feature | Covered by | Gap? |
|--------------------|-----------|------|
| Aggregate / NonValueAggregate | §6 Aggregates (Count/Sum/Avg/Min/Max) | — |
| GroupBy | GroupByCount, GroupBySumAvg | — |
| OrderBy / MultipleOrderBy / NonStreamingOrderBy | OrderBy*, vector/FTS ranking | MultipleOrderBy (multi-key) not explicit |
| OffsetAndLimit | OffsetLimit | — |
| Distinct | DistinctValue* | — |
| Top | Top, TopWithOrderBy | — |
| HybridSearch / WeightedRankFusion | HybridSearch (RRF) | — |
| DCount | — | **No dedicated test** |
| CompositeAggregate / MultipleAggregates | GroupBySumAvg (two aggs) | partial |

**Intentionally NOT advertised** (documented in code, should be noted to the reviewer):
- `CountIf` — Java has no CountIf aggregator in `SingleGroupAggregator` yet.
- `ListAndSetAggregate` — Java has no MAKELIST/MAKESET support.
- `HybridSearchSkipOrderByRewrite` — Java hybrid pipeline cannot yet consume the optimized plan (would 400 / SC1001).

---

## 8. Known gaps / limitations (for reviewer sign-off)

1. **`DCount` has no dedicated test** — highest-priority coverage gap.
2. **`MultipleOrderBy`** (multi-key `ORDER BY a, b`) is not exercised explicitly.
3. **Cross-partition aggregate/GROUP BY** — aggregates run within a single logical partition (`partitionedOptions`); cross-partition aggregate merge through the proxy is not directly asserted.
4. **Large result drain** — continuation draining uses 10 docs / page size 3 (multiple pages, but small). No high-cardinality (hundreds/thousands of docs) drain to stress merge/continuation under realistic volume.
5. **Tolerance-based scalar/JSON compare** could in principle mask a genuine sub-1e-6 aggregate discrepancy; acceptable but worth noting.
6. **Mixed/empty-result edge cases** (query matching zero docs, null-projection rows) are implicit at best.
7. Sibling suites (point-op, change feed, sproc) are summarized in §0 but specced separately.

---

## 9. What a reviewer (e.g. Aditya Sarpotdar) should verify

- [ ] The **oracle is sound**: Direct path is an acceptable reference for every advertised feature (esp. NonStreamingOrderBy / HybridSearch).
- [ ] The **ordered-vs-unordered** split (§4.2) matches the service ordering contract for each query class.
- [ ] **Endpoint provenance** (§4.1) is genuinely sufficient to prove no Direct fallback (i.e. `assertThinClientEndpointUsed` inspects the actual transport, not a cached/first-hop signal).
- [ ] The **error contract** test (§6.2) asserts the right status/substatus for the proxy's actual error frame.
- [ ] Agreement on the **gap list** (§8) — especially whether `DCount`/`MultipleOrderBy` must be added before merge.
- [ ] Multi-range test (§6.4 / row 19) actually spans **multiple physical partitions** (24k RU container) so EPK-range sort correctness through `convertToSortedEpkRanges` is exercised.

---

*Reverse-engineered from the committed test code. No behavioral claims here are asserted beyond what the test assertions enforce.*
