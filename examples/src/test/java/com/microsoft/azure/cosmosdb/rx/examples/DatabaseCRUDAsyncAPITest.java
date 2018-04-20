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

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedResponse;
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
 * delete, replace, and update Databases.
 * 
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * 
 * You can also use the async API without java8 lambda expression support.
 * 
 * For example
 * <ul>
 * <li>{@link #testCreateDatabase_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 * 
 * <li>{@link #testCreateDatabase_Async_withoutLambda()} demonstrates how to 
 * do the same thing without lambda expression.
 * </ul>
 * 
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 */
public class DatabaseCRUDAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCRUDAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";
    private Database databaseDefinition;

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

        databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    /**
     * Create a database using async api.
     * This test uses java8 lambda expression.
     * See testCreateDatabase_Async_withoutLambda for usage without lambda.
     */
    @Test
    public void testCreateDatabase_Async() throws Exception {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(databaseDefinition,
                null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        createDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the database: actual cause: " + error.getMessage());
                });

        // Wait till database creation completes
        successfulCompletionLatch.await();
    }

    /**
     * Create a database using async api, without java8 lambda expressions
     */
    @Test
    public void testCreateDatabase_Async_withoutLambda() throws Exception {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(databaseDefinition,
                null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);
        Action1<ResourceResponse<Database>> onDatabaseCreationAction = new Action1<ResourceResponse<Database>>() {

            @Override
            public void call(ResourceResponse<Database> resourceResponse) {
                // Database is created
                System.out.println(resourceResponse.getActivityId());
                successfulCompletionLatch.countDown();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.err
                        .println("an error occurred while creating the database: actual cause: " + error.getMessage());
            }
        };

        createDatabaseObservable.single() // We know there is only a single event
                .subscribe(onDatabaseCreationAction, onError);

        // Wait till database creation completes
        successfulCompletionLatch.await();
    }

    /**
     * Create a database in a blocking manner
     */
    @Test
    public void testCreateDatabase_toBlocking() throws DocumentClientException {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(databaseDefinition,
                null);

        // toBlocking() converts to a blocking observable.
        // single() gets the only result.
        createDatabaseObservable.toBlocking().single();
    }

    /**
     * Attempt to create a database which already exists
     * - First create a database
     * - Using the async api generate an async database creation observable
     * - Converts the Observable to blocking using Observable.toBlocking() api
     * - Catch already exist failure (409)
     */
    @Test
    public void testCreateDatabase_toBlocking_DatabaseAlreadyExists_Fails() throws DocumentClientException {
        asyncClient.createDatabase(databaseDefinition, null).toBlocking().single();

        // Create the database for test.
        Observable<ResourceResponse<Database>> databaseForTestObservable = asyncClient
                .createDatabase(databaseDefinition, null);

        try {
            databaseForTestObservable.toBlocking() // Blocks
                    .single(); // Gets the single result
            assertThat("Should not reach here", false);
        } catch (Exception e) {
            assertThat("Database already exists.", ((DocumentClientException) e.getCause()).getStatusCode(),
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
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(databaseDefinition,
                null);
        ListenableFuture<ResourceResponse<Database>> future = ListenableFutureObservable.to(createDatabaseObservable);

        ResourceResponse<Database> rrd = future.get();

        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.print(rrd.getRequestCharge());
    }

    /**
     * Read a Database in an Async manner
     */
    @Test
    public void testCreateAndReadDatabase() throws Exception {
        // Create a database
        Database database = asyncClient.createDatabase(databaseDefinition, null).toBlocking().single().getResource();

        // Read the created database using async api
        Observable<ResourceResponse<Database>> readDatabaseObservable = asyncClient.readDatabase("dbs/" + database.getId(),
                null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        readDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while reading the database: actual cause: " + error.getMessage());
                });

        // Wait till read database completes
        successfulCompletionLatch.await();
    }

    /**
     * Delete a Database in an Async manner
     */
    @Test
    public void testCreateAndDeleteDatabase() throws Exception {
        // Create a database
        Database database = asyncClient.createDatabase(databaseDefinition, null).toBlocking().single().getResource();

        // Delete the created database using async api
        Observable<ResourceResponse<Database>> deleteDatabaseObservable = asyncClient
                .deleteDatabase("dbs/" + database.getId(), null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        deleteDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    successfulCompletionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while deleting the database: actual cause: " + error.getMessage());
                });

        // Wait till database deletion completes
        successfulCompletionLatch.await();
    }

    /**
     * Query a Database in an Async manner
     */
    @Test
    public void testDatabaseCreateAndQuery() throws Exception {
        // Create a database
        asyncClient.createDatabase(databaseDefinition, null).toBlocking().single().getResource();

        // Query the created database using async api
        Observable<FeedResponse<Database>> queryDatabaseObservable = asyncClient
                .queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseDefinition.getId()), null);

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        queryDatabaseObservable.toList().subscribe(databaseFeedResponseList -> {
            // toList() should return a list of size 1
            assertThat(databaseFeedResponseList.size(), equalTo(1));

            // First element of the list should have only 1 result
            FeedResponse<Database> databaseFeedResponse = databaseFeedResponseList.get(0);
            assertThat(databaseFeedResponse.getResults().size(), equalTo(1));

            // This database should have the same id as the one we created
            Database foundDatabase = databaseFeedResponse.getResults().get(0);
            assertThat(foundDatabase.getId(), equalTo(databaseDefinition.getId()));

            System.out.println(databaseFeedResponse.getActivityId());
            successfulCompletionLatch.countDown();
        }, error -> {
            System.err.println("an error occurred while querying the database: actual cause: " + error.getMessage());
        });

        // Wait till database query completes
        successfulCompletionLatch.await();
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
