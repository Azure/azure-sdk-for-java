// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncTrigger;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class TriggerCrudTest extends TestSuiteBase {
    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TriggerCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 100)
    public void createTrigger() throws Exception {

        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);

        Mono<CosmosTriggerResponse> createObservable = createdCollection.getScripts().createTrigger(trigger);

        // validate trigger creation
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosTriggerResponse readBackTriggerResponse = createdCollection.getScripts().createTrigger(trigger).block();
        CosmosAsyncTrigger readBackTrigger =
            createdCollection.getScripts().getTrigger(readBackTriggerResponse.getProperties().getId());

        // read trigger
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosTriggerResponse> readObservable = readBackTrigger.read();

        // validate read trigger
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosTriggerResponse readBackTriggerResponse = createdCollection.getScripts().createTrigger(trigger).block();
        CosmosAsyncTrigger readBackTrigger =
            createdCollection.getScripts().getTrigger(readBackTriggerResponse.getProperties().getId());

        // delete trigger
        Mono<CosmosTriggerResponse> deleteObservable = readBackTrigger.delete();

        // validate delete trigger
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_TriggerCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
