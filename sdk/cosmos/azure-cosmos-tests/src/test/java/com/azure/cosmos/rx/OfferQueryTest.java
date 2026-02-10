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
 * Tests for throughput (offer) operations using public APIs.
 */
public class OfferQueryTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;
    private static final int INITIAL_THROUGHPUT = 10100;

    private final String databaseId = CosmosDatabaseForTest.generateId();
    private List<CosmosAsyncContainer> createdContainers = new ArrayList<>();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @Factory(dataProvider = "clientBuilders")
    public OfferQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void readThroughputForContainer() {
        CosmosAsyncContainer container = createdContainers.get(0);

        ThroughputResponse response = container.readThroughput().block();

        assertThat(response).isNotNull();
        assertThat(response.getProperties()).isNotNull();
        assertThat(response.getProperties().getManualThroughput()).isEqualTo(INITIAL_THROUGHPUT);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void readThroughputForMultipleContainers() {
        // Read throughput for all created containers
        for (CosmosAsyncContainer container : createdContainers) {
            ThroughputResponse response = container.readThroughput().block();

            assertThat(response).isNotNull();
            assertThat(response.getProperties()).isNotNull();
            assertThat(response.getProperties().getManualThroughput()).isEqualTo(INITIAL_THROUGHPUT);
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void readThroughputForDatabase() {
        // Create a database with throughput
        String dbWithThroughputId = CosmosDatabaseForTest.generateId();
        client.createDatabase(dbWithThroughputId, ThroughputProperties.createManualThroughput(4000)).block();
        CosmosAsyncDatabase dbWithThroughput = client.getDatabase(dbWithThroughputId);

        try {
            ThroughputResponse response = dbWithThroughput.readThroughput().block();

            assertThat(response).isNotNull();
            assertThat(response.getProperties()).isNotNull();
            assertThat(response.getProperties().getManualThroughput()).isEqualTo(4000);
        } finally {
            safeDeleteDatabase(dbWithThroughput);
        }
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_OfferQueryTest() {
        client = getClientBuilder().buildAsyncClient();
        client.createDatabase(databaseId).block();
        database = client.getDatabase(databaseId);

        // Create multiple containers with throughput
        for (int i = 0; i < 3; i++) {
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                "/mypk"
            );
            database.createContainer(
                containerProperties,
                ThroughputProperties.createManualThroughput(INITIAL_THROUGHPUT),
                new CosmosContainerRequestOptions()
            ).block();
            createdContainers.add(database.getContainer(containerProperties.getId()));
        }
    }

    @AfterClass(groups = { "query" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
