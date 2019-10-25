// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.examples;

import com.azure.cosmos.internal.AsyncDocumentClient;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DataType;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.internal.DocumentCollection;
import com.azure.cosmos.IncludedPath;
import com.azure.cosmos.Index;
import com.azure.cosmos.IndexingPolicy;
import com.azure.cosmos.internal.Offer;
import com.azure.cosmos.PartitionKeyDefinition;
import com.azure.cosmos.internal.RequestOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This integration test class demonstrates how to use Async API to query and
 * replace an Offer.
 */
public class OfferCRUDAsyncAPITest extends DocumentClientTest {
    private final static int TIMEOUT = 60000;
    private Database createdDatabase;
    private AsyncDocumentClient client;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().setConnectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    /**
     * Query for all the offers existing in the database account.
     * REPLACE the required offer so that it has a higher throughput.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void updateOffer() throws Exception {

        int initialThroughput = 10200;
        int newThroughput = 10300;

        // Set the throughput to be 10,200
        RequestOptions multiPartitionRequestOptions = new RequestOptions();
        multiPartitionRequestOptions.setOfferThroughput(initialThroughput);

        // CREATE the collection
        DocumentCollection createdCollection = client.createCollection("dbs/" + createdDatabase.getId(),
                                                                            getMultiPartitionCollectionDefinition(), multiPartitionRequestOptions).single().block()
                                                     .getResource();

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Find offer associated with this collection
        client.queryOffers(
                String.format("SELECT * FROM r where r.offerResourceId = '%s'", createdCollection.getResourceId()),
                null).flatMap(offerFeedResponse -> {
            List<Offer> offerList = offerFeedResponse.getResults();
            // NUMBER of offers returned should be 1
            assertThat(offerList.size(), equalTo(1));

            // This offer must correspond to the collection we created
            Offer offer = offerList.get(0);
            int currentThroughput = offer.getThroughput();
            assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
            assertThat(currentThroughput, equalTo(initialThroughput));
            System.out.println("initial throughput: " + currentThroughput);

            // UPDATE the offer's throughput
            offer.setThroughput(newThroughput);

            // REPLACE the offer
            return client.replaceOffer(offer);
        }).subscribe(offerResourceResponse -> {
            Offer offer = offerResourceResponse.getResource();
            int currentThroughput = offer.getThroughput();

            // The current throughput of the offer must be equal to the new throughput getValue
            assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
            assertThat(currentThroughput, equalTo(newThroughput));

            System.out.println("updated throughput: " + currentThroughput);
            successfulCompletionLatch.countDown();
        }, error -> {
            System.err
                    .println("an getError occurred while updating the offer: actual cause: " + error.getMessage());
        });

        successfulCompletionLatch.await();
    }

    private DocumentCollection getMultiPartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        List<String> paths = new ArrayList<>();
        paths.add("/city");
        partitionKeyDefinition.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        BridgeInternal.setProperty(stringIndex, "precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        BridgeInternal.setProperty(numberIndex, "precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }
}
