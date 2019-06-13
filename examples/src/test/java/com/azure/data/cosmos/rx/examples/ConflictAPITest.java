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
import com.azure.data.cosmos.Conflict;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.HttpConstants;
import com.google.common.util.concurrent.ListenableFuture;
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
public class ConflictAPITest extends DocumentClientTest {
    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private DocumentCollection createdCollection;
    private Database createdDatabase;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        // CREATE collection
        createdCollection = client
                .createCollection("/dbs/" + createdDatabase.id(), collectionDefinition, null)
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
     * READ conflicts
     * Converts the conflict read feed observable to blocking observable and
     * uses that to find all conflicts
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void readConflicts_toBlocking_toIterator() {
        // read all conflicts
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);

        Observable<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Covert the observable to a blocking observable, then convert the blocking
        // observable to an iterator
        Iterator<FeedResponse<Conflict>> it = conflictReadFeedObservable.toBlocking().getIterator();

        int expectedNumberOfConflicts = 0;

        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<Conflict> page = it.next();
            System.out.println("items: " + page.results());
            String pageSizeAsString = page.responseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat("header item count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            assertThat("Result size must match header item count", page.results(), hasSize(pageSize));
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
        options.maxItemCount(requestPageSize);

        Observable<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Convert to observable of list of pages
        Observable<List<FeedResponse<Conflict>>> allPagesObservable = conflictReadFeedObservable.toList();

        // Convert the observable of list of pages to a Future
        ListenableFuture<List<FeedResponse<Conflict>>> future = ListenableFutureObservable.to(allPagesObservable);

        List<FeedResponse<Conflict>> pageList = future.get();

        int totalNumberOfRetrievedConflicts = 0;
        for (FeedResponse<Conflict> page : pageList) {
            totalNumberOfRetrievedConflicts += page.results().size();
        }
        assertThat(0, equalTo(totalNumberOfRetrievedConflicts));
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id();
    }
}

