// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListKeysResult;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String docDBName = Utils.randomResourceName(azureResourceManager, "docDb", 10);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV", 24);

        try {
            //============================================================
            // Create a CosmosDB

            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azureResourceManager.cosmosDBAccounts().define(docDBName)
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
            // work around CosmosDB service issue returning 404 ManagementException on delete operation
            try {
                azureResourceManager.cosmosDBAccounts().deleteById(cosmosDBAccount.id());
            } catch (ManagementException e) {
            }
            System.out.println("Deleted the CosmosDB");

            return true;
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted resource group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    private static void createDBAndAddCollection(String masterKey, String endPoint) {
        try {
            CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(endPoint)
                .key(masterKey)
                .directMode(DirectConnectionConfig.getDefaultConfig())
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildClient();

            // Define a new database using the id above.
            cosmosClient.createDatabase(DATABASE_ID, ThroughputProperties.createManualThroughput(400));
            CosmosDatabase myDatabase = cosmosClient.getDatabase(DATABASE_ID);

            System.out.println("Created a new database:");
            System.out.println(myDatabase.toString());

            // Create a new collection.
            myDatabase.createContainer(COLLECTION_ID, "/keyPath", ThroughputProperties.createManualThroughput(1000));
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageHACosmosDB() {
    }
}
