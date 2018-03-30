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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observable.ListenableFutureObservable;

/**
 * This integration test class demonstrates how to use Async API to query for
 * documents.
 * 
 * NOTE: you can use rxJava based async api with java8 lambda expression. Using of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * 
 * You can also use the async API without java8 lambda expression.
 * 
 * For example
 * <ul>
 * <li>{@link #testQueryDocuments_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 * 
 * <li>{@link #testQueryDocuments_Async_withoutLambda()} demonstrates how to the same
 * thing without lambda expression.
 * </ul>
 * 
 * Also if you need to work with Future or ListenableFuture it is possible to transform
 * an observable to ListenableFuture. Please see {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 */
public class DocumentQueryAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentQueryAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";

    private AsyncDocumentClient asyncClient;

    private DocumentCollection createdCollection;
    private Database createdDatabase;

    private int numberOfDocuments;

    @Before
    public void setUp() throws DocumentClientException {

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

        createdDatabase = databaseCreationResponse.getResource();
        
        // create collection
        createdCollection = asyncClient
                .createCollection(databaseCreationResponse.getResource().getSelfLink(), collectionDefinition, null)
                .toBlocking().single().getResource();

        numberOfDocuments = 20;
        // add documents
        for (int i = 0; i < numberOfDocuments; i++) {
            Document doc = new Document(String.format("{ 'id': 'loc%d', 'counter': %d}", i, i));
            asyncClient.createDocument(createdCollection.getSelfLink(), doc, null, true).toBlocking().single();
        }
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    @Test
    public void testQueryDocuments_Async() throws Exception {

        // query for documents
        // creates a document query observable and verifies the async behavior
        // of document query observable

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options);

        final CountDownLatch mainThreadBarrier = new CountDownLatch(1);

        final CountDownLatch resultsCountDown = new CountDownLatch(numberOfDocuments);

        // forEach(.) is an alias for subscribe(.)
        
        documentQueryObservable.forEach(page -> {
            try {
                // waits on the barrier
                mainThreadBarrier.await();
            } catch (InterruptedException e) {
            }

            for (@SuppressWarnings("unused") Document d : page.getResults()) {
                resultsCountDown.countDown();
            }
        });
        
        // The following code will run concurrently

        System.out.println("action is subscribed to the observable");

        // release main thread barrier
        System.out.println("after main thread barrier is released, subscribed observable action can continue");
        mainThreadBarrier.countDown();

        System.out.println("waiting for all the results using result count down latch");

        resultsCountDown.await();
    }

    @Test
    public void testQueryDocuments_Async_withoutLambda() throws Exception {

        // query for documents
        // creates a document query observable and verifies the async behavior
        // of document query observable
        
        // NOTE: does the same thing as testQueryDocuments_Async without java8 lambda expression

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options);

        final CountDownLatch mainThreadBarrier = new CountDownLatch(1);

        final CountDownLatch resultsCountDown = new CountDownLatch(numberOfDocuments);

        Action1<FeedResponse<Document>> actionPerPage = new Action1<FeedResponse<Document>>() {

            @SuppressWarnings("unused")
            @Override
            public void call(FeedResponse<Document> t) {

                try {
                    // waits on the barrier
                    mainThreadBarrier.await();
                } catch (InterruptedException e) {
                }

                for (Document d : t.getResults()) {
                    resultsCountDown.countDown();
                }
            }
        };

        // forEach(.) is an alias for subscribe(.)
        documentQueryObservable.forEach(actionPerPage);
        // the following code will run concurrently

        System.out.println("action is subscribed to the observable");

        // release main thread barrier
        System.out.println("after main thread barrier is released, subscribed observable action can continue");
        mainThreadBarrier.countDown();

        System.out.println("waiting for all the results using result count down latch");

        resultsCountDown.await();
    }

    @Test
    public void testQueryDocuments_findTotalRequestCharge() throws Exception {

        // queries for documents and sum up the total request charge

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<Double> totalChargeObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options)
                .map(FeedResponse::getRequestCharge)                          // map the page to its request charge
                .reduce((totalCharge, charge) -> totalCharge + charge);       // sum up all the request charges

        final CountDownLatch doneLatch = new CountDownLatch(1);

        // subscribe(.) is the same as forEach(.)
        totalChargeObservable.subscribe(totalCharge -> {
            System.out.println(totalCharge);
            doneLatch.countDown();
        });
        
        doneLatch.await();
    }
    
    @Test
    public void testQueryDocuments_unsubscribeAfterFirstPage() throws Exception {

        // subscriber unsubscribes after first page

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Document>> requestChargeObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options);

        AtomicInteger onNextCounter = new AtomicInteger();
        AtomicInteger onCompletedCounter = new AtomicInteger();
        AtomicInteger onErrorCounter = new AtomicInteger();

        requestChargeObservable.subscribe(new Subscriber<FeedResponse<Document>>() {

            @Override
            public void onCompleted() {
                onCompletedCounter.incrementAndGet();
            }

            @Override
            public void onError(Throwable e) {
                onErrorCounter.incrementAndGet();
            }

            @Override
            public void onNext(FeedResponse<Document> page) {
                onNextCounter.incrementAndGet();    
                unsubscribe();
            }
        });
        
        Thread.sleep(4000);
        
        // after subscriber unsubscribes, it doesn't receive any more events.
        assertThat(onNextCounter.get(), equalTo(1));
        assertThat(onCompletedCounter.get(), equalTo(0));
        assertThat(onErrorCounter.get(), equalTo(0));
    }

    @Test
    public void testQueryDocuments_filterFetchedResults() throws Exception {
        // queries for documents and filter out the fetched results

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Func1<Document, Boolean> isPrimeNumber = new Func1<Document, Boolean>() {

            @Override
            public Boolean call(Document doc) {
                int n = doc.getInt("counter");
                if (n <= 1) return false;
                for(int i = 2; 2*i < n; i++) {
                    if(n % i == 0)
                        return false;
                }
                return true;
            }
        };
        
        List<Document> resultList = Collections.synchronizedList(new ArrayList<Document>());
        
        asyncClient
            .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options)
            .map(FeedResponse::getResults)              // map the page to the list of documents
            .concatMap(Observable::from)                    // flatten the observable<list<document>> to observable<document>
            .filter(isPrimeNumber)                          // filter documents using isPrimeNumber predicate
            .subscribe(doc -> resultList.add(doc));         // collect the results
        
        Thread.sleep(4000);
        
        int expectedNumberOfPrimes = 0;
        // find all the documents with prime number counter
        for(int i = 0; i < numberOfDocuments; i++) {
            boolean isPrime = true;
            if (i <= 1) isPrime = false;
            for(int j = 2; 2*j < i; j++) {
                if(i % j == 0) {
                    isPrime = false;
                    break;
                }
            }

            if (isPrime) {
                expectedNumberOfPrimes++;
            }
        }
        
        // assert that we only collected what's expected
        assertThat(resultList, hasSize(expectedNumberOfPrimes));
    }
    
    @Test
    public void testQueryDocuments_toBlocking_toIterator() throws DocumentClientException {

        // queries for documents
        // converts the document query observable to blocking observable and
        // uses that to find all documents

        // query for documents
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options);
        
        // covert the observable to a blocking observable, then convert the blocking observable to an iterator
        Iterator<FeedResponse<Document>> it = documentQueryObservable.toBlocking().getIterator();

        int pageCounter = 0;
        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<Document> page = it.next();
            pageCounter++;

            String pageSizeAsString = page.getResponseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat("header item count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            assertThat("Result size must match header item count", page.getResults(), hasSize(pageSize));
            numberOfResults += pageSize;
        }
        assertThat("number of total results", numberOfResults, equalTo(numberOfDocuments));
        assertThat("number of result pages", pageCounter,
                equalTo((numberOfDocuments + requestPageSize - 1) / requestPageSize));
    }
    
    @Test
    public void testOrderBy_Async() throws Exception {
        // create a partitioned collection
        String collectionId = UUID.randomUUID().toString();
        DocumentCollection multiPartitionCollection = createMultiPartitionCollection(createdDatabase.getSelfLink(), collectionId, "/key");

        // insert documents
        int totalNumberOfDocumentsInMultiPartitionCollection = 10;
        for (int i = 0; i < totalNumberOfDocumentsInMultiPartitionCollection; i++) {
            
            Document doc = new Document(String.format( "{\"id\":\"documentId%d\",\"key\":\"%s\",\"prop\":%d}", 
                    i, RandomStringUtils.randomAlphabetic(2), i));
            asyncClient.createDocument(multiPartitionCollection.getSelfLink(), doc, null, true).toBlocking().single();
        }
        
        // query for the documents order by the prop field
        SqlQuerySpec query = new SqlQuerySpec("SELECT r.id FROM r ORDER BY r.prop", new SqlParameterCollection());
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(1);

        // get the observable order by query documents
        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(multiPartitionCollection.getSelfLink(), query, options);        
        
        List<String> resultList = (List<String>) Collections.synchronizedList(new ArrayList<String>());
        
        documentQueryObservable
                .map(FeedResponse::getResults)          // map the logical page to the list of documents in the page
                .concatMap(Observable::from)                // flatten the list of documents
                .map(doc -> doc.getId())                    // map to the document Id
                .forEach(docId -> resultList.add(docId));   // add each document Id to the resultList

        Thread.sleep(4000);

        // assert we found all the results
        assertThat(resultList, hasSize(totalNumberOfDocumentsInMultiPartitionCollection));
        for(int i = 0; i < totalNumberOfDocumentsInMultiPartitionCollection; i++) {
            String docId = resultList.get(i);
            // assert that the order of the documents are valid
            assertThat(docId, equalTo("documentId" + i));
        }
    }
    
    @Test
    public void testTransformObservableToGoogleGuavaListenableFuture() throws Exception {
        // You can convert an Observable to a ListenableFuture.
        // ListenableFuture (part of google guava library) is a popular extension
        // of Java's Future which allows registering listener callbacks:
        // https://github.com/google/guava/wiki/ListenableFutureExplained

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM root", options);
        
        // convert to observable of list of pages
        Observable<List<FeedResponse<Document>>> allPagesObservable = documentQueryObservable.toList();
        
        // convert the observable of list of pages to a Future
        ListenableFuture<List<FeedResponse<Document>>> future = ListenableFutureObservable.to(allPagesObservable);

        List<FeedResponse<Document>> pageList = future.get();
        
        int totalNumberOfRetrievedDocuments = 0;
        for(FeedResponse<Document> page: pageList) {
            totalNumberOfRetrievedDocuments += page.getResults().size();
        }
        assertThat(numberOfDocuments, equalTo(totalNumberOfRetrievedDocuments));
    }
    
    private void cleanUpGeneratedDatabases() throws DocumentClientException {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponse<Database>> feedResponsePages = asyncClient
                        .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterCollection(new SqlParameter("@id", id))), null)
                        .toList().toBlocking().single();

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
    
    private DocumentCollection createMultiPartitionCollection(String databaseLink, String collectionId,
            String partitionKeyPath) throws DocumentClientException {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(partitionKeyPath);
        partitionKeyDef.setPaths(paths);

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(collectionId);
        collectionDefinition.setPartitionKey(partitionKeyDef);
        DocumentCollection createdCollection = asyncClient
                .createCollection(databaseLink, collectionDefinition, options).toBlocking().single().getResource();

        return createdCollection;
    }
}
