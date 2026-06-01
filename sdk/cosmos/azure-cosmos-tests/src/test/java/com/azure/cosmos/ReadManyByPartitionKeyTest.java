/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ReadManyByPartitionKeyTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    // Single PK container (/mypk)
    private CosmosContainer singlePkContainer;

    // HPK container (/city, /zipcode, /areaCode)
    private CosmosContainer multiHashContainer;

    @Factory(dataProvider = "clientBuilders")
    public ReadManyByPartitionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_ReadManyByPartitionKeyTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);

        // Single PK container
        String singlePkContainerName = UUID.randomUUID().toString();
        CosmosContainerProperties singlePkProps = new CosmosContainerProperties(singlePkContainerName, "/mypk");
        createdDatabase.createContainer(singlePkProps);
        singlePkContainer = createdDatabase.getContainer(singlePkContainerName);

        // HPK container
        String multiHashContainerName = UUID.randomUUID().toString();
        PartitionKeyDefinition hpkDef = new PartitionKeyDefinition();
        hpkDef.setKind(PartitionKind.MULTI_HASH);
        hpkDef.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/city");
        paths.add("/zipcode");
        paths.add("/areaCode");
        hpkDef.setPaths(paths);

        CosmosContainerProperties hpkProps = new CosmosContainerProperties(multiHashContainerName, hpkDef);
        createdDatabase.createContainer(hpkProps);
        multiHashContainer = createdDatabase.getContainer(multiHashContainerName);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    //region Single PK tests

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_basic() {
        // Create items with different PKs
        List<ObjectNode> items = createSinglePkItems("pk1", 3);
        items.addAll(createSinglePkItems("pk2", 2));
        items.addAll(createSinglePkItems("pk3", 4));

        // Read by 2 partition keys
        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKey("pk1"),
            new PartitionKey("pk2"));

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(5); // 3 + 2
        resultList.forEach(item -> {
            String pk = item.get("mypk").asText();
            assertThat(pk).isIn("pk1", "pk2");
        });

        // Cleanup
        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withProjection() {
        List<ObjectNode> items = createSinglePkItems("pkProj", 2);

        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pkProj"));
        SqlQuerySpec customQuery = new SqlQuerySpec("SELECT c.id, c.mypk FROM c");

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(
            pkValues, customQuery, null, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(2);
        // Should only have id and mypk fields (plus system properties)
        resultList.forEach(item -> {
            assertThat(item.has("id")).isTrue();
            assertThat(item.has("mypk")).isTrue();
        });

        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withAdditionalFilter() {
        // Create items with different "status" values
        createSinglePkItemsWithStatus("pkFilter", "active", 3);
        createSinglePkItemsWithStatus("pkFilter", "inactive", 2);

        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pkFilter"));
        SqlQuerySpec customQuery = new SqlQuerySpec(
            "SELECT * FROM c WHERE c.status = @status",
            Arrays.asList(new SqlParameter("@status", "active")));

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(
            pkValues, customQuery, null, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(3);
        resultList.forEach(item -> {
            assertThat(item.get("status").asText()).isEqualTo("active");
        });

        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_emptyResults() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("nonExistent"));

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).isEmpty();
    }

    //endregion

    //region HPK tests

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpk_readManyByPartitionKey_fullPk() {
        createHpkItems();

        // Read by full PKs
        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").add("98053").add(1).build(),
            new PartitionKeyBuilder().add("Pittsburgh").add("15232").add(2).build());

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        // Redmond/98053/1 has 2 items, Pittsburgh/15232/2 has 1 item
        assertThat(resultList).hasSize(3);

        cleanupContainer(multiHashContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpk_readManyByPartitionKey_partialPk_singleLevel() {
        createHpkItems();

        // Read by partial PK (only city)
        List<PartitionKey> pkValues = Collections.singletonList(
            new PartitionKeyBuilder().add("Redmond").build());

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        // Redmond has 3 items total (2 with 98053/1 and 1 with 12345/1)
        assertThat(resultList).hasSize(3);
        resultList.forEach(item -> {
            assertThat(item.get("city").asText()).isEqualTo("Redmond");
        });

        cleanupContainer(multiHashContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpk_readManyByPartitionKey_partialPk_twoLevels() {
        createHpkItems();

        // Read by partial PK (city + zipcode)
        List<PartitionKey> pkValues = Collections.singletonList(
            new PartitionKeyBuilder().add("Redmond").add("98053").build());

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        // Redmond/98053 has 2 items
        assertThat(resultList).hasSize(2);
        resultList.forEach(item -> {
            assertThat(item.get("city").asText()).isEqualTo("Redmond");
            assertThat(item.get("zipcode").asText()).isEqualTo("98053");
        });

        cleanupContainer(multiHashContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("deprecation")
    public void hpk_readManyByPartitionKey_withNoneComponent() {
        try {
            createHpkItems();

            ObjectNode item = com.azure.cosmos.implementation.Utils.getSimpleObjectMapper().createObjectNode();
            item.put("id", UUID.randomUUID().toString());
            item.put("city", "Redmond");
            item.put("zipcode", "98053");

            try {
                multiHashContainer.createItem(item);
                fail("Should have thrown CosmosException for HPK item with missing trailing partition key component");
            } catch (CosmosException e) {
                assertThat(e.getMessage()).contains("wrong-pk-value");
            }

            try {
                new PartitionKeyBuilder().add("Redmond").add("98053").addNoneValue().build();
                fail("Should have thrown IllegalStateException for HPK addNoneValue");
            } catch (IllegalStateException e) {
                assertThat(e.getMessage()).contains("PartitionKey.None can't be used with multiple paths");
            }
        } finally {
            cleanupContainer(multiHashContainer);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpk_readManyByPartitionKey_withProjection() {
        createHpkItems();

        List<PartitionKey> pkValues = Collections.singletonList(
            new PartitionKeyBuilder().add("Redmond").add("98053").add(1).build());

        SqlQuerySpec customQuery = new SqlQuerySpec("SELECT c.id, c.city FROM c");

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKeys(
            pkValues, customQuery, null, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(2);

        cleanupContainer(multiHashContainer);
    }

    //endregion

    //region Negative/validation tests

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsAggregateQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec aggregateQuery = new SqlQuerySpec("SELECT COUNT(1) FROM c");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, aggregateQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for aggregate query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("aggregates");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsOrderByQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec orderByQuery = new SqlQuerySpec("SELECT * FROM c ORDER BY c.id");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, orderByQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for ORDER BY query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("ORDER BY");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsDistinctQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec distinctQuery = new SqlQuerySpec("SELECT DISTINCT c.mypk FROM c");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, distinctQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for DISTINCT query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("DISTINCT");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsGroupByQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec groupByQuery = new SqlQuerySpec("SELECT c.mypk FROM c GROUP BY c.mypk");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, groupByQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for GROUP BY query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("GROUP BY");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsGroupByWithAggregateQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec groupByWithAggregateQuery = new SqlQuerySpec("SELECT c.mypk, COUNT(1) as cnt FROM c GROUP BY c.mypk");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, groupByWithAggregateQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for GROUP BY with aggregate query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("GROUP BY");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT, expectedExceptions = NullPointerException.class)
    public void rejectsNullPartitionKeyList() {
        singlePkContainer.readManyByPartitionKeys((List<PartitionKey>) null, ObjectNode.class);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void rejectsEmptyPartitionKeyList() {
        singlePkContainer.readManyByPartitionKeys(new ArrayList<>(), ObjectNode.class)
            .stream().collect(Collectors.toList());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsOffsetQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec offsetQuery = new SqlQuerySpec("SELECT * FROM c OFFSET 0 LIMIT 10");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, offsetQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for OFFSET query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("OFFSET");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsTopQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec topQuery = new SqlQuerySpec("SELECT TOP 5 * FROM c");

        try {
            singlePkContainer.readManyByPartitionKeys(pkValues, topQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for TOP query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("TOP");
        }
    }

    // DCOUNT, standalone LIMIT, and hybrid/vector/full-text search cannot be tested against the
    // emulator: DCOUNT is not recognized as a built-in function, standalone LIMIT is not valid
    // Cosmos SQL syntax (only valid with OFFSET, already covered by rejectsOffsetQuery), and
    // hybrid search requires vector indexes. All three are covered by unit tests in
    // ReadManyByPartitionKeyQueryPlanValidationTest (rejectsDCountQueryPlan, rejectsLimitQueryPlan,
    // rejectsHybridSearchQueryPlanWithoutDereferencingNullQueryInfo).



    //endregion


    //region Batch size tests (#10)

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withSmallBatchSize() {
        // Temporarily set batch size to 2 to exercise the batching/interleaving logic
        String originalValue = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "2");

            // Create items across 4 PKs (more than the batch size of 2)
            List<ObjectNode> items = createSinglePkItems("batchPk1", 2);
            items.addAll(createSinglePkItems("batchPk2", 2));
            items.addAll(createSinglePkItems("batchPk3", 2));
            items.addAll(createSinglePkItems("batchPk4", 2));

            // Read all 4 PKs — should be split into batches of 2
            List<PartitionKey> pkValues = Arrays.asList(
                new PartitionKey("batchPk1"),
                new PartitionKey("batchPk2"),
                new PartitionKey("batchPk3"),
                new PartitionKey("batchPk4"));

            CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(pkValues, ObjectNode.class);
            List<FeedResponse<ObjectNode>> pages = new ArrayList<>();
            results.iterableByPage().forEach(pages::add);
            List<ObjectNode> resultList = pages.stream()
                .flatMap(page -> page.getResults().stream())
                .collect(Collectors.toList());

            assertThat(resultList).hasSize(8); // 2 items per PK * 4 PKs
            assertThat(pages.size()).isGreaterThan(1);
            resultList.forEach(item -> {
                String pk = item.get("mypk").asText();
                assertThat(pk).isIn("batchPk1", "batchPk2", "batchPk3", "batchPk4");
            });

            cleanupContainer(singlePkContainer);
        } finally {
            if (originalValue != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalValue);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }

    //endregion

    //region Custom serializer regression tests (#5)

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withRequestOptions() {
        List<ObjectNode> items = createSinglePkItems("pkOpts", 3);

        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pkOpts"));
        com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options = new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
        AtomicInteger deserializeCount = new AtomicInteger();
        options.setCustomItemSerializer(new CosmosItemSerializerNoExceptionWrapping() {
            @Override
            public <T> Map<String, Object> serialize(T item) {
                return CosmosItemSerializer.DEFAULT_SERIALIZER.serialize(item);
            }

            @Override
            public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
                deserializeCount.incrementAndGet();
                return CosmosItemSerializer.DEFAULT_SERIALIZER.deserialize(jsonNodeMap, classType);
            }
        });

        CosmosPagedIterable<ReadManyByPartitionKeyPojo> results = singlePkContainer.readManyByPartitionKeys(
            pkValues, options, ReadManyByPartitionKeyPojo.class);
        List<ReadManyByPartitionKeyPojo> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(3);
        assertThat(deserializeCount.get()).isEqualTo(3);
        assertThat(resultList.stream().map(item -> item.mypk)).containsOnly("pkOpts");

        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withRequestOptionsAndMaxConcurrentBatchPrefetch() {
        // Regression test: passing non-null CosmosReadManyByPartitionKeysRequestOptions
        // with maxConcurrentBatchPrefetch set should not throw NullPointerException
        // from auto-unboxing a null MaxDegreeOfParallelism during options cloning.
        List<ObjectNode> items = createSinglePkItems("pkMdop", 3);

        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pkMdop"));
        com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options =
            new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
        options.setMaxConcurrentBatchPrefetch(2);

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(
            pkValues, options, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(3);
        resultList.forEach(item -> {
            assertThat(item.get("mypk").asText()).isEqualTo("pkMdop");
        });

        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_readManyByPartitionKey_withRequestOptionsAndMaxBatchSize() {
        // Exercises the per-request maxBatchSize override (precedence over global default).
        // Use batch size of 1 so every PK ends up in its own batch — verifies results
        // are still correctly assembled from many small batches.
        List<ObjectNode> items = createSinglePkItems("batchSzPk1", 2);
        items.addAll(createSinglePkItems("batchSzPk2", 2));
        items.addAll(createSinglePkItems("batchSzPk3", 2));

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKey("batchSzPk1"),
            new PartitionKey("batchSzPk2"),
            new PartitionKey("batchSzPk3"));

        com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options =
            new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
        options.setMaxBatchSize(1);

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKeys(
            pkValues, options, ObjectNode.class);
        List<ObjectNode> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(6);
        resultList.forEach(item -> {
            assertThat(item.get("mypk").asText()).isIn("batchSzPk1", "batchSzPk2", "batchSzPk3");
        });

        cleanupContainer(singlePkContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void singlePk_readManyByPartitionKey_setMaxBatchSizeZeroThrows() {
        com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options =
            new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
        options.setMaxBatchSize(0); // must throw IllegalArgumentException
    }
    //endregion

    //region Continuation token tests

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_sequentialExecution_emitsContinuationTokens() {
        // Use small batch size so we get multiple batches (and thus multiple continuation tokens)
        String originalValue = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "2");

            // Create items across 3 PKs
            createSinglePkItems("seqPk1", 3);
            createSinglePkItems("seqPk2", 3);
            createSinglePkItems("seqPk3", 3);

            List<PartitionKey> pkValues = Arrays.asList(
                new PartitionKey("seqPk1"),
                new PartitionKey("seqPk2"),
                new PartitionKey("seqPk3"));

            // Use the async container to collect FeedResponse pages with continuation tokens
            CosmosAsyncContainer asyncContainer = client.asyncClient()
                .getDatabase(preExistingDatabaseId)
                .getContainer(singlePkContainer.getId());

            List<FeedResponse<ObjectNode>> pages = asyncContainer
                .readManyByPartitionKeys(pkValues, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(pages).isNotNull();
            assertThat(pages).isNotEmpty();

            // All non-final pages should have a non-null continuation token
            for (int i = 0; i < pages.size() - 1; i++) {
                assertThat(pages.get(i).getContinuationToken())
                    .as("Page %d should have a continuation token", i)
                    .isNotNull()
                    .isNotEmpty();
            }

            // The final page should have null continuation
            assertThat(pages.get(pages.size() - 1).getContinuationToken())
                .as("Last page should have null continuation token")
                .isNull();

            // Total items should be 9
            long totalItems = pages.stream()
                .mapToLong(p -> p.getResults().size())
                .sum();
            assertThat(totalItems).isEqualTo(9);

            cleanupContainer(singlePkContainer);
        } finally {
            if (originalValue != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalValue);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_continuationToken_resumesCorrectly() {
        // Use small batch size to force multiple batches
        String originalValue = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "1");

            // Create items across 3 PKs
            createSinglePkItems("resumePk1", 2);
            createSinglePkItems("resumePk2", 2);
            createSinglePkItems("resumePk3", 2);

            List<PartitionKey> pkValues = Arrays.asList(
                new PartitionKey("resumePk1"),
                new PartitionKey("resumePk2"),
                new PartitionKey("resumePk3"));

            CosmosAsyncContainer asyncContainer = client.asyncClient()
                .getDatabase(preExistingDatabaseId)
                .getContainer(singlePkContainer.getId());

            // First pass: collect all items
            List<FeedResponse<ObjectNode>> allPages = asyncContainer
                .readManyByPartitionKeys(pkValues, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(allPages).isNotNull();
            assertThat(allPages.size()).isGreaterThan(1);

            // Pick a continuation token from the first page
            String continuationAfterFirstPage = allPages.get(0).getContinuationToken();
            assertThat(continuationAfterFirstPage).isNotNull();
            List<ObjectNode> itemsFromFirstPage = allPages.get(0).getResults();

            // Second pass: resume from the continuation token
            com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options2 =
                new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
            options2.setContinuationToken(continuationAfterFirstPage);

            List<FeedResponse<ObjectNode>> remainingPages = asyncContainer
                .readManyByPartitionKeys(pkValues, options2, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(remainingPages).isNotNull();

            // Collect all item ids
            List<String> firstPageIds = itemsFromFirstPage.stream()
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());
            List<String> remainingIds = remainingPages.stream()
                .flatMap(p -> p.getResults().stream())
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());
            List<String> allIds = allPages.stream()
                .flatMap(p -> p.getResults().stream())
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());

            // Items from first page + remaining should equal all items (no overlap, no gap)
            List<String> combined = new ArrayList<>(firstPageIds);
            combined.addAll(remainingIds);

            assertThat(combined).hasSameElementsAs(allIds);

            // No duplicates
            assertThat(combined).doesNotHaveDuplicates();

            cleanupContainer(singlePkContainer);
        } finally {
            if (originalValue != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalValue);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }


    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void singlePk_continuationToken_resumesCorrectly_whenInputContainsDuplicatePartitionKeys() {
        String originalValue = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "1");

            createSinglePkItems("dupResumePk1", 2);
            createSinglePkItems("dupResumePk2", 2);
            createSinglePkItems("dupResumePk3", 2);

            List<PartitionKey> pkValues = Arrays.asList(
                new PartitionKey("dupResumePk2"),
                new PartitionKey("dupResumePk1"),
                new PartitionKey("dupResumePk2"),
                new PartitionKey("dupResumePk3"),
                new PartitionKey("dupResumePk1"));

            CosmosAsyncContainer asyncContainer = client.asyncClient()
                .getDatabase(preExistingDatabaseId)
                .getContainer(singlePkContainer.getId());

            List<FeedResponse<ObjectNode>> allPages = asyncContainer
                .readManyByPartitionKeys(pkValues, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(allPages).isNotNull();
            assertThat(allPages.size()).isGreaterThan(1);

            String continuationAfterFirstPage = allPages.get(0).getContinuationToken();
            assertThat(continuationAfterFirstPage).isNotNull();
            List<ObjectNode> itemsFromFirstPage = allPages.get(0).getResults();

            com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions options2 =
                new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
            options2.setContinuationToken(continuationAfterFirstPage);

            List<FeedResponse<ObjectNode>> remainingPages = asyncContainer
                .readManyByPartitionKeys(pkValues, options2, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(remainingPages).isNotNull();

            List<String> firstPageIds = itemsFromFirstPage.stream()
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());
            List<String> remainingIds = remainingPages.stream()
                .flatMap(p -> p.getResults().stream())
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());
            List<String> allIds = allPages.stream()
                .flatMap(p -> p.getResults().stream())
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());

            List<String> combined = new ArrayList<>(firstPageIds);
            combined.addAll(remainingIds);

            assertThat(combined).hasSameElementsAs(allIds);
            assertThat(combined).doesNotHaveDuplicates();
            assertThat(combined).hasSize(6);

            cleanupContainer(singlePkContainer);
        } finally {
            if (originalValue != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalValue);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }

    //endregion

    //region Continuation-token resume correctness tests

    @Test(groups = {"emulator"}, timeOut = TIMEOUT * 3)
    public void singlePk_continuationToken_resumeAtEveryPageBoundary_noLossNoDuplicates() {
        // Validates that resuming from ANY page boundary produces a complete, duplicate-free
        // result set when combined with the items from earlier pages.
        String originalValue = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "2");

            createSinglePkItems("resumePk1", 4);
            createSinglePkItems("resumePk2", 4);
            createSinglePkItems("resumePk3", 4);
            createSinglePkItems("resumePk4", 4);

            List<PartitionKey> pkValues = Arrays.asList(
                new PartitionKey("resumePk1"),
                new PartitionKey("resumePk2"),
                new PartitionKey("resumePk3"),
                new PartitionKey("resumePk4"));

            CosmosAsyncContainer asyncContainer = client.asyncClient()
                .getDatabase(preExistingDatabaseId)
                .getContainer(singlePkContainer.getId());

            // Collect all pages in a single pass
            List<FeedResponse<ObjectNode>> allPages = asyncContainer
                .readManyByPartitionKeys(pkValues, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            assertThat(allPages).isNotNull();
            assertThat(allPages.size()).isGreaterThan(1);

            List<String> allIds = allPages.stream()
                .flatMap(p -> p.getResults().stream())
                .map(n -> n.get("id").asText())
                .collect(Collectors.toList());
            assertThat(allIds).hasSize(16);
            assertThat(allIds).doesNotHaveDuplicates();

            // For each page boundary, resume from that boundary's continuation token
            // and verify: items before + items after = all items, no duplicates
            for (int splitAt = 0; splitAt < allPages.size() - 1; splitAt++) {
                String continuation = allPages.get(splitAt).getContinuationToken();
                if (continuation == null) {
                    continue; // last page has null continuation
                }

                // Items from pages 0..splitAt
                List<String> beforeIds = new ArrayList<>();
                for (int p = 0; p <= splitAt; p++) {
                    allPages.get(p).getResults().forEach(n -> beforeIds.add(n.get("id").asText()));
                }

                // Resume from continuation
                com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions resumeOptions =
                    new com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions();
                resumeOptions.setContinuationToken(continuation);

                List<ObjectNode> resumedResults = asyncContainer
                    .readManyByPartitionKeys(pkValues, resumeOptions, ObjectNode.class)
                    .byPage()
                    .flatMapIterable(FeedResponse::getResults)
                    .collectList()
                    .block();

                assertThat(resumedResults).isNotNull();
                List<String> afterIds = resumedResults.stream()
                    .map(n -> n.get("id").asText())
                    .collect(Collectors.toList());

                List<String> combined = new ArrayList<>(beforeIds);
                combined.addAll(afterIds);

                assertThat(combined)
                    .as("Resume at page boundary %d should produce all items", splitAt)
                    .doesNotHaveDuplicates();
                assertThat(combined)
                    .as("Resume at page boundary %d should cover all 16 items", splitAt)
                    .hasSameElementsAs(allIds);
            }

            cleanupContainer(singlePkContainer);
        } finally {
            if (originalValue != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalValue);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }

    //endregion

    //region helper methods

    private List<ObjectNode> createSinglePkItems(String pkValue, int count) {
        List<ObjectNode> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ObjectNode item = com.azure.cosmos.implementation.Utils.getSimpleObjectMapper().createObjectNode();
            item.put("id", UUID.randomUUID().toString());
            item.put("mypk", pkValue);
            singlePkContainer.createItem(item);
            items.add(item);
        }
        return items;
    }

    private List<ObjectNode> createSinglePkItemsWithStatus(String pkValue, String status, int count) {
        List<ObjectNode> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ObjectNode item = com.azure.cosmos.implementation.Utils.getSimpleObjectMapper().createObjectNode();
            item.put("id", UUID.randomUUID().toString());
            item.put("mypk", pkValue);
            item.put("status", status);
            singlePkContainer.createItem(item);
            items.add(item);
        }
        return items;
    }

    private void createHpkItems() {
        // Same data as CosmosMultiHashTest.createItems()
        createHpkItem("Redmond", "98053", 1);
        createHpkItem("Redmond", "98053", 1);
        createHpkItem("Pittsburgh", "15232", 2);
        createHpkItem("Stonybrook", "11790", 3);
        createHpkItem("Stonybrook", "11794", 3);
        createHpkItem("Stonybrook", "11791", 3);
        createHpkItem("Redmond", "12345", 1);
    }

    private void createHpkItem(String city, String zipcode, int areaCode) {
        ObjectNode item = com.azure.cosmos.implementation.Utils.getSimpleObjectMapper().createObjectNode();
        item.put("id", UUID.randomUUID().toString());
        item.put("city", city);
        item.put("zipcode", zipcode);
        item.put("areaCode", areaCode);
        multiHashContainer.createItem(item);
    }

    private void cleanupContainer(CosmosContainer container) {
        CosmosPagedIterable<ObjectNode> allItems = container.queryItems(
            "SELECT * FROM c", new com.azure.cosmos.models.CosmosQueryRequestOptions(), ObjectNode.class);
        allItems.forEach(item -> {
            try {
                container.deleteItem(item, new CosmosItemRequestOptions());
            } catch (CosmosException e) {
                // ignore cleanup failures
            }
        });
    }

    private static class ReadManyByPartitionKeyPojo {
        public String id;
        public String mypk;
    }

    //endregion
}
