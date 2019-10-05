// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.FailureValidator;
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

        waitIfNeededForReplicasToCatchUp(clientBuilder());
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

        waitIfNeededForReplicasToCatchUp(this.clientBuilder());

        Mono<CosmosAsyncStoredProcedureResponse> readObservable = storedProcedure.read(null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = 10_000 * SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }
}
