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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.TriggerOperation;
import com.microsoft.azure.cosmosdb.TriggerType;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;


public class TriggerCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(TriggerCrudTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public TriggerCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createTrigger() throws Exception {

        // create a trigger
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);

        Observable<ResourceResponse<Trigger>> createObservable = client.createTrigger(getCollectionLink(), trigger, null);

        // validate trigger creation
        ResourceResponseValidator<Trigger> validator = new ResourceResponseValidator.Builder<Trigger>()
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
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);
        Trigger readBackTrigger = client.createTrigger(getCollectionLink(), trigger, null).toBlocking().single().getResource();

        // read trigger
        Observable<ResourceResponse<Trigger>> readObservable = client.readTrigger(readBackTrigger.getSelfLink(), null);

        // validate read trigger
        ResourceResponseValidator<Trigger> validator = new ResourceResponseValidator.Builder<Trigger>()
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
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.Create);
        trigger.setTriggerType(TriggerType.Pre);
        Trigger readBackTrigger = client.createTrigger(getCollectionLink(), trigger, null).toBlocking().single().getResource();

        // delete trigger
        Observable<ResourceResponse<Trigger>> deleteObservable = client.deleteTrigger(readBackTrigger.getSelfLink(), null);

        // validate delete trigger
        ResourceResponseValidator<Trigger> validator = new ResourceResponseValidator.Builder<Trigger>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();;       
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
   
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}
