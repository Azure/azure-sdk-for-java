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

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This integration test class demonstrates how to use Async API for
 * Conflicts.
 *
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #testTransformObservableToGoogleGuavaListenableFuture()}
 * 
 */
public class ConflictAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConflictAPITest.class);
    private static final String DATABASE_ID = Utils.getDatabaseId(ConflictAPITest.class);

    private AsyncDocumentClient client;

    private DocumentCollection createdCollection;
    private Database createdDatabase;

    @Before
    public void setUp() {

        client = new AsyncDocumentClient.Builder()
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

        // Create database
        ResourceResponse<Database> databaseCreationResponse = client.createDatabase(databaseDefinition, null)
                .toBlocking().single();

        createdDatabase = databaseCreationResponse.getResource();

        // Create collection
        createdCollection = client
                .createCollection("/dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .toBlocking().single().getResource();


        int numberOfDocuments = 20;
        // Add documents
        for (int i = 0; i < numberOfDocuments; i++) {
            Document doc = new Document(String.format("{ 'id': 'loc%d', 'counter': %d}", i, i));
            client.createDocument(getCollectionLink(), doc, null, true).toBlocking().single();
        }
    }

    @After
    public void shutdown() {
        Utils.safeclean(client, DATABASE_ID);
    }

    /**
     * Read conflicts
     * Converts the conflict read feed observable to blocking observable and
     * uses that to find all conflicts
     */
    @Test
    public void testReadConflicts_toBlocking_toIterator() {
        // read all conflicts
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Covert the observable to a blocking observable, then convert the blocking
        // observable to an iterator
        Iterator<FeedResponse<Conflict>> it = conflictReadFeedObservable.toBlocking().getIterator();

        int expectedNumberOfConflicts = 0;

        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<Conflict> page = it.next();
            System.out.println("items: " + page.getResults());
            String pageSizeAsString = page.getResponseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat("header item count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            assertThat("Result size must match header item count", page.getResults(), hasSize(pageSize));
            numberOfResults += pageSize;
        }
        assertThat("number of total results", numberOfResults, equalTo(expectedNumberOfConflicts));
    }

    /**
     * You can convert an Observable to a ListenableFuture.
     * ListenableFuture (part of google guava library) is a popular extension
     * of Java's Future which allows registering listener callbacks:
     * https://github.com/google/guava/wiki/ListenableFutureExplained
     */
    @Test
    public void testTransformObservableToGoogleGuavaListenableFuture() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Convert to observable of list of pages
        Observable<List<FeedResponse<Conflict>>> allPagesObservable = conflictReadFeedObservable.toList();

        // Convert the observable of list of pages to a Future
        ListenableFuture<List<FeedResponse<Conflict>>> future = ListenableFutureObservable.to(allPagesObservable);

        List<FeedResponse<Conflict>> pageList = future.get();

        int totalNumberOfRetrievedConflicts = 0;
        for (FeedResponse<Conflict> page : pageList) {
            totalNumberOfRetrievedConflicts += page.getResults().size();
        }
        assertThat(0, equalTo(totalNumberOfRetrievedConflicts));
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }

    private void cleanUpGeneratedDatabases() {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponse<Database>> feedResponsePages = client
                        .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterCollection(new SqlParameter("@id", id))), null)
                        .toList().toBlocking().single();

                if (!feedResponsePages.get(0).getResults().isEmpty()) {
                    Database res = feedResponsePages.get(0).getResults().get(0);
                    LOGGER.info("deleting a database " + feedResponsePages.get(0));
                    client.deleteDatabase("dbs/" + res.getId(), null).toBlocking().single();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

