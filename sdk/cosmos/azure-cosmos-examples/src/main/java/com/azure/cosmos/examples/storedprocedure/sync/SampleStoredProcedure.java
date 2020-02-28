// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.storedprocedure.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.changefeed.SampleChangeFeedProcessor;
import com.azure.cosmos.examples.common.AccountSettings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.Iterator;

public class SampleStoredProcedure {

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    private String sprocId;

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
        SampleStoredProcedure p = new SampleStoredProcedure();

        try {
            p.sprocDemo();
            logger.info("Demo complete, please hold while resources are released");
            p.shutdown();
            logger.info("Done.\n");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Cosmos getStarted failed with %s", e));
            p.close();
        } finally {
        }
    }

    //  </Main>

    private void sprocDemo() throws Exception {
            //Setup client, DB
            setUp();

            //Create, list and execute stored procedure
            createStoredProcedure();
            readAllSprocs();
            executeStoredProcedure();
    }

    public void setUp() throws Exception{
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

            logger.info("Create database " + databaseName + " with container " + containerName + " if either does not already exist.\n");

            database = client.createDatabaseIfNotExists(databaseName).getDatabase();

            CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/lastName");
            container = database.createContainerIfNotExists(containerProperties, 400).getContainer();
    }

    public void shutdown() throws Exception {
        //Safe clean & close
        deleteStoredProcedure();
    }

    public void createStoredProcedure() throws Exception {
        logger.info("Creating stored procedure...\n");

        sprocId = UUID.randomUUID().toString();
        CosmosStoredProcedureProperties storedProcedureDef =  new CosmosStoredProcedureProperties(sprocId,"function() {var x = 11;}");
        container.getScripts()
            .createStoredProcedure(storedProcedureDef,
            new CosmosStoredProcedureRequestOptions());
    }

    private void readAllSprocs() throws Exception {

        FeedOptions feedOptions = new FeedOptions();
        CosmosContinuablePagedIterable<CosmosStoredProcedureProperties> feedResponseIterable =
                container.getScripts().readAllStoredProcedures(feedOptions);

        Iterator<CosmosStoredProcedureProperties> feedResponseIterator = feedResponseIterable.iterator();

        while(feedResponseIterator.hasNext()) {
            CosmosStoredProcedureProperties storedProcedureProperties = feedResponseIterator.next();
            logger.info(String.format("Stored Procedure: %s\n",storedProcedureProperties));
        }
        logger.info("\n");
    }

    public void executeStoredProcedure() throws Exception {
        logger.info(String.format("Executing stored procedure %s...\n\n",sprocId));

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                                                         .getStoredProcedure(sprocId)
                                                         .execute(null, options);

        logger.info(String.format("Stored procedure %s returned %s (HTTP %d), at cost %.3f RU.\n",
                                         sprocId,
                                         executeResponse.responseAsString(),
                                         executeResponse.getStatusCode(),
                                         //executeResponse.getRequestLatency().toString(),
                                         executeResponse.getRequestCharge()));
    }

    public void deleteStoredProcedure() throws Exception {
        logger.info("-Deleting stored procedure...\n");
        container.getScripts()
            .getStoredProcedure(sprocId)
            .delete();
        logger.info("-Closing client instance...\n");
        client.close();
    }
}
