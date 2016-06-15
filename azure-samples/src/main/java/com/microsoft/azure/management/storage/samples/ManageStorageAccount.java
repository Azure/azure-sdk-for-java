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

        try {

            final File credFile = new File("my.azureauth");

            Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            final String storageAccountName = Utils.createRandomName("sa");

            // Create a storage account
            StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                    .withRegion(Region.US_EAST)
                    .withNewGroup()
                    .create();

            // Set a default storage account


            // Create storage account access keys


            // Create another storage account


            // List storage accounts


            // Delete a storage account

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private ManageStorageAccount() {

    }


}