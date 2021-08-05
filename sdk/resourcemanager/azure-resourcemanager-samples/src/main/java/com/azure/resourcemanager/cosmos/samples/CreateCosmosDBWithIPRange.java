// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure CosmosDB sample for high availability.
 *  - Create a CosmosDB configured with IP range filterR
 *  - Delete the CosmosDB.
 */
public final class CreateCosmosDBWithIPRange {
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
                    .withIpRangeFilter("13.91.6.132,13.91.6.1/24")
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Delete CosmosDB
            System.out.println("Deleting the CosmosDB");
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

    private CreateCosmosDBWithIPRange() {
    }
}
