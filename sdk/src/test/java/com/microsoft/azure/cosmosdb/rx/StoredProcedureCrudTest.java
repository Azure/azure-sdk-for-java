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
import com.microsoft.azure.cosmosdb.StoredProcedure;

import rx.Observable;


public class StoredProcedureCrudTest extends TestSuiteBase {

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createStoredProcedure() throws Exception {

        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");

        Observable<ResourceResponse<StoredProcedure>> createObservable = client.createStoredProcedure(getCollectionLink(), storedProcedureDef, null);

        // validate stored procedure creation
        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(storedProcedureDef.getId())
                .withStoredProcedureBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readStoredProcedure() throws Exception {
        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        StoredProcedure storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedureDef, null).toBlocking().single().getResource();

        // read stored procedure
        waitIfNeededForReplicasToCatchUp(clientBuilder);
        Observable<ResourceResponse<StoredProcedure>> readObservable = client.readStoredProcedure(storedProcedure.getSelfLink(), null);

        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(storedProcedureDef.getId())
                .withStoredProcedureBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteStoredProcedure() throws Exception {
        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        StoredProcedure storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedureDef, null).toBlocking().single().getResource();

        // delete
        Observable<ResourceResponse<StoredProcedure>> deleteObservable = client.deleteStoredProcedure(storedProcedure.getSelfLink(), null);

        // validate
        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        // attempt to read stored procedure which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        Observable<ResourceResponse<StoredProcedure>> readObservable = client.readStoredProcedure(storedProcedure.getSelfLink(), null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_SINGLE_PARTITION_COLLECTION;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}
