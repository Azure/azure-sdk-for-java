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
package com.azure.data.cosmos.rx.examples;

import com.google.common.util.concurrent.ListenableFuture;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.RequestOptions;
import com.azure.data.cosmos.SqlParameterCollection;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.AsyncDocumentClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observable.ListenableFutureObservable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class demonstrates how to use Async API to query for
 * Documents.
 * <p>
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use
 * of rxJava based async APIs with java8 lambda expressions is much prettier.
 * <p>
 * You can also use the async API without java8 lambda expression.
 * <p>
 * For example
 * <ul>
 * <li>{@link #queryDocuments_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 *
 * <li>{@link #queryDocuments_Async_withoutLambda()} demonstrates how to do
 * the same thing without lambda expression.
 * </ul>
 * <p>
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #transformObservableToGoogleGuavaListenableFuture()}
 */
public class DocumentQueryAsyncAPITest {
    private final static int TIMEOUT = 3 * 60000;
    private AsyncDocumentClient asyncClient;
    private DocumentCollection createdCollection;
    private Database createdDatabase;
    private int numberOfDocuments;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE database

        createdDatabase = Utils.createDatabaseForTest(asyncClient);

        // CREATE collection
        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.id(), collectionDefinition, null)
                .toBlocking().single().getResource();

        numberOfDocuments = 20;
        // Add documents
        for (int i = 0; i < numberOfDocuments; i++) {
            Document doc = new Document(String.format("{ 'id': 'loc%d', 'counter': %d}", i, i));
            asyncClient.createDocument(getCollectionLink(), doc, null, true).toBlocking().single();
        }
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(asyncClient, createdDatabase);
        Utils.safeClose(asyncClient);
    }

    /**
     * Query for documents using java8 lambda expressions
     * Creates a document query observable and verifies the async behavior
     * of document query observable
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_Async() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options);

        final CountDownLatch mainThreadBarrier = new CountDownLatch(1);

        final CountDownLatch resultsCountDown = new CountDownLatch(numberOfDocuments);

        // forEach(.) is an alias for subscribe(.)
        documentQueryObservable.forEach(page -> {
            try {
                // Waits on the barrier
                mainThreadBarrier.await();
            } catch (InterruptedException e) {
            }

            for (@SuppressWarnings("unused")
                    Document d : page.results()) {
                resultsCountDown.countDown();
            }
        });

        // The following code will run concurrently
        System.out.println("action is subscribed to the observable");

        // Release main thread barrier
        System.out.println("after main thread barrier is released, subscribed observable action can continue");
        mainThreadBarrier.countDown();

        System.out.println("waiting for all the results using result count down latch");

        resultsCountDown.await();
    }

    /**
     * Query for documents, without using java8 lambda expressions
     * Creates a document query observable and verifies the async behavior
     * of document query observable
     * NOTE: does the same thing as testQueryDocuments_Async without java8 lambda
     * expression
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_Async_withoutLambda() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options);

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

                for (Document d : t.results()) {
                    resultsCountDown.countDown();
                }
            }
        };

        // forEach(.) is an alias for subscribe(.)
        documentQueryObservable.forEach(actionPerPage);
        // The following code will run concurrently

        System.out.println("action is subscribed to the observable");

        // Release main thread barrier
        System.out.println("after main thread barrier is released, subscribed observable action can continue");
        mainThreadBarrier.countDown();

        System.out.println("waiting for all the results using result count down latch");

        resultsCountDown.await();
    }

    /**
     * Queries for documents and sum up the total request charge
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_findTotalRequestCharge() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<Double> totalChargeObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options)
                .map(FeedResponse::requestCharge) // Map the page to its request charge
                .reduce((totalCharge, charge) -> totalCharge + charge); // Sum up all the request charges

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // subscribe(.) is the same as forEach(.)
        totalChargeObservable.subscribe(totalCharge -> {
            System.out.println(totalCharge);
            successfulCompletionLatch.countDown();
        });

        successfulCompletionLatch.await();
    }

    /**
     * Subscriber unsubscribes after first page
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_unsubscribeAfterFirstPage() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<FeedResponse<Document>> requestChargeObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options);

        AtomicInteger onNextCounter = new AtomicInteger();
        AtomicInteger onCompletedCounter = new AtomicInteger();
        AtomicInteger onErrorCounter = new AtomicInteger();

        // Subscribe to the pages of Documents emitted by the observable
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

        // After subscriber unsubscribes, it doesn't receive any more pages.
        assertThat(onNextCounter.get(), equalTo(1));
        assertThat(onCompletedCounter.get(), equalTo(0));
        assertThat(onErrorCounter.get(), equalTo(0));
    }

    /**
     * Queries for documents and filter out the fetched results
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_filterFetchedResults() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Func1<Document, Boolean> isPrimeNumber = new Func1<Document, Boolean>() {

            @Override
            public Boolean call(Document doc) {
                int n = doc.getInt("counter");
                if (n <= 1)
                    return false;
                for (int i = 2; 2 * i < n; i++) {
                    if (n % i == 0)
                        return false;
                }
                return true;
            }
        };

        List<Document> resultList = Collections.synchronizedList(new ArrayList<Document>());

        asyncClient.queryDocuments(getCollectionLink(), "SELECT * FROM root", options)
                .map(FeedResponse::results) // Map the page to the list of documents
                .concatMap(Observable::from) // Flatten the observable<list<document>> to observable<document>
                .filter(isPrimeNumber) // Filter documents using isPrimeNumber predicate
                .subscribe(doc -> resultList.add(doc)); // Collect the results

        Thread.sleep(4000);

        int expectedNumberOfPrimes = 0;
        // Find all the documents with prime number counter
        for (int i = 0; i < numberOfDocuments; i++) {
            boolean isPrime = true;
            if (i <= 1)
                isPrime = false;
            for (int j = 2; 2 * j < i; j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }

            if (isPrime) {
                expectedNumberOfPrimes++;
            }
        }

        // Assert that we only collected what's expected
        assertThat(resultList, hasSize(expectedNumberOfPrimes));
    }

    /**
     * Queries for documents
     * Converts the document query observable to blocking observable and
     * uses that to find all documents
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void queryDocuments_toBlocking_toIterator() {
        // Query for documents
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options);

        // Covert the observable to a blocking observable, then convert the blocking
        // observable to an iterator
        Iterator<FeedResponse<Document>> it = documentQueryObservable.toBlocking().getIterator();

        int pageCounter = 0;
        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<Document> page = it.next();
            pageCounter++;

            String pageSizeAsString = page.responseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat("header item count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            assertThat("Result size must match header item count", page.results(), hasSize(pageSize));
            numberOfResults += pageSize;
        }
        assertThat("number of total results", numberOfResults, equalTo(numberOfDocuments));
        assertThat("number of result pages", pageCounter,
                   equalTo((numberOfDocuments + requestPageSize - 1) / requestPageSize));
    }

    /**
     * Queries for documents using an Orderby query.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void qrderBy_Async() throws Exception {
        // CREATE a partitioned collection
        String collectionId = UUID.randomUUID().toString();
        DocumentCollection multiPartitionCollection = createMultiPartitionCollection("dbs/" + createdDatabase.id(),
                                                                                     collectionId, "/key");

        // Insert documents
        int totalNumberOfDocumentsInMultiPartitionCollection = 10;
        for (int i = 0; i < totalNumberOfDocumentsInMultiPartitionCollection; i++) {

            Document doc = new Document(String.format("{\"id\":\"documentId%d\",\"key\":\"%s\",\"prop\":%d}", i,
                                                      RandomStringUtils.randomAlphabetic(2), i));
            asyncClient.createDocument("dbs/" + createdDatabase.id() + "/colls/" + multiPartitionCollection.id(),
                                       doc, null, true).toBlocking().single();
        }

        // Query for the documents order by the prop field
        SqlQuerySpec query = new SqlQuerySpec("SELECT r.id FROM r ORDER BY r.prop", new SqlParameterCollection());
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxItemCount(5);

        // Max degree of parallelism determines the number of partitions that
        // the SDK establishes simultaneous connections to.
        options.maxDegreeOfParallelism(2);

        // Get the observable order by query documents
        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient.queryDocuments(
                "dbs/" + createdDatabase.id() + "/colls/" + multiPartitionCollection.id(), query, options);

        List<String> resultList = Collections.synchronizedList(new ArrayList<>());

        documentQueryObservable.map(FeedResponse::results)
                // Map the logical page to the list of documents in the page
                .concatMap(Observable::from) // Flatten the list of documents
                .map(doc -> doc.id()) // Map to the document Id
                .forEach(docId -> resultList.add(docId)); // Add each document Id to the resultList

        Thread.sleep(4000);

        // Assert we found all the results
        assertThat(resultList, hasSize(totalNumberOfDocumentsInMultiPartitionCollection));
        for (int i = 0; i < totalNumberOfDocumentsInMultiPartitionCollection; i++) {
            String docId = resultList.get(i);
            // Assert that the order of the documents are valid
            assertThat(docId, equalTo("documentId" + i));
        }
    }

    /**
     * You can convert an Observable to a ListenableFuture.
     * ListenableFuture (part of google guava library) is a popular extension
     * of Java's Future which allows registering listener callbacks:
     * https://github.com/google/guava/wiki/ListenableFutureExplained
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToGoogleGuavaListenableFuture() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Observable<FeedResponse<Document>> documentQueryObservable = asyncClient
                .queryDocuments(getCollectionLink(), "SELECT * FROM root", options);

        // Convert to observable of list of pages
        Observable<List<FeedResponse<Document>>> allPagesObservable = documentQueryObservable.toList();

        // Convert the observable of list of pages to a Future
        ListenableFuture<List<FeedResponse<Document>>> future = ListenableFutureObservable.to(allPagesObservable);

        List<FeedResponse<Document>> pageList = future.get();

        int totalNumberOfRetrievedDocuments = 0;
        for (FeedResponse<Document> page : pageList) {
            totalNumberOfRetrievedDocuments += page.results().size();
        }
        assertThat(numberOfDocuments, equalTo(totalNumberOfRetrievedDocuments));
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id();
    }

    private DocumentCollection createMultiPartitionCollection(String databaseLink, String collectionId,
                                                              String partitionKeyPath) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(partitionKeyPath);
        partitionKeyDef.paths(paths);

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(collectionId);
        collectionDefinition.setPartitionKey(partitionKeyDef);
        DocumentCollection createdCollection = asyncClient.createCollection(databaseLink, collectionDefinition, options)
                .toBlocking().single().getResource();

        return createdCollection;
    }
}
