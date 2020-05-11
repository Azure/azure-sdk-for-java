// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples.ChangeFeed;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosAsyncContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Sample for Change Feed Processor.
 *
 */
public class SampleChangeFeedProcessor {

    public static int WAIT_FOR_WORK = 60;
    public static final String DATABASE_NAME = "db_" + RandomStringUtils.randomAlphabetic(7);
    public static final String COLLECTION_NAME = "coll_" + RandomStringUtils.randomAlphabetic(7);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    protected static Logger logger = LoggerFactory.getLogger(SampleChangeFeedProcessor.class.getSimpleName());


    private static ChangeFeedProcessor changeFeedProcessorInstance;
    private static boolean isWorkCompleted = false;

    public static void main (String[]args) {
        System.out.println("BEGIN Sample");

        try {

            System.out.println("-->CREATE DocumentClient");
            CosmosAsyncClient client = getCosmosClient();

            System.out.println("-->CREATE sample's database: " + DATABASE_NAME);
            CosmosAsyncDatabase cosmosDatabase = createNewDatabase(client, DATABASE_NAME);

            System.out.println("-->CREATE container for documents: " + COLLECTION_NAME);
            CosmosAsyncContainer feedContainer = createNewCollection(client, DATABASE_NAME, COLLECTION_NAME);

            System.out.println("-->CREATE container for lease: " + COLLECTION_NAME + "-leases");
            CosmosAsyncContainer leaseContainer = createNewLeaseCollection(client, DATABASE_NAME, COLLECTION_NAME + "-leases");

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

    public static ChangeFeedProcessor getChangeFeedProcessor(String hostName, CosmosAsyncContainer feedContainer, CosmosAsyncContainer leaseContainer) {
        return ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleChanges((List<JsonNode> docs) -> {
                System.out.println("--->setHandleChanges() START");

                for (JsonNode document : docs) {
                    try {
                        System.out.println("---->DOCUMENT RECEIVED: " + OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                                                                            .writeValueAsString(document));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("--->handleChanges() END");

            })
            .build();
    }

    public static CosmosAsyncClient getCosmosClient() {

        return new CosmosClientBuilder()
                .endpoint(SampleConfigurations.HOST)
                .key(SampleConfigurations.MASTER_KEY)
                .connectionPolicy(ConnectionPolicy.getDefaultPolicy())
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    public static CosmosAsyncDatabase createNewDatabase(CosmosAsyncClient client, String databaseName) {
        return client.createDatabaseIfNotExists(databaseName).block().getDatabase();
    }

    public static void deleteDatabase(CosmosAsyncDatabase cosmosDatabase) {
        cosmosDatabase.delete().block();
    }

    public static CosmosAsyncContainer createNewCollection(CosmosAsyncClient client, String databaseName, String collectionName) {
        CosmosAsyncDatabase databaseLink = client.getDatabase(databaseName);
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(collectionName);
        CosmosAsyncContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
            }
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosClientException) {
                CosmosClientException cosmosClientException = (CosmosClientException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
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

        return containerResponse.getContainer();
    }

    public static CosmosAsyncContainer createNewLeaseCollection(CosmosAsyncClient client, String databaseName, String leaseCollectionName) {
        CosmosAsyncDatabase databaseLink = client.getDatabase(databaseName);
        CosmosAsyncContainer leaseCollectionLink = databaseLink.getContainer(leaseCollectionName);
        CosmosAsyncContainerResponse leaseContainerResponse = null;

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
            if (ex instanceof CosmosClientException) {
                CosmosClientException cosmosClientException = (CosmosClientException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
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

        return leaseContainerResponse.getContainer();
    }

    public static void createNewDocuments(CosmosAsyncContainer containerClient, int count, Duration delay) {
        String suffix = RandomStringUtils.randomAlphabetic(10);
        for (int i = 0; i <= count; i++) {
            CosmosItemProperties document = new CosmosItemProperties();
            document.setId(String.format("0%d-%s", i, suffix));

            containerClient.createItem(document).subscribe(doc -> {
                try {
                    System.out.println("---->DOCUMENT WRITE: " + OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                                                                     .writeValueAsString(doc));
                } catch (JsonProcessingException e) {
                    logger.error("Failure in processing json [{}]", e.getMessage(), e);
                }
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
