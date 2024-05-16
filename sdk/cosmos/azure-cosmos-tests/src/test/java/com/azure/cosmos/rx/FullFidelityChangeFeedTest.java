// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FullFidelityChangeFeedTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncClient client;

    @Factory(dataProvider = "simpleClientBuildersWithDirectTcp")
    public FullFidelityChangeFeedTest(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void fullFidelityChangeFeed_FromNowForLogicalPartition() throws Exception {
        CosmosAsyncContainer cosmosContainer = initializeFFCFContainer();
        try {
            CosmosChangeFeedRequestOptions options1 = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forLogicalPartition(new PartitionKey("mypk-1")));
            options1.allVersionsAndDeletes();

            Iterator<FeedResponse<JsonNode>> results1 = cosmosContainer
                .queryChangeFeed(options1, JsonNode.class)
                .byPage()
                .toIterable()
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
            cosmosContainer.createItem(item1).block();
            cosmosContainer.createItem(item2).block();
            String originalLastNameItem1 = item1.getProp();
            item1.setProp("Gates");
            cosmosContainer.upsertItem(item1).block();
            String originalLastNameItem2 = item2.getProp();
            item2.setProp("Doe");
            cosmosContainer.upsertItem(item2).block();
            cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions()).block();

            options1 = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken1);
            options1.allVersionsAndDeletes();

            results1 = cosmosContainer
                .queryChangeFeed(options1, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            if (results1.hasNext()) {
                FeedResponse<JsonNode> response = results1.next();
                List<JsonNode> itemChanges = response.getResults();
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
            } else {
                fail("change feed missing results");
            }

            CosmosChangeFeedRequestOptions options2 = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forLogicalPartition(new PartitionKey("mypk-2")));
            options2.allVersionsAndDeletes();

            Iterator<FeedResponse<JsonNode>> results2 = cosmosContainer
                .queryChangeFeed(options2, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            String continuationToken2 = "";
            while (results2.hasNext()) {
                FeedResponse<JsonNode> response = results2.next();
                continuationToken2 = response.getContinuationToken();
            }

            cosmosContainer.createItem(item3).block();
            String originalLastNameItem3 = item3.getProp();
            item3.setProp("Potter");
            cosmosContainer.upsertItem(item3).block();
            cosmosContainer.deleteItem(item3, new CosmosItemRequestOptions()).block();

            options2 = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken2);
            options2.allVersionsAndDeletes();

            results2 = cosmosContainer
                .queryChangeFeed(options2, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            if (results2.hasNext()) {
                FeedResponse<JsonNode> response = results2.next();
                List<JsonNode> itemChanges = response.getResults();
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
            } else {
                fail("change feed missing results");
            }
        } finally {
            safeDeleteCollection(cosmosContainer);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void fullFidelityChangeFeed_FromContinuationToken() throws Exception {
        CosmosAsyncContainer cosmosContainer = initializeFFCFContainer();
        try {
            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange());
            options.allVersionsAndDeletes();

            Iterator<FeedResponse<JsonNode>> results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            String continuationToken = null;
            while (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                continuationToken = response.getContinuationToken();
            }

            options = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);
            options.allVersionsAndDeletes();

            TestItem item1 = new TestItem(
                UUID.randomUUID().toString(),
                "mypk", "Johnson");
            TestItem item2 = new TestItem(
                UUID.randomUUID().toString(),
                "mypk", "Smith");
            cosmosContainer.upsertItem(item1).block();
            cosmosContainer.upsertItem(item2).block();
            String originalLastName = item1.getProp();
            item1.setProp("Gates");
            cosmosContainer.upsertItem(item1).block();
            cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions()).block();

            results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            if (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                List<JsonNode> itemChanges = response.getResults();
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
            } else {
                fail("change feed missing results");
            }
        } finally {
            safeDeleteCollection(cosmosContainer);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void fullFidelityChangeFeed_FromContinuationTokenOperationsOrder() throws Exception {
        CosmosAsyncContainer cosmosContainer = initializeFFCFContainer();
        try {
            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange());
            options.allVersionsAndDeletes();

            Iterator<FeedResponse<JsonNode>> results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            String continuationToken = null;
            while (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                continuationToken = response.getContinuationToken();
            }

            options = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
            options.allVersionsAndDeletes();
            options.setMaxItemCount(150); // get all results in one page

            // Create, replace, and delete 50 objects for 150 total operations
            for (int i = 0; i < 50; i++) {
                TestItem currentItem = new TestItem("item"+ i, "mypk", "Smith");
                cosmosContainer.upsertItem(currentItem).block();
                currentItem.setProp("Jefferson");
                cosmosContainer.upsertItem(currentItem).block();
                cosmosContainer.deleteItem(currentItem, new CosmosItemRequestOptions()).block();
            }

            results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            if (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                List<JsonNode> itemChanges = response.getResults();
                assertThat(itemChanges.size()).isEqualTo(150);
                // Verify that operations order shows properly
                for (int index = 0; index < 150; index+=3) {
                    assertThat(itemChanges.get(index).get("metadata").get("operationType").asText()).isEqualTo("create");
                    assertThat(itemChanges.get(index+1).get("metadata").get("operationType").asText()).isEqualTo("replace");
                    assertThat(itemChanges.get(index+2).get("metadata").get("operationType").asText()).isEqualTo("delete");
                }
            } else {
                fail("change feed missing results");
            }
        } finally {
            safeDeleteCollection(cosmosContainer);
        }
    }

    // TODO: re-enable this test once pipeline emulator has these changes - currently only in preview
    @Test(groups = { "emulator" }, timeOut = TIMEOUT, enabled = false)
    public void fullFidelityChangeFeed_VerifyPreviousPresentOnReplace() throws Exception {
        CosmosAsyncContainer cosmosContainer = initializeFFCFContainer();
        try {
            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange());
            options.allVersionsAndDeletes();

            Iterator<FeedResponse<JsonNode>> results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            String continuationToken = null;
            while (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                continuationToken = response.getContinuationToken();
            }

            options = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);
            options.allVersionsAndDeletes();

            TestItem item1 = new TestItem(
                UUID.randomUUID().toString(),
                "mypk", "Johnson");
            cosmosContainer.upsertItem(item1).block();
            String originalLastName = item1.getProp();
            item1.setProp("Gates");
            cosmosContainer.upsertItem(item1).block();
            String secondLastName = item1.getProp();
            item1.setProp("DiCaprio");
            String thirdLastName = item1.getProp();
            cosmosContainer.upsertItem(item1).block();
            item1.setProp(originalLastName);
            cosmosContainer.upsertItem(item1).block();
            cosmosContainer.deleteItem(item1, new CosmosItemRequestOptions()).block();

            results = cosmosContainer
                .queryChangeFeed(options, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            if (results.hasNext()) {
                FeedResponse<JsonNode> response = results.next();
                List<JsonNode> itemChanges = response.getResults();
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
            } else {
                fail("change feed missing results");
            }
        } finally {
            safeDeleteCollection(cosmosContainer);
        }
    }

    public CosmosAsyncContainer initializeFFCFContainer() {
        CosmosContainerProperties cosmosContainerProperties = getCollectionDefinition();
        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofMinutes(5)));
        return createCollection(client, createdDatabase.getId(), cosmosContainerProperties);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ChangeFeedTest() throws Exception {
        // set up the client
        client = this.getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(this.client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
