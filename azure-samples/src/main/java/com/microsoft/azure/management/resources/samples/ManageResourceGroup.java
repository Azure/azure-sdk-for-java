/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.LogLevel;

import java.io.File;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgRSMA", 24);
        final String rgName2 = SdkContext.randomResourceName("rgRSMA", 24);
        final String resourceTagName = SdkContext.randomResourceName("rgRSTN", 24);
        final String resourceTagValue = SdkContext.randomResourceName("rgRSTV", 24);
        try {


            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
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

            azure.resourceGroups().define(rgName2)
                    .withRegion(Region.US_WEST)
                    .create();

            System.out.println("Created another resource group with name: " + rgName2);


            //=============================================================
            // List resource groups.

            System.out.println("Listing all resource groups");

            for (ResourceGroup rGroup : azure.resourceGroups().list()) {
                System.out.println("Resource group: " + rGroup.name());
            }


            //=============================================================
            // Delete a resource group.

            System.out.println("Deleting resource group: " + rgName2);

            azure.resourceGroups().deleteByName(rgName2);

            System.out.println("Deleted resource group: " + rgName2);
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

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
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
