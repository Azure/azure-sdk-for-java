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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

/**
 * This integration test class demonstrates how to use Async API to query and
 * replace an Offer.
 * 
 */
public class OfferCRUDAsyncAPITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferCRUDAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";
    private Database createdDatabase;

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

        // Create database
        createdDatabase = new Database();
        createdDatabase.setId(DATABASE_ID);
        createdDatabase = asyncClient.createDatabase(createdDatabase, null).toBlocking().single().getResource();
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    /**
     * Query for all the offers existing in the database account.
     * Replace the required offer so that it has a higher throughput.
     */
    @Test
    public void testUpdateOffer() throws Exception {

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
                    int curentThroughput = offer.getContent().getInt("offerThroughput");
                    assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
                    assertThat(curentThroughput, equalTo(initialThroughput));
                    System.out.println("initial throughput: " + curentThroughput);

                    // Update the offer's throughput
                    offer.getContent().put("offerThroughput", newThroughput);

                    // Replace the offer
                    return asyncClient.replaceOffer(offer);
                }).subscribe(offerResourceResponse -> {
                    Offer offer = offerResourceResponse.getResource();
                    int curentThroughput = offer.getContent().getInt("offerThroughput");

                    // The current throughput of the offer must be equal to the new throughput value
                    assertThat(offer.getString("offerResourceId"), equalTo(createdCollection.getResourceId()));
                    assertThat(curentThroughput, equalTo(newThroughput));

                    System.out.println("updated throughput: " + curentThroughput);
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
        Collection<String> paths = new ArrayList<String>();
        paths.add("/city");
        partitionKeyDefinition.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<Index>();
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