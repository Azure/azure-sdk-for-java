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
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;


public class EventLoopSizeTest extends TestSuiteBase {
    private final static String DATABASE_ID = getDatabaseId(EventLoopSizeTest.class);
    
    private AsyncDocumentClient client;
    private Database database;
    private DocumentCollection collection;
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT, expectedExceptions = { IllegalArgumentException.class }, enabled = false)
    public void invalidBuilder() throws Exception {
        
        ConnectionPolicy cp = new ConnectionPolicy();
        cp.setConnectionMode(ConnectionMode.DirectHttps);
        new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .withWorkers(2)
                .withConnectionPolicy(cp).build();
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument() throws Exception {
        
        AsyncDocumentClient newClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .withWorkers(2)
                .build();

        try {
            Document docDefinition = getDocumentDefinition();

            Observable<ResourceResponse<Document>> createObservable = newClient
                    .createDocument(collection.getSelfLink(), docDefinition, null, false);

            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(docDefinition.getId())
                    .build();

            validateSuccess(createObservable, validator);
        } finally {
            newClient.close();
        }
    }
   
    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client        
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();
        
        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);
        
        try {
            client.deleteDatabase(Utils.getDatabaseLink(databaseDefinition, true), null).toBlocking().single();
        } catch (Exception e) {
           // ignore failure if it doesn't exist
        }
        
        database = client.createDatabase(databaseDefinition, null).toBlocking().single().getResource();
        collection = client.createCollection(database.getSelfLink(), getCollectionDefinition(), null).toBlocking().single().getResource();
    }
    
    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        client.deleteDatabase(database.getSelfLink(), null).toBlocking().single();
        safeClose(client);
    }
}
