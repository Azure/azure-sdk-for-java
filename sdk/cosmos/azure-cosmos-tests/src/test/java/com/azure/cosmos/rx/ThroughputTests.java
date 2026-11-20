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

public class ThroughputTests extends TestSuiteBase{

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ThroughputTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "fast" }, timeOut = SETUP_TIMEOUT)
    public void before_ThroughputTests() {
        client = getClientBuilder().buildAsyncClient();
    }

    @Test(groups = { "fast" }, timeOut = CONTROL_PLANE_TIMEOUT)
    public void readReplaceAutoscaleThroughputDb() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        CosmosAsyncDatabase database = client.getDatabase(databaseName);
        int initalThroughput = 5000;
        ThroughputProperties properties = ThroughputProperties.createAutoscaledThroughput(initalThroughput);
        try {
            client.createDatabase(databaseName, properties).block();
            ThroughputResponse readThroughputResponse = database.readThroughput().block();
            assertThat(readThroughputResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(initalThroughput);
            String collectionId = UUID.randomUUID().toString();
            database.createContainer(collectionId, "/myPk").block();
            // Replace
            int tagetThroughput = 6000;
            properties = ThroughputProperties.createAutoscaledThroughput(tagetThroughput);
            ThroughputResponse replaceResponse = database.replaceThroughput(properties).block();
            assertThat(replaceResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(tagetThroughput);
        } finally {
            safeDeleteDatabase(database);
        }
    }

    @Test(groups = { "fast" }, timeOut = CONTROL_PLANE_TIMEOUT)
    public void readReplaceManualThroughputDb() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        CosmosAsyncDatabase database = client.getDatabase(databaseName);
        int initalThroughput = 5000;
        ThroughputProperties properties = ThroughputProperties.createManualThroughput(initalThroughput);
        try {
            client.createDatabase(databaseName, properties).block();
            ThroughputResponse readThroughputResponse = database.readThroughput().block();
            assertThat(readThroughputResponse.getProperties().getManualThroughput()).isEqualTo(initalThroughput);
            database.createContainer("testCol", "/myPk").block();
            int tagetThroughput = 6000;
            properties = ThroughputProperties.createManualThroughput(tagetThroughput);
            ThroughputResponse replaceResponse = database.replaceThroughput(properties).block();
            assertThat(replaceResponse.getProperties().getManualThroughput()).isEqualTo(tagetThroughput);
        } finally {
            safeDeleteDatabase(database);
        }
    }

    @Test(groups = {"fast"}, timeOut = CONTROL_PLANE_TIMEOUT)
    public void readReplaceManualThroughputCollection() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        CosmosAsyncDatabase database = client.getDatabase(databaseName);
        int initalThroughput = 5000;
        ThroughputProperties throughputProperties =
            ThroughputProperties.createManualThroughput(initalThroughput);
        try {
            client.createDatabase(databaseName).block();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties("testCol", "/myPk");
            database.createContainer(
                containerProperties,
                throughputProperties,
                new CosmosContainerRequestOptions()).block();
            CosmosAsyncContainer container = database.getContainer(containerProperties.getId());

            // Read
            ThroughputResponse readThroughputResponse = container.readThroughput().block();
            assertThat(readThroughputResponse.getProperties().getManualThroughput()).isEqualTo(initalThroughput);

            // Replace
            int tagetThroughput = 6000;
            throughputProperties = ThroughputProperties.createManualThroughput(tagetThroughput);
            ThroughputResponse replaceResponse = container.replaceThroughput(throughputProperties).block();
            assertThat(replaceResponse.getProperties().getManualThroughput()).isEqualTo(tagetThroughput);
        } finally {
            safeDeleteDatabase(database);
        }
    }

    @Test(groups = { "fast" }, timeOut = CONTROL_PLANE_TIMEOUT)
    public void readReplaceAutoscaleThroughputCollection() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        CosmosAsyncDatabase database = client.getDatabase(databaseName);
        int initalThroughput = 5000;
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(initalThroughput);
        try {
            client.createDatabase(databaseName).block();
            String collectionId = UUID.randomUUID().toString();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(collectionId, "/myPk");
            database.createContainer(
                containerProperties,
                throughputProperties,
                new CosmosContainerRequestOptions()).block();
            CosmosAsyncContainer container = database.getContainer(containerProperties.getId());

            // Read
            ThroughputResponse readThroughputResponse = container.readThroughput().block();
            assertThat(readThroughputResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(initalThroughput);

            // Replace
            int tagetThroughput = 6000;
            throughputProperties = ThroughputProperties.createAutoscaledThroughput(tagetThroughput);
            ThroughputResponse replaceResponse = container.replaceThroughput(throughputProperties).block();
            assertThat(replaceResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(tagetThroughput);
        } finally {
            safeDeleteDatabase(database);
        }
    }

    @AfterClass(groups = { "fast" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }
}
