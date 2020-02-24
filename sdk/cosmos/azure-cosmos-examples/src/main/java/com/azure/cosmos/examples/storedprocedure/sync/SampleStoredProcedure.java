// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.storedprocedure.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.common.AccountSettings;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.Iterator;

public class SampleStoredProcedure {

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    private String sprocId;

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
            System.out.println("Demo complete, please hold while resources are released");
            p.shutdown();
            System.out.println("Done.\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
            p.close();
        } finally {
        }
        System.exit(0);
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
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

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

            System.out.println("Create database " + databaseName + " with container " + containerName + " if either does not already exist.\n");

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
        System.out.println("Creating stored procedure...\n");

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

//        System.out.println(String.format("\nListing stored procedures associated with container..."));
        while(feedResponseIterator.hasNext()) {
            CosmosStoredProcedureProperties storedProcedureProperties = feedResponseIterator.next();
            System.out.println(String.format("Stored Procedure: %s\n",storedProcedureProperties));
        }
        System.out.println();
    }

    public void executeStoredProcedure() throws Exception {
        System.out.println(String.format("Executing stored procedure %s...\n\n",sprocId));

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                                                         .getStoredProcedure(sprocId)
                                                         .execute(null, options);

        System.out.println(String.format("Stored procedure %s returned %s (HTTP %d), at cost %.3f RU.\n",
                                         sprocId,
                                         executeResponse.responseAsString(),
                                         executeResponse.getStatusCode(),
                                         //executeResponse.getRequestLatency().toString(),
                                         executeResponse.getRequestCharge()));
    }

    public void deleteStoredProcedure() throws Exception {
        System.out.println("-Deleting stored procedure...\n");
        container.getScripts()
            .getStoredProcedure(sprocId)
            .delete();
        System.out.println("-Closing client instance...\n");
        client.close();
    }
}
