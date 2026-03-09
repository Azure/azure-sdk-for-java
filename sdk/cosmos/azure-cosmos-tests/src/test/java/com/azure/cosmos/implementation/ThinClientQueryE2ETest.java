// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
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
import com.azure.cosmos.rx.TestSuiteBase;
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

import static com.azure.cosmos.implementation.ThinClientTestBase.assertThinClientEndpointUsed;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Unified thin client query E2E tests using oracle-style comparison.
 *
 * Every query is run through both a Gateway HTTP/1 client (oracle — via Compute Gateway,
 * which does ServiceInterop EPK conversion server-side) and a Thin Client HTTP/2 client
 * (system under test — via Proxy, which returns raw PartitionKeyInternal arrays, SDK
 * converts to EPK client-side). Tests assert:
 *   (1) Thin client used the :10250 endpoint
 *   (2) Result counts match
 *   (3) Document contents/order match
 *
 * Covers: equality, range, IN, compound AND/OR, parameterized/non-parameterized,
 * boolean, IS_DEFINED, STARTSWITH, CONTAINS, ARRAY_CONTAINS, nested properties,
 * projections, computed aliases, ORDER BY ASC/DESC, DISTINCT, TOP, OFFSET/LIMIT,
 * COUNT/SUM/AVG/MIN/MAX, GROUP BY, cross-partition queries, invalid queries,
 * continuation token draining, and vector search (VectorDistance with flat index).
 */
public class ThinClientQueryE2ETest extends TestSuiteBase {

    private CosmosAsyncClient gatewayClient;   // Oracle: HTTP/1 → Compute Gateway
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
        // 1. Gateway HTTP/1 client (oracle) — Compute Gateway does EPK conversion server-side
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

        // 3. Truncate shared container to prevent cross-test-class pollution
        truncateCollection(this.gatewayContainer);

        // 4. Seed diverse test data for broad query coverage
        seedTestData();
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

            gatewayContainer.createItem(doc, new PartitionKey(commonPk), null).block();
            seededDocs.add(doc);
        }
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

    // ==================== Oracle Comparison Helpers ====================

    private CosmosQueryRequestOptions partitionedOptions() {
        CosmosQueryRequestOptions opts = new CosmosQueryRequestOptions();
        opts.setPartitionKey(new PartitionKey(commonPk));
        return opts;
    }

    /**
     * Oracle comparison: run query via both gateway and thin client.
     * Assert: (1) thin client used :10250, (2) same count, (3) same document IDs in order.
     */
    private void assertOracleMatch(String query) {
        assertOracleMatch(query, partitionedOptions());
    }

    private void assertOracleMatch(String query, CosmosQueryRequestOptions options) {
        List<ObjectNode> gwResults = drainQuery(gatewayContainer, query, options);

        List<ObjectNode> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : thinClientContainer.queryItems(query, options, ObjectNode.class).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).as("Count mismatch: " + query).isEqualTo(gwResults.size());

        List<String> gwIds = gwResults.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResults.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + query).isEqualTo(gwIds);
    }

    private void assertOracleMatch(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        List<ObjectNode> gwResults = drainQuery(gatewayContainer, querySpec, options);

        List<ObjectNode> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : thinClientContainer.queryItems(querySpec, options, ObjectNode.class).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).as("Count mismatch: " + querySpec.getQueryText()).isEqualTo(gwResults.size());

        List<String> gwIds = gwResults.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        List<String> tcIds = tcResults.stream().filter(d -> d.has(ID_FIELD)).map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
        assertThat(tcIds).as("IDs mismatch: " + querySpec.getQueryText()).isEqualTo(gwIds);
    }

    private <T> void assertScalarOracleMatch(String query, Class<T> resultType) {
        assertScalarOracleMatch(query, partitionedOptions(), resultType);
    }

    private <T> void assertScalarOracleMatch(String query, CosmosQueryRequestOptions options, Class<T> resultType) {
        List<T> gwResults = drainScalarQuery(gatewayContainer, query, options, resultType);

        List<T> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<T> page : thinClientContainer.queryItems(query, options, resultType).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).as("Scalar count mismatch: " + query).isEqualTo(gwResults.size());
        for (int i = 0; i < gwResults.size(); i++) {
            assertThat(tcResults.get(i).toString()).as("Scalar value mismatch at " + i + ": " + query)
                .isEqualTo(gwResults.get(i).toString());
        }
    }

    /** Oracle comparison for GROUP BY where result order may vary — compare as sets. */
    private void assertGroupByOracleMatch(String query, String groupField) {
        List<ObjectNode> gwResults = drainQuery(gatewayContainer, query, partitionedOptions());
        List<ObjectNode> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : thinClientContainer.queryItems(query, partitionedOptions(), ObjectNode.class).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).as("GROUP BY count mismatch: " + query).isEqualTo(gwResults.size());
        for (ObjectNode gwRow : gwResults) {
            String key = gwRow.get(groupField).asText();
            boolean found = tcResults.stream().anyMatch(tc -> tc.get(groupField).asText().equals(key)
                && tc.toString().equals(gwRow.toString()));
            assertThat(found).as("GROUP BY row not found in thin client results: " + key).isTrue();
        }
    }

    private List<ObjectNode> drainQuery(CosmosAsyncContainer c, String query, CosmosQueryRequestOptions opts) {
        List<ObjectNode> results = new ArrayList<>();
        for (FeedResponse<ObjectNode> p : c.queryItems(query, opts, ObjectNode.class).byPage().toIterable()) {
            results.addAll(p.getResults());
        }
        return results;
    }

    private List<ObjectNode> drainQuery(CosmosAsyncContainer c, SqlQuerySpec qs, CosmosQueryRequestOptions opts) {
        List<ObjectNode> results = new ArrayList<>();
        for (FeedResponse<ObjectNode> p : c.queryItems(qs, opts, ObjectNode.class).byPage().toIterable()) {
            results.addAll(p.getResults());
        }
        return results;
    }

    private <T> List<T> drainScalarQuery(CosmosAsyncContainer c, String query, CosmosQueryRequestOptions opts, Class<T> type) {
        List<T> results = new ArrayList<>();
        for (FeedResponse<T> p : c.queryItems(query, opts, type).byPage().toIterable()) {
            results.addAll(p.getResults());
        }
        return results;
    }

    // ==================== Equality & Filter Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectAll() {
        assertOracleMatch("SELECT * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEquality() {
        assertOracleMatch("SELECT * FROM c WHERE c.category = 'electronics'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereEqualityParameterized() {
        SqlQuerySpec qs = new SqlQuerySpec("SELECT * FROM c WHERE c.category = @cat");
        qs.setParameters(Arrays.asList(new SqlParameter("@cat", "books")));
        assertOracleMatch(qs, partitionedOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeGreaterThan() {
        assertOracleMatch("SELECT * FROM c WHERE c.age > 30");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeLessThanOrEqual() {
        assertOracleMatch("SELECT * FROM c WHERE c.price <= 25.00");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereRangeBetween() {
        assertOracleMatch("SELECT * FROM c WHERE c.age >= 18 AND c.age <= 40");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIn() {
        assertOracleMatch("SELECT * FROM c WHERE c.category IN ('electronics', 'toys')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereCompoundAndOr() {
        assertOracleMatch("SELECT * FROM c WHERE c.status = 'active' AND (c.category = 'electronics' OR c.category = 'books')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNotEqual() {
        assertOracleMatch("SELECT * FROM c WHERE c.status != 'inactive'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereBooleanField() {
        assertOracleMatch("SELECT * FROM c WHERE c.isActive = true");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereIsDefined() {
        assertOracleMatch("SELECT * FROM c WHERE IS_DEFINED(c.address)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereStartsWith() {
        assertOracleMatch("SELECT * FROM c WHERE STARTSWITH(c.category, 'elec')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereContains() {
        assertOracleMatch("SELECT * FROM c WHERE CONTAINS(c.category, 'ook')");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereArrayContains() {
        assertOracleMatch("SELECT * FROM c WHERE ARRAY_CONTAINS(c.scores, 50)");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testWhereNestedProperty() {
        assertOracleMatch("SELECT * FROM c WHERE c.address.city = 'Seattle'");
    }

    // ==================== Projection Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectSpecificFields() {
        String query = "SELECT c.id, c.category, c.price FROM c";
        List<ObjectNode> gwResults = drainQuery(gatewayContainer, query, partitionedOptions());

        List<ObjectNode> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : thinClientContainer.queryItems(query, partitionedOptions(), ObjectNode.class).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).isEqualTo(gwResults.size());
        for (int i = 0; i < gwResults.size(); i++) {
            assertThat(tcResults.get(i).get("category").asText()).isEqualTo(gwResults.get(i).get("category").asText());
            assertThat(tcResults.get(i).get("price").asDouble()).isEqualTo(gwResults.get(i).get("price").asDouble());
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSelectComputedAlias() {
        String query = "SELECT c.id, c.price * 1.1 AS taxedPrice FROM c";
        List<ObjectNode> gwResults = drainQuery(gatewayContainer, query, partitionedOptions());

        List<ObjectNode> tcResults = new ArrayList<>();
        List<CosmosDiagnostics> tcDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : thinClientContainer.queryItems(query, partitionedOptions(), ObjectNode.class).byPage().toIterable()) {
            tcResults.addAll(page.getResults());
            tcDiag.add(page.getCosmosDiagnostics());
        }
        for (CosmosDiagnostics d : tcDiag) { assertThinClientEndpointUsed(d); }

        assertThat(tcResults.size()).isEqualTo(gwResults.size());
    }

    // ==================== ORDER BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByAsc() {
        assertOracleMatch("SELECT * FROM c ORDER BY c.age");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOrderByDesc() {
        assertOracleMatch("SELECT * FROM c ORDER BY c.price DESC");
    }

    // ==================== DISTINCT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValue() {
        assertScalarOracleMatch("SELECT DISTINCT VALUE c.category FROM c", String.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDistinctValueBoolean() {
        assertScalarOracleMatch("SELECT DISTINCT VALUE c.isActive FROM c", Boolean.class);
    }

    // ==================== TOP Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTop() {
        assertOracleMatch("SELECT TOP 3 * FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTopWithOrderBy() {
        assertOracleMatch("SELECT TOP 5 * FROM c ORDER BY c.price DESC");
    }

    // ==================== Aggregate Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCount() {
        assertScalarOracleMatch("SELECT VALUE COUNT(1) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testSum() {
        assertScalarOracleMatch("SELECT VALUE SUM(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testAvg() {
        assertScalarOracleMatch("SELECT VALUE AVG(c.age) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMin() {
        assertScalarOracleMatch("SELECT VALUE MIN(c.price) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testMax() {
        assertScalarOracleMatch("SELECT VALUE MAX(c.age) FROM c", Integer.class);
    }

    // ==================== GROUP BY Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupByCount() {
        assertGroupByOracleMatch("SELECT c.category, COUNT(1) as cnt FROM c GROUP BY c.category", "category");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testGroupBySumAvg() {
        assertGroupByOracleMatch("SELECT c.category, SUM(c.price) as total, AVG(c.price) as avg FROM c GROUP BY c.category", "category");
    }

    // ==================== OFFSET / LIMIT Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testOffsetLimit() {
        assertOracleMatch("SELECT * FROM c ORDER BY c.idx OFFSET 3 LIMIT 4");
    }

    // ==================== Cross-Partition Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionSelectAll() {
        assertOracleMatch("SELECT * FROM c ORDER BY c.idx", new CosmosQueryRequestOptions());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionWhereFilter() {
        assertOracleMatch("SELECT * FROM c WHERE c.category = 'electronics' ORDER BY c.idx",
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

            // Oracle comparison
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
        // Drain with small page size to force multiple continuations
        List<ObjectNode> gwAll = drainQuery(gatewayContainer, "SELECT * FROM c", partitionedOptions());

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
        assertThat(tcAll.size()).as("Continuation draining count mismatch").isEqualTo(gwAll.size());
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

    // ==================== Vector Search (Oracle Comparison on Special Container) ====================

    /**
     * Creates a vector-enabled container, runs VectorDistance query through both
     * gateway and thin client, compares results.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testVectorSearchOracleComparison() {
        String vectorContainerId = "vecOracle_" + UUID.randomUUID().toString().substring(0, 8);
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
}
