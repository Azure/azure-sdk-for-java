/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosDatabaseTest extends TestSuiteBase {
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosDatabaseTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosDatabaseTest() {
        client = getClientBuilder().buildClient();
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
    public void createDatabase_withPropertiesAndOptions() {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseDefinition,
                new CosmosDatabaseRequestOptions());

        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withProperties() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        assertThat(createResponse.getRequestCharge()).isGreaterThan(0);
        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_alreadyExists() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        validateDatabaseResponse(databaseDefinition, createResponse);
        try {
            client.createDatabase(databaseProperties);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withId() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosDatabaseResponse createResponse = client.createDatabase(databaseDefinition.getId());
        validateDatabaseResponse(databaseDefinition, createResponse);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withPropertiesThroughputAndOptions() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());
        CosmosDatabaseRequestOptions requestOptions = new CosmosDatabaseRequestOptions();
        int throughput = 400;
        try {
            CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties, ThroughputProperties.createManualThroughput(throughput), requestOptions);
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withPropertiesAndThroughput() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());
        int throughput = 1000;
        try {
            CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties, ThroughputProperties.createManualThroughput(throughput));
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (Exception ex) {
            if (ex instanceof CosmosException) {
                assertThat(((CosmosException) ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
            } else {
                throw ex;
            }
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createDatabase_withIdAndThroughput() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        int throughput = 1000;
        try {
            CosmosDatabaseResponse createResponse = client.createDatabase(databaseDefinition.getId(), ThroughputProperties.createManualThroughput(throughput));
            validateDatabaseResponse(databaseDefinition, createResponse);
        } catch (Exception ex) {
            if (ex instanceof CosmosException) {
                assertThat(((CosmosException) ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.FORBIDDEN);
            } else {
                throw ex;
            }
        }
    }

    @Test
    public void readDatabase() throws Exception {
        CosmosDatabase database = client.getDatabase(createdDatabase.getId());
        CosmosDatabaseProperties properties = new CosmosDatabaseProperties(createdDatabase.getId());
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();

        CosmosDatabaseResponse read = database.read();
        validateDatabaseResponse(properties, read);

        CosmosDatabaseResponse read1 = database.read(options);
        validateDatabaseResponse(properties, read1);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void readAllDatabases() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosDatabaseProperties> readIterator = client.readAllDatabases(options);
        // Basic validation
        assertThat(readIterator.iterableByPage(2).iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosDatabaseProperties> readIterator1 = client.readAllDatabases();
        // Basic validation
        assertThat(readIterator1.iterableByPage(2).iterator().hasNext()).isTrue();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void queryAllDatabases() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        String query = String.format("SELECT * from c where c.getId = '%s'", createdDatabase.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosDatabaseProperties> queryIterator = client.queryDatabases(query, options);
        // Basic validation
        assertThat(queryIterator.iterableByPage(2).iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosDatabaseProperties> queryIterator1 = client.queryDatabases(querySpec, options);
        // Basic validation
        assertThat(queryIterator1.iterableByPage(2).iterator().hasNext()).isTrue();
    }


    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());
        CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties);

        client.getDatabase(databaseDefinition.getId()).delete();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteDatabase_withOptions() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabaseProperties databaseProperties = new CosmosDatabaseProperties(databaseDefinition.getId());
        CosmosDatabaseResponse createResponse = client.createDatabase(databaseProperties);
        CosmosDatabaseRequestOptions options = new CosmosDatabaseRequestOptions();
        client.getDatabase(databaseDefinition.getId()).delete(options);
    }


    private void validateDatabaseResponse(CosmosDatabaseProperties databaseDefinition, CosmosDatabaseResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
                .as("check Resource Id")
                .isEqualTo(databaseDefinition.getId());

    }
}
