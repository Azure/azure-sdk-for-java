// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosTrigger;
import com.azure.data.cosmos.CosmosTriggerProperties;
import com.azure.data.cosmos.CosmosTriggerResponse;
import com.azure.data.cosmos.TriggerOperation;
import com.azure.data.cosmos.TriggerType;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class TriggerCrudTest extends TestSuiteBase {
    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TriggerCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 100)
    public void createTrigger() throws Exception {

        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.id(UUID.randomUUID().toString());
        trigger.body("function() {var x = 10;}");
        trigger.triggerOperation(TriggerOperation.CREATE);
        trigger.triggerType(TriggerType.PRE);

        Mono<CosmosTriggerResponse> createObservable = createdCollection.getScripts().createTrigger(trigger);

        // validate trigger creation
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.id())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.id(UUID.randomUUID().toString());
        trigger.body("function() {var x = 10;}");
        trigger.triggerOperation(TriggerOperation.CREATE);
        trigger.triggerType(TriggerType.PRE);
        CosmosTrigger readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().trigger();

        // read trigger
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosTriggerResponse> readObservable = readBackTrigger.read();

        // validate read trigger
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.id())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    //FIXME test is flaky
    @Ignore
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.id(UUID.randomUUID().toString());
        trigger.body("function() {var x = 10;}");
        trigger.triggerOperation(TriggerOperation.CREATE);
        trigger.triggerType(TriggerType.PRE);
        CosmosTrigger readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().trigger();

        // delete trigger
        Mono<CosmosResponse> deleteObservable = readBackTrigger.delete();

        // validate delete trigger
        CosmosResponseValidator<CosmosResponse> validator = new CosmosResponseValidator.Builder<CosmosResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
