// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase_AlreadyExists() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();

        // attempt to create the database
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        FailureValidator validator = new FailureValidator.Builder()
            .resourceAlreadyExists()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase(preExistingDatabaseId).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(preExistingDatabaseId).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase_DoesntExist() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase("I don't exist").read();

        // validate
        FailureValidator validator = new FailureValidator.Builder()
            .resourceNotFound()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateFailure(readObservable, validator);
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        // create the database
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();
        CosmosAsyncDatabase database = client.getDatabase(databaseDefinition.getId());

        // delete the database
        Mono<CosmosDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase_DoesntExist() throws Exception {
        // delete the database
        Mono<CosmosDatabaseResponse> deleteObservable = client.getDatabase("I don't exist").delete();

        // validate
        FailureValidator validator = new FailureValidator.Builder()
            .resourceNotFound()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateFailure(deleteObservable, validator);
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
