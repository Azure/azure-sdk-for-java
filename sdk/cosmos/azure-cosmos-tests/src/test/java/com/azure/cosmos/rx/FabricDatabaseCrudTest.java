// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.FabricTestSuiteBase;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class FabricDatabaseCrudTest extends FabricTestSuiteBase {
    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuildersWithGatewayForFabric")
    public FabricDatabaseCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void createDatabase() throws Exception {
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        // create the database
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate the failure
        FailureValidator validator = new FailureValidator.Builder().statusCode(401).build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase(DATABASE_ID).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(DATABASE_ID).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void readFeedDatabases() throws Exception {
        // read database
        List<FeedResponse<CosmosDatabaseProperties>> feedResponses = client.readAllDatabases().byPage().collectList().block();

        assert feedResponses.size() == 1;
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
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

    @BeforeClass(groups = { "fabric-test" }, timeOut = SETUP_TIMEOUT)
    public void before_DatabaseCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = client.getDatabase(DATABASE_ID);
    }

    @AfterClass(groups = { "fabric-test" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
