/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.SqlParameter;
import com.microsoft.azure.documentdb.SqlParameterCollection;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;
import com.microsoft.azure.documentdb.rx.examples.TestConfigurations;

import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and update.
 * 
 * NOTE: you can use rxJava based async api with java8 lambda expression. Using of
 * rxJava based async APIs with java8 lambda expressions is much prettier.
 * 
 * You can also use the async API without java8 lambda expression support.
 * 
 * For example
 * <ul>
 * <li>{@link #testCreateDatabase_Async()} demonstrates how to use async api with
 * java8 lambda expression.
 * 
 * <li>{@link #testCreateDatabase_Async_withoutLambda()} demonstrates how to the same
 * thing without lambda expression.
 * </ul>
 * 
 * Also if you need to work with Future or ListenableFuture it is possible to transform
 * an observable to ListenableFuture. Please see {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 */
public class DatabaseAndCollectionCreationAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAndCollectionCreationAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";
    private DocumentCollection collectionDefinition;
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
        
        collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
    }
    
    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    @Test
    public void testCreateDatabase_Async() throws Exception {

        // create a database using async api
        // this test uses java8 lambda expression see testCreateDatabase_Async_withoutLambda
        
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient
                .createDatabase(databaseDefinition, null);

        final CountDownLatch doneLatch = new CountDownLatch(1);

        createDatabaseObservable
            .single()   // we know there is only single result
            .subscribe(
                databaseResourceResponse -> {
                    System.out.println(databaseResourceResponse.getActivityId());
                    doneLatch.countDown();
                },

                error -> {
                    System.err.println("an error happened in database creation: actual cause: " + error.getMessage());
                }
        );
        
        // wait till database creation completes
        doneLatch.await();
    }
    
    @Test
    public void testCreateDatabase_Async_withoutLambda() throws Exception {
        
        // create a database using async api
        
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient
                .createDatabase(databaseDefinition, null);

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
                System.err.println("an error happened in database creation: actual cause: " + error.getMessage());
            }
        };

        createDatabaseObservable
            .single()           //we know there is only a single event
            .subscribe(onDatabaseCreationAction, onError);
        
        // wait till database creation completes
        successfulCompletionLatch.await();
    }
    
    
    @Test
    public void testCreateDatabase_toBlocking() throws DocumentClientException {
        
        // create a database
        // toBlocking() converts the observable to a blocking observable
        
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient
                .createDatabase(databaseDefinition, null);
        
        // toBlocking() converts to a blocking observable
        // single() gets the only result
        createDatabaseObservable.toBlocking().single();
    }

    @Test
    public void testCreateDatabase_toBlocking_DatabaseAlreadyExists_Fails() throws DocumentClientException {

        // attempt to create a database which already exists
        // - first create a database
        // - Using the async api generate an async database creation observable
        // - Converts the Observable to blocking using Observable.toBlocking() api
        // - catch already exist failure (409)
        
        asyncClient.createDatabase(databaseDefinition, null).toBlocking().single();

        // Create the database for test.
        Observable<ResourceResponse<Database>> databaseForTestObservable = asyncClient
                .createDatabase(databaseDefinition, null);

        try {
            databaseForTestObservable
                .toBlocking()           //blocks
                .single();              //gets the single result
            assertThat("Should not reach here", false);
        } catch (Exception e) {
            assertThat("Database already exists.", 
                    ((DocumentClientException) e.getCause()).getStatusCode(), equalTo(409));
        }
    }
    
    @Test
    public void testTransformObservableToGoogleGuavaListenableFuture() throws Exception {
        
        // You can convert an Observable to a ListenableFuture.
        // ListenableFuture (part of google guava library) is a popular extension
        // of Java's Future which allows registering listener callbacks:
        // https://github.com/google/guava/wiki/ListenableFutureExplained
        
        Observable<ResourceResponse<Database>> createDatabaseObservable = asyncClient.createDatabase(databaseDefinition, null);
        ListenableFuture<ResourceResponse<Database>> future = ListenableFutureObservable.to(createDatabaseObservable);

        ResourceResponse<Database> rrd = future.get();
        
        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.print(rrd.getRequestCharge());
    }

    private void cleanUpGeneratedDatabases() throws DocumentClientException {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponsePage<Database>> feedResponsePages = asyncClient
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
