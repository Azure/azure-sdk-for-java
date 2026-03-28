// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.rx.ThinClientTestBase.assertGatewayEndpointUsed;
import static com.azure.cosmos.rx.ThinClientTestBase.assertThinClientEndpointUsed;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Unified thin client query E2E tests using thin client vs compute gateway comparison.
 * <p>
 * Every query is run through both a Gateway HTTP/1 client (via Compute Gateway,
 * which does ServiceInterop EPK conversion server-side) and a Thin Client HTTP/2 client
 * (system under test — via Proxy, which returns raw PartitionKeyInternal arrays, SDK
 * converts to EPK client-side). Tests assert:
 *   (1) Thin client used the :10250 endpoint
 *   (2) Result counts match
 *   (3) Document contents/order match
 * <p>
 * Covers: equality, range, IN, compound AND/OR, parameterized/non-parameterized,
 * boolean, IS_DEFINED, STARTSWITH, CONTAINS, ARRAY_CONTAINS, nested properties,
 * projections, computed aliases, ORDER BY ASC/DESC, DISTINCT, TOP, OFFSET/LIMIT,
 * COUNT/SUM/AVG/MIN/MAX, GROUP BY, cross-partition queries, invalid queries,
 * continuation token draining, and vector search (VectorDistance with flat index).
 */
public class ThinClientQueryE2ETest extends TestSuiteBase {

    private CosmosAsyncClient gatewayClient;   // Gateway: HTTP/1 → Compute Gateway
    private CosmosAsyncClient thinClient;       // SUT: HTTP/2 → Proxy (thin client)
    private CosmosAsyncContainer gatewayContainer;
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
            // 1. Gateway HTTP/1 client (baseline) — Compute Gateway does EPK conversion server-side
            CosmosClientBuilder gatewayBuilder = createGatewayRxDocumentClient();
            this.gatewayClient = gatewayBuilder.buildAsyncClient();
            this.gatewayContainer = getSharedMultiPartitionCosmosContainer(this.gatewayClient);

            // 2. Thin client HTTP/2 — Proxy returns raw PartitionKeyInternal, SDK converts client-side
            // If running locally, uncomment these lines
            System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            CosmosClientBuilder thinBuilder = createGatewayRxDocumentClient(
                TestConfigurations.HOST, null, true, null, true, true, true);
            this.thinClient = thinBuilder.buildAsyncClient();
            this.thinClientContainer = this.thinClient.getDatabase(
                gatewayContainer.getDatabase().getId()).getContainer(gatewayContainer.getId());

            // 3. Clean up shared container to prevent cross-test-class pollution
            cleanUpContainer(this.gatewayContainer);

            // 4. Seed diverse test data for broad query coverage
            seedTestData();
        } catch (Exception e) {
            // Clean up any clients that were successfully created before the failure
            if (this.thinClient != null) { this.thinClient.close(); this.thinClient = null; }
            if (this.gatewayClient != null) { this.gatewayClient.close(); this.gatewayClient = null; }
            throw e;
        }
    }

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

        bulkInsert(gatewayContainer, seededDocs).blockLast();
    }

    @AfterClass(groups = {"thinclient"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        for (ObjectNode doc : seededDocs) {
            try { gatewayContainer.deleteItem(doc.get(ID_FIELD).asText(), new PartitionKey(commonPk)).block(); }
            catch (Exception e) { /* ignore */ }
        }
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        if (this.thinClient != null) { this.thinClient.close(); }
        if (this.gatewayClient != null) { this.gatewayClient.close(); }
    }

    // ==================== Equality & Filter Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectAll() {
        assertGatewayAndThinClientMatch("SELECT * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEquality() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category = 'electronics'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEqualityParameterized() {
        SqlQuerySpec qs = new SqlQuerySpec("SELECT * FROM c WHERE c.category = @cat");
        qs.setParameters(Arrays.asList(new SqlParameter("@cat", "books")));
        assertGatewayAndThinClientMatch(qs, partitionedOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeGreaterThan() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.age > 30");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeLessThanOrEqual() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.price <= 25.00");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeBetween() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.age >= 18 AND c.age <= 40");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIn() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category IN ('electronics', 'toys')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereCompoundAndOr() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.status = 'active' AND (c.category = 'electronics' OR c.category = 'books')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNotEqual() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.status != 'inactive'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereBooleanField() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.isActive = true");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIsDefined() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE IS_DEFINED(c.address)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereStartsWith() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE STARTSWITH(c.category, 'elec')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereContains() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE CONTAINS(c.category, 'ook')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereArrayContains() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE ARRAY_CONTAINS(c.scores, 50)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNestedProperty() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.address.city = 'Seattle'");
    }

    // ==================== Projection Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectSpecificFields() {
        String query = "SELECT c.id, c.category, c.price FROM c";
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
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
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());
    }

    // ==================== ORDER BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByAsc() {
        assertGatewayAndThinClientMatch("SELECT * FROM c ORDER BY c.age");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByDesc() {
        assertGatewayAndThinClientMatch("SELECT * FROM c ORDER BY c.price DESC");
    }

    // ==================== DISTINCT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValue() {
        assertScalarGatewayAndThinClientMatch("SELECT DISTINCT VALUE c.category FROM c", String.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValueBoolean() {
        assertScalarGatewayAndThinClientMatch("SELECT DISTINCT VALUE c.isActive FROM c", Boolean.class);
    }

    // ==================== TOP Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTop() {
        assertGatewayAndThinClientMatch("SELECT TOP 3 * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTopWithOrderBy() {
        assertGatewayAndThinClientMatch("SELECT TOP 5 * FROM c ORDER BY c.price DESC");
    }

    // ==================== Aggregate Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCount() {
        assertScalarGatewayAndThinClientMatch("SELECT VALUE COUNT(1) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSum() {
        assertScalarGatewayAndThinClientMatch("SELECT VALUE SUM(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testAvg() {
        assertScalarGatewayAndThinClientMatch("SELECT VALUE AVG(c.age) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMin() {
        assertScalarGatewayAndThinClientMatch("SELECT VALUE MIN(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMax() {
        assertScalarGatewayAndThinClientMatch("SELECT VALUE MAX(c.age) FROM c", Integer.class);
    }

    // ==================== GROUP BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupByCount() {
        assertGroupByGatewayAndThinClientMatch("SELECT c.category, COUNT(1) as cnt FROM c GROUP BY c.category", "category");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupBySumAvg() {
        assertGroupByGatewayAndThinClientMatch("SELECT c.category, SUM(c.price) as total, AVG(c.price) as avg FROM c GROUP BY c.category", "category");
    }

    // ==================== OFFSET / LIMIT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOffsetLimit() {
        assertGatewayAndThinClientMatch("SELECT * FROM c ORDER BY c.idx OFFSET 3 LIMIT 4");
    }

    // ==================== JOIN Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinScoresArray() {
        // Self-join on scores array — produces one row per array element
        assertGatewayAndThinClientMatch("SELECT c.id, s AS score FROM c JOIN s IN c.scores");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinWithFilter() {
        // Self-join with WHERE filter on the joined element
        assertGatewayAndThinClientMatch("SELECT c.id, s AS score FROM c JOIN s IN c.scores WHERE s >= 50");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testJoinTagsArray() {
        // Self-join on tags string array
        assertGatewayAndThinClientMatch("SELECT c.id, t AS tag FROM c JOIN t IN c.tags");
    }

    // ==================== EXISTS Subquery Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsSubquery() {
        // Docs pattern: use EXISTS to check if any array element matches
        assertGatewayAndThinClientMatch(
            "SELECT * FROM c WHERE EXISTS (SELECT VALUE s FROM s IN c.scores WHERE s > 60)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsSubqueryWithStringMatch() {
        // EXISTS on tags array with string match
        assertGatewayAndThinClientMatch(
            "SELECT * FROM c WHERE EXISTS (SELECT VALUE t FROM t IN c.tags WHERE t = 'on-sale')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExistsAliasInProjection() {
        // EXISTS aliased in SELECT — returns boolean column
        assertGatewayAndThinClientMatch(
            "SELECT c.id, EXISTS (SELECT VALUE s FROM s IN c.scores WHERE s > 60) AS hasHighScore FROM c");
    }

    // ==================== LIKE Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikePrefix() {
        // LIKE with prefix pattern
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE 'elec%'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeSuffix() {
        // LIKE with suffix pattern
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '%ing'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeContains() {
        // LIKE with contains pattern (substring match via wildcards)
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '%ook%'");
    }

    // ==================== Cross-Partition Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionSelectAll() {
        assertGatewayAndThinClientMatch("SELECT * FROM c ORDER BY c.idx", new CosmosQueryRequestOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionWhereFilter() {
        assertGatewayAndThinClientMatch("SELECT * FROM c WHERE c.category = 'electronics' ORDER BY c.idx",
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
        CosmosAsyncDatabase gwDb = gatewayClient.getDatabase(gatewayContainer.getDatabase().getId());
        CosmosAsyncContainer gwContainer = null;
        CosmosAsyncContainer tcContainer = null;
        List<ObjectNode> createdDocs = new ArrayList<>();

        try {
            // Create 24K RU container — yields ~3 physical partitions
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));
            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);
            gwDb.createContainer(props, ThroughputProperties.createManualThroughput(24000)).block();
            gwContainer = gwDb.getContainer(containerId);
            tcContainer = thinClient.getDatabase(gwDb.getId()).getContainer(containerId);

            // Insert docs across different PKs
            for (int i = 0; i < pkValues.length; i++) {
                String docId = "mr-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, docId);
                doc.put(PK_FIELD, pkValues[i]);
                doc.put("idx", i);
                doc.put("val", i * 100);
                gwContainer.createItem(doc, new PartitionKey(pkValues[i]), null).block();
                createdDocs.add(doc);
            }

            // Build query from template (replace %s with constructed IN list if needed)
            String query = queryTemplate;

            // Gateway vs thin client comparison
            List<ObjectNode> gwResults = new ArrayList<>();
            for (FeedResponse<ObjectNode> page : gwContainer.queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class).byPage().toIterable()) {
                gwResults.addAll(page.getResults());
            }

            List<ObjectNode> tcResults = new ArrayList<>();
            List<CosmosDiagnostics> tcDiag = new ArrayList<>();
            for (FeedResponse<ObjectNode> page : tcContainer.queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class).byPage().toIterable()) {
                tcResults.addAll(page.getResults());
                tcDiag.add(page.getCosmosDiagnostics());
            }
            for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

            assertThat(tcResults.size()).as("Multi-range count mismatch for: " + query).isEqualTo(gwResults.size());
            assertThat(tcResults.size()).isEqualTo(expectedCount);

            // Compare as sets (cross-partition queries may return in different order)
            List<String> gwIds = gwResults.stream().map(d -> d.get(ID_FIELD).asText()).sorted().collect(Collectors.toList());
            List<String> tcIds = tcResults.stream().map(d -> d.get(ID_FIELD).asText()).sorted().collect(Collectors.toList());
            assertThat(tcIds).isEqualTo(gwIds);

        } finally {
            if (gwContainer != null) {
                try { gwContainer.delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
            }
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
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, "SELECT * FROM c", partitionedOptions(), ObjectNode.class);
        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }

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
            // Gateway returns 400; thin client proxy may return 400 or surface the error
            // with a different status code. The key assertion is that the query fails.
            assertThat(e.getStatusCode() == 400 || e.getStatusCode() == 0)
                .as("Invalid query should fail with 400 or proxy error, got: " + e.getStatusCode())
                .isTrue();
            logger.info("Expected error for invalid query: {} (status {})", e.getMessage(), e.getStatusCode());
        }
    }

    // ==================== Vector Search (Gateway vs Thin Client on Vector Container) ====================

    /**
     * Creates a vector-enabled container, runs VectorDistance query through both
     * gateway and thin client, compares results.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testVectorSearchGatewayVsThinClient() {
        String vectorContainerId = "vecCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase gwDb = gatewayClient.getDatabase(gatewayContainer.getDatabase().getId());
        CosmosAsyncContainer gwVectorContainer = null;
        CosmosAsyncContainer tcVectorContainer = null;

        try {
            // 1. Create vector-enabled container
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

            gwDb.createContainer(props).block();
            gwVectorContainer = gwDb.getContainer(vectorContainerId);
            tcVectorContainer = thinClient.getDatabase(gwDb.getId()).getContainer(vectorContainerId);

            // 2. Insert docs with 3D embeddings
            double[][] embeddings = {
                {1.0, 0.0, 0.0},   // doc0 - unit x
                {0.0, 1.0, 0.0},   // doc1 - unit y
                {0.0, 0.0, 1.0},   // doc2 - unit z
                {1.0, 1.0, 0.0},   // doc3 - x+y diagonal
                {0.9, 0.1, 0.0},   // doc4 - close to doc0
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
                gwVectorContainer.createItem(doc, new PartitionKey(vecPk), null).block();
            }

            // 3. Run VectorDistance query through both paths
            String query = "SELECT TOP 5 c.id, c.text, VectorDistance(c.embedding, [1.0, 0.0, 0.0]) AS score "
                + "FROM c ORDER BY VectorDistance(c.embedding, [1.0, 0.0, 0.0])";

            List<ObjectNode> gwResults = new ArrayList<>();
            for (FeedResponse<ObjectNode> page : gwVectorContainer.queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class).byPage().toIterable()) {
                gwResults.addAll(page.getResults());
            }

            List<ObjectNode> tcResults = new ArrayList<>();
            List<CosmosDiagnostics> tcDiag = new ArrayList<>();
            for (FeedResponse<ObjectNode> page : tcVectorContainer.queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class).byPage().toIterable()) {
                tcResults.addAll(page.getResults());
                tcDiag.add(page.getCosmosDiagnostics());
            }

            // 4. Assert thin client endpoint used
            for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

            // 5. Compare results
            assertThat(tcResults.size()).isEqualTo(gwResults.size());
            assertThat(tcResults.size()).isEqualTo(5);

            // Same document order
            for (int i = 0; i < gwResults.size(); i++) {
                assertThat(tcResults.get(i).get("id").asText()).isEqualTo(gwResults.get(i).get("id").asText());
            }

            // Most similar to [1,0,0] should be doc0
            assertThat(tcResults.get(0).get("id").asText()).isEqualTo(docIds.get(0));
            assertThat(tcResults.get(0).get("score").asDouble()).isGreaterThan(0.99);

        } finally {
            if (gwVectorContainer != null) {
                try { gwVectorContainer.delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
            }
        }
    }

    // ==================== Full-Text Search (Expected to fail — capability not enabled) ====================

    /**
     * Creates a container with full-text policy and index, runs FullTextContains query.
     * Expected to fail: account requires EnableNoSQLFullTextSearch capability.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testFullTextSearchGatewayVsThinClient() {
        String containerId = "ftsCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase gwDb = gatewayClient.getDatabase(gatewayContainer.getDatabase().getId());
        CosmosAsyncContainer gwFtsContainer = null;

        try {
            // 1. Create container with full-text policy and full-text index
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

            gwDb.createContainer(props).block();
            gwFtsContainer = gwDb.getContainer(containerId);
            CosmosAsyncContainer tcFtsContainer = thinClient.getDatabase(gwDb.getId()).getContainer(containerId);

            // 2. Insert docs with text content
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
                gwFtsContainer.createItem(doc, new PartitionKey(ftsPk), null).block();
            }

            // 3. Run FullTextContains query through both paths
            String query = "SELECT TOP 10 * FROM c WHERE FullTextContains(c.text, 'mountain')";

            QueryResult<ObjectNode> gwResult = drainQuery(gwFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcFtsContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
            assertThat(gwResult.results.size()).as("Full-text query should return results (docs contain 'mountain')").isPositive();
            assertThat(tcResult.results.size()).isEqualTo(gwResult.results.size());

        } finally {
            if (gwFtsContainer != null) {
                try { gwFtsContainer.delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
            }
        }
    }

    // ==================== Hybrid Search (Expected to fail — capability not enabled) ====================

    /**
     * Creates a container with vector + full-text policies, runs hybrid RRF query.
     * Expected to fail: account requires both EnableNoSQLVectorSearch and EnableNoSQLFullTextSearch.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testHybridSearchGatewayVsThinClient() {
        String containerId = "hybridCompare_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase gwDb = gatewayClient.getDatabase(gatewayContainer.getDatabase().getId());
        CosmosAsyncContainer gwHybridContainer = null;

        try {
            // 1. Create container with both vector and full-text policies
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));

            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);

            // Vector policy
            CosmosVectorEmbeddingPolicy vecPolicy = new CosmosVectorEmbeddingPolicy();
            CosmosVectorEmbedding emb = new CosmosVectorEmbedding();
            emb.setPath("/vector");
            emb.setDataType(CosmosVectorDataType.FLOAT32);
            emb.setEmbeddingDimensions(3);
            emb.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);
            vecPolicy.setCosmosVectorEmbeddings(Collections.singletonList(emb));
            props.setVectorEmbeddingPolicy(vecPolicy);

            // Full-text policy
            CosmosFullTextPath ftPath = new CosmosFullTextPath();
            ftPath.setPath("/text");
            ftPath.setLanguage("en-US");
            CosmosFullTextPolicy ftPolicy = new CosmosFullTextPolicy();
            ftPolicy.setDefaultLanguage("en-US");
            ftPolicy.setPaths(Collections.singletonList(ftPath));
            props.setFullTextPolicy(ftPolicy);

            // Indexing policy with vector + full-text indexes
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

            gwDb.createContainer(props).block();
            gwHybridContainer = gwDb.getContainer(containerId);
            CosmosAsyncContainer tcHybridContainer = thinClient.getDatabase(gwDb.getId()).getContainer(containerId);

            // 2. Insert docs with both text and vector
            String hybridPk = UUID.randomUUID().toString();
            String[] texts = {
                "Red bicycle on the mountain trail",
                "Blue car parked in the city",
                "Green bicycle near the lake"
            };
            double[][] vectors = {
                {1.0, 0.0, 0.0},
                {0.0, 1.0, 0.0},
                {0.0, 0.0, 1.0}
            };
            for (int i = 0; i < texts.length; i++) {
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "hybrid_" + i + "_" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(PK_FIELD, hybridPk);
                doc.put("text", texts[i]);
                ArrayNode arr = doc.putArray("vector");
                for (double v : vectors[i]) { arr.add(v); }
                gwHybridContainer.createItem(doc, new PartitionKey(hybridPk), null).block();
            }

            // 3. Run hybrid RRF query combining VectorDistance + FullTextScore
            String query = "SELECT TOP 3 * FROM c "
                + "ORDER BY RANK RRF(VectorDistance(c.vector, [1.0, 0.0, 0.0]), FullTextScore(c.text, 'bicycle'))";

            QueryResult<ObjectNode> gwResult = drainQuery(gwHybridContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcHybridContainer, query, new CosmosQueryRequestOptions(), ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }
            assertThat(tcResult.results.size()).isEqualTo(gwResult.results.size());

        } finally {
            if (gwHybridContainer != null) {
                try { gwHybridContainer.delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
            }
        }
    }

    // ==================== Assertion & Drain Helpers ====================

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
     * Gateway vs thin client comparison: run query via both gateway and thin client.
     * Assert: (1) gateway used :443, (2) thin client used :10250, (3) same count, (4) same document IDs in order.
     */
    private void assertGatewayAndThinClientMatch(String query) {
        assertGatewayAndThinClientMatch(query, partitionedOptions());
    }

    private void assertGatewayAndThinClientMatch(String query, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, query, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, options, ObjectNode.class);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());

        List<String> gwIds = gwResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + query).isEqualTo(gwIds);
    }

    private void assertGatewayAndThinClientMatch(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, querySpec, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, querySpec, options, ObjectNode.class);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + querySpec.getQueryText()).isEqualTo(gwResult.results.size());

        List<String> gwIds = gwResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResult.results.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + querySpec.getQueryText()).isEqualTo(gwIds);
    }

    private <T> void assertScalarGatewayAndThinClientMatch(String query, Class<T> resultType) {
        assertScalarGatewayAndThinClientMatch(query, partitionedOptions(), resultType);
    }

    private <T> void assertScalarGatewayAndThinClientMatch(String query, CosmosQueryRequestOptions options, Class<T> resultType) {
        QueryResult<T> gwResult = drainQuery(gatewayContainer, query, options, resultType);
        QueryResult<T> tcResult = drainQuery(thinClientContainer, query, options, resultType);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Scalar count mismatch: " + query).isEqualTo(gwResult.results.size());
        for (int i = 0; i < gwResult.results.size(); i++) {
            assertThat(tcResult.results.get(i).toString()).as("Scalar value mismatch at " + i + ": " + query)
                .isEqualTo(gwResult.results.get(i).toString());
        }
    }

    /** Gateway vs thin client comparison for GROUP BY where result order may vary — compare as sets. */
    private void assertGroupByGatewayAndThinClientMatch(String query, String groupField) {
        QueryResult<ObjectNode> gwResult = drainQuery(gatewayContainer, query, partitionedOptions(), ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, partitionedOptions(), ObjectNode.class);

        for (CosmosDiagnostics d : gwResult.diagnostics) { assertGatewayEndpointUsed(d); }
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
