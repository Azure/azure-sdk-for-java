/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.cosmosdb.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.CloudException;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.cosmosdb.CosmosDBAccount;
import com.azure.management.cosmosdb.DatabaseAccountKind;
import com.azure.management.cosmosdb.DatabaseAccountListKeysResult;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;

import java.io.File;

/**
 * Azure CosmosDB sample for high availability.
 *  - Create a CosmosDB configured with a single read location
 *  - Get the credentials for the CosmosDB
 *  - Update the CosmosDB with additional read locations
 *  - add collection to the CosmosDB with throughput 4000
 *  - Delete the CosmosDB
 */
public final class ManageHACosmosDB {
    static final String DATABASE_ID = "TestDB";
    static final String COLLECTION_ID = "TestCollection";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param clientId client id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String clientId) {
        final String docDBName = azure.sdkContext().randomResourceName("docDb", 10);
        final String rgName = azure.sdkContext().randomResourceName("rgNEMV", 24);

        try {
            //============================================================
            // Create a CosmosDB

            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azure.cosmosDBAccounts().define(docDBName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                    .withSessionConsistency()
                    .withWriteReplication(Region.US_WEST)
                    .withReadReplication(Region.US_CENTRAL)
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Update cosmos db with three additional read regions

            System.out.println("Updating CosmosDB with three additional read replication regions");
            cosmosDBAccount = cosmosDBAccount.update()
                    .withReadReplication(Region.ASIA_EAST)
                    .withReadReplication(Region.AUSTRALIA_SOUTHEAST)
                    .withReadReplication(Region.UK_SOUTH)
                    .apply();
                    
            System.out.println("Updated CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Get credentials for the CosmosDB.

            System.out.println("Get credentials for the CosmosDB");
            DatabaseAccountListKeysResult databaseAccountListKeysResult = cosmosDBAccount.listKeys();
            String masterKey = databaseAccountListKeysResult.primaryMasterKey();
            String endPoint = cosmosDBAccount.documentEndpoint();

            //============================================================
            // Connect to CosmosDB and add a collection

            System.out.println("Connecting and adding collection");
            createDBAndAddCollection(masterKey, endPoint);

            //============================================================
            // Delete CosmosDB.
            System.out.println("Deleting the docuemntdb");
            // work around CosmosDB service issue returning 404 CloudException on delete operation
            try {
                azure.cosmosDBAccounts().deleteById(cosmosDBAccount.id());
            } catch (CloudException e) {
            }
            System.out.println("Deleted the CosmosDB");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted resource group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }

        return false;
    }

    private static void createDBAndAddCollection(String masterKey, String endPoint) throws CosmosClientException {
        try {
            CosmosClient cosmosClient = new CosmosClientBuilder()
                    .setEndpoint(endPoint)
                    .setKey(masterKey)
                    .setConnectionPolicy(ConnectionPolicy.getDefaultPolicy())
                    .setConsistencyLevel(ConsistencyLevel.SESSION)
                    .buildClient();

            // Define a new database using the id above.
            CosmosDatabase myDatabase = cosmosClient.createDatabase(DATABASE_ID, 400).getDatabase();

            System.out.println("Created a new database:");
            System.out.println(myDatabase.toString());

            // Create a new collection.
            CosmosContainer myCollection = myDatabase.createContainer(COLLECTION_ID, "/keyPath/", 1000).getContainer();
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, ApplicationTokenCredential.fromFile(credFile).getClientId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageHACosmosDB() {
    }
}
