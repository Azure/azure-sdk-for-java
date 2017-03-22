/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.StoredProcedure;

import rx.Observable;

public class StoredProcedureCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(StoredProcedureCrudTest.class);

    private static AsyncDocumentClient houseKeepingClient;
    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
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
                .withBody("function() {var x = 10;}")
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
        Observable<ResourceResponse<StoredProcedure>> readObservable = client.readStoredProcedure(storedProcedure.getSelfLink(), null);


        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(storedProcedureDef.getId())
                .withBody("function() {var x = 10;}")
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

        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        this.client = this.clientBuilder.build();       
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        this.client.close();
    }

    @BeforeSuite(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public static void beforeSuite() {
        houseKeepingClient = createGatewayRxDocumentClient().build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(houseKeepingClient, d);
        createdCollection = safeCreateCollection(houseKeepingClient, createdDatabase.getSelfLink(), getCollectionDefinition());
    }

    @AfterSuite(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public static void afterSuite() {

        deleteDatabase(houseKeepingClient, createdDatabase.getId());
        houseKeepingClient.close();
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}
