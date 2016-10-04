/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.samples;

import java.io.File;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;

import okhttp3.logging.HttpLoggingInterceptor;

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
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {

        try {
            final String rgName = ResourceNamer.randomResourceName("rgRSMA", 24);
            final String rgName2 = ResourceNamer.randomResourceName("rgRSMA", 24);
            final String resourceTagName = ResourceNamer.randomResourceName("rgRSTN", 24);
            final String resourceTagValue = ResourceNamer.randomResourceName("rgRSTV", 24);

            try {


                //=================================================================
                // Authenticate

                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

                Azure azure = Azure.configure()
                        .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                        .authenticate(credFile)
                        .withDefaultSubscription();

                try {


                    //=============================================================
                    // Create resource group.

                    System.out.println("Creating a resource group with name: " + rgName);

                    ResourceGroup resourceGroup = azure.resourceGroups()
                            .define(rgName)
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

                    azure.resourceGroups()
                        .define(rgName)
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

                    azure.resourceGroups().delete(rgName2);

                    System.out.println("Deleted resource group: " + rgName2);

                } catch (Exception f) {

                    System.out.println(f.getMessage());
                    f.printStackTrace();

                } finally {

                    try {
                        System.out.println("Deleting Resource Group: " + rgName);
                        azure.resourceGroups().delete(rgName);
                        System.out.println("Deleted Resource Group: " + rgName);
                    } catch (NullPointerException npe) {
                        System.out.println("Did not create any resources in Azure. No clean up is necessary");
                    } catch (Exception g) {
                        g.printStackTrace();
                    }

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
