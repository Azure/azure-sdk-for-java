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
package com.microsoft.azure.cosmosdb.rx.examples;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and upsert. If you are interested in examples for querying
 * for documents please see {@link DocumentQueryAsyncAPITest}
 * 
 * NOTE: you can use rxJava based async api with java8 lambda expression. Using
 * of rxJava based async APIs with java8 lambda expressions is much prettier.
 * 
 * You can also use the async API without java8 lambda expression.
 * 
 * For example
 * <ul>
 * <li>{@link #testCreateDocument_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 * 
 * <li>{@link #testCreateDocument_Async_withoutLambda()} demonstrates how to the same
 * thing without lambda expression.
 * </ul>
 * 
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 */
public class DocumentCRUDAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCRUDAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";

    private AsyncDocumentClient asyncClient;
    private DocumentCollection createdCollection;

    @Before
    public void setUp() throws DocumentClientException {

        // sets up the requirements for each test
        
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();
        // Clean up the database.
        this.cleanUpGeneratedDatabases();

        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // create database
        ResourceResponse<Database> databaseCreationResponse = asyncClient.createDatabase(databaseDefinition, null)
                .toBlocking().single();
        
        // create collection
        createdCollection = asyncClient
                .createCollection(databaseCreationResponse.getResource().getSelfLink(), collectionDefinition, null)
                .toBlocking().single().getResource();
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    @Test
    public void testCreateDocument_Async() throws Exception {
   
        // create a document
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, true);

        final CountDownLatch doneLatch = new CountDownLatch(1);

        // subscribe to events emitted by the observable
        createDocumentObservable
            .single()           // we know there will be one response
            .subscribe(
                
                documentResourceResponse -> {
                    System.out.println(documentResourceResponse.getActivityId());
                    doneLatch.countDown();
                },

                error -> {
                    System.err.println("an error happened in document creation: actual cause: " + error.getMessage());
                });

        // wait till document creation completes
        doneLatch.await();
    }
    
    @Test
    public void testCreateDocument_Async_withoutLambda() throws Exception {
        
        // create a document in without java8 lambda expressions        
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, true);

        final CountDownLatch doneLatch = new CountDownLatch(1);
        
        Action1<ResourceResponse<Document>> onNext = new Action1<ResourceResponse<Document>>() {

            @Override
            public void call(ResourceResponse<Document> documentResourceResponse) {
                System.out.println(documentResourceResponse.getActivityId());
                doneLatch.countDown();                
            }
        };
        
        Action1<Throwable> onError = new Action1<Throwable>() {

            @Override
            public void call(Throwable error) {
                System.err.println("an error happened in document creation: actual cause: " + error.getMessage());                
            }
        };
        
        // subscribe to events emitted by the observable
        createDocumentObservable
            .single()            // we know there will be one response
            .subscribe(onNext, onError);

        // wait till document creation completes
        doneLatch.await();
    }
    
    @Test
    public void testCreateDocument_toBlocking() throws DocumentClientException {
        
        // create a document
        // toBlocking() converts the observable to a blocking observable
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        Observable<ResourceResponse<Document>> createDocumentObservable =
                asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, true);

        
        // toBlocking() converts to a blocking observable
        // single() gets the only result
        createDocumentObservable
            .toBlocking()           //converts the observable to a blocking observable
            .single();              //gets the single result
    }
    
    @Test
    public void testDocumentCreation_SumUpRequestCharge() throws Exception {
        
        // create 10 documents and sum up all the documents creation request charges

        // create 10 documents
        List<Observable<ResourceResponse<Document>>> listOfCreateDocumentObservables = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", i, i));

            Observable<ResourceResponse<Document>> createDocumentObservable = 
                    asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false);
            listOfCreateDocumentObservables.add(createDocumentObservable);
        }
        
        // merge all document creation observables into one observable
        Observable<ResourceResponse<Document>> mergedObservable = Observable.merge(listOfCreateDocumentObservables);
        
        // create a new observable emitting the total charge of creating all 10 documents
        Observable<Double> totalChargeObservable = mergedObservable
                .map(ResourceResponse::getRequestCharge)                //map to request charge
                .reduce((totalCharge, charge) -> totalCharge + charge); //sum up all the charges
        
        final CountDownLatch doneLatch = new CountDownLatch(1);

        // subscribe to the total request charge observable
        totalChargeObservable.subscribe(totalCharge -> {
            // print the total charge
            System.out.println(totalCharge);
            doneLatch.countDown();
        });
        
        doneLatch.await();
    }

    @Test
    public void testCreateDocument_toBlocking_DocumentAlreadyExists_Fails() throws DocumentClientException {

        // attempt to create a document which already exists
        // - first create a document
        // - Using the async api generate an async document creation observable
        // - Converts the Observable to blocking using Observable.toBlocking() api
        // - catch already exist failure (409)
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single();

        // Create the document
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(createdCollection.getSelfLink(), doc, null, false);

        try {
            createDocumentObservable
                .toBlocking()           //converts the observable to a blocking observable
                .single();              //gets the single result
            Assert.fail("Document Already Exists. Document Creation must fail");
        } catch (Exception e) {
            assertThat("Document already exists.", 
                    ((DocumentClientException) e.getCause()).getStatusCode(), equalTo(409));
        }
    }
    
    @Test
    public void testCreateDocument_Async_DocumentAlreadyExists_Fails() throws Exception {

        // attempt to create a document which already exists
        // - first create a document
        // - Using the async api generate an async document creation observable
        // - Converts the Observable to blocking using Observable.toBlocking() api
        // - catch already exist failure (409)
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single();

        // Create the document
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(createdCollection.getSelfLink(), doc, null, false);

        List<Throwable> errorList = Collections.synchronizedList(new ArrayList<Throwable>());

        createDocumentObservable.subscribe(
            resourceResponse -> {},
                
            error -> {
                errorList.add(error);
                System.err.println("failed to create a document due to: " + error.getMessage());
            }
        );
        
        Thread.sleep(2000);
        assertThat(errorList, hasSize(1));
        assertThat(errorList.get(0), is(instanceOf(DocumentClientException.class)));
        assertThat(((DocumentClientException) errorList.get(0)).getStatusCode(), equalTo(409));
    }
    
    @Test
    public void testDocumentReplace_Async() throws Exception {

        // replace a document
        
        // create a document 
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        String documentLink = asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single().getResource().getSelfLink();
        
        // try to replace the existing document
        Document replacingDocument = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d', 'new-prop' : '2'}", 1, 1));
        Observable<ResourceResponse<Document>> replaceDocumentObservable = asyncClient
                .replaceDocument(documentLink, replacingDocument, null);

        List<ResourceResponse<Document>> capturedResponse = Collections.synchronizedList(new ArrayList<ResourceResponse<Document>>()); 
        
        replaceDocumentObservable.subscribe(
            resourceResponse -> {
                capturedResponse.add(resourceResponse);
            }

        );
        
        Thread.sleep(2000);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }
    
    @Test
    public void testDocumentUpsert_Async() throws Exception {

        // upsert a document
        
        // create a document
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single();
        
        // upsert the existing document
        Document upsertingDocument = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d', 'new-prop' : '2'}", 1, 1));
        Observable<ResourceResponse<Document>> upsertDocumentObservable = asyncClient
                .upsertDocument(createdCollection.getSelfLink(), upsertingDocument, null, false);

        List<ResourceResponse<Document>> capturedResponse = Collections.synchronizedList(new ArrayList<ResourceResponse<Document>>()); 
        
        upsertDocumentObservable.subscribe(
            resourceResponse -> {
                capturedResponse.add(resourceResponse);
            }

        );
        
        Thread.sleep(4000);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }
    
    @Test
    public void testDocumentDelete_Async() throws Exception {

        // delete a document
        
        // create a document
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        String documentLink = asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single().getResource().getSelfLink();
        
        // delete the existing document
        Observable<ResourceResponse<Document>> deleteDocumentObservable = asyncClient
                .deleteDocument(documentLink, null);

        List<ResourceResponse<Document>> capturedResponse = Collections.synchronizedList(new ArrayList<ResourceResponse<Document>>()); 
        
        deleteDocumentObservable.subscribe(
            resourceResponse -> {
                capturedResponse.add(resourceResponse);
            }

        );
        
        Thread.sleep(2000);

        assertThat(capturedResponse, hasSize(1));
        
        // assert document is deleted
        List<Document> listOfDocuments = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", null)
                .map(FeedResponse::getResults)              //map page to its list of documents
                .concatMap(Observable::from)                //flatten the observable
                .toList()                                   //transform to a observable
                .toBlocking()                               //block
                .single();                                  //gets the List<Document>
        
        // assert that there is no document found
        assertThat(listOfDocuments, hasSize(0));
    }
    
    @Test
    public void testDocumentRead_Async() throws Exception {

        // read a document
        
        //create a document
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        String documentLink = asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, false).toBlocking().single().getResource().getSelfLink();
        
        // read the document
        Observable<ResourceResponse<Document>> readDocumentObservable = asyncClient
                .readDocument(documentLink, null);

        List<ResourceResponse<Document>> capturedResponse = Collections.synchronizedList(new ArrayList<ResourceResponse<Document>>()); 
        
        readDocumentObservable.subscribe(
            resourceResponse -> {
                capturedResponse.add(resourceResponse);
            }

        );
        
        Thread.sleep(2000);

        // assert document is retrieved
        assertThat(capturedResponse, hasSize(1));
    }
    
    @Test
    public void testTransformObservableToGoogleGuavaListenableFuture() throws Exception {
        
        // You can convert an Observable to a ListenableFuture.
        // ListenableFuture (part of google guava library) is a popular extension
        // of Java's Future which allows registering listener callbacks:
        // https://github.com/google/guava/wiki/ListenableFutureExplained
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(createdCollection.getSelfLink(), doc, null, false);
        ListenableFuture<ResourceResponse<Document>> listenableFuture = ListenableFutureObservable.to(createDocumentObservable);

        ResourceResponse<Document> rrd = listenableFuture.get();
        
        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.print(rrd.getRequestCharge());
    }

    private void cleanUpGeneratedDatabases() throws DocumentClientException {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponse<Database>> feedResponsePages = asyncClient
                        .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterCollection(new SqlParameter("@id", id))), null).toList().toBlocking().single();
                
                
                if (!feedResponsePages.get(0).getResults().isEmpty()) {
                    Database res = feedResponsePages.get(0).getResults().get(0);
                    LOGGER.info("deleting a database " + feedResponsePages.get(0));
                    asyncClient.deleteDatabase(res.getSelfLink(), null).toBlocking().single();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
