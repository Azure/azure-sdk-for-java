// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A sample to demonstrate creating containers, databases and items in Cosmos DB using GraalVM.
 */
public final class CosmosSample {
    private static final String AZURE_COSMOS_ENDPOINT = System.getenv("AZURE_COSMOS_ENDPOINT");
    private static final String AZURE_COSMOS_KEY = System.getenv("AZURE_COSMOS_KEY");

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSample.class);

    /**
     * The method to run the cosmos sample.
     */
    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting Cosmos Sample");
        System.out.println("================================================================");

        if (AZURE_COSMOS_ENDPOINT == null || AZURE_COSMOS_ENDPOINT.isEmpty()) {
            System.err.println("azure_cosmos_endpoint environment variable is not set - exiting");
            return;
        }

        if (AZURE_COSMOS_KEY == null || AZURE_COSMOS_KEY.isEmpty()) {
            System.err.println("azure_cosmos_key environment variable is not set - exiting");
            return;
        }

        CosmosSample sample = new CosmosSample();

        try {
            sample.startSample();
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            System.out.println("Error running Cosmos sample " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Closing the client");
            sample.shutdown();
        }

        System.out.println("\n================================================================");
        System.out.println(" Cosmos Sample Complete");
        System.out.println("================================================================");
    }

    private void startSample() throws Exception {
        //  Create sync client
        client = new CosmosClientBuilder().endpoint(AZURE_COSMOS_ENDPOINT)
            .key(AZURE_COSMOS_KEY)
            .preferredRegions(Arrays.asList("West US"))
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .contentResponseOnWriteEnabled(true)
            .buildClient();

        createDatabaseIfNotExists();
        System.out.println("Creating container");
        createContainerIfNotExists();

        //  Setup family items to create
        List<Family> familiesToCreate = new ArrayList<>();
        familiesToCreate.add(Families.getAndersenFamilyItem());
        familiesToCreate.add(Families.getWakefieldFamilyItem());
        familiesToCreate.add(Families.getJohnsonFamilyItem());
        familiesToCreate.add(Families.getSmithFamilyItem());

        // Creates several items in the container
        // Also applies an upsert operation to one of the items (create if not present, otherwise replace)
        createFamilies(familiesToCreate);

        System.out.println("Reading items.");
        readItems(familiesToCreate);

        System.out.println("Replacing items.");
        replaceItems(familiesToCreate);

        System.out.println("Querying items.");
        queryItems();

        System.out.println("Delete an item.");
        deleteItem(familiesToCreate.get(0));
    }

    private void createDatabaseIfNotExists() throws Exception {
        System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        //  <CreateDatabaseIfNotExists>
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        //  </CreateDatabaseIfNotExists>

        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        //  <CreateContainerIfNotExists>
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/lastName");

        //  Create container with 400 RU/s
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
        CosmosContainerResponse containerResponse
            = database.createContainerIfNotExists(containerProperties, throughputProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
        //  </CreateContainerIfNotExists>

        System.out.println("Checking container " + container.getId() + " completed!\n");
    }

    private void createFamilies(List<Family> families) throws Exception {
        double totalRequestCharge = 0;
        for (Family family : families) {

            //  <CreateItem>
            //  Create item using container that we created using sync client

            //  Use lastName as partitionKey for cosmos item
            //  Using appropriate partition key improves the performance of database operations
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<Family> item
                = container.createItem(family, new PartitionKey(family.getLastName()), cosmosItemRequestOptions);
            //  </CreateItem>

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            System.out.println(String.format("Created item with request charge of %.2f within duration %s",
                item.getRequestCharge(), item.getDuration()));

            totalRequestCharge += item.getRequestCharge();
        }
        System.out.println(
            String.format("Created %d items with total request charge of %.2f", families.size(), totalRequestCharge));

        Family familyToUpsert = families.get(0);
        System.out.println(String.format("Upserting the item with id %s after modifying the isRegistered field...",
            familyToUpsert.getId()));
        familyToUpsert.setRegistered(!familyToUpsert.isRegistered());

        CosmosItemResponse<Family> item = container.upsertItem(familyToUpsert);

        //  Get upsert request charge and other properties like latency, and diagnostics strings, etc.
        System.out.println(String.format("Upserted item with request charge of %.2f within duration %s",
            item.getRequestCharge(), item.getDuration()));
    }

    private void readItems(List<Family> familiesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        familiesToCreate.forEach(family -> {
            //  <ReadItem>
            try {
                CosmosItemResponse<Family> item
                    = container.readItem(family.getId(), new PartitionKey(family.getLastName()), Family.class);
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getDuration();
                System.out.println(
                    String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.getItem().getId(), requestCharge, requestLatency));
            } catch (CosmosException e) {
                e.printStackTrace();
                System.out.println(String.format("Read Item failed with %s", e));
            }
            //  </ReadItem>
        });
    }

    private void replaceItems(List<Family> familiesToCreate) {
        familiesToCreate.forEach(family -> {
            //  <ReadItem>
            try {
                String district = family.getDistrict();
                family.setDistrict(district + "_newDistrict");
                CosmosItemResponse<Family> item = container.replaceItem(family, family.getId(),
                    new PartitionKey(family.getLastName()), new CosmosItemRequestOptions());
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getDuration();
                System.out.printf("Item successfully replaced with id: %s, district: %s, charge: %s, duration: %s",
                    item.getItem().getId(), item.getItem().getDistrict(), requestCharge, requestLatency);
            } catch (CosmosException e) {
                LOGGER.error(String.format("Replace Item failed with %s", e));
            }
            //  </ReadItem>
        });
    }

    private void queryItems() {
        //  <QueryItems>

        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //queryOptions.setEnableCrossPartitionQuery(true); //No longer necessary in SDK v4
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<Family> familiesPagedIterable
            = container.queryItems("SELECT * FROM Family WHERE Family.lastName IN ('Andersen', 'Wakefield', 'Johnson')",
                queryOptions, Family.class);

        familiesPagedIterable.iterableByPage(preferredPageSize).forEach(cosmosItemPropertiesFeedResponse -> {
            System.out.println("Got a page of query result with " + cosmosItemPropertiesFeedResponse.getResults().size()
                + " items(s)" + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            System.out.println("Item Ids " + cosmosItemPropertiesFeedResponse.getResults()
                .stream()
                .map(Family::getId)
                .collect(Collectors.toList()));
        });
        //  </QueryItems>
    }

    private void deleteItem(Family item) {
        container.deleteItem(item.getId(), new PartitionKey(item.getLastName()), new CosmosItemRequestOptions());
    }

    private void shutdown() {
        try {
            //Clean shutdown
            System.out.println("Deleting Cosmos DB resources");
            System.out.println("-Deleting container...");
            if (container != null) {
                container.delete();
            }
            System.out.println("-Deleting database...");
            if (database != null) {
                database.delete();
            }
            System.out.println("-Closing the client...");
        } catch (Exception err) {
            LOGGER.error(
                "Deleting Cosmos DB resources failed, will still attempt to close the client. See stack trace below.");
            err.printStackTrace();
        }
        client.close();
        System.out.println("Done.");
    }

    private CosmosSample() {
    }
}
