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
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.examples.TestConfigurations;

import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and update Document Collections.
 * 
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * 
 * You can also use the async API without java8 lambda expression support.
 * 
 * For example
 * <ul>
 * <li>{@link #testCreateCollection_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 * 
 * <li>{@link #testCreateCollection_Async_withoutLambda()} demonstrates how to
 * do the same thing without lambda expression.
 * </ul>
 * 
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 * To Modify the Collection's throughput after it has been created, you need to
 * update the corresponding Offer. Please see
 * {@see com.microsoft.azure.cosmosdb.rx.examples.OfferCRUDAsyncAPITest#testUpdateOffer()}
 */
public class CollectionCRUDAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionCRUDAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";
    private Database createdDatabase;
    private DocumentCollection collectionDefinition;

    private AsyncDocumentClient asyncClient;

    @Before
    public void setUp() throws DocumentClientException {

        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        // Clean up before setting up
        this.cleanUpGeneratedDatabases();

        createdDatabase = new Database();
        createdDatabase.setId(DATABASE_ID);
        createdDatabase = asyncClient.createDatabase(createdDatabase, null).toBlocking().single().getResource();

        collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    /**
     * Create a document collection using async api.
     * If you want a single partition collection with 10,000 RU/s throughput,
     * the only way to do so is to create a single partition collection with lower
     * throughput (400) and then increase the throughput.
     */
    @Test
    public void testCreateCollection_SinglePartition_Async() throws Exception {
        RequestOptions singlePartitionRequestOptions = new RequestOptions();
        singlePartitionRequestOptions.setOfferThroughput(400);
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, singlePartitionRequestOptions);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        createCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the collection: actual cause: " + error.getMessage());
                });

        // Wait till collection creation completes
        successfulCompletionLatch.await();
    }

    /** 
     * Create a document collection using async api.
     * This test uses java8 lambda expression.
     * See testCreateCollection_Async_withoutLambda for usage without lambda
     * expressions.
     * Set the throughput to be > 10,000 RU/s
     * to create a multi partition collection.
     */
    @Test
    public void testCreateCollection_MultiPartition_Async() throws Exception {
        RequestOptions multiPartitionRequestOptions = new RequestOptions();
        multiPartitionRequestOptions.setOfferThroughput(20000);

        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = asyncClient.createCollection(
                getDatabaseLink(), getMultiPartitionCollectionDefinition(), multiPartitionRequestOptions);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        createCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the collection: actual cause: " + error.getMessage());
                });

        // Wait till collection creation completes
        successfulCompletionLatch.await();
    }

    /**
     * Create a document Collection using async api, without java8 lambda expressions
     */
    @Test
    public void testCreateCollection_Async_withoutLambda() throws Exception {
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);
        Action1<ResourceResponse<DocumentCollection>> onCollectionCreationAction = new Action1<ResourceResponse<DocumentCollection>>() {

            @Override
            public void call(ResourceResponse<DocumentCollection> resourceResponse) {
                // Collection is created
                System.out.println(resourceResponse.getActivityId());
                successfulCompletionLatch.countDown();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.err.println(
                        "an error occurred while creating the collection: actual cause: " + error.getMessage());
            }
        };

        createCollectionObservable.single() // We know there is only a single event
                .subscribe(onCollectionCreationAction, onError);

        // Wait till collection creation completes
        successfulCompletionLatch.await();
    }

    /**
     * Create a collection in a blocking manner
     */
    @Test
    public void testCreateCollection_toBlocking() throws DocumentClientException {
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        // toBlocking() converts the observable to a blocking observable.
        // single() gets the only result.
        createCollectionObservable.toBlocking().single();
    }

    /**
     * Attempt to create a Collection which already exists
     *     - First create a Collection
     *     - Using the async api generate an async collection creation observable
     *     - Converts the Observable to blocking using Observable.toBlocking() api
     *     - Catch already exist failure (409)
     */
    @Test
    public void testCreateCollection_toBlocking_CollectionAlreadyExists_Fails() throws DocumentClientException {
        asyncClient.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single();

        // Create the collection for test.
        Observable<ResourceResponse<DocumentCollection>> collectionForTestObservable = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null);

        try {
            collectionForTestObservable.toBlocking() // Blocks
                    .single(); // Gets the single result
            assertThat("Should not reach here", false);
        } catch (Exception e) {
            assertThat("Collection already exists.", ((DocumentClientException) e.getCause()).getStatusCode(),
                    equalTo(409));
        }
    }

    /**
     * You can convert an Observable to a ListenableFuture.
     * ListenableFuture (part of google guava library) is a popular extension
     * of Java's Future which allows registering listener callbacks:
     * https://github.com/google/guava/wiki/ListenableFutureExplained
     */
    @Test
    public void testTransformObservableToGoogleGuavaListenableFuture() throws Exception {
        Observable<ResourceResponse<DocumentCollection>> createCollectionObservable = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null);
        ListenableFuture<ResourceResponse<DocumentCollection>> future = ListenableFutureObservable
                .to(createCollectionObservable);

        ResourceResponse<DocumentCollection> rrd = future.get();

        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.println(rrd.getRequestCharge());
    }

    /**
     * Read a Collection in an Async manner
     */
    @Test
    public void testCreateAndReadCollection() throws Exception {
        // Create a Collection
        DocumentCollection documentCollection = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // Read the created collection using async api
        Observable<ResourceResponse<DocumentCollection>> readCollectionObservable = asyncClient
                .readCollection(getCollectionLink(documentCollection), null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        readCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while reading the collection: actual cause: " + error.getMessage());
                });

        // Wait till read collection completes
        successfulCompletionLatch.await();
    }

    /**
     * Delete a Collection in an Async manner
     */
    @Test
    public void testCreateAndDeleteCollection() throws Exception {
        // Create a Collection
        DocumentCollection documentCollection = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // Delete the created collection using async api
        Observable<ResourceResponse<DocumentCollection>> deleteCollectionObservable = asyncClient
                .deleteCollection(getCollectionLink(documentCollection), null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        deleteCollectionObservable.single() // We know there is only single result
                .subscribe(collectionResourceResponse -> {
                    System.out.println(collectionResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while deleting the collection: actual cause: " + error.getMessage());
                });

        // Wait till collection deletion completes
        successfulCompletionLatch.await();
    }

    /**
     * Query a Collection in an Async manner
     */
    @Test
    public void testCollectionCreateAndQuery() throws Exception {
        // Create a Collection
        DocumentCollection collection = asyncClient
                .createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single()
                .getResource();

        // Query the created collection using async api
        Observable<FeedResponse<DocumentCollection>> queryCollectionObservable = asyncClient.queryCollections(
                getDatabaseLink(), String.format("SELECT * FROM r where r.id = '%s'", collection.getId()),
                null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        queryCollectionObservable.toList().subscribe(collectionFeedResponseList -> {
            // toList() should return a list of size 1
            assertThat(collectionFeedResponseList.size(), equalTo(1));

            // First element of the list should have only 1 result
            FeedResponse<DocumentCollection> collectionFeedResponse = collectionFeedResponseList.get(0);
            assertThat(collectionFeedResponse.getResults().size(), equalTo(1));

            // This collection should have the same id as the one we created
            DocumentCollection foundCollection = collectionFeedResponse.getResults().get(0);
            assertThat(foundCollection.getId(), equalTo(collection.getId()));

            System.out.println(collectionFeedResponse.getActivityId());
            successfulCompletionLatch.countDown();
        }, error -> {
            System.err.println("an error occurred while querying the collection: actual cause: " + error.getMessage());
        });

        // Wait till collection query completes
        successfulCompletionLatch.await();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }

    private String getCollectionLink(DocumentCollection collection) {
        return "dbs/" + createdDatabase.getId() + "/colls/" + collection.getId();
    }

    private DocumentCollection getMultiPartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection.
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        Collection<String> paths = new ArrayList<String>();
        paths.add("/city");
        partitionKeyDefinition.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<Index>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
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
                    asyncClient.deleteDatabase("dbs/" + res.getId(), null).toBlocking().single();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
