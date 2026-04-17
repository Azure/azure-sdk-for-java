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

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKey(
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

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKey(
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

        CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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

        CosmosPagedIterable<ObjectNode> results = multiHashContainer.readManyByPartitionKey(
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
            singlePkContainer.readManyByPartitionKey(pkValues, aggregateQuery, null, ObjectNode.class)
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
            singlePkContainer.readManyByPartitionKey(pkValues, orderByQuery, null, ObjectNode.class)
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
            singlePkContainer.readManyByPartitionKey(pkValues, distinctQuery, null, ObjectNode.class)
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
            singlePkContainer.readManyByPartitionKey(pkValues, groupByQuery, null, ObjectNode.class)
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
            singlePkContainer.readManyByPartitionKey(pkValues, groupByWithAggregateQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for GROUP BY with aggregate query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("GROUP BY");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void rejectsNullPartitionKeyList() {
        singlePkContainer.readManyByPartitionKey((List<PartitionKey>) null, ObjectNode.class);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void rejectsEmptyPartitionKeyList() {
        singlePkContainer.readManyByPartitionKey(new ArrayList<>(), ObjectNode.class)
            .stream().collect(Collectors.toList());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void rejectsOffsetQuery() {
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));
        SqlQuerySpec offsetQuery = new SqlQuerySpec("SELECT * FROM c OFFSET 0 LIMIT 10");

        try {
            singlePkContainer.readManyByPartitionKey(pkValues, offsetQuery, null, ObjectNode.class)
                .stream().collect(Collectors.toList());
            fail("Should have thrown IllegalArgumentException for OFFSET query");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("OFFSET");
        }
    }



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

            CosmosPagedIterable<ObjectNode> results = singlePkContainer.readManyByPartitionKey(pkValues, ObjectNode.class);
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
        com.azure.cosmos.models.CosmosReadManyRequestOptions options = new com.azure.cosmos.models.CosmosReadManyRequestOptions();
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

        CosmosPagedIterable<ReadManyByPartitionKeyPojo> results = singlePkContainer.readManyByPartitionKey(
            pkValues, options, ReadManyByPartitionKeyPojo.class);
        List<ReadManyByPartitionKeyPojo> resultList = results.stream().collect(Collectors.toList());

        assertThat(resultList).hasSize(3);
        assertThat(deserializeCount.get()).isEqualTo(3);
        assertThat(resultList.stream().map(item -> item.mypk)).containsOnly("pkOpts");

        cleanupContainer(singlePkContainer);
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
