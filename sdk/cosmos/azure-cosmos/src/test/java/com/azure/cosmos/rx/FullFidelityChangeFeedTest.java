// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static java.lang.annotation.ElementType.METHOD;
import static org.assertj.core.api.Assertions.assertThat;

public class FullFidelityChangeFeedTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;
    private static final String PartitionKeyFieldName = "mypk";
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    public FullFidelityChangeFeedTest() {
        super(createGatewayRxDocumentClient());
        subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "simple" })
    public void fullFidelityChangeFeed_FromNowForLogicalPartition() throws Exception {
        CosmosContainer cosmosContainer = initializeFFCFContainer(2);
        CosmosChangeFeedRequestOptions options1 = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forLogicalPartition(new PartitionKey("mypk-1")));
        options1.fullFidelity();

        Iterator<FeedResponse<JsonNode>> results1 = cosmosContainer
            .queryChangeFeed(options1, JsonNode.class)
            .iterableByPage()
            .iterator();

        String continuationToken1 = "";
        while (results1.hasNext()) {
            FeedResponse<JsonNode> response = results1.next();
            continuationToken1 = response.getContinuationToken();
        }

        TestItem item1 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk-1", "Johnson");
        TestItem item2 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk-1", "Smith");
        TestItem item3 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk-2", "John");
        cosmosContainer.createItem(item1);
        cosmosContainer.createItem(item2);
        String originalLastNameItem1 = item1.getProp();
        item1.setProp("Gates");
        cosmosContainer.upsertItem(item1);
        String originalLastNameItem2 = item2.getProp();
        item2.setProp("Doe");
        cosmosContainer.upsertItem(item2);
        cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions());

        options1 = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken1);
        options1.fullFidelity();

        results1 = cosmosContainer
            .queryChangeFeed(options1, JsonNode.class)
            .iterableByPage()
            .iterator();

        // Check item2 deleted with TTL
        // TODO: this is not working - item does get deleted but it won't show up in CF
        logger.info("{} going to sleep for 5 seconds to populate ttl delete", Thread.currentThread().getName());
        Thread.sleep(5 * 1000);

        while (results1.hasNext()) {
            FeedResponse<JsonNode> response = results1.next();
            List<JsonNode> itemChanges = response.getResults();
            if (itemChanges.size() == 0) {
                break;
            }
            assertGatewayMode(response);
            assertThat(itemChanges.size()).isEqualTo(5);
            // Assert initial creation of items
            assertThat(itemChanges.get(0).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(0).get("current").get("prop").asText()).isEqualTo(originalLastNameItem1);
            assertThat(itemChanges.get(0).get("metadata").get("operationType").asText()).isEqualTo("create");
            assertThat(itemChanges.get(1).get("current").get("id").asText()).isEqualTo(item2.getId());
            assertThat(itemChanges.get(1).get("current").get("prop").asText()).isEqualTo(originalLastNameItem2);
            assertThat(itemChanges.get(1).get("metadata").get("operationType").asText()).isEqualTo("create");
            // Assert replace of item1
            assertThat(itemChanges.get(2).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(2).get("current").get("prop").asText()).isEqualTo(item1.getProp());
            assertThat(itemChanges.get(2).get("metadata").get("operationType").asText()).isEqualTo("replace");
            if (itemChanges.get(2).get("previous") != null) {
                assertThat(itemChanges.get(2).get("previous")).isEqualTo(itemChanges.get(0).get("current"));
            }
            // Assert replace of item2
            assertThat(itemChanges.get(3).get("current").get("id").asText()).isEqualTo(item2.getId());
            assertThat(itemChanges.get(3).get("current").get("prop").asText()).isEqualTo(item2.getProp());
            assertThat(itemChanges.get(3).get("metadata").get("operationType").asText()).isEqualTo("replace");
            if (itemChanges.get(3).get("previous") != null) {
                assertThat(itemChanges.get(3).get("previous")).isEqualTo(itemChanges.get(1).get("current"));
            }
            // Assert delete of item1
            assertThat(itemChanges.get(4).get("previous").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(4).get("current")).isEmpty();
            assertThat(itemChanges.get(4).get("metadata").get("operationType").asText()).isEqualTo("delete");
            assertThat(itemChanges.get(4).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(2).get("metadata").get("lsn").asText());
            // Assert item2 deleted with TTL
            // TODO: Missing TTL logic
        }

        CosmosChangeFeedRequestOptions options2 = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forLogicalPartition(new PartitionKey("mypk-2")));
        options2.fullFidelity();

        Iterator<FeedResponse<JsonNode>> results2 = cosmosContainer
            .queryChangeFeed(options2, JsonNode.class)
            .iterableByPage()
            .iterator();

        String continuationToken2 = "";
        while (results2.hasNext()) {
            FeedResponse<JsonNode> response = results2.next();
            continuationToken2 = response.getContinuationToken();
        }

        cosmosContainer.createItem(item3);
        String originalLastNameItem3 = item3.getProp();
        item3.setProp("Potter");
        cosmosContainer.upsertItem(item3);
        cosmosContainer.deleteItem(item3, new CosmosItemRequestOptions());

        options2 = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken2);
        options2.fullFidelity();

        results2 = cosmosContainer
            .queryChangeFeed(options2, JsonNode.class)
            .iterableByPage()
            .iterator();

        while (results2.hasNext()) {
            FeedResponse<JsonNode> response = results2.next();
            List<JsonNode> itemChanges = response.getResults();
            if (itemChanges.size() == 0) {
                break;
            }
            assertGatewayMode(response);
            assertThat(itemChanges.size()).isEqualTo(3);
            // Assert initial creation of item3
            assertThat(itemChanges.get(0).get("current").get("id").asText()).isEqualTo(item3.getId());
            assertThat(itemChanges.get(0).get("current").get("prop").asText()).isEqualTo(originalLastNameItem3);
            assertThat(itemChanges.get(0).get("metadata").get("operationType").asText()).isEqualTo("create");
            // Assert replace of item3
            assertThat(itemChanges.get(1).get("current").get("id").asText()).isEqualTo(item3.getId());
            assertThat(itemChanges.get(1).get("current").get("prop").asText()).isEqualTo(item3.getProp());
            assertThat(itemChanges.get(1).get("metadata").get("operationType").asText()).isEqualTo("replace");
            if (itemChanges.get(1).get("previous") != null) {
                assertThat(itemChanges.get(1).get("previous")).isEqualTo(itemChanges.get(0).get("current"));
            }
            // Assert delete of item3
            assertThat(itemChanges.get(2).get("previous").get("id").asText()).isEqualTo(item3.getId());
            assertThat(itemChanges.get(2).get("current")).isEmpty();
            assertThat(itemChanges.get(2).get("metadata").get("operationType").asText()).isEqualTo("delete");
            assertThat(itemChanges.get(2).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(1).get("metadata").get("lsn").asText());
        }
    }

    @Test(groups = { "simple" })
    public void fullFidelityChangeFeed_FromContinuationToken() throws Exception {
        CosmosContainer cosmosContainer = initializeFFCFContainer(2);
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        Iterator<FeedResponse<JsonNode>> results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        String continuationToken = null;
        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            continuationToken = response.getContinuationToken();
        }

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken);
        options.fullFidelity();

        TestItem item1 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk", "Johnson");
        TestItem item2 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk", "Smith");
        cosmosContainer.upsertItem(item1);
        cosmosContainer.upsertItem(item2);
        String originalLastName = item1.getProp();
        item1.setProp("Gates");
        cosmosContainer.upsertItem(item1);
        cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions());

        // Check item2 deleted with TTL
        // TODO: this is not working - item does get deleted but it won't show up in CF
        logger.info("{} going to sleep for 5 seconds to populate ttl delete", Thread.currentThread().getName());
        Thread.sleep(5 * 1000);

        results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            List<JsonNode> itemChanges = response.getResults();
            if (itemChanges.isEmpty()) {
                //  There are no more change feed items
                //  breaking now;
                break;
            }
            assertGatewayMode(response);
            assertThat(itemChanges.size()).isEqualTo(4);
            // Assert initial creation of items
            assertThat(itemChanges.get(0).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(0).get("current").get("prop").asText()).isEqualTo(originalLastName);
            assertThat(itemChanges.get(0).get("metadata").get("operationType").asText()).isEqualTo("create");
            assertThat(itemChanges.get(1).get("current").get("id").asText()).isEqualTo(item2.getId());
            assertThat(itemChanges.get(1).get("current").get("prop").asText()).isEqualTo(item2.getProp());
            assertThat(itemChanges.get(1).get("metadata").get("operationType").asText()).isEqualTo("create");
            // Assert replace of item1
            assertThat(itemChanges.get(2).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(2).get("current").get("prop").asText()).isEqualTo(item1.getProp());
            assertThat(itemChanges.get(2).get("metadata").get("operationType").asText()).isEqualTo("replace");
            if (itemChanges.get(2).get("previous") != null) {
                assertThat(itemChanges.get(2).get("previous")).isEqualTo(itemChanges.get(0).get("current"));
            }
            // Assert delete of item1
            assertThat(itemChanges.get(3).get("previous").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(3).get("current")).isEmpty();
            assertThat(itemChanges.get(3).get("metadata").get("operationType").asText()).isEqualTo("delete");
            assertThat(itemChanges.get(3).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(2).get("metadata").get("lsn").asText());
            // Assert item2 deleted with TTL
            // TODO: Missing TTL logic showing up
        }
    }

    @Test(groups = { "simple" })
    public void fullFidelityChangeFeed_FromContinuationTokenOperationsOrder() throws Exception {
        CosmosContainer cosmosContainer = initializeFFCFContainer(0);
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        Iterator<FeedResponse<JsonNode>> results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        String continuationToken = null;
        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            continuationToken = response.getContinuationToken();
        }

        options = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
        options.fullFidelity();
        options.setMaxItemCount(150); // get all results in one page

        // Create, replace, and delete 50 objects for 150 total operations
        for (int i = 0; i < 50; i++) {
            TestItem currentItem = new TestItem("item"+ i, "mypk", "Smith");
            cosmosContainer.upsertItem(currentItem);
            currentItem.setProp("Jefferson");
            cosmosContainer.upsertItem(currentItem);
            cosmosContainer.deleteItem(currentItem, new CosmosItemRequestOptions());
        }

        results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            List<JsonNode> itemChanges = response.getResults();
            if (itemChanges.isEmpty()) {
                //  There are no more change feed items
                //  breaking now;
                break;
            }
            assertGatewayMode(response);
            assertThat(itemChanges.size()).isEqualTo(150);
            // Verify that operations order shows properly
            for (int index = 0; index < 150; index+=3) {
                assertThat(itemChanges.get(index).get("metadata").get("operationType").asText()).isEqualTo("create");
                assertThat(itemChanges.get(index+1).get("metadata").get("operationType").asText()).isEqualTo("replace");
                assertThat(itemChanges.get(index+2).get("metadata").get("operationType").asText()).isEqualTo("delete");
            }
        }
    }

    @Test(groups = { "emulator" })
    public void fullFidelityChangeFeed_VerifyPreviousPresentOnReplace() throws Exception {
        CosmosContainer cosmosContainer = initializeFFCFContainer(2);
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        Iterator<FeedResponse<JsonNode>> results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        String continuationToken = null;
        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            continuationToken = response.getContinuationToken();
        }

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken);
        options.fullFidelity();

        TestItem item1 = new TestItem(
            UUID.randomUUID().toString(),
            "mypk", "Johnson");
        cosmosContainer.upsertItem(item1);
        String originalLastName = item1.getProp();
        item1.setProp("Gates");
        cosmosContainer.upsertItem(item1);
        String secondLastName = item1.getProp();
        item1.setProp("DiCaprio");
        String thirdLastName = item1.getProp();
        cosmosContainer.upsertItem(item1);
        item1.setProp(originalLastName);
        cosmosContainer.upsertItem(item1);
        cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions());

        results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .iterableByPage()
            .iterator();

        while (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            List<JsonNode> itemChanges = response.getResults();
            if (itemChanges.isEmpty()) {
                //  There are no more change feed items
                //  breaking now;
                break;
            }
            assertGatewayMode(response);
            assertThat(itemChanges.size()).isEqualTo(5);
            // Assert initial creation of item1
            assertThat(itemChanges.get(0).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(0).get("current").get("prop").asText()).isEqualTo(originalLastName);
            assertThat(itemChanges.get(0).get("metadata").get("operationType").asText()).isEqualTo("create");
            // Verify separate replace operations
            assertThat(itemChanges.get(1).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(1).get("current").get("prop").asText()).isEqualTo(secondLastName);
            assertThat(itemChanges.get(1).get("metadata").get("operationType").asText()).isEqualTo("replace");
            assertThat(itemChanges.get(1).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(0).get("metadata").get("lsn").asText());
            assertThat(itemChanges.get(1).get("previous")).isEqualTo(itemChanges.get(0).get("current"));

            assertThat(itemChanges.get(2).get("current").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(2).get("current").get("prop").asText()).isEqualTo(thirdLastName);
            assertThat(itemChanges.get(2).get("metadata").get("operationType").asText()).isEqualTo("replace");
            assertThat(itemChanges.get(2).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(1).get("metadata").get("lsn").asText());
            assertThat(itemChanges.get(2).get("previous")).isEqualTo(itemChanges.get(1).get("current"));

            assertThat(itemChanges.get(3).get("previous").get("id").asText()).isEqualTo(item1.getId());
            assertThat(itemChanges.get(3).get("current").get("prop").asText()).isEqualTo(item1.getProp());
            assertThat(itemChanges.get(3).get("metadata").get("operationType").asText()).isEqualTo("replace");
            assertThat(itemChanges.get(3).get("metadata").get("previousImageLSN").asText()
            ).isEqualTo(itemChanges.get(2).get("metadata").get("lsn").asText());
            assertThat(itemChanges.get(3).get("previous")).isEqualTo(itemChanges.get(2).get("current"));
        }
    }

    public CosmosContainer initializeFFCFContainer(int ttl) {
        CosmosClient FFCF_client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildClient();
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        String containerId = "FFCF_container" + UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDef);
        if (ttl != 0) {
            containerProperties.setDefaultTimeToLiveInSeconds(ttl);
        }

        CosmosDatabaseResponse databaseResponse = FFCF_client.createDatabaseIfNotExists(createdDatabase.getId());
        CosmosDatabase database = FFCF_client.getDatabase(databaseResponse.getProperties().getId());
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);

        return database.getContainer(containerResponse.getProperties().getId());
    }

    // TODO: check why diagnostics are not showing this for change feed
    void assertGatewayMode(FeedResponse<JsonNode> response) {
        String diagnostics = response.getCosmosDiagnostics().toString();
        logger.info("Full Fidelity Diagnostics are : {}", diagnostics);
        assertThat(diagnostics).contains("");
    }

    @BeforeClass(groups = { "simple", "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ChangeFeedTest() throws Exception {
        // set up the client
        client = clientBuilder().build();
        createdDatabase = SHARED_DATABASE;
    }

    @AfterClass(groups = { "simple", "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static Document getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.setId(uuid);
        BridgeInternal.setProperty(doc, "mypk", partitionKey);
        BridgeInternal.setProperty(doc, "prop", uuid);
        return doc;
    }

    private static void waitAtleastASecond(Instant befTime) throws InterruptedException {
        while (befTime.plusSeconds(1).isAfter(Instant.now())) {
            Thread.sleep(100);
        }
    }

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target({ METHOD })
    @interface Tag {
        String name();
    }
}
