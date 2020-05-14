// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.examples;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class demonstrates how to use Async API for
 * Conflicts.
 * <p>
 * Also if you need to work with Future or CompletableFuture it is possible to
 * transform a flux to CompletableFuture. Please see
 * {@link #transformObservableToCompletableFuture()}
 */

public class ConflictAPITest extends DocumentClientTest {
    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private DocumentCollection createdCollection;
    private Database createdDatabase;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void before_ConflictAPITest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        // CREATE collection
        createdCollection = client
                .createCollection("/dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .single().block().getResource();

        int numberOfDocuments = 20;
        // Add documents
        List<Mono<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfDocuments; i++) {
            Document doc = new Document(String.format("{ 'id': 'loc%d', 'counter': %d}", i, i));
            tasks.add(client.createDocument(getCollectionLink(), doc, null, true).then());
        }
        Flux.merge(tasks).then().block();
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
        ModelBridgeInternal.setFeedOptionsMaxItemCount(options, requestPageSize);

        Flux<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Covert the flux to an iterable, and then to iterator
        Iterator<FeedResponse<Conflict>> it = conflictReadFeedObservable.toIterable().iterator();

        int expectedNumberOfConflicts = 0;

        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<Conflict> page = it.next();
            System.out.println("items: " + page.getResults());
            String pageSizeAsString = page.getResponseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat("header getItem count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            assertThat("Result size must match header getItem count", page.getResults(), hasSize(pageSize));
            numberOfResults += pageSize;
        }
        assertThat("number of total results", numberOfResults, equalTo(expectedNumberOfConflicts));
    }

    /**
     * You can convert a Flux to a CompletableFuture.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToCompletableFuture() throws Exception {
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        ModelBridgeInternal.setFeedOptionsMaxItemCount(options, requestPageSize);

        Flux<FeedResponse<Conflict>> conflictReadFeedObservable = client
                .readConflicts(getCollectionLink(), options);

        // Convert to observable of list of pages
        Mono<List<FeedResponse<Conflict>>> allPagesObservable = conflictReadFeedObservable.collectList();

        // Convert the observable of list of pages to a Future
        CompletableFuture<List<FeedResponse<Conflict>>> future = allPagesObservable.toFuture();

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

