/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosDatabaseContentResponseOnWriteTest extends TestSuiteBase {

    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    //  Currently Gateway and Direct TCP support minimal response feature.
    @Factory(dataProvider = "clientBuildersWithDirectTcpWithContentResponseOnWriteDisabled")
    public CosmosDatabaseContentResponseOnWriteTest(CosmosClientBuilder clientBuilder) {
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
        for (String dbId : databases) {
            safeDeleteSyncDatabase(client.getDatabase(dbId));
        }
        safeCloseSyncClient(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withContentResponseOnWriteDisabled() {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void readDatabase_withContentResponseOnWriteDisabled() throws Exception {
        CosmosDatabase database = client.getDatabase(createdDatabase.getId());
        CosmosDatabaseProperties properties = new CosmosDatabaseProperties(createdDatabase.getId());
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();

        CosmosDatabaseResponse read = database.read();
        validateDatabaseResponse(properties, read);

        CosmosDatabaseResponse read1 = database.read(options);
        validateDatabaseResponse(properties, read1);
    }

    private void validateDatabaseResponse(CosmosDatabaseProperties databaseDefinition, CosmosDatabaseResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(databaseDefinition.getId());

    }

}
