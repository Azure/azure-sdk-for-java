// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncStoredProcedure;
import com.azure.cosmos.models.CosmosAsyncStoredProcedureResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StoredProcedureCrudTest extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");

        Mono<CosmosAsyncStoredProcedureResponse> createObservable = container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions());

        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
            .withId(storedProcedureDef.getId())
            .withStoredProcedureBody("function() {var x = 10;}")
            .notNullEtag()
            .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        CosmosAsyncStoredProcedure storedProcedure = container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block().getStoredProcedure();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosAsyncStoredProcedureResponse> readObservable = storedProcedure.read(null);

        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
            .withId(storedProcedureDef.getId())
            .withStoredProcedureBody("function() {var x = 10;}")
            .notNullEtag()
            .build();

        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");

        CosmosAsyncStoredProcedure storedProcedure = this.container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block().getStoredProcedure();
        Mono<CosmosAsyncStoredProcedureResponse> deleteObservable = storedProcedure.delete(new CosmosStoredProcedureRequestOptions());

        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
            .nullResource()
            .build();

        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        Mono<CosmosAsyncStoredProcedureResponse> readObservable = storedProcedure.read(null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = 10_000 * SETUP_TIMEOUT)
    public void before_StoredProcedureCrudTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }
}
