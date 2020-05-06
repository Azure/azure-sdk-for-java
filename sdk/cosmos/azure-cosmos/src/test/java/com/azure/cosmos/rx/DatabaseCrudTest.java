// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseCrudTest extends TestSuiteBase {
    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public DatabaseCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        // create the database
        Mono<CosmosAsyncDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
                .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase_AlreadyExists() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();

        // attempt to create the database
        Mono<CosmosAsyncDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Mono<CosmosAsyncDatabaseResponse> readObservable = client.getDatabase(preExistingDatabaseId).read();

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
                .withId(preExistingDatabaseId).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase_DoesntExist() throws Exception {
        // read database
        Mono<CosmosAsyncDatabaseResponse> readObservable = client.getDatabase("I don't exist").read();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        // create the database
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosAsyncDatabase database = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().getDatabase();

        // delete the database
        Mono<CosmosAsyncDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase_DoesntExist() throws Exception {
        // delete the database
        Mono<CosmosAsyncDatabaseResponse> deleteObservable = client.getDatabase("I don't exist").delete();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readReplaceAutoscaleThroughput() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        int initalThroughput = 5000;
        ThroughputProperties properties = ThroughputProperties.createAutoscaledThroughput(initalThroughput);
        CosmosAsyncDatabase database = client.createDatabase(databaseName, properties)
                                           .block()
                                           .getDatabase();

        ThroughputResponse readThroughputResponse = database.readThroughput().block();
        assertThat(readThroughputResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(initalThroughput);
        database.createContainer("testCol", "/myPk").block();
        int tagetThroughput = 6000;
        properties = ThroughputProperties.createAutoscaledThroughput(tagetThroughput);
        // Enable later
//        ThroughputResponse replaceResponse = database.replaceThroughput(properties).block();
//        assertThat(replaceResponse.getProperties().getAutoscaleMaxThroughput()).isEqualTo(tagetThroughput);
        safeDeleteDatabase(client.getDatabase(databaseName));

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readReplaceManualThroughput() throws Exception {
        final String databaseName = CosmosDatabaseForTest.generateId();
        int initalThroughput = 5000;
        ThroughputProperties properties = ThroughputProperties.createManualThroughput(initalThroughput);
        CosmosAsyncDatabase database = client.createDatabase(databaseName, properties)
                                           .block()
                                           .getDatabase();

        ThroughputResponse readThroughputResponse = database.readThroughput().block();
        assertThat(readThroughputResponse.getProperties().getManualThroughput()).isEqualTo(initalThroughput);
        database.createContainer("testCol", "/myPk").block();
        int tagetThroughput = 6000;
        properties = ThroughputProperties.createManualThroughput(tagetThroughput);
        ThroughputResponse replaceResponse = database.replaceThroughput(properties).block();
        assertThat(replaceResponse.getProperties().getManualThroughput()).isEqualTo(tagetThroughput);
        safeDeleteDatabase(client.getDatabase(databaseName));

    }



    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_DatabaseCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        for(String dbId: databases) {
            safeDeleteDatabase(client.getDatabase(dbId));
        }
        safeClose(client);
    }
}
