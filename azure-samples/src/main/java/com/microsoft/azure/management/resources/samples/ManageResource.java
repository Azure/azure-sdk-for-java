/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure Resource sample for managing resources -
 * - Create a resource
 * - Update a resource
 * - Create another resource
 * - List resources
 * - Delete a resource.
 */

public final class ManageResource {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgRSMR", 24);
        final String resourceName1 = SdkContext.randomResourceName("rn1", 24);
        final String resourceName2 = SdkContext.randomResourceName("rn2", 24);
        try {


            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            azure.resourceGroups().define(rgName)
                    .withRegion(Region.US_WEST)
                    .create();


            //=============================================================
            // Create storage account.

            System.out.println("Creating a storage account with name: " + resourceName1);

            StorageAccount storageAccount = azure.storageAccounts().define(resourceName1)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Storage account created: " + storageAccount.id());


            //=============================================================
            // Update - set the sku name

            System.out.println("Updating the storage account with name: " + resourceName1);

            storageAccount.update()
                    .withSku(SkuName.STANDARD_RAGRS)
                    .apply();

            System.out.println("Updated the storage account with name: " + resourceName1);


            //=============================================================
            // Create another storage account.

            System.out.println("Creating another storage account with name: " + resourceName2);

            StorageAccount storageAccount2 = azure.storageAccounts().define(resourceName2)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Storage account created: " + storageAccount2.id());


            //=============================================================
            // List storage accounts.

            System.out.println("Listing all storage accounts for resource group: " + rgName);

            for (StorageAccount sAccount : azure.storageAccounts().list()) {
                System.out.println("Storage account: " + sAccount.name());
            }


            //=============================================================
            // Delete a storage accounts.

            System.out.println("Deleting storage account: " + resourceName2);

            azure.storageAccounts().deleteById(storageAccount2.id());

            System.out.println("Deleted storage account: " + resourceName2);
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
