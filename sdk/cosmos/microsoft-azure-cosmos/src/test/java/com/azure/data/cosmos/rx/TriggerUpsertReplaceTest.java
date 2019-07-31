// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponseValidator;
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

//FIXME beforeClass times out inconsistently
@Ignore
public class TriggerUpsertReplaceTest extends TestSuiteBase {

    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TriggerUpsertReplaceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceTrigger() throws Exception {

        // create a trigger
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.id(UUID.randomUUID().toString());
        trigger.body("function() {var x = 10;}");
        trigger.triggerOperation(TriggerOperation.CREATE);
        trigger.triggerType(TriggerType.PRE);
        CosmosTriggerProperties readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().properties();
        
        // read trigger to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosTriggerResponse> readObservable = createdCollection.getScripts().getTrigger(readBackTrigger.id()).read();

        // validate trigger creation
        CosmosResponseValidator<CosmosTriggerResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(readBackTrigger.id())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update trigger
        readBackTrigger.body("function() {var x = 11;}");

        Mono<CosmosTriggerResponse> updateObservable = createdCollection.getScripts().getTrigger(readBackTrigger.id()).replace(readBackTrigger);

        // validate trigger replace
        CosmosResponseValidator<CosmosTriggerResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(readBackTrigger.id())
                .withTriggerBody("function() {var x = 11;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }
    
    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
