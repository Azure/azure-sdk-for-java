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
package com.azure.data.cosmos.examples.ChangeFeed;

import com.azure.data.cosmos.ChangeFeedProcessor;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosContainerSettings;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.SerializationFormattingPolicy;
import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Sample class to test the implementation.
 *
 */
public class SampleChangeFeedProcessor {

    public static int WAIT_FOR_WORK = 60;
    public static final String DATABASE_NAME = "db_" + RandomStringUtils.randomAlphabetic(7);
    public static final String COLLECTION_NAME = "coll_" + RandomStringUtils.randomAlphabetic(7);

    private static ChangeFeedProcessor changeFeedProcessorInstance;
    private static boolean isWorkCompleted = false;

    public static void main (String[]args) {
        System.out.println("BEGIN Sample");

        try {

            System.out.println("-->CREATE DocumentClient");
            CosmosClient client = getCosmosClient();

            System.out.println("-->CREATE sample's database: " + DATABASE_NAME);
            CosmosDatabase cosmosDatabase = createNewDatabase(client, DATABASE_NAME);

            System.out.println("-->CREATE container for documents: " + COLLECTION_NAME);
            CosmosContainer feedContainer = createNewCollection(client, DATABASE_NAME, COLLECTION_NAME);

            System.out.println("-->CREATE container for lease: " + COLLECTION_NAME + "-leases");
            CosmosContainer leaseContainer = createNewLeaseCollection(client, DATABASE_NAME, COLLECTION_NAME + "-leases");

            Mono<ChangeFeedProcessor> changeFeedProcessor1 = getChangeFeedProcessor("SampleHost_1", feedContainer, leaseContainer);

            changeFeedProcessor1.subscribe(changeFeedProcessor -> {
                    changeFeedProcessorInstance = changeFeedProcessor;
                    changeFeedProcessor.start().subscribe(aVoid -> {
                        createNewDocuments(feedContainer, 10, Duration.ofSeconds(3));
                        isWorkCompleted = true;
                    });
                });

            long remainingWork = WAIT_FOR_WORK;
            while (!isWorkCompleted && remainingWork > 0) {
                Thread.sleep(100);
                remainingWork -= 100;
            }

            if (isWorkCompleted) {
                if (changeFeedProcessorInstance != null) {
                    changeFeedProcessorInstance.stop().wait(10000);
                }
            } else {
                throw new RuntimeException("The change feed processor initialization and automatic create document feeding process did not complete in the expected time");
            }

            System.out.println("-->DELETE sample's database: " + DATABASE_NAME);
            deleteDatabase(cosmosDatabase);

            Thread.sleep(15000);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("END Sample");
        System.exit(0);
    }

    public static Mono<ChangeFeedProcessor> getChangeFeedProcessor(String hostName, CosmosContainer feedContainer, CosmosContainer leaseContainer) {
        return ChangeFeedProcessor.Builder()
            .withHostName(hostName)
            .withFeedContainerClient(feedContainer)
            .withLeaseContainerClient(leaseContainer)
            .withChangeFeedObserver(SampleObserverImpl.class)
            .build();
    }

    public static CosmosClient getCosmosClient() {

        return CosmosClient.builder()
                .endpoint(SampleConfigurations.HOST)
                .key(SampleConfigurations.MASTER_KEY)
                .connectionPolicy(ConnectionPolicy.defaultPolicy())
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .build();
    }

    public static CosmosDatabase createNewDatabase(CosmosClient client, String databaseName) {
        return client.createDatabaseIfNotExists(databaseName).block().database();
    }

    public static void deleteDatabase(CosmosDatabase cosmosDatabase) {
        cosmosDatabase.delete().block();
    }

    public static CosmosContainer createNewCollection(CosmosClient client, String databaseName, String collectionName) {
        CosmosDatabase databaseLink = client.getDatabase(databaseName);
        CosmosContainer collectionLink = databaseLink.getContainer(collectionName);
        CosmosContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
            }
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof CosmosClientException) {
                CosmosClientException cosmosClientException = (CosmosClientException) ex.getCause();

                if (cosmosClientException.statusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }

        CosmosContainerSettings containerSettings = new CosmosContainerSettings(collectionName, "/id");

        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        requestOptions.offerThroughput(10000);

        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();

        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.", collectionName, databaseName));
        }

        return containerResponse.container();
    }

    public static CosmosContainer createNewLeaseCollection(CosmosClient client, String databaseName, String leaseCollectionName) {
        CosmosDatabase databaseLink = client.getDatabase(databaseName);
        CosmosContainer leaseCollectionLink = databaseLink.getContainer(leaseCollectionName);
        CosmosContainerResponse leaseContainerResponse = null;

        try {
            leaseContainerResponse = leaseCollectionLink.read().block();

            if (leaseContainerResponse != null) {
                leaseCollectionLink.delete().block();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof CosmosClientException) {
                CosmosClientException cosmosClientException = (CosmosClientException) ex.getCause();

                if (cosmosClientException.statusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }

        CosmosContainerSettings containerSettings = new CosmosContainerSettings(leaseCollectionName, "/id");
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        requestOptions.offerThroughput(400);

        leaseContainerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();

        if (leaseContainerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.", leaseCollectionName, databaseName));
        }

        return leaseContainerResponse.container();
    }

    public static void createNewDocuments(CosmosContainer containerClient, int count, Duration delay) {
        String suffix = RandomStringUtils.randomAlphabetic(10);
        for (int i = 0; i <= count; i++) {
            CosmosItemProperties document = new CosmosItemProperties();
            document.id(String.format("0%d-%s", i, suffix));

            containerClient.createItem(document).subscribe(doc -> {
                System.out.println("---->DOCUMENT WRITE: " + doc.properties().toJson(SerializationFormattingPolicy.INDENTED));
            });

            long remainingWork = delay.toMillis();
            try {
                while (remainingWork > 0) {
                    Thread.sleep(100);
                    remainingWork -= 100;
                }
            } catch (InterruptedException iex) {
                // exception caught
                break;
            }
        }
    }

    public static boolean ensureWorkIsDone(Duration delay) {
        long remainingWork = delay.toMillis();
        try {
            while (!isWorkCompleted && remainingWork > 0) {
                Thread.sleep(100);
                remainingWork -= 100;
            }
        } catch (InterruptedException iex) {
            return false;
        }

        return remainingWork > 0;
    }

}
