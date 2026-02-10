// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for reading throughput (offers) using public APIs.
 */
public class ReadFeedOffersTest extends TestSuiteBase {

    private static final int FEED_TIMEOUT = 60000;
    private static final int INITIAL_THROUGHPUT = 10100;

    private final String databaseId = CosmosDatabaseForTest.generateId();
    private List<CosmosAsyncContainer> createdContainers = new ArrayList<>();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedOffersTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readThroughputForAllContainers() {
        // Verify we can read throughput for each container
        assertThat(createdContainers).hasSize(3);

        for (CosmosAsyncContainer container : createdContainers) {
            ThroughputResponse response = container.readThroughput().block();

            assertThat(response).isNotNull();
            assertThat(response.getProperties()).isNotNull();
            assertThat(response.getProperties().getManualThroughput()).isEqualTo(INITIAL_THROUGHPUT);
            assertThat(response.getMinThroughput()).isNotNull();
        }
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readThroughputRequestCharge() {
        CosmosAsyncContainer container = createdContainers.get(0);

        ThroughputResponse response = container.readThroughput().block();

        assertThat(response).isNotNull();
        assertThat(response.getRequestCharge()).isGreaterThan(0);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readThroughputActivityId() {
        CosmosAsyncContainer container = createdContainers.get(0);

        ThroughputResponse response = container.readThroughput().block();

        assertThat(response).isNotNull();
        assertThat(response.getActivityId()).isNotNull();
        assertThat(response.getActivityId()).isNotEmpty();
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedOffersTest() {
        client = getClientBuilder().buildAsyncClient();
        client.createDatabase(databaseId).block();
        createdDatabase = client.getDatabase(databaseId);

        for (int i = 0; i < 3; i++) {
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                "/mypk"
            );
            createdDatabase.createContainer(
                containerProperties,
                ThroughputProperties.createManualThroughput(INITIAL_THROUGHPUT),
                new CosmosContainerRequestOptions()
            ).block();
            createdContainers.add(createdDatabase.getContainer(containerProperties.getId()));
        }
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
