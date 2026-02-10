// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Change Feed using public APIs.
 */
public class ChangeFeedTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ChangeFeedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "changeFeedStartFromParam")
    public Object[][] changeFeedStartFromParam() {
        return new Object[][] {
            { "fromBeginning" },
            { "fromNow" }
        };
    }

    @Test(groups = { "query", "emulator" }, timeOut = TIMEOUT, dataProvider = "changeFeedStartFromParam")
    public void changeFeedWithPartitionKey(String startFrom) {
        String partitionKeyValue = UUID.randomUUID().toString();

        // Create some items
        List<TestObject> createdItems = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TestObject item = new TestObject(
                UUID.randomUUID().toString(),
                partitionKeyValue,
                Arrays.asList(),
                "prop" + i
            );
            createdCollection.createItem(item).block();
            createdItems.add(item);
        }

        // Query change feed
        CosmosChangeFeedRequestOptions options;
        if ("fromBeginning".equals(startFrom)) {
            options = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(partitionKeyValue)));
        } else {
            options = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forLogicalPartition(new PartitionKey(partitionKeyValue)));
        }

        List<JsonNode> results = createdCollection
            .queryChangeFeed(options, JsonNode.class)
            .byPage()
            .flatMapIterable(FeedResponse::getResults)
            .collectList()
            .block();

        if ("fromBeginning".equals(startFrom)) {
            assertThat(results).isNotNull();
            assertThat(results.size()).isGreaterThanOrEqualTo(createdItems.size());
        }
    }

    @Test(groups = { "query", "emulator" }, timeOut = TIMEOUT)
    public void changeFeedWithContinuationToken() {
        String partitionKeyValue = UUID.randomUUID().toString();

        // Get initial continuation token
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(partitionKeyValue)));

        Iterator<FeedResponse<JsonNode>> iterator = createdCollection
            .queryChangeFeed(options, JsonNode.class)
            .byPage()
            .toIterable()
            .iterator();

        String continuationToken = null;
        while (iterator.hasNext()) {
            FeedResponse<JsonNode> response = iterator.next();
            continuationToken = response.getContinuationToken();
        }
        assertThat(continuationToken).isNotNull();

        // Create an item
        TestObject item = new TestObject(
            UUID.randomUUID().toString(),
            partitionKeyValue,
            Arrays.asList(),
            "testProp"
        );
        createdCollection.createItem(item).block();

        // Query from continuation token
        CosmosChangeFeedRequestOptions optionsFromContinuation = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken);

        List<JsonNode> results = createdCollection
            .queryChangeFeed(optionsFromContinuation, JsonNode.class)
            .byPage()
            .flatMapIterable(FeedResponse::getResults)
            .collectList()
            .block();

        assertThat(results).isNotNull();
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
    }

    @Test(groups = { "query", "emulator" }, timeOut = TIMEOUT)
    public void changeFeedEntireContainer() {
        String partitionKeyValue1 = UUID.randomUUID().toString();
        String partitionKeyValue2 = UUID.randomUUID().toString();

        // Create items in different partitions
        TestObject item1 = new TestObject(UUID.randomUUID().toString(), partitionKeyValue1, Arrays.asList(), "prop1");
        TestObject item2 = new TestObject(UUID.randomUUID().toString(), partitionKeyValue2, Arrays.asList(), "prop2");
        createdCollection.createItem(item1).block();
        createdCollection.createItem(item2).block();

        // Query change feed for entire container
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        List<JsonNode> results = createdCollection
            .queryChangeFeed(options, JsonNode.class)
            .byPage()
            .flatMapIterable(FeedResponse::getResults)
            .collectList()
            .block();

        assertThat(results).isNotNull();
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
    }

    @Test(groups = { "query", "emulator" }, timeOut = TIMEOUT)
    public void changeFeedFromPointInTime() throws InterruptedException {
        String partitionKeyValue = UUID.randomUUID().toString();

        // Create initial items
        TestObject item1 = new TestObject(UUID.randomUUID().toString(), partitionKeyValue, Arrays.asList(), "before");
        createdCollection.createItem(item1).block();

        // Wait a moment to ensure point in time is distinguishable
        Thread.sleep(2000);
        Instant pointInTime = Instant.now();
        Thread.sleep(1000);

        // Create more items after point in time
        TestObject item2 = new TestObject(UUID.randomUUID().toString(), partitionKeyValue, Arrays.asList(), "after");
        createdCollection.createItem(item2).block();

        // Query change feed from point in time
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromPointInTime(pointInTime, FeedRange.forLogicalPartition(new PartitionKey(partitionKeyValue)));

        List<JsonNode> results = createdCollection
            .queryChangeFeed(options, JsonNode.class)
            .byPage()
            .flatMapIterable(FeedResponse::getResults)
            .collectList()
            .block();

        assertThat(results).isNotNull();
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
    }

    @BeforeClass(groups = { "query", "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ChangeFeedTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, CosmosDatabaseForTest.generateId());

        CosmosContainerProperties containerProperties = getCollectionDefinition();
        createdDatabase.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(10100),
            new CosmosContainerRequestOptions()
        ).block();
        createdCollection = createdDatabase.getContainer(containerProperties.getId());
    }

    @AfterClass(groups = { "query", "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
