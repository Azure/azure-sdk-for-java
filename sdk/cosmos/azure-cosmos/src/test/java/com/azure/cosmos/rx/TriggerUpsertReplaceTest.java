// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
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
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        CosmosTriggerProperties readBackTrigger = createdCollection.getScripts().createTrigger(trigger).block().getProperties();

        // read trigger to validate creation
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosTriggerResponse> readObservable = createdCollection.getScripts().getTrigger(readBackTrigger.getId()).read();

        // validate trigger creation
        CosmosResponseValidator<CosmosTriggerResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(readBackTrigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);

        //update getTrigger
        readBackTrigger.setBody("function() {var x = 11;}");

        Mono<CosmosTriggerResponse> updateObservable = createdCollection.getScripts().getTrigger(readBackTrigger.getId()).replace(readBackTrigger);

        // validate getTrigger replace
        CosmosResponseValidator<CosmosTriggerResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(readBackTrigger.getId())
                .withTriggerBody("function() {var x = 11;}")
                .withTriggerInternals(TriggerType.PRE, TriggerOperation.CREATE)
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_TriggerUpsertReplaceTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
