/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.documentdb.samples;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.documentdb.DatabaseAccountKind;
import com.microsoft.azure.management.documentdb.DocumentDBAccount;
import com.microsoft.azure.management.documentdb.implementation.DatabaseAccountListKeysResult;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure DocumentDB sample for high availability.
 *  - Create a DocumentDB configured with a single read location
 *  - Get the credentials for the DocumentDB
 *  - Update the DocumentDB with additional read locations
 *  - add collection to the DocumentDB with throughput 4000
 *  - Delete the DocumentDB
 */
public final class ManageHADocumentDB {
    static final String DATABASE_ID = "TestDB";
    static final String COLLECTION_ID = "TestCollection";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param clientId client id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String clientId) {
        final String docDBName = SdkContext.randomResourceName("docDb", 10);
        final String rgName = SdkContext.randomResourceName("rgNEMV", 24);

        try {
            //============================================================
            // Create a DocumentDB

            System.out.println("Creating a DocumentDB...");
            DocumentDBAccount documentDBAccount = azure.documentDBs().define(docDBName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                    .withSessionConsistency()
                    .withWriteReplication(Region.US_WEST)
                    .withReadReplication(Region.US_CENTRAL)
                    .create();

            System.out.println("Created DocumentDB");
            Utils.print(documentDBAccount);

            //============================================================
            // Update document db with three additional read regions

            System.out.println("Updating DocumentDB with three additional read replication regions");
            documentDBAccount = documentDBAccount.update()
                    .withReadReplication(Region.ASIA_EAST)
                    .withReadReplication(Region.AUSTRALIA_SOUTHEAST)
                    .withReadReplication(Region.UK_SOUTH)
                    .apply();
                    
            System.out.println("Updated DocumentDB");
            Utils.print(documentDBAccount);

            //============================================================
            // Get credentials for the DocumentDB.

            System.out.println("Get credentials for the DocumentDB");
            DatabaseAccountListKeysResult databaseAccountListKeysResult = documentDBAccount.listKeys();
            String masterKey = databaseAccountListKeysResult.primaryMasterKey();
            String endPoint = documentDBAccount.documentEndpoint();

            //============================================================
            // Connect to DocumentDB and add a collection

            System.out.println("Connecting and adding collection");
            createDBAndAddCollection(masterKey, endPoint);

            //============================================================
            // Delete DocumentDB.
            System.out.println("Deleting the docuemntdb");
            azure.documentDBs().deleteById(documentDBAccount.id());
            System.out.println("Deleted the DocumentDB");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted resource group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }

        return false;
    }

    private static void createDBAndAddCollection(String masterKey, String endPoint) throws DocumentClientException {
        try {
            DocumentClient documentClient = new DocumentClient(endPoint,
                    masterKey, ConnectionPolicy.GetDefault(),
                    ConsistencyLevel.Session);

            // Define a new database using the id above.
            Database myDatabase = new Database();
            myDatabase.setId(DATABASE_ID);

            myDatabase = documentClient.createDatabase(myDatabase, null)
                    .getResource();

            System.out.println("Created a new database:");
            System.out.println(myDatabase.toString());

            // Define a new collection using the id above.
            DocumentCollection myCollection = new DocumentCollection();
            myCollection.setId(COLLECTION_ID);

            // Set the provisioned throughput for this collection to be 1000 RUs.
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setOfferThroughput(4000);

            // Create a new collection.
            myCollection = documentClient.createCollection(
                    "dbs/" + DATABASE_ID, myCollection, requestOptions)
                    .getResource();
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
                    .withLogLevel(LogLevel.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, ApplicationTokenCredentials.fromFile(credFile).clientId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageHADocumentDB() {
    }
}
