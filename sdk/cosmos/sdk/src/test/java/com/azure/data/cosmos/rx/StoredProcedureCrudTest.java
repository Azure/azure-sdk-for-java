// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosStoredProcedure;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosStoredProcedureResponse;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.internal.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StoredProcedureCrudTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 10;}");

        Mono<CosmosStoredProcedureResponse> createObservable = container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions());

        CosmosResponseValidator<CosmosStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
            .withId(storedProcedureDef.id())
            .withStoredProcedureBody("function() {var x = 10;}")
            .notNullEtag()
            .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 10;}");
        CosmosStoredProcedure storedProcedure = container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block().storedProcedure();

        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosStoredProcedureResponse> readObservable = storedProcedure.read(null);

        CosmosResponseValidator<CosmosStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
            .withId(storedProcedureDef.id())
            .withStoredProcedureBody("function() {var x = 10;}")
            .notNullEtag()
            .build();

        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 10;}");

        CosmosStoredProcedure storedProcedure = this.container.getScripts().createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block().storedProcedure();
        Mono<CosmosResponse> deleteObservable = storedProcedure.delete(new CosmosStoredProcedureRequestOptions());

        CosmosResponseValidator<CosmosResponse> validator = new CosmosResponseValidator.Builder<>()
            .nullResource()
            .build();

        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(this.clientBuilder());

        Mono<CosmosStoredProcedureResponse> readObservable = storedProcedure.read(null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = 10_000 * SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().build();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }
}
