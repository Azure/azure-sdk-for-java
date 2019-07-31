// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.examples.ChangeFeed;

import com.azure.data.cosmos.ChangeFeedProcessor;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.SerializationFormattingPolicy;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Duration;

/**
 * Sample for Change Feed Processor.
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

            changeFeedProcessorInstance = getChangeFeedProcessor("SampleHost_1", feedContainer, leaseContainer);

            changeFeedProcessorInstance.start().subscribe(aVoid -> {
                createNewDocuments(feedContainer, 10, Duration.ofSeconds(3));
                isWorkCompleted = true;
            });

            long remainingWork = WAIT_FOR_WORK;
            while (!isWorkCompleted && remainingWork > 0) {
                Thread.sleep(100);
                remainingWork -= 100;
            }

            if (isWorkCompleted) {
                if (changeFeedProcessorInstance != null) {
                    changeFeedProcessorInstance.stop().subscribe().wait(10000);
                }
            } else {
                throw new RuntimeException("The change feed processor initialization and automatic create document feeding process did not complete in the expected time");
            }

            System.out.println("-->DELETE sample's database: " + DATABASE_NAME);
            deleteDatabase(cosmosDatabase);

            Thread.sleep(500);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("END Sample");
        System.exit(0);
    }

    public static ChangeFeedProcessor getChangeFeedProcessor(String hostName, CosmosContainer feedContainer, CosmosContainer leaseContainer) {
        return ChangeFeedProcessor.Builder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleChanges(docs -> {
                System.out.println("--->handleChanges() START");

                for (CosmosItemProperties document : docs) {
                    System.out.println("---->DOCUMENT RECEIVED: " + document.toJson(SerializationFormattingPolicy.INDENTED));
                }
                System.out.println("--->handleChanges() END");

            })
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

        CosmosContainerProperties containerSettings = new CosmosContainerProperties(collectionName, "/id");

        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();

        containerResponse = databaseLink.createContainer(containerSettings, 10000, requestOptions).block();

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

        CosmosContainerProperties containerSettings = new CosmosContainerProperties(leaseCollectionName, "/id");
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();

        leaseContainerResponse = databaseLink.createContainer(containerSettings, 400,requestOptions).block();

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
