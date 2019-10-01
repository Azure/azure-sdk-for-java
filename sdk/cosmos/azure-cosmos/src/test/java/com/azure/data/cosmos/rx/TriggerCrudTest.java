// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);

        Mono<CosmosAsyncTriggerResponse> createObservable = createdCollection.getScripts().createTrigger(trigger);

        // validate trigger creation
        CosmosResponseValidator<CosmosAsyncTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncTriggerResponse>()
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
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosAsyncTrigger readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().getTrigger();

        // read trigger
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosAsyncTriggerResponse> readObservable = readBackTrigger.read();

        // validate read trigger
        CosmosResponseValidator<CosmosAsyncTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncTriggerResponse>()
                .withId(trigger.getId())
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
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosAsyncTrigger readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().getTrigger();

        // delete trigger
        Mono<CosmosAsyncTriggerResponse> deleteObservable = readBackTrigger.delete();

        // validate delete trigger
        CosmosResponseValidator<CosmosAsyncTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncTriggerResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
