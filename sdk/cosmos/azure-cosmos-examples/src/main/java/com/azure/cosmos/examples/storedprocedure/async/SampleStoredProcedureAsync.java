// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.



// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.storedprocedure.async;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.common.AccountSettings;
import com.google.common.collect.Lists;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class SampleStoredProcedureAsync {

    private CosmosAsyncClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

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
        SampleStoredProcedureAsync p = new SampleStoredProcedureAsync();

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
            .buildAsyncClient();

            System.out.println("Create database " + databaseName + " with container " + containerName + " if either does not already exist.\n");

            client.createDatabaseIfNotExists(databaseName).flatMap(databaseResponse -> {
                database = databaseResponse.getDatabase();
                return Mono.empty();
            }).block();

            CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/lastName");
            database.createContainerIfNotExists(containerProperties, 400).flatMap(containerResponse -> {
                container = containerResponse.getContainer();
                return Mono.empty();
            }).block();
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
            new CosmosStoredProcedureRequestOptions()).block();
    }

    private void readAllSprocs() throws Exception {

        FeedOptions feedOptions = new FeedOptions();
        CosmosContinuablePagedFlux<CosmosStoredProcedureProperties> fluxResponse =
                container.getScripts().readAllStoredProcedures(feedOptions);

//        System.out.println(String.format("\nListing stored procedures associated with container..."));

        final CountDownLatch completionLatch = new CountDownLatch(1);

        fluxResponse.flatMap(storedProcedureProperties -> {
            System.out.println(String.format("Stored Procedure: %s\n",storedProcedureProperties.getId()));
            return Mono.empty();
        }).subscribe(
            s -> {},
            err -> {
                if (err instanceof CosmosClientException) {
                    //Client-specific errors
                    CosmosClientException cerr = (CosmosClientException)err;
                    cerr.printStackTrace();
                    System.err.println(String.format("Read Item failed with %s\n", cerr));
                } else {
                    //General errors
                    err.printStackTrace();
                }

                completionLatch.countDown();
            },
            () -> {completionLatch.countDown();}
        );

        completionLatch.await();
    }

    public void executeStoredProcedure() throws Exception {
        System.out.println(String.format("Executing stored procedure %s...\n\n",sprocId));

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);

        container.getScripts()
            .getStoredProcedure(sprocId)
            .execute(null, options)
            .flatMap(executeResponse -> {
                System.out.println(String.format("Stored procedure %s returned %s (HTTP %d), at cost %.3f RU.\n",
                    sprocId,
                    executeResponse.getResponseAsString(),
                    executeResponse.getStatusCode(),
                    //executeResponse.getRequestLatency().toString(),
                    executeResponse.getRequestCharge()));
                return Mono.empty();
        }).block();
    }

    public void deleteStoredProcedure() throws Exception {
        System.out.println("-Deleting stored procedure...\n");
        container.getScripts()
            .getStoredProcedure(sprocId)
            .delete().block();
        System.out.println("-Closing client instance...\n");
        client.close();
    }
}
