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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for reading and replacing throughput (offers) using public APIs.
 */
public class OfferReadReplaceTest extends TestSuiteBase {

    private static final int INITIAL_THROUGHPUT = 10100;

    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuilders")
    public OfferReadReplaceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAndReplaceThroughput() {
        // Read throughput
        ThroughputResponse readResponse = container.readThroughput().block();
        assertThat(readResponse).isNotNull();
        assertThat(readResponse.getProperties()).isNotNull();
        int oldThroughput = readResponse.getProperties().getManualThroughput();
        assertThat(oldThroughput).isEqualTo(INITIAL_THROUGHPUT);

        // Replace throughput
        int newThroughput = oldThroughput + 100;
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(newThroughput);
        ThroughputResponse replaceResponse = container.replaceThroughput(throughputProperties).block();

        assertThat(replaceResponse).isNotNull();
        assertThat(replaceResponse.getProperties()).isNotNull();
        assertThat(replaceResponse.getProperties().getManualThroughput()).isEqualTo(newThroughput);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_OfferReadReplaceTest() {
        client = getClientBuilder().buildAsyncClient();
        client.createDatabase(databaseId).block();
        database = client.getDatabase(databaseId);

        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/mypk");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(INITIAL_THROUGHPUT);
        database.createContainer(containerProperties, throughputProperties, new CosmosContainerRequestOptions()).block();
        container = database.getContainer(containerId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
