/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import java.util.UUID;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosRequestOptions;
import com.microsoft.azure.cosmos.CosmosResponse;
import com.microsoft.azure.cosmos.CosmosResponseValidator;
import com.microsoft.azure.cosmos.CosmosTrigger;
import com.microsoft.azure.cosmos.CosmosTriggerResponse;
import com.microsoft.azure.cosmos.CosmosTriggerSettings;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.TriggerOperation;
import com.microsoft.azure.cosmosdb.TriggerType;

import reactor.core.publisher.Mono;

public class TriggerCrudTest extends TestSuiteBase {
    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TriggerCrudTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 100)
    public void createTrigger() throws Exception {

        // create a trigger
        CosmosTriggerSettings trigger = new CosmosTriggerSettings();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);

        Mono<CosmosTriggerResponse> createObservable = createdCollection.createTrigger(trigger, new CosmosRequestOptions());

        // validate trigger creation
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.Pre, TriggerOperation.Create)
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        // create a trigger
        CosmosTriggerSettings trigger = new CosmosTriggerSettings();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);
        CosmosTrigger readBackTrigger = createdCollection.createTrigger(trigger, new CosmosRequestOptions()).block().getCosmosTrigger();

        // read trigger
        waitIfNeededForReplicasToCatchUp(clientBuilder);
        Mono<CosmosTriggerResponse> readObservable = readBackTrigger.read(new RequestOptions());

        // validate read trigger
        CosmosResponseValidator<CosmosTriggerResponse> validator = new CosmosResponseValidator.Builder<CosmosTriggerResponse>()
                .withId(trigger.getId())
                .withTriggerBody("function() {var x = 10;}")
                .withTriggerInternals(TriggerType.Pre, TriggerOperation.Create)
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        // create a trigger
        CosmosTriggerSettings trigger = new CosmosTriggerSettings();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);
        CosmosTrigger readBackTrigger = createdCollection.createTrigger(trigger, new CosmosRequestOptions()).block().getCosmosTrigger();

        // delete trigger
        Mono<CosmosResponse> deleteObservable = readBackTrigger.delete(new CosmosRequestOptions());

        // validate delete trigger
        CosmosResponseValidator<CosmosResponse> validator = new CosmosResponseValidator.Builder<CosmosResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
