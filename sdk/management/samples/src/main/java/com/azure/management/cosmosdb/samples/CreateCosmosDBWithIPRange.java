// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.cosmosdb.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.CloudException;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.cosmosdb.CosmosDBAccount;
import com.azure.management.cosmosdb.DatabaseAccountKind;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;

import java.io.File;

/**
 * Azure CosmosDB sample for high availability.
 *  - Create a CosmosDB configured with IP range filterR
 *  - Delete the CosmosDB.
 */
public final class CreateCosmosDBWithIPRange {
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
                    .withIpRangeFilter("13.91.6.132,13.91.6.1/24")
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Delete CosmosDB
            System.out.println("Deleting the CosmosDB");
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

    private CreateCosmosDBWithIPRange() {
    }
}
