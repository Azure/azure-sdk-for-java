// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure Resource sample for managing resource groups -
 * - Create a resource group
 * - Update a resource group
 * - Create another resource group
 * - List resource groups
 * - Delete a resource group.
 */

public final class ManageResourceGroup {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgRSMA", 24);
        final String rgName2 = Utils.randomResourceName(azureResourceManager, "rgRSMA", 24);
        final String resourceTagName = Utils.randomResourceName(azureResourceManager, "rgRSTN", 24);
        final String resourceTagValue = Utils.randomResourceName(azureResourceManager, "rgRSTV", 24);
        try {


            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_WEST)
                    .create();

            System.out.println("Created a resource group with name: " + rgName);


            //=============================================================
            // Update the resource group.

            System.out.println("Updating the resource group with name: " + rgName);

            resourceGroup.update()
                    .withTag(resourceTagName, resourceTagValue)
                    .apply();

            System.out.println("Updated the resource group with name: " + rgName);


            //=============================================================
            // Create another resource group.

            System.out.println("Creating another resource group with name: " + rgName2);

            azureResourceManager.resourceGroups().define(rgName2)
                    .withRegion(Region.US_WEST)
                    .create();

            System.out.println("Created another resource group with name: " + rgName2);


            //=============================================================
            // List resource groups.

            System.out.println("Listing all resource groups");

            for (ResourceGroup rGroup : azureResourceManager.resourceGroups().list()) {
                System.out.println("Resource group: " + rGroup.name());
            }


            //=============================================================
            // Delete a resource group.

            System.out.println("Deleting resource group: " + rgName2);

            azureResourceManager.resourceGroups().beginDeleteByName(rgName2);
            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
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

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
