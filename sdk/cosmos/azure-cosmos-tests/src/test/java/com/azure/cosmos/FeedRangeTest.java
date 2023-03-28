/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
public class FeedRangeTest extends TestSuiteBase {
    private CosmosClientBuilder cosmosClientBuilderUnderTest;
    private CosmosClient houseKeepingClient;
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "clientBuilders")
    public FeedRangeTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        cosmosClientBuilderUnderTest = getClientBuilder();
        houseKeepingClient = createGatewayHouseKeepingDocumentClient(false).buildClient();
        houseKeepingClient.createDatabase(preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(houseKeepingClient.getDatabase(preExistingDatabaseId));
        safeCloseSyncClient(houseKeepingClient);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void feedRange_RecreateContainerWithSameName() {
        String containerName = UUID.randomUUID().toString();
        String databaseName = preExistingDatabaseId;
        try(CosmosAsyncClient clientUnderTest = cosmosClientBuilderUnderTest.buildAsyncClient()) {
            for (int i = 0; i < 2; i++) {
                CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(containerName, "/PE_Name");
                houseKeepingClient.getDatabase(databaseName).createContainerIfNotExists(cosmosContainerProperties);

                List<FeedRange> rsp =
                    clientUnderTest.getDatabase(databaseName).getContainer(containerName).getFeedRanges().block();
                assertThat(rsp).isNotNull();
                assertThat(rsp).hasSize(1);

                houseKeepingClient.getDatabase(databaseName).getContainer(containerName).delete();
            }
        }
    }
}
