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
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure DocumentDB sample for high availability -
 *  - Create a documentdb configured with IP range filter
 *  - Delete the document db.
 */
public final class DocumentDBWithIPRange {
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
            // Create a documentdb

            System.out.println("Creating a documentdb...");
            DocumentDBAccount documentDBAccount = azure.documentDBs().define(docDBName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                    .withSessionConsistency()
                    .withWriteReplication(Region.US_WEST)
                    .withReadReplication(Region.US_CENTRAL)
                    .withIpRangeFilter("13.91.6.132,13.91.6.1/24")
                    .create();

            System.out.println("Created documentdb");
            Utils.print(documentDBAccount);

            //============================================================
            // Delete documentdb
            System.out.println("Deleting the docuemntdb");
            azure.documentDBs().deleteById(documentDBAccount.id());
            System.out.println("Deleted the documentdb");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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

    private DocumentDBWithIPRange() {
    }
}
