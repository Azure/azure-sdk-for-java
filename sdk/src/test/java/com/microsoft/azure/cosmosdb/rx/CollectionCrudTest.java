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

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;

public class CollectionCrudTest extends TestSuiteBase {
    private final static String DATABASE_ID = getDatabaseId(CollectionCrudTest.class);
    
    protected static final int TIMEOUT = 20000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;
    
    private AsyncDocumentClient client;
    private Database database;

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createCollection() throws Exception {
        DocumentCollection collectionDefinition = getCollectionDefinition();
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client
                .createCollection(database.getSelfLink(), collectionDefinition, null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collectionDefinition.getId()).build();
        
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readCollection() throws Exception {
        DocumentCollection collectionDefinition = getCollectionDefinition();
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(database.getSelfLink(), collectionDefinition,
                null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();

        Observable<ResourceResponse<DocumentCollection>> readObservable = client.readCollection(collection.getSelfLink(), null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readCollection_NameBase() throws Exception {
        DocumentCollection collectionDefinition = getCollectionDefinition();
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(database.getSelfLink(), collectionDefinition,
                null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();
        
        Observable<ResourceResponse<DocumentCollection>> readObservable = client.readCollection(
                Utils.getCollectionNameLink(database.getId(), collection.getId()), null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readCollection_DoesntExist() throws Exception {

        Observable<ResourceResponse<DocumentCollection>> readObservable = client
                .readCollection(Utils.getCollectionNameLink(database.getId(), "I don't exist"), null);

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteCollection() throws Exception {
        DocumentCollection collectionDefinition = getCollectionDefinition();
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(database.getSelfLink(), collectionDefinition, null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();

        Observable<ResourceResponse<DocumentCollection>> deleteObservable = client.deleteCollection(collection.getSelfLink(),
                null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
        
        //TODO validate after deletion the resource is actually deleted (not found)
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceCollection() throws Exception {
        // create a collection
        DocumentCollection collectionDefinition = getCollectionDefinition();
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(database.getSelfLink(), collectionDefinition, null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();
        // sanity check
        assertThat(collection.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.Consistent);
        
        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.Lazy);
        collection.setIndexingPolicy(indexingMode);
        Observable<ResourceResponse<DocumentCollection>> readObservable = client.replaceCollection(collection, null);
        
        // validate
        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .indexingMode(IndexingMode.Lazy).build();
        validateSuccess(readObservable, validator);
    }
    
    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client
        
        client = new AsyncDocumentClient.Builder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKey(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(ConnectionPolicy.GetDefault())
            .withConsistencyLevel(ConsistencyLevel.Session).build();

        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);
        
        try {
            client.deleteDatabase(Utils.getDatabaseLink(databaseDefinition, true), null).toBlocking().single();
        } catch (Exception e) {
           // ignore failure if it doesn't exist
        }
        
        database = client.createDatabase(databaseDefinition, null).toBlocking().single().getResource();
    }
    
    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        client.deleteDatabase(database.getSelfLink(), null).toBlocking().single();
        safeClose(client);
    }
}
