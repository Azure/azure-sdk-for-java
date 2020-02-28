// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.indexmanagement.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.changefeed.SampleChangeFeedProcessor;
import com.azure.cosmos.examples.common.AccountSettings;
import com.azure.cosmos.examples.common.Families;
import com.azure.cosmos.examples.common.Family;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SampleIndexManagement {

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    protected static Logger logger = LoggerFactory.getLogger(SampleChangeFeedProcessor.class.getSimpleName());

    public void close() {
        client.close();
    }

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        SampleIndexManagement p = new SampleIndexManagement();

        try {
            logger.info("Starting SYNC main");
            p.getStartedDemo();
            logger.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            logger.info("Closing the client");
            p.close();
        }
    }

    //  </Main>

    private void getStartedDemo() throws Exception {
        logger.info("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
        //  Setting the preferred location to Cosmos DB Account region
        //  West US is just an example. User should set preferred location to the Cosmos DB region closest to the application
        defaultPolicy.setPreferredLocations(Lists.newArrayList("West US"));

        //  Create sync client
        //  <CreateSyncClient>
        client = new CosmosClientBuilder()
                .setEndpoint(AccountSettings.HOST)
                .setKey(AccountSettings.MASTER_KEY)
                .setConnectionPolicy(defaultPolicy)
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        //  </CreateSyncClient>

        createDatabaseIfNotExists();
        createContainerIfNotExistsWithSpecifiedIndex();



        //  Setup family items to create
        ArrayList<Family> familiesToCreate = new ArrayList<>();
        familiesToCreate.add(Families.getAndersenFamilyItem());
        familiesToCreate.add(Families.getWakefieldFamilyItem());
        familiesToCreate.add(Families.getJohnsonFamilyItem());
        familiesToCreate.add(Families.getSmithFamilyItem());

        createFamilies(familiesToCreate);

        logger.info("Reading items.");
        readItems(familiesToCreate);

        logger.info("Querying items.");
        queryItems();
    }

    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        //  <CreateDatabaseIfNotExists>
        database = client.createDatabaseIfNotExists(databaseName).getDatabase();
        //  </CreateDatabaseIfNotExists>

        logger.info("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExistsWithSpecifiedIndex() throws Exception {
        logger.info("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/lastName");

        // <CustomIndexingPolicy>
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT); //To turn indexing off set IndexingMode.NONE

        // Included paths
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        // Excluded paths
        List<ExcludedPath> excludedPaths = new ArrayList<>();
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.setPath("/name/*");
        excludedPaths.add(excludedPath);
        indexingPolicy.setExcludedPaths(excludedPaths);

        // Spatial indices - if you need them, here is how to set them up:
        /*
        List<SpatialSpec> spatialIndexes = new ArrayList<SpatialSpec>();
        List<SpatialType> collectionOfSpatialTypes = new ArrayList<SpatialType>();

        SpatialSpec spec = new SpatialSpec();
        spec.setPath("/locations/*");
        collectionOfSpatialTypes.add(SpatialType.Point);
        spec.setSpatialTypes(collectionOfSpatialTypes);
        spatialIndexes.add(spec);

        indexingPolicy.setSpatialIndexes(spatialIndexes);
         */

        // Composite indices - if you need them, here is how to set them up:
        /*
        List<List<CompositePath>> compositeIndexes = new ArrayList<>();
        List<CompositePath> compositePaths = new ArrayList<>();

        CompositePath nameCompositePath = new CompositePath();
        nameCompositePath.setPath("/name");
        nameCompositePath.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath ageCompositePath = new CompositePath();
        ageCompositePath.setPath("/age");
        ageCompositePath.setOrder(CompositePathSortOrder.DESCENDING);

        compositePaths.add(ageCompositePath);
        compositePaths.add(nameCompositePath);

        compositeIndexes.add(compositePaths);
        indexingPolicy.setCompositeIndexes(compositeIndexes);
         */

        containerProperties.setIndexingPolicy(indexingPolicy);

        // </CustomIndexingPolicy>

        //  Create container with 400 RU/s
        container = database.createContainerIfNotExists(containerProperties, 400).getContainer();

        logger.info("Checking container " + container.getId() + " completed!\n");
    }

    private void createFamilies(List<Family> families) throws Exception {
        double totalRequestCharge = 0;
        for (Family family : families) {

            //  <CreateItem>
            //  Create item using container that we created using sync client

            //  Use lastName as partitionKey for cosmos item
            //  Using appropriate partition key improves the performance of database operations
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<Family> item = container.createItem(family, new PartitionKey(family.getLastName()), cosmosItemRequestOptions);
            //  </CreateItem>

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            logger.info(String.format("Created item with request charge of %.2f within" +
                            " duration %s",
                    item.getRequestCharge(), item.getRequestLatency()));
            totalRequestCharge += item.getRequestCharge();
        }
        logger.info(String.format("Created %d items with total request " +
                        "charge of %.2f",
                families.size(),
                totalRequestCharge));
    }

    private void readItems(ArrayList<Family> familiesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        familiesToCreate.forEach(family -> {
            //  <ReadItem>
            try {
                CosmosItemResponse<Family> item = container.readItem(family.getId(), new PartitionKey(family.getLastName()), Family.class);
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getRequestLatency();
                System.out.println(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.getResource().getId(), requestCharge, requestLatency));
            } catch (CosmosClientException e) {
                e.printStackTrace();
                System.err.println(String.format("Read Item failed with %s", e));
            }
            //  </ReadItem>
        });
    }

    private void queryItems() {
        //  <QueryItems>
        // Set some common query options
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.maxItemCount(10);
        //  Set populate query metrics to get metrics around query executions
        queryOptions.populateQueryMetrics(true);

        CosmosContinuablePagedIterable<Family> familiesPagedIterable = container.queryItems(
                "SELECT * FROM Family WHERE Family.lastName IN ('Andersen', 'Wakefield', 'Johnson')", queryOptions, Family.class);

        familiesPagedIterable.iterableByPage().forEach(cosmosItemPropertiesFeedResponse -> {
            logger.info("Got a page of query result with " +
                    cosmosItemPropertiesFeedResponse.getResults().size() + " items(s)"
                    + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            logger.info("Item Ids " + cosmosItemPropertiesFeedResponse
                    .getResults()
                    .stream()
                    .map(Family::getId)
                    .collect(Collectors.toList()));
        });
        //  </QueryItems>
    }
}
