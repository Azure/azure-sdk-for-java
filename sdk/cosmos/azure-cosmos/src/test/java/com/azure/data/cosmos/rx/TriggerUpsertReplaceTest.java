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

//FIXME beforeClass times out inconsistently
@Ignore
public class TriggerUpsertReplaceTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TriggerUpsertReplaceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceTrigger() throws Exception {

        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosTriggerProperties readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().getProperties();
        
        // read trigger to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosAsyncTriggerResponse> readObservable = createdCollection.getScripts().getTrigger(readBackTrigger.getId()).read();

        // validate trigger creation
        CosmosResponseValidator<CosmosAsyncTriggerResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncTriggerResponse>()
                .withId(readBackTrigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update getTrigger
        readBackTrigger.setBody("function() {var x = 11;}");

        Mono<CosmosAsyncTriggerResponse> updateObservable = createdCollection.getScripts().getTrigger(readBackTrigger.getId()).replace(readBackTrigger);

        // validate getTrigger replace
        CosmosResponseValidator<CosmosAsyncTriggerResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosAsyncTriggerResponse>()
                .withId(readBackTrigger.getId())
                .withTriggerBody("function() {var x = 11;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }
    
    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
