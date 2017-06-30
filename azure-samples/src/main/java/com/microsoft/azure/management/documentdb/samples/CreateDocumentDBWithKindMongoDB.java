/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.documentdb.samples;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.documentdb.DatabaseAccountKind;
import com.microsoft.azure.management.documentdb.DocumentDBAccount;
import com.microsoft.azure.management.documentdb.implementation.DatabaseAccountListConnectionStringsResult;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure DocumentDB sample for high availability.
 *  - Create a DocumentDB configured with MongoDB kind.
 *  - Get the mongodb connection string
 *  - Delete the DocumentDB.
 */
public final class CreateDocumentDBWithKindMongoDB {
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
            DocumentDBAccount documentDBAccount = azure.documentDBAccounts().define(docDBName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.MONGO_DB)
                    .withEventualConsistency()
                    .withWriteReplication(Region.US_WEST)
                    .withReadReplication(Region.US_CENTRAL)
                    .create();

            System.out.println("Created DocumentDB");
            Utils.print(documentDBAccount);

            System.out.println("Get the MongoDB connection string");
            DatabaseAccountListConnectionStringsResult databaseAccountListConnectionStringsResult = documentDBAccount.listConnectionStrings();
            System.out.println("MongoDB connection string: "
                    + databaseAccountListConnectionStringsResult.connectionStrings().get(0).connectionString());

            //============================================================
            // Delete DocumentDB
            System.out.println("Deleting the DocumentDB");
            azure.documentDBAccounts().deleteById(documentDBAccount.id());
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

    private CreateDocumentDBWithKindMongoDB() {
    }
}
