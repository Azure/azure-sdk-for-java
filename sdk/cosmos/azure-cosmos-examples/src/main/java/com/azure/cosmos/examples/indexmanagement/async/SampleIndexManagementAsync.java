// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.indexmanagement.async;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.changefeed.SampleChangeFeedProcessor;
import com.azure.cosmos.examples.common.AccountSettings;
import com.azure.cosmos.examples.common.Families;
import com.azure.cosmos.examples.common.Family;
import com.google.common.collect.Lists;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleIndexManagementAsync {

    private CosmosAsyncClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

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
        SampleIndexManagementAsync p = new SampleIndexManagementAsync();

        try {
            logger.info("Starting ASYNC main");
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

        //  Create async client
        //  <CreateAsyncClient>
        client = new CosmosClientBuilder()
                .setEndpoint(AccountSettings.HOST)
                .setKey(AccountSettings.MASTER_KEY)
                .setConnectionPolicy(defaultPolicy)
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildAsyncClient();

        //  </CreateAsyncClient>

        createDatabaseIfNotExists();
        createContainerIfNotExistsWithSpecifiedIndex();

        Family andersenFamilyItem=Families.getAndersenFamilyItem();
        Family wakefieldFamilyItem=Families.getWakefieldFamilyItem();
        Family johnsonFamilyItem=Families.getJohnsonFamilyItem();
        Family smithFamilyItem=Families.getSmithFamilyItem();

        //  Setup family items to create
        Flux<Family> familiesToCreate = Flux.just(andersenFamilyItem,
                wakefieldFamilyItem,
                johnsonFamilyItem,
                smithFamilyItem);

        createFamilies(familiesToCreate);

        familiesToCreate = Flux.just(andersenFamilyItem,
                wakefieldFamilyItem,
                johnsonFamilyItem,
                smithFamilyItem);

        logger.info("Reading items.");
        readItems(familiesToCreate);

        logger.info("Querying items.");
        queryItems();
    }

    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        //  <CreateDatabaseIfNotExists>
        Mono<CosmosAsyncDatabaseResponse> databaseIfNotExists = client.createDatabaseIfNotExists(databaseName);
        databaseIfNotExists.flatMap(databaseResponse -> {
            database = databaseResponse.getDatabase();
            logger.info("Checking database " + database.getId() + " completed!\n");
            return Mono.empty();
        }).block();
        //  </CreateDatabaseIfNotExists>
    }

    private void createContainerIfNotExistsWithSpecifiedIndex() throws Exception {
        logger.info("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        //  <CreateContainerIfNotExists>

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/lastName");

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

        Mono<CosmosAsyncContainerResponse> containerIfNotExists = database.createContainerIfNotExists(containerProperties, 400);

        //  Create container with 400 RU/s
        containerIfNotExists.flatMap(containerResponse -> {
            container = containerResponse.getContainer();
            logger.info("Checking container " + container.getId() + " completed!\n");
            return Mono.empty();
        }).block();

        //  </CreateContainerIfNotExists>
    }

    private void createFamilies(Flux<Family> families) throws Exception {

        //  <CreateItem>

        final CountDownLatch completionLatch = new CountDownLatch(1);

        //  Combine multiple item inserts, associated success println's, and a final aggregate stats println into one Reactive stream.
        families.flatMap(family -> {
            return container.createItem(family);
        }) //Flux of item request responses
                .flatMap(itemResponse -> {
                    logger.info(String.format("Created item with request charge of %.2f within" +
                                    " duration %s",
                            itemResponse.getRequestCharge(), itemResponse.getRequestLatency()));
                    logger.info(String.format("Item ID: %s\n", itemResponse.getResource().getId()));
                    return Mono.just(itemResponse.getRequestCharge());
                }) //Flux of request charges
                .reduce(0.0,
                        (charge_n,charge_nplus1) -> charge_n + charge_nplus1
                ) //Mono of total charge - there will be only one item in this stream
                .subscribe(charge -> {
                        logger.info(String.format("Created items with total request charge of %.2f\n",
                                    charge));
                        },
                        err -> {
                            if (err instanceof CosmosClientException) {
                                //Client-specific errors
                                CosmosClientException cerr = (CosmosClientException)err;
                                cerr.printStackTrace();
                                logger.info(String.format("Read Item failed with %s\n", cerr));
                            } else {
                                //General errors
                                err.printStackTrace();
                            }

                            completionLatch.countDown();
                        },
                        () -> {completionLatch.countDown();}
                ); //Preserve the total charge and print aggregate charge/item count stats.

        try {
            completionLatch.await();
        } catch (InterruptedException err) {
            throw new AssertionError("Unexpected Interruption",err);
        }

        //  </CreateItem>
    }

    private void readItems(Flux<Family> familiesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        //  <ReadItem>

        final CountDownLatch completionLatch = new CountDownLatch(1);

        familiesToCreate.flatMap(family -> {
            Mono<CosmosAsyncItemResponse<Family>> asyncItemResponseMono = container.readItem(family.getId(), new PartitionKey(family.getLastName()), Family.class);
            return asyncItemResponseMono;
        })
                .subscribe(
                        itemResponse -> {
                            double requestCharge = itemResponse.getRequestCharge();
                            Duration requestLatency = itemResponse.getRequestLatency();
                            logger.info(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                                    itemResponse.getResource().getId(), requestCharge, requestLatency));
                        },
                        err -> {
                            if (err instanceof CosmosClientException) {
                                //Client-specific errors
                                CosmosClientException cerr = (CosmosClientException)err;
                                cerr.printStackTrace();
                                logger.info(String.format("Read Item failed with %s\n", cerr));
                            } else {
                                //General errors
                                err.printStackTrace();
                            }

                            completionLatch.countDown();
                        },
                        () -> {completionLatch.countDown();}
                );

        try {
            completionLatch.await();
        } catch (InterruptedException err) {
            throw new AssertionError("Unexpected Interruption",err);
        }

        //  </ReadItem>
    }

    private void queryItems() {
        //  <QueryItems>
        // Set some common query options

        FeedOptions queryOptions = new FeedOptions();
        queryOptions.maxItemCount(10);
        //  Set populate query metrics to get metrics around query executions
        queryOptions.populateQueryMetrics(true);

        CosmosContinuablePagedFlux<Family> pagedFluxResponse = container.queryItems(
                "SELECT * FROM Family WHERE Family.lastName IN ('Andersen', 'Wakefield', 'Johnson')", queryOptions, Family.class);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        pagedFluxResponse.byPage().subscribe(
                fluxResponse -> {
                    logger.info("Got a page of query result with " +
                            fluxResponse.getResults().size() + " items(s)"
                            + " and request charge of " + fluxResponse.getRequestCharge());

                    logger.info("Item Ids " + fluxResponse
                            .getResults()
                            .stream()
                            .map(Family::getId)
                            .collect(Collectors.toList()));
                },
                err -> {
                    if (err instanceof CosmosClientException) {
                        //Client-specific errors
                        CosmosClientException cerr = (CosmosClientException)err;
                        cerr.printStackTrace();
                        logger.error(String.format("Read Item failed with %s\n", cerr));
                    } else {
                        //General errors
                        err.printStackTrace();
                    }

                    completionLatch.countDown();
                },
                () -> {completionLatch.countDown();}
        );

        try {
            completionLatch.await();
        } catch (InterruptedException err) {
            throw new AssertionError("Unexpected Interruption",err);
        }

        // </QueryItems>
    }
}
