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
import com.microsoft.azure.cosmosdb.Conflict;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class demonstrates how to use Async API for
 * Conflicts.
 * <p>
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #transformObservableToGoogleGuavaListenableFuture()}
 */
public class ConflictAPITest {
    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private DocumentCollection createdCollection;
    private Database createdDatabase;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // Create database
        createdDatabase = Utils.createDatabaseForTest(client);

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

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    /**
     * Read conflicts
     * Converts the conflict read feed observable to blocking observable and
     * uses that to find all conflicts
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void readConflicts_toBlocking_toIterator() {
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
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToGoogleGuavaListenableFuture() throws Exception {
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
}

