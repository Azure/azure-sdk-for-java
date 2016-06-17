/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.storage.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Azure Storage sample for managing storage accounts -
 *  - Create a storage account
 *  - Set a default storage account
 *  - Create storage account access keys
 *  - Create another storage account
 *  - List storage accounts
 *  - Delete a storage account.
 */

public final class ManageStorageAccount {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String storageAccountName = Utils.createRandomName("sa");
        final String rgName = Utils.createRandomName("rgSTMS");

        try {

            final File credFile = new File("my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {


                // ============================================================
                // Create a storage account
                StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .create();


                // Set a default storage account


                // Create storage account access keys


                // Create another storage account


                // List storage accounts


                // Delete a storage account
            } catch (Exception f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            } finally {
                if (azure.resourceGroups().get(rgName) != null) {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } else {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageStorageAccount() {

    }


}