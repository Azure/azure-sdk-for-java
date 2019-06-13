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

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.RequestOptions;
import com.azure.data.cosmos.ResourceResponse;
import com.google.common.util.concurrent.ListenableFuture;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and update Document Collections.
 * <p>
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * <p>
 * You can also use the async API without java8 lambda expression support.
 * <p>
 * For example
 * <ul>
 * <li>{@link #createCollection_MultiPartition_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 *
 * <li>{@link #createCollection_Async_withoutLambda()} demonstrates how to
 * do the same thing without lambda expression.
 * </ul>
 * <p>
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #transformObservableToGoogleGuavaListenableFuture()}
 * <p>
 * To Modify the Collection's throughput after it has been created, you need to
 * update the corresponding Offer. Please see
 * {@see com.azure.data.cosmos.rx.examples.OfferCRUDAsyncAPITest#testUpdateOffer()}
 */
public class CollectionCRUDAsyncAPITest extends DocumentClientTest {

    private final static int TIMEOUT = 120000;
    private Database createdDatabase;
    private AsyncDocumentClient client;
    private DocumentCollection collectionDefinition;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        createdDatabase = Utils.createDatabaseForTest(client);
    }

    @BeforeMethod(groups = "samples", timeOut = TIMEOUT)
    public void before() {
        collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    /**
     * CREATE a document collection using async api.
     * If you want a single partition collection with 10,000 RU/s throughput,
     * the only way to do so is to create a single partition collection with lower
     * throughput (400) and then increase the throughput.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createCollection_SinglePartition_Async() throws Exception {
        RequestOptions singlePartitionRequestOptions = new RequestOptions();
        singlePartitionRequestOptions.setOfferThroughput(400);
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = client
                .createCollection(getDatabaseLink(), collectionDefinition, singlePartitionRequestOptions);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        createCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    countDownLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the collection: actual cause: " + error.getMessage());
                    countDownLatch.countDown();
                });

        // Wait till collection creation completes
        countDownLatch.await();
    }

    /**
     * CREATE a document collection using async api.
     * This test uses java8 lambda expression.
     * See testCreateCollection_Async_withoutLambda for usage without lambda
     * expressions.
     * Set the throughput to be > 10,000 RU/s
     * to create a multi partition collection.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createCollection_MultiPartition_Async() throws Exception {
        RequestOptions multiPartitionRequestOptions = new RequestOptions();
        multiPartitionRequestOptions.setOfferThroughput(20000);

        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = client.createCollection(
                getDatabaseLink(), getMultiPartitionCollectionDefinition(), multiPartitionRequestOptions);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        createCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    countDownLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the collection: actual cause: " + error.getMessage());
                    countDownLatch.countDown();
                });

        // Wait till collection creation completes
        countDownLatch.await();
    }

    /**
     * CREATE a document Collection using async api, without java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createCollection_Async_withoutLambda() throws Exception {
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = client
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Action1<ResourceResponse<DocumentCollection>> onCollectionCreationAction = new Action1<ResourceResponse<DocumentCollection>>() {

            @Override
            public void call(ResourceResponse<DocumentCollection> resourceResponse) {
                // Collection is created
                System.out.println(resourceResponse.getActivityId());
                countDownLatch.countDown();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.err.println(
                        "an error occurred while creating the collection: actual cause: " + error.getMessage());
                countDownLatch.countDown();
            }
        };

        createCollectionObservable.single() // We know there is only a single event
                .subscribe(onCollectionCreationAction, onError);

        // Wait till collection creation completes
        countDownLatch.await();
    }

    /**
     * CREATE a collection in a blocking manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createCollection_toBlocking() {
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = client
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        // toBlocking() converts the observable to a blocking observable.
        // single() gets the only result.
        createCollectionObservable.toBlocking().single();
    }

    /**
     * Attempt to create a Collection which already exists
     * - First create a Collection
     * - Using the async api generate an async collection creation observable
     * - Converts the Observable to blocking using Observable.toBlocking() api
     * - Catch already exist failure (409)
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createCollection_toBlocking_CollectionAlreadyExists_Fails() {
        client.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single();

        // CREATE the collection for test.
        Observable<ResourceResponse<DocumentCollection>> collectionForTestObservable = client
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        try {
            collectionForTestObservable.toBlocking() // Blocks
                    .single(); // Gets the single result
            assertThat("Should not reach here", false);
        } catch (Exception e) {
            assertThat("Collection already exists.", ((CosmosClientException) e.getCause()).statusCode(),
                       equalTo(409));
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
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = client
                .createCollection(getDatabaseLink(), collectionDefinition, null);
        ListenableFuture<ResourceResponse<DocumentCollection>> future = ListenableFutureObservable
                .to(createCollectionObservable);

        ResourceResponse<DocumentCollection> rrd = future.get();

        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.println(rrd.getRequestCharge());
    }

    /**
     * READ a Collection in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createAndReadCollection() throws Exception {
        // CREATE a Collection
        DocumentCollection documentCollection = client
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // READ the created collection using async api
        Observable<ResourceResponse<DocumentCollection>> readCollectionObservable = client
                .readCollection(getCollectionLink(documentCollection), null);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        readCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    countDownLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while reading the collection: actual cause: " + error.getMessage());
                    countDownLatch.countDown();
                });

        // Wait till read collection completes
        countDownLatch.await();
    }

    /**
     * DELETE a Collection in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createAndDeleteCollection() throws Exception {
        // CREATE a Collection
        DocumentCollection documentCollection = client
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // DELETE the created collection using async api
        Observable<ResourceResponse<DocumentCollection>> deleteCollectionObservable = client
                .deleteCollection(getCollectionLink(documentCollection), null);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        deleteCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    countDownLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while deleting the collection: actual cause: " + error.getMessage());
                    countDownLatch.countDown();
                });

        // Wait till collection deletion completes
        countDownLatch.await();
    }

    /**
     * Query a Collection in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void collectionCreateAndQuery() throws Exception {
        // CREATE a Collection
        DocumentCollection collection = client
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // Query the created collection using async api
        Observable<FeedResponse<DocumentCollection>> queryCollectionObservable = client.queryCollections(
                getDatabaseLink(), String.format("SELECT * FROM r where r.id = '%s'", collection.id()),
                null);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        queryCollectionObservable.toList().subscribe(collectionFeedResponseList -> {
            // toList() should return a list of size 1
            assertThat(collectionFeedResponseList.size(), equalTo(1));

            // First element of the list should have only 1 result
            FeedResponse<DocumentCollection> collectionFeedResponse = collectionFeedResponseList.get(0);
            assertThat(collectionFeedResponse.results().size(), equalTo(1));

            // This collection should have the same id as the one we created
            DocumentCollection foundCollection = collectionFeedResponse.results().get(0);
            assertThat(foundCollection.id(), equalTo(collection.id()));

            System.out.println(collectionFeedResponse.activityId());
            countDownLatch.countDown();
        }, error -> {
            System.err.println("an error occurred while querying the collection: actual cause: " + error.getMessage());
            countDownLatch.countDown();
        });

        // Wait till collection query completes
        countDownLatch.await();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.id();
    }

    private String getCollectionLink(DocumentCollection collection) {
        return "dbs/" + createdDatabase.id() + "/colls/" + collection.id();
    }

    private DocumentCollection getMultiPartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection.
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        List<String> paths = new ArrayList<>();
        paths.add("/city");
        partitionKeyDefinition.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }
}
