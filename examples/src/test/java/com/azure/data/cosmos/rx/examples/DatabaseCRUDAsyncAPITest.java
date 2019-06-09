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

import com.azure.data.cosmos.*;
import com.google.common.util.concurrent.ListenableFuture;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and update Databases.
 * <p>
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * <p>
 * You can also use the async API without java8 lambda expression support.
 * <p>
 * For example
 * <ul>
 * <li>{@link #createDatabase_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 *
 * <li>{@link #createDatabase_Async_withoutLambda()} demonstrates how to
 * do the same thing without lambda expression.
 * </ul>
 * <p>
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #transformObservableToGoogleGuavaListenableFuture()}
 */
public class DatabaseCRUDAsyncAPITest {
    private final static int TIMEOUT = 60000;
    private final List<String> databaseIds = new ArrayList<>();

    private AsyncDocumentClient asyncClient;

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
    }

    private Database getDatabaseDefinition() {
        Database databaseDefinition = new Database();
        databaseDefinition.id(Utils.generateDatabaseId());

        databaseIds.add(databaseDefinition.id());

        return databaseDefinition;
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        for (String id : databaseIds) {
            Utils.safeClean(asyncClient, id);
        }
        Utils.safeClose(asyncClient);
    }

    /**
     * CREATE a database using async api.
     * This test uses java8 lambda expression.
     * See testCreateDatabase_Async_withoutLambda for usage without lambda.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDatabase_Async() throws Exception {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(getDatabaseDefinition(),
                                                                                                     null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        createDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    completionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the database: actual cause: " + error.getMessage());
                    completionLatch.countDown();
                });

        // Wait till database creation completes
        completionLatch.await();
    }

    /**
     * CREATE a database using async api, without java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDatabase_Async_withoutLambda() throws Exception {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(getDatabaseDefinition(),
                                                                                                     null);

        final CountDownLatch completionLatch = new CountDownLatch(1);
        Action1<ResourceResponse<Database>> onDatabaseCreationAction = new Action1<ResourceResponse<Database>>() {

            @Override
            public void call(ResourceResponse<Database> resourceResponse) {
                // Database is created
                System.out.println(resourceResponse.getActivityId());
                completionLatch.countDown();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.err
                        .println("an error occurred while creating the database: actual cause: " + error.getMessage());
                completionLatch.countDown();
            }
        };

        createDatabaseObservable.single() // We know there is only a single event
                .subscribe(onDatabaseCreationAction, onError);

        // Wait till database creation completes
        completionLatch.await();
    }

    /**
     * CREATE a database in a blocking manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDatabase_toBlocking() {
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(getDatabaseDefinition(),
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
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDatabase_toBlocking_DatabaseAlreadyExists_Fails() {
        Database databaseDefinition = getDatabaseDefinition();
        asyncClient.createDatabase(databaseDefinition, null).toBlocking().single();

        // CREATE the database for test.
        Observable<ResourceResponse<Database>> databaseForTestObservable = asyncClient
                .createDatabase(databaseDefinition, null);

        try {
            databaseForTestObservable.toBlocking() // Blocks
                    .single(); // Gets the single result
            assertThat("Should not reach here", false);
        } catch (Exception e) {
            assertThat("Database already exists.", ((CosmosClientException) e.getCause()).statusCode(),
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
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(getDatabaseDefinition(),
                                                                                                     null);
        ListenableFuture<ResourceResponse<Database>> future = ListenableFutureObservable.to(createDatabaseObservable);

        ResourceResponse<Database> rrd = future.get();

        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.print(rrd.getRequestCharge());
    }

    /**
     * READ a Database in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createAndReadDatabase() throws Exception {
        // CREATE a database
        Database database = asyncClient.createDatabase(getDatabaseDefinition(), null).toBlocking().single().getResource();

        // READ the created database using async api
        Observable<ResourceResponse<Database>> readDatabaseObservable = asyncClient.readDatabase("dbs/" + database.id(),
                                                                                                 null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        readDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    completionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while reading the database: actual cause: " + error.getMessage());
                    completionLatch.countDown();
                });

        // Wait till read database completes
        completionLatch.await();
    }

    /**
     * DELETE a Database in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createAndDeleteDatabase() throws Exception {
        // CREATE a database
        Database database = asyncClient.createDatabase(getDatabaseDefinition(), null).toBlocking().single().getResource();

        // DELETE the created database using async api
        Observable<ResourceResponse<Database>> deleteDatabaseObservable = asyncClient
                .deleteDatabase("dbs/" + database.id(), null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        deleteDatabaseObservable.single() // We know there is only single result
                .subscribe(databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    completionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while deleting the database: actual cause: " + error.getMessage());
                    completionLatch.countDown();
                });

        // Wait till database deletion completes
        completionLatch.await();
    }

    /**
     * Query a Database in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void databaseCreateAndQuery() throws Exception {
        // CREATE a database
        Database databaseDefinition = getDatabaseDefinition();
        asyncClient.createDatabase(databaseDefinition, null).toBlocking().single().getResource();

        // Query the created database using async api
        Observable<FeedResponse<Database>> queryDatabaseObservable = asyncClient
                .queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseDefinition.id()), null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        queryDatabaseObservable.toList().subscribe(databaseFeedResponseList -> {
            // toList() should return a list of size 1
            assertThat(databaseFeedResponseList.size(), equalTo(1));

            // First element of the list should have only 1 result
            FeedResponse<Database> databaseFeedResponse = databaseFeedResponseList.get(0);
            assertThat(databaseFeedResponse.results().size(), equalTo(1));

            // This database should have the same id as the one we created
            Database foundDatabase = databaseFeedResponse.results().get(0);
            assertThat(foundDatabase.id(), equalTo(databaseDefinition.id()));

            System.out.println(databaseFeedResponse.activityId());
            completionLatch.countDown();
        }, error -> {
            System.err.println("an error occurred while querying the database: actual cause: " + error.getMessage());
            completionLatch.countDown();
        });

        // Wait till database query completes
        completionLatch.await();
    }
}
