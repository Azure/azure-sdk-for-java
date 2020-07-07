/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerContentResponseOnWriteTest extends TestSuiteBase {

    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    //  Currently Gateway and Direct TCP support minimal response feature.
    @Factory(dataProvider = "clientBuildersWithDirectTcpWithContentResponseOnWriteDisabled")
    public CosmosContainerContentResponseOnWriteTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withContentResponseOnWriteDisabled() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readContainer_withContentResponseOnWriteDisabled() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        CosmosContainerResponse read = syncContainer.read();
        validateContainerResponse(containerProperties, read);

        CosmosContainerResponse read1 = syncContainer.read(options);
        validateContainerResponse(containerProperties, read1);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replaceContainer_withContentResponseOnWriteDisabled() throws Exception {

        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        CosmosContainerResponse replaceResponse = createdDatabase.getContainer(containerProperties.getId())
                                                                   .replace(containerResponse.getProperties().setIndexingPolicy(
                                                                       new IndexingPolicy().setIndexingMode(IndexingMode.CONSISTENT)));
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);

        CosmosContainerResponse replaceResponse1 = createdDatabase.getContainer(containerProperties.getId())
                                                                    .replace(containerResponse.getProperties().setIndexingPolicy(
                                                                        new IndexingPolicy().setIndexingMode(IndexingMode.CONSISTENT)),
                                                                        options);
        assertThat(replaceResponse1.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);

    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }

}
