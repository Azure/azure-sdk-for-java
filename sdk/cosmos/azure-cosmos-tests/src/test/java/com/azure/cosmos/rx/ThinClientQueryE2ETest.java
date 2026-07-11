// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
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
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private CosmosAsyncClient thinClient;      // System under test: Gateway V2 (thin client)
    private CosmosAsyncContainer directContainer;
    private CosmosAsyncContainer thinClientContainer;

    // Dedicated container whose documents reside in multiple physical partitions (distinct partition
    // keys over a 12,000 RU/s container). Used by the cross-partition tests so they genuinely fan out.
    private CosmosAsyncContainer directCrossPartitionContainer;
    private CosmosAsyncContainer thinClientCrossPartitionContainer;
    private String crossPartitionContainerId;

    // Dedicated container with a HIERARCHICAL (MULTI_HASH) partition key (/tenantId, /userId) over a
    // 12,000 RU/s container, so its rows span multiple physical partitions. The thin-client (Gateway
    // V2 proxy) emits this container's QueryPlan with MULTI-COMPONENT PartitionKeyInternal min/max
    // arrays (one element per hierarchical path), which the client converts to EPK ranges through the
    // MULTI_HASH branch of convertToSortedEpkRanges. Single-path containers never reach that branch,
    // so this fixture is required for the hierarchical-PK coverage and half-open prefix-range
    // coverage.
    private CosmosAsyncContainer directHierarchicalContainer;
    private CosmosAsyncContainer thinClientHierarchicalContainer;
    private String hierarchicalContainerId;
    private final List<String[]> hierarchicalKeys = new ArrayList<>(); // {tenantId, userId} per seeded doc
    private static final String TENANT_FIELD = "tenantId";
    private static final String USER_FIELD = "userId";
    private static final int HIER_TENANTS = 4;
    private static final int HIER_USERS_PER_TENANT = 6;

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
            CosmosClientBuilder directClientBuilder = createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true);
            this.directClient = directClientBuilder.buildAsyncClient();
            this.directContainer = getSharedMultiPartitionCosmosContainer(this.directClient);

            // 2. Gateway V2 thin client (system under test)
            CosmosClientBuilder thinClientBuilder = createGatewayRxDocumentClient(
                TestConfigurations.HOST, null, true, null, true, true, true);
            this.thinClient = thinClientBuilder.buildAsyncClient();
            this.thinClientContainer = this.thinClient.getDatabase(
                directContainer.getDatabase().getId()).getContainer(directContainer.getId());

            // 3. Clean up shared container to prevent cross-test-class pollution
            cleanUpContainer(this.directContainer);

            // 4. Seed diverse test data for broad query coverage
            seedTestData();

            // 5. Seed a dedicated multi-physical-partition container for genuine cross-partition coverage
            seedCrossPartitionData();

            // 6. Seed a dedicated hierarchical (MULTI_HASH) partition-key container for the
            //    multi-component QueryPlan range-conversion coverage.
            seedHierarchicalData();
        } catch (Exception e) {
            // Best-effort cleanup of any resources created before the failure. Dedicated container
            // deletes must run before the owning clients are closed, and any cleanup failure must
            // not mask the original setup exception.
            try {
                if (this.directCrossPartitionContainer != null) { safeDeleteContainer(this.directCrossPartitionContainer); }
                if (this.directHierarchicalContainer != null) { safeDeleteContainer(this.directHierarchicalContainer); }
            } catch (Exception cleanupError) {
                logger.warn("Cleanup after setup failure did not fully succeed: {}", cleanupError.getMessage());
            }
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

    /**
     * Seeds a dedicated container whose documents reside in multiple physical partitions. The
     * container is provisioned at 12,000 RU/s - above the 10,000 RU/s single-partition limit, so the
     * service splits it into multiple physical partitions - and each document is given a distinct
     * partition key, spreading the rows across those partitions. Cross-partition queries against this
     * container therefore genuinely fan out, unlike the single-partition shared fixture.
     */
    private void seedCrossPartitionData() {
        crossPartitionContainerId = "thinXp_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());

        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));
        CosmosContainerProperties props = new CosmosContainerProperties(crossPartitionContainerId, pkDef);
        db.createContainer(props, ThroughputProperties.createManualThroughput(12000)).block();

        this.directCrossPartitionContainer = db.getContainer(crossPartitionContainerId);
        this.thinClientCrossPartitionContainer =
            thinClient.getDatabase(db.getId()).getContainer(crossPartitionContainerId);

        String[] categories = {"electronics", "books", "clothing", "toys"};
        for (int i = 0; i < 30; i++) {
            ObjectNode doc = OBJECT_MAPPER.createObjectNode();
            doc.put(ID_FIELD, "xp-" + i + "-" + UUID.randomUUID().toString().substring(0, 8));
            // Distinct partition key per document so the rows spread across physical partitions.
            doc.put(PK_FIELD, "xppk-" + i + "-" + UUID.randomUUID());
            doc.put("category", categories[i % categories.length]);
            doc.put("idx", i); // distinct -> ORDER BY c.idx is a total order
            directCrossPartitionContainer.createItem(doc, new PartitionKey(doc.get(PK_FIELD).asText()), null).block();
        }
    }

    /**
     * Seeds a dedicated container with a HIERARCHICAL (MULTI_HASH) partition key (/tenantId, /userId),
     * provisioned at 12,000 RU/s so the rows span multiple physical partitions. Exactly
     * {@code HIER_TENANTS * HIER_USERS_PER_TENANT} documents are seeded, one per distinct
     * (tenantId, userId) pair, with a distinct {@code idx} (so {@code ORDER BY c.idx} is a total
     * order) and a cycled {@code category}. The thin-client proxy emits this container's QueryPlan
     * with MULTI-COMPONENT PartitionKeyInternal min/max arrays, exercising the MULTI_HASH branch of
     * convertToSortedEpkRanges that single-path containers never reach.
     */
    private void seedHierarchicalData() {
        hierarchicalContainerId = "thinHier_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());

        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        pkDef.setKind(PartitionKind.MULTI_HASH);
        pkDef.setVersion(PartitionKeyDefinitionVersion.V2);
        pkDef.setPaths(Arrays.asList("/" + TENANT_FIELD, "/" + USER_FIELD));
        CosmosContainerProperties props = new CosmosContainerProperties(hierarchicalContainerId, pkDef);
        db.createContainer(props, ThroughputProperties.createManualThroughput(12000)).block();

        this.directHierarchicalContainer = db.getContainer(hierarchicalContainerId);
        this.thinClientHierarchicalContainer =
            thinClient.getDatabase(db.getId()).getContainer(hierarchicalContainerId);

        String[] categories = {"electronics", "books", "clothing", "toys"};
        int idx = 0;
        for (int t = 0; t < HIER_TENANTS; t++) {
            String tenantId = "tenant-" + t + "-" + UUID.randomUUID().toString().substring(0, 8);
            for (int u = 0; u < HIER_USERS_PER_TENANT; u++) {
                String userId = "user-" + u + "-" + UUID.randomUUID().toString().substring(0, 8);
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "hier-" + idx + "-" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(TENANT_FIELD, tenantId);
                doc.put(USER_FIELD, userId);
                doc.put("category", categories[idx % categories.length]);
                doc.put("idx", idx); // distinct -> ORDER BY c.idx is a total order
                PartitionKey pk = new PartitionKeyBuilder().add(tenantId).add(userId).build();
                directHierarchicalContainer.createItem(doc, pk, null).block();
                hierarchicalKeys.add(new String[]{tenantId, userId});
                idx++;
            }
        }
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
        if (directCrossPartitionContainer != null) {
            safeDeleteContainer(directCrossPartitionContainer);
        }
        if (directHierarchicalContainer != null) {
            safeDeleteContainer(directHierarchicalContainer);
        }
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

    /**
     * Multi-property ORDER BY (composite ordering). A multi-key ORDER BY can only be served when a
     * matching composite index exists, so this test provisions a dedicated container with a
     * (category ASC, age DESC) composite index, seeds documents on a single logical partition, and
     * asserts the thin client returns the exact same strictly-ordered sequence as Direct. This
     * exercises the multi-component ORDER BY composition / merge path, which single-key ORDER BY
     * does not. Mirrors the multi-ORDER-BY coverage in the .NET SDK.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 2)
    public void testMultipleOrderBy() {
        String containerId = "multiOrderBy_" + UUID.randomUUID().toString().substring(0, 8);
        CosmosAsyncDatabase db = directClient.getDatabase(directContainer.getDatabase().getId());
        CosmosAsyncContainer directTestContainer = db.getContainer(containerId);

        try {
            PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
            pkDef.setPaths(Collections.singletonList("/" + PK_FIELD));
            CosmosContainerProperties props = new CosmosContainerProperties(containerId, pkDef);

            // Multi-property ORDER BY requires a matching composite index.
            IndexingPolicy idxPolicy = new IndexingPolicy();
            List<CompositePath> composite = new ArrayList<>();
            composite.add(new CompositePath().setPath("/category").setOrder(CompositePathSortOrder.ASCENDING));
            composite.add(new CompositePath().setPath("/age").setOrder(CompositePathSortOrder.DESCENDING));
            idxPolicy.setCompositeIndexes(Collections.singletonList(composite));
            props.setIndexingPolicy(idxPolicy);
            db.createContainer(props, ThroughputProperties.createManualThroughput(400)).block();

            CosmosAsyncContainer tcContainer = thinClient.getDatabase(db.getId()).getContainer(containerId);

            String pk = "mob-" + UUID.randomUUID().toString().substring(0, 8);
            String[] categories = {"alpha", "beta", "alpha", "gamma", "beta", "alpha"};
            int[] ages = {30, 25, 20, 40, 35, 50};
            for (int i = 0; i < categories.length; i++) {
                ObjectNode doc = OBJECT_MAPPER.createObjectNode();
                doc.put(ID_FIELD, "mob-" + i + "-" + UUID.randomUUID().toString().substring(0, 8));
                doc.put(PK_FIELD, pk);
                doc.put("category", categories[i]);
                doc.put("age", ages[i]);
                directTestContainer.createItem(doc, new PartitionKey(pk), null).block();
            }

            String query = "SELECT * FROM c ORDER BY c.category ASC, c.age DESC";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(pk));

            QueryResult<ObjectNode> directResult = drainQuery(directTestContainer, query, options, ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainQuery(tcContainer, query, options, ObjectNode.class);

            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

            assertThat(tcResult.results.size()).as("Multi-ORDER-BY count mismatch: " + query).isEqualTo(directResult.results.size());
            assertThat(tcResult.results.size()).isEqualTo(categories.length);
            // Strict sequence parity — the multi-key ORDER BY ordering must be identical to Direct.
            assertSameDocumentIds(directResult.results, tcResult.results, true, query);
        } finally {
            safeDeleteContainer(directTestContainer);
        }
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

    /**
     * DCount (distinct count). Validates the thin client serves the distinct-count aggregate identically
     * to Direct. Cosmos SQL does not accept the standard {@code COUNT(DISTINCT ...)} form; the canonical
     * DCount idiom is {@code COUNT(1)} over a {@code SELECT DISTINCT VALUE} subquery, which the query
     * engine folds into a DCount aggregate. DCount is a distinct query feature that must be advertised in
     * SupportedQueryFeatures for the proxy-generated query plan; this guards that negotiation path.
     * The .NET SDK has equivalent DCount coverage.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testDCount() {
        assertScalarDirectAndThinClientMatch(
            "SELECT VALUE COUNT(1) FROM (SELECT DISTINCT VALUE c.category FROM c) AS t", Integer.class);
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

    // ==================== QueryOracle-derived coverage ====================
    // The CosmosDB QueryOracle (CloudTest "Release-QueryOracle") fuzzes queries from category
    // configs (Like, ScalarExpression, Builtin, Aggregate, ...). These tests bring the LIKE and
    // scalar-expression categories that were not yet exercised above into the Direct-vs-thin-client
    // oracle. Geospatial (ST_DISTANCE/ST_WITHIN) and UDF categories are intentionally NOT covered
    // here because they require a geospatial-indexed container and registered user-defined
    // functions respectively, which the shared seeded fixture does not provide.

    // ---- LIKE: advanced patterns (character classes, single-char wildcard, negation) ----

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeSingleCharWildcard() {
        // '_' matches exactly one character: 'electronic_' matches "electronics".
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE 'electronic_'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeCharacterClassRange() {
        // Character range '[a-c]' — categories starting with a..c (books, clothing).
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '[a-c]%'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testLikeNegatedCharacterClass() {
        // Negated character class '[^bc]' — categories NOT starting with b or c (electronics, toys).
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category LIKE '[^bc]%'");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testNotLike() {
        assertDirectAndThinClientMatch("SELECT * FROM c WHERE c.category NOT LIKE 'elec%'");
    }

    // ---- Scalar expressions: coalesce, computed member access, array literal, unary, modulo, ternary ----

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCoalesceOperator() {
        // '??' returns the right operand when the left is undefined.
        assertScalarDirectAndThinClientMatch("SELECT VALUE (c.missingField ?? c.category) FROM c", String.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testComputedMemberIndexer() {
        // Quoted/computed property accessor c["category"].
        assertScalarDirectAndThinClientMatch("SELECT VALUE c[\"category\"] FROM c", String.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testArrayLiteralProjection() {
        // Array-create scalar expression in the projection.
        assertDirectAndThinClientMatch("SELECT c.id, [c.age, c.idx] AS pair FROM c");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testUnaryNegation() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE -c.age FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testModuloOperator() {
        assertScalarDirectAndThinClientMatch("SELECT VALUE (c.age % 2) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTernaryConditional() {
        // Ternary '?:' — distinct parse path from IIF(...).
        assertScalarDirectAndThinClientMatch(
            "SELECT VALUE (c.age >= 18 ? 'adult' : 'minor') FROM c", String.class);
    }

    // ==================== Query Plan Caching Tests ====================
    // A query scoped to a single logical partition (partition key set on the options) caches the
    // proxy-generated query plan on the client and reuses it on subsequent identical queries. This
    // test verifies that the cached plan still executes correctly - i.e. a cached QueryPlan obtained
    // from the thin-client proxy continues to produce results identical to the Direct (TCP) baseline.

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCachedQueryPlanFromProxyExecutesCorrectly() {
        // GROUP BY requires a query plan. With a partition key set (assertGroupByDirectAndThinClientMatch
        // uses partitionedOptions()), the proxy-generated plan is cached after the first execution.
        String query = "SELECT c.category, COUNT(1) AS cachedPlanCount FROM c GROUP BY c.category";

        // First execution fetches and caches the query plan generated by the thin-client proxy.
        assertGroupByDirectAndThinClientMatch(query, "category");

        // Second execution reuses the cached plan; assert it still matches the Direct baseline.
        assertGroupByDirectAndThinClientMatch(query, "category");
    }

    // ==================== Cross-Partition Tests ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionSelectAll() {
        assertCrossPartitionDirectAndThinClientMatch("SELECT * FROM c ORDER BY c.idx");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionWhereFilter() {
        assertCrossPartitionDirectAndThinClientMatch(
            "SELECT * FROM c WHERE c.category = 'electronics' ORDER BY c.idx");
    }

    // ============ Cross-Partition Aggregate / GROUP BY Merge Tests ============
    // These run aggregate and GROUP BY queries with NO partition key against the dedicated
    // multi-physical-partition container, so the thin client must fan out to every physical partition
    // and MERGE the partial results into the final value/groups. This is distinct from the
    // single-partition aggregate/GROUP BY tests above (which set a partition key and never merge
    // across partitions). Each helper also asserts the Direct baseline genuinely contacted more than
    // one partition key range, proving the cross-partition merge path actually ran.

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionCount() {
        assertCrossPartitionScalarMatch("SELECT VALUE COUNT(1) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionSum() {
        assertCrossPartitionScalarMatch("SELECT VALUE SUM(c.idx) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionAvg() {
        assertCrossPartitionScalarMatch("SELECT VALUE AVG(c.idx) FROM c", Double.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionMin() {
        assertCrossPartitionScalarMatch("SELECT VALUE MIN(c.idx) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionMax() {
        assertCrossPartitionScalarMatch("SELECT VALUE MAX(c.idx) FROM c", Integer.class);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionGroupByCount() {
        assertCrossPartitionGroupByMatch(
            "SELECT c.category, COUNT(1) AS cnt FROM c GROUP BY c.category", "category");
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testCrossPartitionGroupBySumAvg() {
        assertCrossPartitionGroupByMatch(
            "SELECT c.category, SUM(c.idx) AS total, AVG(c.idx) AS avg FROM c GROUP BY c.category", "category");
    }

    // ============ QueryPlan Range-Conversion Boundary Tests ============
    // These tests target the thin-client (Gateway V2 proxy) QueryPlan emission, where the
    // PartitionedQueryExecutionInfo carries queryRanges as PartitionKeyInternal min/max JSON arrays
    // (e.g. {"min":[],"max":["Infinity"]}) rather than the Gateway V1 EPK hex strings. The client
    // converts those arrays to EPK ranges via PartitionKeyInternalHelper.convertToSortedEpkRanges:
    //   - an empty min array  -> MinimumInclusiveEffectivePartitionKey ("")
    //   - an "Infinity" max    -> MaximumExclusiveEffectivePartitionKey ("FF")
    //   - a MULTI_HASH key     -> the multi-component branch of the conversion
    // We assert result parity against the Direct-TCP baseline (whose plan comes from Gateway V1),
    // proving both QueryPlan sources resolve to equivalent ranges.

    /**
     * Full-range / Infinity boundary. {@code SELECT * FROM c} with no partition key forces the
     * proxy to emit the widest possible range (empty min -> "", "Infinity" max -> "FF"). Asserts the
     * cross-partition container returns ALL seeded rows on both paths (absolute count, distinct from
     * {@link #testCrossPartitionSelectAll} which only checks parity) and that the fan-out genuinely
     * spans more than one partition key range.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testFullRangeInfinityBoundary() {
        int expected = 30; // seedCrossPartitionData seeds exactly 30 docs
        CosmosQueryRequestOptions crossPartitionOptions = new CosmosQueryRequestOptions();
        QueryResult<ObjectNode> directResult =
            drainQuery(directCrossPartitionContainer, "SELECT * FROM c", crossPartitionOptions, ObjectNode.class);
        QueryResult<ObjectNode> tcResult =
            drainQuery(thinClientCrossPartitionContainer, "SELECT * FROM c", crossPartitionOptions, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(directResult.results.size()).as("Direct full-range count").isEqualTo(expected);
        assertThat(tcResult.results.size()).as("Thin-client full-range count").isEqualTo(expected);
        assertSameDocumentIds(directResult.results, tcResult.results, false, "full-range SELECT *");
        assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
            .as("full-range query must contact more than one partition key range")
            .isGreaterThan(1);
    }

    /**
     * Explicit user-supplied {@link FeedRange} (EPK-range) scoped read parity.
     * <p>
     * The over-span fix in {@code ThinClientStoreModel#wrapInHttpRequest} keys off the presence of the
     * StartEpk/EndEpk headers (paired with {@code READ_FEED_KEY_TYPE = EffectivePartitionKeyRange}),
     * which {@code FeedRangeEpkImpl} emits whenever a caller supplies an explicit {@link FeedRange} on
     * the query options. The prefix-HPK tests exercise that header path indirectly through the query
     * plan; this test exercises it DIRECTLY by sharding the multi-physical-partition container into its
     * physical feed ranges and querying each one, so a broken EPK-range guard would over-span a shard
     * and surface documents belonging to a sibling partition.
     * <p>
     * For every physical feed range asserts: (1) the thin client routed through the :10250 endpoint,
     * and (2) the thin-client rows for the shard exactly equal the Direct baseline for the SAME shard -
     * an over-span would make the thin-client set a strict superset of the Direct set. Across all shards
     * it further asserts the union exactly reconstructs the full unsharded fan-out with no duplicates,
     * proving the shards are a clean, non-overlapping partition of the key space.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testExplicitFeedRangeShardedReadParity() {
        List<FeedRange> feedRanges = directCrossPartitionContainer.getFeedRanges().block();
        assertThat(feedRanges.size())
            .as("multi-physical-partition container must expose more than one feed range")
            .isGreaterThan(1);

        List<String> unionThinClientIds = new ArrayList<>();
        for (FeedRange feedRange : feedRanges) {
            CosmosQueryRequestOptions directOptions = new CosmosQueryRequestOptions().setFeedRange(feedRange);
            CosmosQueryRequestOptions tcOptions = new CosmosQueryRequestOptions().setFeedRange(feedRange);

            QueryResult<ObjectNode> directShard =
                drainQuery(directCrossPartitionContainer, "SELECT * FROM c", directOptions, ObjectNode.class);
            QueryResult<ObjectNode> tcShard =
                drainQuery(thinClientCrossPartitionContainer, "SELECT * FROM c", tcOptions, ObjectNode.class);
            for (CosmosDiagnostics d : tcShard.diagnostics) { assertThinClientEndpointUsed(d); }

            // Per-shard parity: the thin client must return EXACTLY the Direct baseline's documents for
            // this feed range. A broken EPK-range guard would over-span and pull in sibling-shard docs,
            // making the thin-client set a strict superset of the Direct set.
            assertThat(tcShard.results.size())
                .as("Thin-client feed-range shard count vs Direct for " + feedRange)
                .isEqualTo(directShard.results.size());
            assertThat(idsSorted(tcShard.results))
                .as("Thin-client feed-range shard over-span vs Direct for " + feedRange)
                .isEqualTo(idsSorted(directShard.results));

            unionThinClientIds.addAll(idsSorted(tcShard.results));
        }

        // The shards must be a clean partition of the whole container: their union reconstructs the full
        // unsharded fan-out exactly, with no duplicates (which would indicate two shards overlap).
        QueryResult<ObjectNode> fullFanOut = drainQuery(
            directCrossPartitionContainer, "SELECT * FROM c", new CosmosQueryRequestOptions(), ObjectNode.class);
        List<String> unionSorted = unionThinClientIds.stream().sorted().collect(Collectors.toList());
        assertThat(unionSorted)
            .as("union of per-feed-range thin-client shards must equal the full fan-out with no duplicates")
            .isEqualTo(idsSorted(fullFanOut.results));
    }

    /**
     * Hierarchical (MULTI_HASH) partition key cross-partition parity. Cross-partition queries
     * over the /tenantId,/userId container force the proxy to emit MULTI-COMPONENT PartitionKeyInternal
     * arrays, exercising the MULTI_HASH branch of convertToSortedEpkRanges. Validates plain, ORDER BY,
     * and filtered-ORDER-BY shapes against the Direct baseline.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testHierarchicalPartitionKeyCrossPartition() {
        assertHierarchicalCrossPartitionMatch("SELECT * FROM c");
        assertHierarchicalCrossPartitionMatch("SELECT * FROM c ORDER BY c.idx");
        assertHierarchicalCrossPartitionMatch("SELECT * FROM c WHERE c.category = 'books' ORDER BY c.idx");
    }

    /**
     * Hierarchical prefix (half-open) range. Setting only the FIRST hierarchical component
     * (tenantId) on the options makes the proxy emit a half-open prefix range whose max is the
     * "Infinity"-suffixed sibling of the min - the prefix-range boundary case of the conversion. The
     * full key (both components) instead resolves to a single point range. Iterates the prefix
     * assertion over ALL seeded tenants so that, with fewer physical partitions than
     * {@code HIER_TENANTS}, at least two co-located tenants deterministically exercise the over-span
     * case, and adds a nonexistent-tenant prefix that must return zero. Asserts each prefix query
     * returns exactly the tenant's users and the full key returns exactly one document, all with
     * parity to the Direct baseline.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testHierarchicalPrefixHalfOpenRange() {
        String[] firstKey = hierarchicalKeys.get(0);
        String tenant0 = firstKey[0];
        String user0 = firstKey[1];

        CosmosQueryRequestOptions fullKeyOptions = new CosmosQueryRequestOptions();
        fullKeyOptions.setPartitionKey(new PartitionKeyBuilder().add(tenant0).add(user0).build());
        assertHierarchicalScopedMatch("SELECT * FROM c ORDER BY c.idx", fullKeyOptions, 1);

        // Iterate the prefix (half-open range) assertion over every tenant. With fewer physical
        // partitions than HIER_TENANTS, at least two tenants are co-located, so the over-span
        // regression reproduces deterministically instead of depending on tenant-0's placement.
        for (int t = 0; t < HIER_TENANTS; t++) {
            String tenant = hierarchicalKeys.get(t * HIER_USERS_PER_TENANT)[0];
            CosmosQueryRequestOptions prefixOptions = new CosmosQueryRequestOptions();
            prefixOptions.setPartitionKey(new PartitionKeyBuilder().add(tenant).build());
            assertHierarchicalScopedMatch("SELECT * FROM c ORDER BY c.idx", prefixOptions, HIER_USERS_PER_TENANT);
        }

        // Negative guard: a nonexistent tenant prefix must scope to zero docs on both paths.
        CosmosQueryRequestOptions absentOptions = new CosmosQueryRequestOptions();
        absentOptions.setPartitionKey(new PartitionKeyBuilder().add("tenant-absent-" + UUID.randomUUID()).build());
        assertHierarchicalScopedMatch("SELECT * FROM c ORDER BY c.idx", absentOptions, 0);
    }

    /**
     * Two-source QueryPlan parity. The same ORDER BY projection query is resolved through both
     * QueryPlan sources: the single-path cross-partition container (proxy emits single-component
     * arrays) and the hierarchical container (proxy emits multi-component arrays). Both must match
     * the Gateway V1 Direct baseline, formalizing the invariant that the two emission formats are
     * interchangeable from the client's perspective.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testTwoSourceQueryPlanParity() {
        String q = "SELECT c.id, c.idx, c.category FROM c WHERE c.idx >= 0 ORDER BY c.idx";
        assertCrossPartitionDirectAndThinClientMatch(q);
        assertHierarchicalCrossPartitionMatch(q);
    }

    // ==================== Multi-EPK-Range Tests (Sort Validation) ====================
    // These tests use a dedicated higher-throughput (24,000 RU/s) container so that metadata
    // exposes multiple EPK (effective partition key) ranges. Note: on the emulator / single-box
    // backend these ranges are typically served by a single physical process (no true multi-box
    // partitioning), so this does not guarantee documents physically land on separate partitions.
    // It does, however, exercise the SDK-side routing and sort pipeline: documents with different
    // partition keys map to different EPK ranges, and after PartitionKeyInternal → EPK hash
    // conversion, the sort in parseQueryRangesForThinClient() ensures
    // RoutingMapProviderHelper.getOverlappingRanges() doesn't throw IllegalArgumentException for
    // unsorted ranges.

    /**
     * Helper: creates a higher-throughput (24K RU) container exposing multiple EPK ranges, runs the
     * test, deletes the container.
     */
    private void runMultiRangeTest(String[] pkValues, String queryTemplate, int expectedCount,
                                   boolean assertMultipleRangesContacted) {
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

            if (assertMultipleRangesContacted) {
                assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
                    .as("multi-range query must contact more than one partition key range: " + query)
                    .isGreaterThan(1);
            }

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
            3, false);
    }

    /**
     * Test: OR on partition key values → 2 disjoint EPK ranges.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT * 3)
    public void testMultiRangePartitionKeyOrClause() {
        String[] pkValues = {"pk-or-1", "pk-or-2", "pk-or-3"};
        runMultiRangeTest(pkValues,
            "SELECT * FROM c WHERE c.mypk = 'pk-or-1' OR c.mypk = 'pk-or-3'",
            2, false);
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
        runMultiRangeTest(pkValues, sb.toString(), 10, true);
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

        List<String> tcDrainedIds = idsSorted(tcAll);
        List<String> gwDrainedIds = idsSorted(gwResult.results);
        assertThat(tcDrainedIds).as("Continuation draining ID-set mismatch").isEqualTo(gwDrainedIds);
        assertThat(new HashSet<>(tcDrainedIds).size())
            .as("Duplicate IDs encountered across drained continuation pages")
            .isEqualTo(tcDrainedIds.size());
    }

    // ==================== Invalid Query ====================

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testInvalidQueryReturnsBadRequest() {
        try {
            thinClientContainer.queryItems("SELEC * FORM c", new CosmosQueryRequestOptions(), ObjectNode.class)
                .byPage().blockFirst();
            fail("Expected exception for invalid query");
        } catch (CosmosException e) {
            assertThat(e.getStatusCode())
                .as("Invalid query must return 400 Bad Request (statusCode 0 indicates the thin-client "
                    + "decode regression), got " + e.getStatusCode())
                .isEqualTo(400);
            assertThinClientEndpointUsed(e.getDiagnostics());
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

    // ==================== readManyByPartitionKeys Tests ====================
    // These exercise the validation QueryPlan path that bifurcates between Compute Gateway
    // and the thin client (Gateway V2) based on the DocumentCollection being passed through.
    // For the thin client container we expect the validation QueryPlan call itself to land on
    // the :10250 endpoint, just like the per-batch query requests it precedes.

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testReadManyByPartitionKeysNoCustomQuery() {
        // No custom query: no QueryPlan is fetched, but the per-batch reads must still
        // route through the thin client endpoint.
        ReadManyResult<ObjectNode> gwResult = drainReadMany(
            directContainer, Collections.singletonList(new PartitionKey(commonPk)), null, ObjectNode.class);
        ReadManyResult<ObjectNode> tcResult = drainReadMany(
            thinClientContainer, Collections.singletonList(new PartitionKey(commonPk)), null, ObjectNode.class);

        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size())
            .as("readManyByPartitionKeys (no custom query) count mismatch")
            .isEqualTo(gwResult.results.size());
        assertThat(idsSorted(tcResult.results))
            .as("readManyByPartitionKeys (no custom query) IDs mismatch")
            .isEqualTo(idsSorted(gwResult.results));
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testReadManyByPartitionKeysWithCustomQuery() {
        // Custom projection + filter triggers QueryPlan validation. With the bifurcation
        // wiring in place the QueryPlan request must travel through Gateway V2 (:10250).
        SqlQuerySpec customQuery = new SqlQuerySpec(
            "SELECT c.id, c.category, c.status FROM c WHERE c.status = 'active'");

        ReadManyResult<ObjectNode> gwResult = drainReadMany(
            directContainer, Collections.singletonList(new PartitionKey(commonPk)), customQuery, ObjectNode.class);
        ReadManyResult<ObjectNode> tcResult = drainReadMany(
            thinClientContainer, Collections.singletonList(new PartitionKey(commonPk)), customQuery, ObjectNode.class);

        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size())
            .as("readManyByPartitionKeys (custom query) count mismatch")
            .isEqualTo(gwResult.results.size());
        assertThat(tcResult.results.size())
            .as("readManyByPartitionKeys (custom query) expected at least one active doc")
            .isGreaterThan(0);
        assertThat(idsSorted(tcResult.results))
            .as("readManyByPartitionKeys (custom query) IDs mismatch")
            .isEqualTo(idsSorted(gwResult.results));

        // Projection assertion: every returned doc has the requested fields only.
        for (ObjectNode doc : tcResult.results) {
            assertThat(doc.has("id")).isTrue();
            assertThat(doc.has("category")).isTrue();
            assertThat(doc.has("status")).isTrue();
            assertThat(doc.get("status").asText()).isEqualTo("active");
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testReadManyByPartitionKeysWithParameterizedCustomQuery() {
        // Parameterized custom query — same validation path, exercises SqlParameter binding.
        SqlQuerySpec customQuery = new SqlQuerySpec(
            "SELECT * FROM c WHERE c.category = @cat",
            Collections.singletonList(new SqlParameter("@cat", "electronics")));

        ReadManyResult<ObjectNode> gwResult = drainReadMany(
            directContainer, Collections.singletonList(new PartitionKey(commonPk)), customQuery, ObjectNode.class);
        ReadManyResult<ObjectNode> tcResult = drainReadMany(
            thinClientContainer, Collections.singletonList(new PartitionKey(commonPk)), customQuery, ObjectNode.class);

        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size())
            .as("readManyByPartitionKeys (parameterized) count mismatch")
            .isEqualTo(gwResult.results.size());
        assertThat(tcResult.results.size())
            .as("readManyByPartitionKeys (parameterized) expected at least one electronics doc")
            .isGreaterThan(0);
        assertThat(idsSorted(tcResult.results))
            .as("readManyByPartitionKeys (parameterized) IDs mismatch")
            .isEqualTo(idsSorted(gwResult.results));

        for (ObjectNode doc : tcResult.results) {
            assertThat(doc.get("category").asText()).isEqualTo("electronics");
        }
    }

    // ==================== Partial (Prefix) HPK read Tests ====================
    // Direct-vs-thin-client parity coverage for partial hierarchical keys (only /tenantId, omitting
    // /userId). Direct TCP is the baseline; the thin client must return exactly the same documents.
    //
    // The isolation mechanism differs by API and is called out per-test below:
    //   - readAllItems / readManyByPartitionKeys with a prefix generate a SQL WHERE predicate on the
    //     present component(s) and route by the resolved physical PK-range id, so foreign co-located
    //     tenants are filtered by that predicate (a routing + SQL-predicate parity guard).
    //   - The RNTBD prefix-EPK range path (a raw query with only a prefix key and no generated
    //     predicate, where isolation is purely by the [hash(prefix), hash(prefix)+"FF") sub-range) is
    //     the direct guard of the thin-client MULTI_HASH prefix EPK over-span fix. It is exercised by
    //     testHierarchicalPrefixHalfOpenRange() and testExplicitFeedRangeShardedReadParity().

    /**
     * readAllItems (ReadFeed) with a partial (prefix) hierarchical partition key - only the first
     * component (/tenantId). Iterates over ALL seeded tenants (not just one): at 12000 RU/s the
     * container has fewer physical partitions than {@code HIER_TENANTS}, so by the pigeonhole
     * principle at least two tenants are co-located on the same physical partition. Each prefix read
     * must return exactly that tenant's {@code HIER_USERS_PER_TENANT} documents, matching the Direct
     * baseline; a final nonexistent-tenant prefix must return zero documents.
     *
     * <p>Note: readAllItems with a prefix key generates a SQL {@code WHERE} predicate on the present
     * component(s) ({@code createLogicalPartitionScanQuerySpec}) and routes by the resolved physical
     * PK-range id, so tenant isolation here is enforced by that predicate rather than by an RNTBD
     * prefix-EPK range header. This is therefore a routing + SQL-predicate parity guard against
     * Direct, not a direct guard of the prefix-EPK over-span fix (see
     * {@link #testHierarchicalPrefixHalfOpenRange()} for that path).
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testHierarchicalReadAllItemsPrefixPartitionKey() {
        for (int t = 0; t < HIER_TENANTS; t++) {
            String tenant = hierarchicalKeys.get(t * HIER_USERS_PER_TENANT)[0];
            PartitionKey prefixKey = new PartitionKeyBuilder().add(tenant).build();

            QueryResult<ObjectNode> directResult = drainReadAllItems(directHierarchicalContainer, prefixKey, ObjectNode.class);
            QueryResult<ObjectNode> tcResult = drainReadAllItems(thinClientHierarchicalContainer, prefixKey, ObjectNode.class);
            for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

            assertThat(directResult.results.size())
                .as("Direct readAllItems prefix count for " + tenant).isEqualTo(HIER_USERS_PER_TENANT);
            assertThat(tcResult.results.size())
                .as("Thin-client readAllItems prefix over-span vs Direct for " + tenant).isEqualTo(directResult.results.size());
            assertThat(idsSorted(tcResult.results))
                .as("readAllItems prefix ID mismatch for " + tenant).isEqualTo(idsSorted(directResult.results));
            // Every returned doc must belong to the prefixed tenant (no over-span into co-located tenants).
            for (ObjectNode doc : tcResult.results) {
                assertThat(doc.get(TENANT_FIELD).asText())
                    .as("readAllItems prefix returned a foreign tenant's document").isEqualTo(tenant);
            }
        }

        // Negative guard: a prefix matching no seeded tenant must return zero docs on both paths.
        // Independent of physical co-location - a broken over-span would surface co-located docs even
        // though this tenant was never seeded.
        PartitionKey absentPrefix = new PartitionKeyBuilder().add("tenant-absent-" + UUID.randomUUID()).build();
        QueryResult<ObjectNode> directAbsent = drainReadAllItems(directHierarchicalContainer, absentPrefix, ObjectNode.class);
        QueryResult<ObjectNode> tcAbsent = drainReadAllItems(thinClientHierarchicalContainer, absentPrefix, ObjectNode.class);
        for (CosmosDiagnostics d : tcAbsent.diagnostics) { assertThinClientEndpointUsed(d); }
        assertThat(directAbsent.results.size()).as("Direct readAllItems nonexistent-tenant prefix count").isEqualTo(0);
        assertThat(tcAbsent.results.size())
            .as("Thin-client readAllItems nonexistent-tenant prefix over-span").isEqualTo(0);
    }

    /**
     * readAllItems (ReadFeed) with the FULL two-component hierarchical key resolves to a single
     * logical partition - exactly one document - matching the Direct baseline. Guards that the prefix
     * handling does not regress the full-key point path.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testHierarchicalReadAllItemsFullPartitionKey() {
        String[] firstKey = hierarchicalKeys.get(0);
        PartitionKey fullKey = new PartitionKeyBuilder().add(firstKey[0]).add(firstKey[1]).build();

        QueryResult<ObjectNode> directResult = drainReadAllItems(directHierarchicalContainer, fullKey, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainReadAllItems(thinClientHierarchicalContainer, fullKey, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(directResult.results.size()).as("Direct readAllItems full-key count").isEqualTo(1);
        assertThat(tcResult.results.size())
            .as("Thin-client readAllItems full-key vs Direct").isEqualTo(directResult.results.size());
        assertThat(idsSorted(tcResult.results))
            .as("readAllItems full-key ID mismatch").isEqualTo(idsSorted(directResult.results));
    }

    /**
     * readManyByPartitionKeys with a partial (prefix) hierarchical key: validates that a prefix
     * readMany request routes over the thin client and returns exactly the tenant's documents,
     * matching Direct.
     *
     * <p>Note: readManyByPartitionKeys groups the supplied keys by physical partition and emits a
     * generated {@code WHERE} predicate on the prefix key component(s), so tenant isolation here is
     * enforced by that SQL predicate rather than by the RNTBD prefix-EPK range header. This is
     * therefore a routing + SQL-predicate parity guard against Direct, not a direct guard of the
     * over-span fix. The prefix-EPK header path itself (a raw query with only a prefix key and no
     * generated predicate) is exercised by {@link #testHierarchicalPrefixHalfOpenRange()} and
     * {@link #testExplicitFeedRangeShardedReadParity()}.
     */
    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testHierarchicalReadManyByPartitionKeysPrefixPartitionKey() {
        String tenant0 = hierarchicalKeys.get(0)[0];
        PartitionKey prefixKey = new PartitionKeyBuilder().add(tenant0).build();
        SqlQuerySpec selectAll = new SqlQuerySpec("SELECT * FROM c");

        ReadManyResult<ObjectNode> directResult = drainReadMany(
            directHierarchicalContainer, Collections.singletonList(prefixKey), selectAll, ObjectNode.class);
        ReadManyResult<ObjectNode> tcResult = drainReadMany(
            thinClientHierarchicalContainer, Collections.singletonList(prefixKey), selectAll, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(directResult.results.size())
            .as("Direct readMany prefix count").isEqualTo(HIER_USERS_PER_TENANT);
        assertThat(tcResult.results.size())
            .as("Thin-client readMany prefix over-span vs Direct").isEqualTo(directResult.results.size());
        assertThat(idsSorted(tcResult.results))
            .as("readMany prefix ID mismatch").isEqualTo(idsSorted(directResult.results));
        for (ObjectNode doc : tcResult.results) {
            assertThat(doc.get(TENANT_FIELD).asText())
                .as("readMany prefix returned a foreign tenant's document").isEqualTo(tenant0);
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

    /** Holds readManyByPartitionKeys results and per-page diagnostics. */
    private static class ReadManyResult<T> {
        final List<T> results = new ArrayList<>();
        final List<CosmosDiagnostics> diagnostics = new ArrayList<>();
    }

    private <T> ReadManyResult<T> drainReadMany(
        CosmosAsyncContainer c,
        List<PartitionKey> partitionKeys,
        SqlQuerySpec customQuery,
        Class<T> type) {
        ReadManyResult<T> result = new ReadManyResult<>();
        for (FeedResponse<T> page : c.readManyByPartitionKeys(partitionKeys, customQuery, type)
                                       .byPage()
                                       .toIterable()) {
            result.results.addAll(page.getResults());
            result.diagnostics.add(page.getCosmosDiagnostics());
        }
        return result;
    }

    private <T> QueryResult<T> drainReadAllItems(
        CosmosAsyncContainer c,
        PartitionKey partitionKey,
        Class<T> type) {
        QueryResult<T> result = new QueryResult<>();
        for (FeedResponse<T> page : c.readAllItems(partitionKey, type).byPage().toIterable()) {
            result.results.addAll(page.getResults());
            result.diagnostics.add(page.getCosmosDiagnostics());
        }
        return result;
    }

    private static List<String> idsSorted(List<ObjectNode> docs) {
        return docs.stream()
            .filter(d -> d.has(ID_FIELD))
            .map(d -> d.get(ID_FIELD).asText())
            .sorted()
            .collect(Collectors.toList());
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
     * Assert: (1) thin client used :10250, (2) same count, (3) same document IDs — compared in
     * strict sequence for ORDER BY queries and for single-partition queries (partition key set on
     * the options), and as sorted sets only for cross-partition non-ORDER-BY queries where ordering
     * is undefined. See {@link #isStrictlyOrdered}.
     */
    private void assertDirectAndThinClientMatch(String query) {
        assertDirectAndThinClientMatch(query, partitionedOptions());
    }

    private void assertDirectAndThinClientMatch(String query, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, query, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, query, options, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(gwResult.results.size());
        assertSameDocumentIds(gwResult.results, tcResult.results, isStrictlyOrdered(query, options), query);
    }

    private void assertDirectAndThinClientMatch(SqlQuerySpec querySpec, CosmosQueryRequestOptions options) {
        QueryResult<ObjectNode> gwResult = drainQuery(directContainer, querySpec, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientContainer, querySpec, options, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + querySpec.getQueryText()).isEqualTo(gwResult.results.size());
        assertSameDocumentIds(gwResult.results, tcResult.results, isStrictlyOrdered(querySpec.getQueryText(), options), querySpec.getQueryText());
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
            assertScalarValueEquals(tcResult.results.get(i), gwResult.results.get(i), i + ": " + query);
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
                && jsonEqualsWithTolerance(tc, gwRow));
            assertThat(found).as("GROUP BY row not found in thin client results: " + key).isTrue();
        }
    }

    private static final double NUMERIC_TOLERANCE = 1e-6;

    private static boolean isOrderedQuery(String queryText) {
        return queryText != null && queryText.toUpperCase(Locale.ROOT).contains("ORDER BY");
    }

    /**
     * Decides whether Direct and thin client document IDs must match in strict sequence. Per query
     * test best practice we validate order for as many queries as possible to flush out hidden
     * ordering/paging bugs, and relax to a sorted-set comparison only when order is genuinely
     * undefined: a cross-partition query (no partition key on the options) that lacks an ORDER BY.
     * Single-partition queries (partitionedOptions sets a partition key) and any ORDER BY query are
     * therefore always compared strictly.
     */
    private static boolean isStrictlyOrdered(String queryText, CosmosQueryRequestOptions options) {
        boolean crossPartition = options == null || options.getPartitionKey() == null;
        return isOrderedQuery(queryText) || !crossPartition;
    }

    /**
     * Compares the Direct (baseline) and thin client result rows. When {@code ordered} is true the
     * sequence is significant (compared position-by-position); otherwise the rows are compared
     * order-insensitively (a cross-partition non-ORDER-BY query, where cross-partition / cross-page
     * ordering is undefined). See {@link #isStrictlyOrdered}.
     * <p>
     * If every row projects an {@code id} it is used as the comparison key (stable and cheap).
     * Otherwise (projection queries that do not select {@code id}, e.g. {@code SELECT <expr> AS x})
     * we do NOT drop the check and lose coverage - the full projected rows are compared instead,
     * with numeric fields matched within {@link #NUMERIC_TOLERANCE} to avoid float-formatting false
     * mismatches between the Direct and thin-client serialization paths.
     */
    private static void assertSameDocumentIds(List<ObjectNode> gwDocs, List<ObjectNode> tcDocs, boolean ordered, String desc) {
        assertThat(tcDocs.size()).as("Row count mismatch: " + desc).isEqualTo(gwDocs.size());

        boolean allHaveId = !gwDocs.isEmpty()
            && gwDocs.stream().allMatch(d -> d.has(ID_FIELD))
            && tcDocs.stream().allMatch(d -> d.has(ID_FIELD));

        if (allHaveId) {
            List<String> gwIds = gwDocs.stream().map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
            List<String> tcIds = tcDocs.stream().map(d -> d.get(ID_FIELD).asText()).collect(Collectors.toList());
            if (ordered) {
                assertThat(tcIds).as("IDs mismatch (ordered): " + desc).isEqualTo(gwIds);
            } else {
                assertThat(tcIds.stream().sorted().collect(Collectors.toList()))
                    .as("IDs mismatch (unordered): " + desc)
                    .isEqualTo(gwIds.stream().sorted().collect(Collectors.toList()));
            }
            return;
        }

        // No id projected - compare the full rows so ordering/content coverage is not lost.
        if (ordered) {
            for (int i = 0; i < gwDocs.size(); i++) {
                assertThat(jsonEqualsWithTolerance(tcDocs.get(i), gwDocs.get(i)))
                    .as("Row mismatch (ordered) at index " + i + ": " + desc
                        + " | expected=" + gwDocs.get(i) + " actual=" + tcDocs.get(i))
                    .isTrue();
            }
        } else {
            // Multiset comparison: each Direct row must have a distinct matching thin-client row.
            List<ObjectNode> remaining = new ArrayList<>(tcDocs);
            for (ObjectNode gwRow : gwDocs) {
                int matchIdx = -1;
                for (int j = 0; j < remaining.size(); j++) {
                    if (jsonEqualsWithTolerance(remaining.get(j), gwRow)) {
                        matchIdx = j;
                        break;
                    }
                }
                assertThat(matchIdx)
                    .as("Row not found in thin-client results (unordered): " + desc + " | row=" + gwRow)
                    .isGreaterThanOrEqualTo(0);
                remaining.remove(matchIdx);
            }
        }
    }

    /**
     * Cross-partition parity check against the dedicated multi-physical-partition container. Asserts
     * (1) thin client routing, (2) result parity with Direct (strict order for ORDER BY queries),
     * and (3) that the query actually fanned out across more than one partition key range - i.e. the
     * seeded data really does span multiple partitions.
     */
    private void assertCrossPartitionDirectAndThinClientMatch(String query) {
        CosmosQueryRequestOptions crossPartitionOptions = new CosmosQueryRequestOptions();
        QueryResult<ObjectNode> directResult = drainQuery(directCrossPartitionContainer, query, crossPartitionOptions, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientCrossPartitionContainer, query, crossPartitionOptions, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Count mismatch: " + query).isEqualTo(directResult.results.size());
        assertSameDocumentIds(directResult.results, tcResult.results, isStrictlyOrdered(query, crossPartitionOptions), query);

        // Prove the fixture data genuinely spans more than one physical partition (so this really is
        // a cross-partition query). Counted from the Direct diagnostics, which reliably report the
        // partition key range per request; the parity check above already proves the thin client
        // fanned out and merged all rows from those partitions correctly.
        assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
            .as("cross-partition query must contact more than one partition key range: " + query)
            .isGreaterThan(1);
    }

    /**
     * Cross-partition scalar-aggregate parity check. Runs a {@code SELECT VALUE <agg>} query with no
     * partition key on the options, so the thin client must fan out to every physical partition,
     * collect each partition's partial aggregate, and merge them into the single returned value -
     * exercising the cross-partition aggregate MERGE path that the single-partition aggregate tests
     * never reach. Asserts (1) thin client routing, (2) value parity with the Direct baseline within
     * numeric tolerance, and (3) that the Direct baseline genuinely fanned out across more than one
     * partition key range, proving the merge actually happened.
     */
    private <T> void assertCrossPartitionScalarMatch(String query, Class<T> resultType) {
        CosmosQueryRequestOptions crossPartitionOptions = new CosmosQueryRequestOptions();
        QueryResult<T> directResult = drainQuery(directCrossPartitionContainer, query, crossPartitionOptions, resultType);
        QueryResult<T> tcResult = drainQuery(thinClientCrossPartitionContainer, query, crossPartitionOptions, resultType);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Scalar count mismatch: " + query).isEqualTo(directResult.results.size());
        for (int i = 0; i < directResult.results.size(); i++) {
            assertScalarValueEquals(tcResult.results.get(i), directResult.results.get(i), i + ": " + query);
        }

        assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
            .as("cross-partition aggregate must contact more than one partition key range: " + query)
            .isGreaterThan(1);
    }

    /**
     * Cross-partition GROUP BY parity check. With no partition key on the options the thin client must
     * fan out to every physical partition and merge each partition's partial groups into the final
     * grouped result - exercising the cross-partition GROUP BY MERGE path. Because group/page ordering
     * across partitions is undefined, rows are compared as a set keyed on {@code groupField} (numeric
     * fields matched within tolerance). Asserts thin client routing, set parity with Direct, and
     * genuine multi-range fan-out on the Direct baseline.
     */
    private void assertCrossPartitionGroupByMatch(String query, String groupField) {
        CosmosQueryRequestOptions crossPartitionOptions = new CosmosQueryRequestOptions();
        QueryResult<ObjectNode> directResult = drainQuery(directCrossPartitionContainer, query, crossPartitionOptions, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientCrossPartitionContainer, query, crossPartitionOptions, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("GROUP BY count mismatch: " + query).isEqualTo(directResult.results.size());
        for (ObjectNode directRow : directResult.results) {
            String key = directRow.get(groupField).asText();
            boolean found = tcResult.results.stream().anyMatch(tc -> tc.get(groupField).asText().equals(key)
                && jsonEqualsWithTolerance(tc, directRow));
            assertThat(found).as("GROUP BY row not found in thin client results: " + key).isTrue();
        }

        assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
            .as("cross-partition GROUP BY must contact more than one partition key range: " + query)
            .isGreaterThan(1);
    }

    /**
     * Hierarchical (MULTI_HASH) cross-partition parity check. With no partition key on the options the
     * thin client fans out across the /tenantId,/userId container's physical partitions, forcing the
     * proxy to emit MULTI-COMPONENT PartitionKeyInternal arrays that drive the MULTI_HASH branch of the
     * range conversion. Asserts (1) thin client routing, (2) count parity, (3) row parity (ordered when
     * the query is strictly ordered), and (4) genuine multi-range fan-out on the Direct baseline.
     */
    private void assertHierarchicalCrossPartitionMatch(String query) {
        CosmosQueryRequestOptions crossPartitionOptions = new CosmosQueryRequestOptions();
        QueryResult<ObjectNode> directResult = drainQuery(directHierarchicalContainer, query, crossPartitionOptions, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientHierarchicalContainer, query, crossPartitionOptions, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(tcResult.results.size()).as("Hierarchical count mismatch: " + query).isEqualTo(directResult.results.size());
        assertSameDocumentIds(directResult.results, tcResult.results, isStrictlyOrdered(query, crossPartitionOptions), query);

        assertThat(distinctPartitionKeyRangesContacted(directResult.diagnostics))
            .as("hierarchical cross-partition query must contact more than one partition key range: " + query)
            .isGreaterThan(1);
    }

    /**
     * Hierarchical SCOPED parity check. Runs the query with a caller-supplied partition key on the
     * options - either the full two-component key (a single point range) or just the first component
     * (a half-open prefix range) - so the proxy emits the corresponding scoped PartitionKeyInternal
     * range. Asserts thin client routing, that the Direct baseline returns {@code expectedCount} rows,
     * and full row parity. No multi-range assertion: a scoped query may legitimately hit a single range.
     */
    private void assertHierarchicalScopedMatch(String query, CosmosQueryRequestOptions options, int expectedCount) {
        QueryResult<ObjectNode> directResult = drainQuery(directHierarchicalContainer, query, options, ObjectNode.class);
        QueryResult<ObjectNode> tcResult = drainQuery(thinClientHierarchicalContainer, query, options, ObjectNode.class);
        for (CosmosDiagnostics d : tcResult.diagnostics) { assertThinClientEndpointUsed(d); }

        assertThat(directResult.results.size()).as("Direct scoped count: " + query).isEqualTo(expectedCount);
        assertThat(tcResult.results.size()).as("Thin-client scoped count: " + query).isEqualTo(directResult.results.size());
        assertSameDocumentIds(directResult.results, tcResult.results, isStrictlyOrdered(query, options), query);
    }

    /** Counts the distinct partition key ranges contacted across all pages' diagnostics. */
    private static int distinctPartitionKeyRangesContacted(List<CosmosDiagnostics> diagnosticsList) {
        Set<String> ranges = new HashSet<>();
        for (CosmosDiagnostics diagnostics : diagnosticsList) {
            CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
            if (ctx == null) {
                continue;
            }
            for (CosmosDiagnosticsRequestInfo requestInfo : ctx.getRequestInfo()) {
                String pkRangeId = requestInfo.getPartitionKeyRangeId();
                if (pkRangeId != null && !pkRangeId.isEmpty()) {
                    ranges.add(pkRangeId);
                }
            }
        }
        return ranges.size();
    }

    /**
     * Compares scalar aggregate values, treating two numeric values as equal when they fall within
     * NUMERIC_TOLERANCE. Avoids false mismatches from floating-point formatting differences (e.g.
     * SUM/AVG) between the Direct and thin client paths; falls back to string equality otherwise.
     */
    private static <T> void assertScalarValueEquals(T tcValue, T gwValue, String desc) {
        String tcStr = String.valueOf(tcValue);
        String gwStr = String.valueOf(gwValue);
        Double tcNum = tryParseDouble(tcStr);
        Double gwNum = tryParseDouble(gwStr);
        if (tcNum != null && gwNum != null) {
            assertThat(Math.abs(tcNum - gwNum)).as("Scalar numeric mismatch at " + desc).isLessThan(NUMERIC_TOLERANCE);
        } else {
            assertThat(tcStr).as("Scalar value mismatch at " + desc).isEqualTo(gwStr);
        }
    }

    private static Double tryParseDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Recursively compares two JSON nodes, treating numeric leaves as equal when they fall within
     * NUMERIC_TOLERANCE. Prevents false GROUP BY mismatches caused by floating-point formatting
     * differences in aggregate values (e.g. SUM/AVG) between the Direct and thin client paths.
     */
    private static boolean jsonEqualsWithTolerance(JsonNode a, JsonNode b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.isNumber() && b.isNumber()) {
            return Math.abs(a.asDouble() - b.asDouble()) < NUMERIC_TOLERANCE;
        }
        if (a.isObject() && b.isObject()) {
            if (a.size() != b.size()) {
                return false;
            }
            Iterator<String> fieldNames = a.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                if (!b.has(field) || !jsonEqualsWithTolerance(a.get(field), b.get(field))) {
                    return false;
                }
            }
            return true;
        }
        if (a.isArray() && b.isArray()) {
            if (a.size() != b.size()) {
                return false;
            }
            for (int i = 0; i < a.size(); i++) {
                if (!jsonEqualsWithTolerance(a.get(i), b.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return a.equals(b);
    }
}
