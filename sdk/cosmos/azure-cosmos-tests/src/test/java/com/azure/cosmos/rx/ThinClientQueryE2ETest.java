// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.rx.ThinClientTestBase.assertThinClientEndpointUsed;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Thin client query E2E tests comparing Direct TCP (baseline) vs Gateway V2 (thin client).
 * <p>
 * Each query is executed through both connection modes and results are compared:
 * <ul>
 *   <li>Direct TCP — baseline, runs against backend partition replicas.</li>
 *   <li>Gateway V2 (thin client) — system under test, routes through proxy (:10250),
 *       proxy returns raw PartitionKeyInternal arrays, SDK converts to EPK client-side.</li>
 * </ul>
 * Assertions:
 * <ol>
 *   <li>Gateway V2 requests routed through the :10250 thin client endpoint.</li>
 *   <li>Result set sizes match between Direct and Gateway V2.</li>
 *   <li>Result set contents match (document IDs in order for ordered queries, set equality for unordered).</li>
 * </ol>
 */
public class ThinClientQueryE2ETest extends TestSuiteBase {

    private CosmosAsyncClient directClient;    // Baseline: Direct TCP
    private CosmosAsyncClient thinClient;      // SUT: Gateway V2 (thin client)
    private CosmosAsyncContainer directContainer;
    private CosmosAsyncContainer thinClientContainer;

    private final List<ObjectNode> seededDocs = new ArrayList<>();
    private final String commonPk = "tc-query-" + UUID.randomUUID().toString().substring(0, 8);

    // Use constants and helpers from ThinClientTestBase to avoid duplication.
    private static final String ID_FIELD = ThinClientTestBase.ID_FIELD;
    private static final String PK_FIELD = ThinClientTestBase.PARTITION_KEY_FIELD;
    private static final ObjectMapper OBJECT_MAPPER = ThinClientTestBase.OBJECT_MAPPER;

    @BeforeClass(groups = {"thinclient"}, timeOut = SETUP_TIMEOUT * 2)
    public void before_ThinClientQueryE2ETest() {
        try {
            // 1. Direct TCP client (baseline)
            CosmosClientBuilder directBuilder = createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true);
            this.directClient = directBuilder.buildAsyncClient();
            this.directContainer = getSharedMultiPartitionCosmosContainer(this.directClient);

            // 2. Gateway V2 thin client (system under test)
            // COSMOS.THINCLIENT_ENABLED must be set before building the thin client
            System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            CosmosClientBuilder thinBuilder = createGatewayRxDocumentClient(
                TestConfigurations.HOST, null, true, null, true, true, true);
            this.thinClient = thinBuilder.buildAsyncClient();
            this.thinClientContainer = this.thinClient.getDatabase(
                directContainer.getDatabase().getId()).getContainer(directContainer.getId());

            // 3. Clean up shared container to prevent cross-test-class pollution
            cleanUpContainer(this.directContainer);

            // 4. Seed diverse test data for broad query coverage
            seedTestData();
        } catch (Exception e) {
            // Clean up any clients that were successfully created before the failure
            if (this.thinClient != null) { this.thinClient.close(); this.thinClient = null; }
            if (this.directClient != null) { this.directClient.close(); this.directClient = null; }
            throw e;
        }
    }

    /**
     * Seeds 10 documents into the shared container with the following schema:
     * <pre>
     * {
     *   "id":        "tcdoc-{i}-{uuid}",  // unique document ID
     *   "mypk":      "{commonPk}",         // partition key — same for all seeded docs
     *   "category":  string,               // one of: electronics, books, clothing, toys
     *   "status":    string,               // "active" or "inactive"
     *   "age":       int,                  // range: 8–61
     *   "price":     double,               // range: 7.50–549.99
     *   "idx":       int,                  // sequential index 0–9
     *   "isActive":  boolean,              // derived from status == "active"
     *   "address":   { "city": string, "zip": int },  // nested object
     *   "scores":    [int, int],           // two-element int array: [i*10, i*10+5]
     *   "tags":      [string, ...]         // variable-length string array
     * }
     * </pre>
     */
    private void seedTestData() {
        String[] categories = {"electronics", "books", "clothing", "electronics", "books",
            "clothing", "electronics", "toys", "toys", "books"};
        String[] statuses = {"active", "inactive", "active", "active", "inactive",
            "active", "inactive", "active", "active", "active"};
        int[] ages = {25, 30, 17, 42, 55, 19, 38, 12, 8, 61};
        double[] prices = {99.99, 14.50, 45.00, 299.99, 9.99, 25.00, 549.99, 19.99, 7.50, 22.00};

        for (int i = 0; i < 10; i++) {
            String docId = "tcdoc-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
            ObjectNode doc = OBJECT_MAPPER.createObjectNode();
            doc.put(ID_FIELD, docId);
            doc.put(PK_FIELD, commonPk);
            doc.put("category", categories[i]);
            doc.put("status", statuses[i]);
            doc.put("age", ages[i]);
            doc.put("price", prices[i]);
            doc.put("idx", i);
            doc.put("isActive", statuses[i].equals("active"));

            ObjectNode address = OBJECT_MAPPER.createObjectNode();
            address.put("city", i % 2 == 0 ? "Seattle" : "Portland");
            address.put("zip", 98100 + i);
            doc.set("address", address);

            doc.putArray("scores").add(i * 10).add(i * 10 + 5);

            // Tags array for JOIN/EXISTS tests — varies per doc
            ArrayNode tags = doc.putArray("tags");
            tags.add(categories[i]); // first tag matches category
            if (i % 2 == 0) tags.add("on-sale");
            if (i % 3 == 0) tags.add("featured");

            seededDocs.add(doc);
        }

        bulkInsert(directContainer, seededDocs).blockLast();
    }

    @AfterClass(groups = {"thinclient"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        if (directContainer != null && !seededDocs.isEmpty()) {
            try {
                List<CosmosItemOperation> deleteOps = seededDocs.stream()
                    .map(doc -> CosmosBulkOperations.getDeleteItemOperation(
                        doc.get(ID_FIELD).asText(), new PartitionKey(commonPk)))
                    .collect(Collectors.toList());
                directContainer.executeBulkOperations(Flux.fromIterable(deleteOps)).blockLast();
            } catch (Exception e) {
                logger.warn("Bulk delete of seeded docs failed: {}", e.getMessage());
            }
        }
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        if (this.thinClient != null) { this.thinClient.close(); }
        if (this.directClient != null) { this.directClient.close(); }
    }

    // ==================== Equality & Filter Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectAll() {
        assertDirectAndThinClientMatch("SELECT * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEquality() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category = 'electronics'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEqualityParameterized() {
        SqlQuerySpec qs = new SqlQuerySpec("SELECT * FROM c WHERE c.category = @cat");
        qs.setParameters(Arrays.asList(new SqlParameter("@cat", "books")));
        assertDirectAndThinClientMatch(qs, partitionedOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeGreaterThan() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.age > 30");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeLessThanOrEqual() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.price <= 25.00");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeBetween() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.age >= 18 AND c.age <= 40");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIn() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category IN ('electronics', 'toys')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereCompoundAndOr() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.status = 'active' AND (c.category = 'electronics' OR c.category = 'books')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNotEqual() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.status != 'inactive'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereBooleanField() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.isActive = true");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIsDefined() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE IS_DEFINED(c.address)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereStartsWith() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE STARTSWITH(c.category, 'elec')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereContains() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE CONTAINS(c.category, 'ook')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereArrayContains() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE ARRAY_CONTAINS(c.scores, 50)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNestedProperty() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.address.city = 'Seattle'");
    }

    // ==================== Projection Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectSpecificFields() {
        String query = "SELECT c.id, c.category, c.price FROM c";
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());
        for (int i = 0; i < gwResult.results.size(); i++) {
            assertThat(tcResult.results.get(i).get("category").asText()).isEqualTo(gwResult.results.get(i).get("category").asText());
            assertThat(tcResult.results.get(i).get("price").asDouble()).isEqualTo(gwResult.results.get(i).get("price").asDouble());
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectComputedAlias() {
        String query = "SELECT c.id, c.price * 1.1 AS taxedPrice FROM c";
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());
    }

    // ==================== ORDER BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByAsc() {
        assertDirectAndThinClientMatch("SELECT * FROM c ORDER BY c.age");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByDesc() {
        assertDirectAndThinClientMatch("SELECT * FROM c ORDER BY c.price DESC");
    }

    // ==================== DISTINCT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValue() {
        assertScalarDirectAndThinClientMatch("SELECT DISTINCT VALUE c.category FROM c", String.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValueBoolean() {
        assertScalarDirectAndThinClientMatch("SELECT DISTINCT VALUE c.isActive FROM c", Boolean.class);
    }

    // ==================== TOP Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTop() {
        assertDirectAndThinClientMatch("SELECT TOP 3 * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTopWithOrderBy() {
        assertDirectAndThinClientMatch("SELECT TOP 5 * FROM c ORDER BY c.price DESC");
    }

    // ==================== Aggregate Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCount() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE COUNT(1) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSum() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE SUM(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testAvg() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE AVG(c.age) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMin() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE MIN(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMax() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE MAX(c.age) FROM c", Integer.class);
    }

    // ==================== GROUP BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupByCount() {
        assertGroupByDirectAndThinClientMatch("SELECT c.category, COUNT(1) as cnt FROM c GROUP BY c.category", "category");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupBySumAvg() {
        assertGroupByDirectAndThinClientMatch("SELECT c.category, SUM(c.price) as total, AVG(c.price) as avg FROM c GROUP BY c.category", "category");
    }

    // ==================== OFFSET / LIMIT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOffsetLimit() {
        assertDirectAndThinClientMatch("SELECT * FROM c ORDER BY c.idx OFFSET 3 LIMIT 4");
    }

    // ==================== JOIN Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinScoresArray() {
        // Self-join on scores array — produces one row per array element
        assertDirectAndThinClientMatch("SELECT c.id, s AS score FROM c JOIN s IN c.scores");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinWithFilter() {
        // Self-join with WHERE filter on the joined element
        assertDirectAndThinClientMatch("SELECT c.id, s AS score FROM c JOIN s IN c.scores WHERE s >= 50");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinTagsArray() {
        // Self-join on tags string array
        assertDirectAndThinClientMatch("SELECT c.id, t AS tag FROM c JOIN t IN c.tags");
    }

    // ==================== EXISTS Subquery Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsSubquery() {
        // Docs pattern: use EXISTS to check if any array element matches
        assertDirectAndThinClientMatch(
            "SELECT * FROM c WHERE EXISTS (SELECT VALUE s FROM s IN c.scores WHERE s > 60)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsSubqueryWithStringMatch() {
        // EXISTS on tags array with string match
        assertDirectAndThinClientMatch(
            "SELECT * FROM c WHERE EXISTS (SELECT VALUE t FROM t IN c.tags WHERE t = 'on-sale')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsAliasInProjection() {
        // EXISTS aliased in SELECT — returns boolean column
        assertDirectAndThinClientMatch(
            "SELECT c.id, EXISTS (SELECT VALUE s FROM s IN c.scores WHERE s > 60) AS hasHighScore FROM c");
    }

    // ==================== LIKE Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikePrefix() {
        // LIKE with prefix pattern
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE 'elec%'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeSuffix() {
        // LIKE with suffix pattern
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '%ing'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeContains() {
        // LIKE with contains pattern (substring match via wildcards)
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '%ook%'");
    }

    // ==================== BETWEEN Keyword ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testBetween() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.age BETWEEN 18 AND 40");
    }

    // ==================== String Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringConcat() {
        assertDirectAndThinClientMatch("SELECT CONCAT(c.category, '-', c.status) AS label FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringEndsWith() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE ENDSWITH(c.category, 'ics')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringLower() {
        assertDirectAndThinClientMatch("SELECT LOWER(c.category) AS lowerCat FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringUpper() {
        assertDirectAndThinClientMatch("SELECT UPPER(c.status) AS upperStatus FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringLength() {
        assertDirectAndThinClientMatch("SELECT c.category, LENGTH(c.category) AS len FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringSubstring() {
        assertDirectAndThinClientMatch("SELECT SUBSTRING(c.category, 0, 4) AS prefix FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringReplace() {
        assertDirectAndThinClientMatch("SELECT REPLACE(c.category, 'o', '0') AS replaced FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringIndexOf() {
        assertDirectAndThinClientMatch("SELECT INDEX_OF(c.category, 'o') AS pos FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringLeft() {
        assertDirectAndThinClientMatch("SELECT LEFT(c.category, 3) AS l FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringReverse() {
        assertDirectAndThinClientMatch("SELECT REVERSE(c.category) AS rev FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStringTrim() {
        assertDirectAndThinClientMatch("SELECT TRIM(c.status) AS trimmed FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testRegexMatch() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE RegexMatch(c.category, '^elec.*')");
    }

    // ==================== Type Checking Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsArray() {
        assertDirectAndThinClientMatch("SELECT c.id, IS_ARRAY(c.scores) AS isArr FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsBool() {
        assertDirectAndThinClientMatch("SELECT c.id, IS_BOOL(c.isActive) AS isBool FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsNull() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE IS_NULL(c.nonExistentField)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsNumber() {
        assertDirectAndThinClientMatch("SELECT c.id, IS_NUMBER(c.age) AS isNum FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsString() {
        assertDirectAndThinClientMatch("SELECT c.id, IS_STRING(c.category) AS isStr FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIsObject() {
        assertDirectAndThinClientMatch("SELECT c.id, IS_OBJECT(c.address) AS isObj FROM c");
    }

    // ==================== Math Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMathAbs() {
        assertDirectAndThinClientMatch("SELECT ABS(c.age - 30) AS diff FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMathCeilingFloor() {
        assertDirectAndThinClientMatch("SELECT CEILING(c.price) AS ceil, FLOOR(c.price) AS flr FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMathRound() {
        assertDirectAndThinClientMatch("SELECT ROUND(c.price) AS rounded FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMathPower() {
        assertDirectAndThinClientMatch("SELECT POWER(c.age, 2) AS ageSq FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMathSqrt() {
        assertDirectAndThinClientMatch("SELECT SQRT(c.price) AS sqrtPrice FROM c");
    }

    // ==================== Array Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testArrayLength() {
        assertDirectAndThinClientMatch("SELECT c.id, ARRAY_LENGTH(c.scores) AS len FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testArraySlice() {
        assertDirectAndThinClientMatch("SELECT c.id, ARRAY_SLICE(c.tags, 0, 1) AS firstTag FROM c");
    }

    // ==================== Conditional Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testIif() {
        assertDirectAndThinClientMatch("SELECT c.id, IIF(c.age >= 18, 'adult', 'minor') AS ageGroup FROM c");
    }

    // ==================== Date/Time Function Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGetCurrentDateTime() {
        // Only assert both paths return a non-empty ISO 8601 string — exact values
        // will differ because gateway and proxy execute at slightly different times.
        QueryResult<String> gwResult = drainQuery(directContainer,
            "SELECT VALUE GetCurrentDateTime()", partitionedOptions(), String.class);
        QueryResult<String> tcResult = drainQuery(thinClientContainer,
            "SELECT VALUE GetCurrentDateTime()", partitionedOptions(), String.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
        assertThat(gwResult.results.size()).isEqualTo(1);
        assertThat(tcResult.results.size()).isEqualTo(1);
        assertThat(gwResult.results.get(0)).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
        assertThat(tcResult.results.get(0)).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
    }

    // ==================== SELECT VALUE / Nested Projection Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectValueObject() {
        assertDirectAndThinClientMatch(
            "SELECT VALUE { name: c.category, loc: c.address.city } FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectValueScalar() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE c.category FROM c", String.class);
    }

    // ==================== Cross-Partition Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionSelectAll() {
        assertDirectAndThinClientMatch("SELECT * FROM c ORDER BY c.idx", new CosmosQueryRequestOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionWhereFilter() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category = 'electronics' ORDER BY c.idx",
            new CosmosQueryRequestOptions());
    }

    // ==================== Multi-EPK-Range Tests (Sort Validation) ====================
    // These tests use a dedicated 24,000 RU/s container (3 physical partitions) to ensure
    // documents with different partition keys land on different physical partitions.
    // After PartitionKeyInternal → EPK hash conversion, the sort in
    // parseQueryRangesForThinClient() ensures RoutingMapProviderHelper.getOverlappingRanges()
    // doesn't throw IllegalArgumentException for unsorted ranges.

    /**
     * Helper: creates a 24K RU container, runs the test, deletes the container.
     */
    private void runMultiRangeTest(String[] pkValues, String queryTemplate, int expectedCount) {
        String containerId = "multiRange_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directTestContainer = db.getContainer(containerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));
            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);
            db.createContainer(props, ThroughputProperties.createManualThroughput(24000)).block();

            CosmosAsyncContainer tcContainer = thinClient.getDatabase(db.getId()).getContainer(containerId);

            for (int i = 0; i < pkValues.length; i++) {
                String docId = "mr-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, docId);
                doc.put(PK_FIELD, pkValues[i]);
                doc.put("idx", i);
                doc.put("val", i * 100);
                directTestContainer.createItem(doc, new PartitionKey(pkValues[i]), null).block();
            }

            String query = queryTemplate;

            QueryResult<ObjectNode> directResult = drainQuery(directTestContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

            assertThat(tcResult.results.size()).as("Multi-range count mismatch for: " + query).isEqualTo(directResult.results.size());
            assertThat(tcResult.results.size()).isEqualTo(expectedCount);

            List<String> directIds = directResult.results.stream().map(d -> d.get(ID_FIELD).asText()).sorted().collect(Collectors.toList());
            List<String> tcIds = tcResult.results.stream().map(d -> d.get(ID_FIELD).asText()).sorted().collect(Collectors.toList());
            assertThat(tcIds).isEqualTo(directIds);

        } finally {
            safeDeleteContainer(directTestContainer);
        }
    }

    /**
     * Test: IN clause on partition key with 3 values → 3 disjoint EPK ranges across 3 physical partitions.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 3)
    public void testMultiRangePartitionKeyInClause() {
        String[] pkValues = {"pk-alpha", "pk-beta", "pk-gamma", "pk-delta", "pk-epsilon"};
        runMultiRangeTest(pkValues,
            "SELECT * FROM c WHERE c.mypk IN ('pk-alpha', 'pk-gamma', 'pk-epsilon')",
            3);
    }

    /**
     * Test: OR on partition key values → 2 disjoint EPK ranges.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 3)
    public void testMultiRangePartitionKeyOrClause() {
        String[] pkValues = {"pk-or-1", "pk-or-2", "pk-or-3"};
        runMultiRangeTest(pkValues,
            "SELECT * FROM c WHERE c.mypk = 'pk-or-1' OR c.mypk = 'pk-or-3'",
            2);
    }

    /**
     * Test: IN clause with 10 PK values → 10 disjoint EPK ranges, stress test for sort correctness.
     * Uses UUID-based PK values to maximize EPK hash spread.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 3)
    public void testMultiRangeManyPartitionKeys() {
        String[] pkValues = new String[10];
        for (int i = 0; i < 10; i++) {
            pkValues[i] = "pk-many-" + UUID.randomUUID().toString();
        }

        // Build IN clause dynamically from the random PK values
        StringBuilder sb = new StringBuilder("SELECT * FROM c WHERE c.mypk IN (");
        for (int i = 0; i < pkValues.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("'").append(pkValues[i]).append("'");
        }
        sb.append(")");
        runMultiRangeTest(pkValues, sb.toString(), 10);
    }

    // ==================== Continuation Token Draining ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testContinuationTokenDraining() {
        // Drain gateway fully for expected count
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, "SELECT * FROM c", partitionedOptions(), ObjectNode.class);

        // Drain thin client with small page size to force multiple continuations
        List<ObjectNode> tcAll = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        String continuationToken = null;
        int pageCount = 0;
        int maxIterations = 100;
        do {
            Iterable<FeedResponse<ObjectNode>> pages = thinClientContainer
                .queryItems("SELECT * FROM c", partitionedOptions(), ObjectNode.class)
                .byPage(continuationToken, 3) // small page size
                .toIterable();
            for (FeedResponse<ObjectNode> page : pages) {
                tcAll.addAll(page.getResults());
                tcDiag.add(page.getCosmosDiagnostics());
                continuationToken = page.getContinuationToken();
                pageCount++;
            }
        } while (continuationToken != null && --maxIterations > 0);

        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }
        assertThat(pageCount).as("Should have multiple pages with page size 3").isGreaterThan(1);
        assertThat(tcAll.size()).as("Continuation draining count mismatch").isEqualTo(gwResult.results.size());
    }

    // ==================== Invalid Query ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testInvalidQueryReturnsBadRequest() {
        try {
            thinClientContainer.queryItems("SELEC * FORM c", new CosmosQueryRequestOptions(), ObjectNode.class)
                .byPage().blockFirst();
            fail("Expected exception for invalid query");
        } catch (CosmosException e) {
            assertThat(e.getStatusCode() == 400 || e.getStatusCode() == 0)
                .as("Invalid query should return 400 Bad Request or a thin-client transport rejection, got "
                    + e.getStatusCode())
                .isTrue();
            if (e.getStatusCode() == 0) {
                assertThinClientEndpointUsed(e.getDiagnostics());
            }
            logger.info("Expected error for invalid query: {} (status {})", e.getMessage(), e.getStatusCode());
        }
    }

    // ==================== Vector Search ====================

    /**
     * Creates a vector-enabled container, runs VectorDistance query through both
     * Direct and thin client, compares results.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testVectorSearchGatewayVsThinClient() {
        String vectorContainerId = "vecCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directVecContainer = db.getContainer(vectorContainerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));

            CosmosContainerProperties props = new CosmosContainerProperties(vectorContainerId, pkDef);

            CosmosVectorEmbeddingPolicy policy = new CosmosVectorEmbeddingPolicy();
            CosmosVectorEmbedding emb = new CosmosVectorEmbedding();
            emb.setPath("/embedding");
            emb.setDataType(CosmosVectorDataType.FLOAT32);
            emb.setEmbeddingDimensions(3);
            emb.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);
            policy.setCosmosVectorEmbeddings(Collections.singletonList(emb));
            props.setVectorEmbeddingPolicy(policy);

            IndexingPolicy idxPolicy = new IndexingPolicy();
            idxPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            idxPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/*")));
            idxPolicy.setExcludedPaths(Arrays.asList(new ExcludedPath("/embedding/*"), new ExcludedPath("/\"_etag\"/?")));
            CosmosVectorIndexSpec vecIdx = new CosmosVectorIndexSpec();
            vecIdx.setPath("/embedding");
            vecIdx.setType(CosmosVectorIndexType.FLAT.toString());
            idxPolicy.setVectorIndexes(Collections.singletonList(vecIdx));
            props.setIndexingPolicy(idxPolicy);

            db.createContainer(props).block();
            CosmosAsyncContainer tcVecContainer = thinClient.getDatabase(db.getId()).getContainer(vectorContainerId);

            double[][] embeddings = {
                {1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0},
                {1.0, 1.0, 0.0}, {0.9, 0.1, 0.0},
            };

            String vecPk = UUID.randomUUID().toString();
            List<String> docIds = new ArrayList<>();
            for (int i = 0; i < embeddings.length; i++) {
                String docId = "vec_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
                docIds.add(docId);
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, docId);
                doc.put(PK_FIELD, vecPk);
                doc.put("text", "document " + i);
                ArrayNode arr = doc.putArray("embedding");
                for (double v : embeddings[i]) { arr.add(v); }
                directVecContainer.createItem(doc, new PartitionKey(vecPk), null).block();
            }

            String query = "SELECT TOP 5 c.id, c.text, VectorDistance(c.embedding, [1.0, 0.0, 0.0]) AS score "
                + "FROM c ORDER BY VectorDistance(c.embedding, [1.0, 0.0, 0.0])";

            QueryResult<ObjectNode> directResult = drainQuery(directVecContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcVecContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

            assertThat(tcResult.results.size()).isEqualTo(directResult.results.size());
            assertThat(tcResult.results.size()).isEqualTo(5);

            for (int i = 0; i < directResult.results.size(); i++) {
                assertThat(tcResult.results.get(i).get("id").asText()).isEqualTo(directResult.results.get(i).get("id").asText());
            }

            assertThat(tcResult.results.get(0).get("id").asText()).isEqualTo(docIds.get(0));
            assertThat(tcResult.results.get(0).get("score").asDouble()).isGreaterThan(0.99);

            // Euclidean variant — validates ORDER BY score semantics with a different distance function
            String euclideanQuery = "SELECT TOP 5 c.id, VectorDistance(c.embedding, [1.0, 0.0, 0.0], false, {'distanceFunction':'euclidean'}) AS score "
                + "FROM c ORDER BY VectorDistance(c.embedding, [1.0, 0.0, 0.0], false, {'distanceFunction':'euclidean'})";

            QueryResult<ObjectNode> directEuclidean = drainQuery(directVecContainer, euclideanQuery, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcEuclidean = drainQuery(tcVecContainer, euclideanQuery, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcEuclidean.diagnostics) { assertThinClientEndpointUsed(d); }

            assertThat(tcEuclidean.results.size()).isEqualTo(directEuclidean.results.size());
            assertThat(tcEuclidean.results.size()).isEqualTo(5);

            for (int i = 0; i < directEuclidean.results.size(); i++) {
                assertThat(tcEuclidean.results.get(i).get("id").asText())
                    .as("Euclidean vector search result mismatch at position " + i)
                    .isEqualTo(directEuclidean.results.get(i).get("id").asText());
            }

        } finally {
            safeDeleteContainer(directVecContainer);
        }
    }

    // ==================== Full-Text Search ====================

    /**
     * Creates a container with full-text policy and index, runs FullTextContains query.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testFullTextSearchGatewayVsThinClient() {
        String containerId = "ftsCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directFtsContainer = db.getContainer(containerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));

            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);

            CosmosFullTextPath ftPath = new CosmosFullTextPath();
            ftPath.setPath("/text");
            ftPath.setLanguage("en-US");
            CosmosFullTextPolicy ftPolicy = new CosmosFullTextPolicy();
            ftPolicy.setDefaultLanguage("en-US");
            ftPolicy.setPaths(Collections.singletonList(ftPath));
            props.setFullTextPolicy(ftPolicy);

            IndexingPolicy idxPolicy = new IndexingPolicy();
            idxPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            idxPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/*")));
            idxPolicy.setExcludedPaths(Collections.singletonList(new ExcludedPath("/\"_etag\"/?")));
            CosmosFullTextIndex ftIndex = new CosmosFullTextIndex();
            ftIndex.setPath("/text");
            idxPolicy.setCosmosFullTextIndexes(Collections.singletonList(ftIndex));
            props.setIndexingPolicy(idxPolicy);

            db.createContainer(props).block();
            CosmosAsyncContainer tcFtsContainer = thinClient.getDatabase(db.getId()).getContainer(containerId);

            String ftsPk = UUID.randomUUID().toString();
            String[] texts = {
                "The quick brown fox jumps over the lazy dog",
                "A red bicycle parked near the mountain trail",
                "Electronic devices on sale at the downtown store",
                "Mountain biking trails with scenic views",
                "The lazy cat sleeps on the warm brown couch"
            };
            for (int i = 0; i < texts.length; i++) {
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "fts_" + i + "_" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(PK_FIELD, ftsPk);
                doc.put("text", texts[i]);
                directFtsContainer.createItem(doc, new PartitionKey(ftsPk), null).block();
            }

            String query = "SELECT TOP 10 * FROM c WHERE FullTextContains(c.text, 'mountain')";

            QueryResult<ObjectNode> directResult = drainQuery(directFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
            assertThat(directResult.results.size()).as("Full-text query should return results").isPositive();
            assertThat(tcResult.results.size()).isEqualTo(directResult.results.size());

            List<String> directIds = directResult.results.stream().map(d -> d.get("id").asText()).sorted().collect(Collectors.toList());
            List<String> tcIds = tcResult.results.stream().map(d -> d.get("id").asText()).sorted().collect(Collectors.toList());
            assertThat(tcIds).isEqualTo(directIds);

        } finally {
            safeDeleteContainer(directFtsContainer);
        }
    }

    /**
     * Creates a container with full-text policy and index, runs ORDER BY RANK FullTextScore query.
     * Compares exact ordering between Direct and thin client.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testFullTextScoreRanking() {
        String containerId = "ftsRank_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directFtsContainer = db.getContainer(containerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));

            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);

            CosmosFullTextPath ftPath = new CosmosFullTextPath();
            ftPath.setPath("/text");
            ftPath.setLanguage("en-US");
            CosmosFullTextPolicy ftPolicy = new CosmosFullTextPolicy();
            ftPolicy.setDefaultLanguage("en-US");
            ftPolicy.setPaths(Collections.singletonList(ftPath));
            props.setFullTextPolicy(ftPolicy);

            IndexingPolicy idxPolicy = new IndexingPolicy();
            idxPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            idxPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/*")));
            idxPolicy.setExcludedPaths(Collections.singletonList(new ExcludedPath("/\"_etag\"/?")));
            CosmosFullTextIndex ftIndex = new CosmosFullTextIndex();
            ftIndex.setPath("/text");
            idxPolicy.setCosmosFullTextIndexes(Collections.singletonList(ftIndex));
            props.setIndexingPolicy(idxPolicy);

            db.createContainer(props).block();
            CosmosAsyncContainer tcFtsContainer = thinClient.getDatabase(db.getId()).getContainer(containerId);

            String ftsPk = UUID.randomUUID().toString();
            String[] texts = {
                "The quick brown fox jumps over the lazy dog",
                "A red bicycle parked near the mountain trail",
                "Electronic devices on sale at the downtown store",
                "Mountain biking trails with scenic views",
                "The lazy cat sleeps on the warm brown couch"
            };
            for (int i = 0; i < texts.length; i++) {
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "ftsRank_" + i + "_" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(PK_FIELD, ftsPk);
                doc.put("text", texts[i]);
                directFtsContainer.createItem(doc, new PartitionKey(ftsPk), null).block();
            }

            String query = "SELECT TOP 5 * FROM c ORDER BY RANK FullTextScore(c.text, 'mountain')";

            QueryResult<ObjectNode> directResult = drainQuery(directFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
            assertThat(directResult.results.size()).as("FullTextScore ranking query should return results").isPositive();
            assertThat(tcResult.results.size()).isEqualTo(directResult.results.size());

            for (int i = 0; i < directResult.results.size(); i++) {
                assertThat(tcResult.results.get(i).get("id").asText())
                    .as("FullTextScore ranking result mismatch at position " + i)
                    .isEqualTo(directResult.results.get(i).get("id").asText());
            }

        } finally {
            safeDeleteContainer(directFtsContainer);
        }
    }

    // ==================== Hybrid Search ====================

    /**
     * Creates a container with vector + full-text policies, runs hybrid RRF query.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testHybridSearchGatewayVsThinClient() {
        String containerId = "hybridCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directHybridContainer = db.getContainer(containerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));

            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);

            CosmosVectorEmbeddingPolicy vecPolicy = new CosmosVectorEmbeddingPolicy();
            CosmosVectorEmbedding emb = new CosmosVectorEmbedding();
            emb.setPath("/vector");
            emb.setDataType(CosmosVectorDataType.FLOAT32);
            emb.setEmbeddingDimensions(3);
            emb.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);
            vecPolicy.setCosmosVectorEmbeddings(Collections.singletonList(emb));
            props.setVectorEmbeddingPolicy(vecPolicy);

            CosmosFullTextPath ftPath = new CosmosFullTextPath();
            ftPath.setPath("/text");
            ftPath.setLanguage("en-US");
            CosmosFullTextPolicy ftPolicy = new CosmosFullTextPolicy();
            ftPolicy.setDefaultLanguage("en-US");
            ftPolicy.setPaths(Collections.singletonList(ftPath));
            props.setFullTextPolicy(ftPolicy);

            IndexingPolicy idxPolicy = new IndexingPolicy();
            idxPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            idxPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/*")));
            idxPolicy.setExcludedPaths(Arrays.asList(new ExcludedPath("/vector/*"), new ExcludedPath("/\"_etag\"/?")));
            CosmosVectorIndexSpec vecIdx = new CosmosVectorIndexSpec();
            vecIdx.setPath("/vector");
            vecIdx.setType(CosmosVectorIndexType.FLAT.toString());
            idxPolicy.setVectorIndexes(Collections.singletonList(vecIdx));
            CosmosFullTextIndex ftIndex = new CosmosFullTextIndex();
            ftIndex.setPath("/text");
            idxPolicy.setCosmosFullTextIndexes(Collections.singletonList(ftIndex));
            props.setIndexingPolicy(idxPolicy);

            db.createContainer(props).block();
            CosmosAsyncContainer tcHybridContainer = thinClient.getDatabase(db.getId()).getContainer(containerId);

            String hybridPk = UUID.randomUUID().toString();
            String[] texts = {
                "Red bicycle on the mountain trail",
                "Blue car parked in the city",
                "Green bicycle near the lake"
            };
            double[][] vectors = {
                {1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}
            };
            for (int i = 0; i < texts.length; i++) {
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "hybrid_" + i + "_" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(PK_FIELD, hybridPk);
                doc.put("text", texts[i]);
                ArrayNode arr = doc.putArray("vector");
                for (double v : vectors[i]) { arr.add(v); }
                directHybridContainer.createItem(doc, new PartitionKey(hybridPk), null).block();
            }

            String query = "SELECT TOP 3 * FROM c "
                + "ORDER BY RANK RRF(VectorDistance(c.vector, [1.0, 0.0, 0.0]), FullTextScore(c.text, 'bicycle'))";

            QueryResult<ObjectNode> directResult = drainQuery(directHybridContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcHybridContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
            assertThat(tcResult.results.size()).isEqualTo(directResult.results.size());

            for (int i = 0; i < directResult.results.size(); i++) {
                assertThat(tcResult.results.get(i).get("id").asText())
                    .as("Hybrid search result mismatch at position " + i)
                    .isEqualTo(directResult.results.get(i).get("id").asText());
            }

        } finally {
            safeDeleteContainer(directHybridContainer);
        }
    }

    // ==================== Assertion & Drain Helpers ====================

    private static void safeDeleteContainer(CosmosAsyncContainer container) {
        if (container != null) {
            try { container.delete().block(); } catch (Exception e) { logger.warn("Container cleanup failed: {}", e.getMessage()); }
        }
    }

    /** Holds query results and per-page diagnostics from a fully drained query. */
    private static class QueryResult<T> {
        final List<T> results = new ArrayList<>();
        final List<CosmosDiagnostics> diagnostics = new ArrayList<>();
    }

    private CosmosQueryRequestOptions partitionedOptions() {
        CosmosQueryRequestOptions opts = new CosmosQueryRequestOptions();
        opts.setPartitionKey(new PartitionKey(commonPk));
        return opts;
    }

    private <T> QueryResult<T> drainQuery(CosmosAsyncContainer c, String query, CosmosQueryRequestOptions opts, Class<T> type) {
        QueryResult<T> result = new QueryResult<>();
        for (FeedResponse<T> page : c.queryItems(query, opts, type).byPage().toIterable()) {
            result.results.addAll(page.getResults());
            result.diagnostics.add(page.getCosmosDiagnostics());
        }
        return result;
    }

    private <T> QueryResult<T> drainQuery(CosmosAsyncContainer c, SqlQuerySpec qs, CosmosQueryRequestOptions opts, Class<T> type) {
        QueryResult<T> result = new QueryResult<>();
        for (FeedResponse<T> page : c.queryItems(qs, opts, type).byPage().toIterable()) {
            result.results.addAll(page.getResults());
            result.diagnostics.add(page.getCosmosDiagnostics());
        }
        return result;
    }

    /**
     * Direct vs thin client comparison: run query via both Direct TCP and thin client.
     * Assert: (1) thin client used :10250, (2) same count, (3) same document IDs in order.
     */
    private void assertDirectAndThinClientMatch(String query) {
        assertDirectAndThinClientMatch(query, partitionedOptions());
    }

    private void assertDirectAndThinClientMatch(String query, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, query, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, options, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());

        List<String> gwIds = gwResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + query).isEqualTo(gwIds);
    }

    private void assertDirectAndThinClientMatch(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, querySpec, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, querySpec, options, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + querySpec.getQueryText()).isEqualTo(gwResult.results.size());

        List<String> gwIds = gwResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + querySpec.getQueryText()).isEqualTo(gwIds);
    }

    private <T> void assertScalarDirectAndThinClientMatch(String query, Class<T> resultType) {
        assertScalarDirectAndThinClientMatch(query, partitionedOptions(), resultType);
    }

    private <T> void assertScalarDirectAndThinClientMatch(String query, CosmosQueryRequestOptions options, Class<T> resultType) {
        QueryResult<T> gwResult = drainQuery(directContainer, query, options, resultType);
        QueryResult<T> tcResult = drainQuery(thinClientContainer, query, options, resultType);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Scalar count mismatch: " + query).isEqualTo(gwResult.results.size());
        for (int i = 0; i < gwResult.results.size(); i++) {
            assertThat(tcResult.results.get(i).toString()).as("Scalar value mismatch at " + i + ": " + query)
                .isEqualTo(gwResult.results.get(i).toString());
        }
    }

    /** Direct vs thin client comparison for GROUP BY where result order may vary — compare as sets. */
    private void assertGroupByDirectAndThinClientMatch(String query, String groupField) {
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("GROUP BY count mismatch: " + query).isEqualTo(gwResult.results.size());
        for (ObjectNode gwRow : gwResult.results) {
            String key = gwRow.get(groupField).asText();
            boolean found = tcResult.results.stream().anyMatch(tc -> tc.get(groupField).asText().equals(key)
                && tc.toString().equals(gwRow.toString()));
            assertThat(found).as("GROUP BY row not found in thin client results: " + key).isTrue();
        }
    }
}
