/*
 * The MIT License (MIT)
 * Copyright (c) 2017 Microsoft Corporation
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

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
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
public class OfferCRUDAsyncAPITest {
    private final static int TIMEOUT = 60000;
    private Database createdDatabase;
    private AsyncDocumentClient asyncClient;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        // Create database
        createdDatabase = Utils.createDatabaseForTest(asyncClient);
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(asyncClient, createdDatabase);
        Utils.safeClose(asyncClient);
    }

    /**
     * Query for all the offers existing in the database account.
     * Replace the required offer so that it has a higher throughput.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void updateOffer() throws Exception {

        int initialThroughput = 10200;
        int newThroughput = 10300;

        // Set the throughput to be 10,200
        RequestOptions multiPartitionRequestOptions = new RequestOptions();
        multiPartitionRequestOptions.setOfferThroughput(initialThroughput);

        // Create the collection
        DocumentCollection createdCollection = asyncClient.createCollection("dbs/" + createdDatabase.getId(),
                                                                            getMultiPartitionCollectionDefinition(), multiPartitionRequestOptions).toBlocking().single()
                .getResource();

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Find offer associated with this collection
        asyncClient.queryOffers(
                String.format("SELECT * FROM r where r.offerResourceId = '%s'", createdCollection.getResourceId()),
                null).flatMap(offerFeedResponse -> {
            List<Offer> offerList = offerFeedResponse.getResults();
            // Number of offers returned should be 1
            assertThat(offerList.size(), equalTo(1));

            // This offer must correspond to the collection we created
            Offer offer = offerList.get(0);
            int currentThroughput = offer.getThroughput();
            assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
            assertThat(currentThroughput, equalTo(initialThroughput));
            System.out.println("initial throughput: " + currentThroughput);

            // Update the offer's throughput
            offer.setThroughput(newThroughput);

            // Replace the offer
            return asyncClient.replaceOffer(offer);
        }).subscribe(offerResourceResponse -> {
            Offer offer = offerResourceResponse.getResource();
            int currentThroughput = offer.getThroughput();

            // The current throughput of the offer must be equal to the new throughput value
            assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
            assertThat(currentThroughput, equalTo(newThroughput));

            System.out.println("updated throughput: " + currentThroughput);
            successfulCompletionLatch.countDown();
        }, error -> {
            System.err
                    .println("an error occurred while updating the offer: actual cause: " + error.getMessage());
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
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
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
}