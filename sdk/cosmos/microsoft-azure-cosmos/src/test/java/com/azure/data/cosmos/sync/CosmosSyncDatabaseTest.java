/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosDatabaseProperties;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncDatabaseTest extends TestSuiteBase {
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosSyncClient client;
    private CosmosSyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncDatabaseTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildSyncClient();
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
    public void createDatabase_withPropertiesAndOptions() throws CosmosClientException {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.id());

        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseDefinition,
                new CosmosDatabaseRequestOptions());

        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withProperties() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.id());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());

        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_alreadyExists() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.id());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());

        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        validateDatabaseResponse(databaseDefinition, createResponse);
        try {
            client.createDatabase(databaseProperties);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withId() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());

        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseDefinition.id());
        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withPropertiesThroughputAndOptions() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());
        CosmosDatabaseRequestOptions requestOptions = new CosmosDatabaseRequestOptions();
        int throughput = 400;
        try {
            CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties, throughput, requestOptions);
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (CosmosClientException ex) {
            assertThat(ex.statusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withPropertiesAndThroughput() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());
        int throughput = 1000;
        try {
            CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties, throughput);
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (Exception ex) {
            if (ex instanceof CosmosClientException) {
                assertThat(((CosmosClientException) ex).statusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
            } else {
                throw ex;
            }
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withIdAndThroughput() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        int throughput = 1000;
        try {
            CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseDefinition.id(), throughput);
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (Exception ex) {
            if (ex instanceof CosmosClientException) {
                assertThat(((CosmosClientException) ex).statusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
            } else {
                throw ex;
            }
        }
    }

    @Test
    public void readDatabase() throws Exception {
        CosmosSyncDatabase database = client.getDatabase(createdDatabase.id());
        CosmosDatabaseProperties properties = new CosmosDatabaseProperties(createdDatabase.id());
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();

        CosmosSyncDatabaseResponse read = database.read();
        validateDatabaseResponse(properties, read);

        CosmosSyncDatabaseResponse read1 = database.read(options);
        validateDatabaseResponse(properties, read1);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void readAllDatabases() throws Exception {
        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Iterator<FeedResponse<CosmosDatabaseProperties>> readIterator = client.readAllDatabases(options);
        // Basic validation
        assertThat(readIterator.hasNext()).isTrue();

        Iterator<FeedResponse<CosmosDatabaseProperties>> readIterator1 = client.readAllDatabases();
        // Basic validation
        assertThat(readIterator1.hasNext()).isTrue();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void queryAllDatabases() throws Exception {
        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        String query = String.format("SELECT * from c where c.id = '%s'", createdDatabase.id());
        FeedOptions feedOptions = new FeedOptions();

        Iterator<FeedResponse<CosmosDatabaseProperties>> queryIterator = client.queryDatabases(query, options);
        // Basic validation
        assertThat(queryIterator.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosDatabaseProperties>> queryIterator1 = client.queryDatabases(querySpec, options);
        // Basic validation
        assertThat(queryIterator1.hasNext()).isTrue();
    }


    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());
        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties);

        client.getDatabase(databaseDefinition.id()).delete();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteDatabase_withOptions() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.id());
        CosmosSyncDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        client.getDatabase(databaseDefinition.id()).delete(options);
    }


    private void validateDatabaseResponse(CosmosDatabaseProperties databaseDefinition, CosmosSyncDatabaseResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
                .as("check Resource Id")
                .isEqualTo(databaseDefinition.id());

    }
}
