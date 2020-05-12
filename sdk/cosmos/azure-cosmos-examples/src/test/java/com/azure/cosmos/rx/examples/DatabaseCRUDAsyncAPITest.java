// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.examples;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

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
 * Also if you need to work with Future or CompletableFuture it is possible to
 * transform a flux to CompletableFuture. Please see
 * {@link #transformObservableToCompletableFuture()}
 */
public class DatabaseCRUDAsyncAPITest extends DocumentClientTest {
    private final static int TIMEOUT = 60000;
    private final List<String> databaseIds = new ArrayList<>();

    private AsyncDocumentClient client;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void before_DatabaseCRUDAsyncAPITest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();
    }

    private Database getDatabaseDefinition() {
        Database databaseDefinition = new Database();
        databaseDefinition.setId(Utils.generateDatabaseId());

        databaseIds.add(databaseDefinition.getId());

        return databaseDefinition;
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        for (String id : databaseIds) {
            Utils.safeClean(client, id);
        }
        Utils.safeClose(client);
    }

    /**
     * CREATE a database using async api.
     * This test uses java8 lambda expression.
     * See testCreateDatabase_Async_withoutLambda for usage without lambda.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDatabase_Async() throws Exception {
        Mono<ResourceResponse<Database>> createDatabaseObservable = client.createDatabase(getDatabaseDefinition(),
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
        Mono<ResourceResponse<Database>> createDatabaseObservable = client.createDatabase(getDatabaseDefinition(),
                                                                                                     null);

        final CountDownLatch completionLatch = new CountDownLatch(1);
        Consumer<ResourceResponse<Database>> onDatabaseCreationAction = new Consumer<ResourceResponse<Database>>() {

            @Override
            public void accept(ResourceResponse<Database> resourceResponse) {
                // Database is created
                System.out.println(resourceResponse.getActivityId());
                completionLatch.countDown();
            }
        };

        Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable error) {
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
        Mono<ResourceResponse<Database>> createDatabaseObservable = client.createDatabase(getDatabaseDefinition(),
                                                                                                     null);

        // toBlocking() converts to a blocking observable.
        // single() gets the only result.
        createDatabaseObservable.single().block();
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
        client.createDatabase(databaseDefinition, null).single().block();

        // CREATE the database for test.
        Mono<ResourceResponse<Database>> databaseForTestObservable = client
                .createDatabase(databaseDefinition, null);

        try {
            databaseForTestObservable.single() // Single
                    .block(); // Blocks to get the result
            assertThat("Should not reach here", false);
        } catch (CosmosClientException e) {
            assertThat("Database already exists.", e.getStatusCode(),
                       equalTo(409));
        }
    }

    /**
     * You can convert a Flux to a CompletableFuture.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToCompletableFuture() throws Exception {
        Mono<ResourceResponse<Database>> createDatabaseObservable = client.createDatabase(getDatabaseDefinition(),
                                                                                                     null);
        CompletableFuture<ResourceResponse<Database>> future = createDatabaseObservable.single().toFuture();

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
        Database database = client.createDatabase(getDatabaseDefinition(), null).single().block().getResource();

        // READ the created getDatabase using async api
        Mono<ResourceResponse<Database>> readDatabaseObservable = client.readDatabase("dbs/" + database.getId(),
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
        Database database = client.createDatabase(getDatabaseDefinition(), null).single().block().getResource();

        // DELETE the created database using async api
        Mono<ResourceResponse<Database>> deleteDatabaseObservable = client
                .deleteDatabase("dbs/" + database.getId(), null);

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
        client.createDatabase(databaseDefinition, null).single().block().getResource();

        // Query the created database using async api
        Flux<FeedResponse<Database>> queryDatabaseObservable = client
                .queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseDefinition.getId()), null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        queryDatabaseObservable.collectList().subscribe(databaseFeedResponseList -> {
            // toList() should return a list of size 1
            assertThat(databaseFeedResponseList.size(), equalTo(1));

            // First element of the list should have only 1 result
            FeedResponse<Database> databaseFeedResponse = databaseFeedResponseList.get(0);
            assertThat(databaseFeedResponse.getResults().size(), equalTo(1));

            // This getDatabase should have the same getId as the one we created
            Database foundDatabase = databaseFeedResponse.getResults().get(0);
            assertThat(foundDatabase.getId(), equalTo(databaseDefinition.getId()));

            System.out.println(databaseFeedResponse.getActivityId());
            completionLatch.countDown();
        }, error -> {
            System.err.println("an error occurred while querying the database: actual cause: " + error.getMessage());
            completionLatch.countDown();
        });

        // Wait till database query completes
        completionLatch.await();
    }
}
