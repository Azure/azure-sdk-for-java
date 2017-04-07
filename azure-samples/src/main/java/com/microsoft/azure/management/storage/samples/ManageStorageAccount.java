/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;

/**
 * Azure Storage sample for managing storage accounts -
 *  - Create a storage account
 *  - Get | regenerate storage account access keys
 *  - Create another storage account
 *  - List storage accounts
 *  - Delete a storage account.
 */

public final class ManageStorageAccount {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String storageAccountName = Utils.createRandomName("sa");
        final String storageAccountName2 = Utils.createRandomName("sa2");
        final String rgName = Utils.createRandomName("rgSTMS");
        try {

            // ============================================================
            // Create a storage account

            System.out.println("Creating a Storage Account");

            StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created a Storage Account:");
            Utils.print(storageAccount);


            // ============================================================
            // Get | regenerate storage account access keys

            System.out.println("Getting storage account access keys");

            List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();

            Utils.print(storageAccountKeys);

            System.out.println("Regenerating first storage account access key");

            storageAccountKeys = storageAccount.regenerateKey(storageAccountKeys.get(0).keyName());

            Utils.print(storageAccountKeys);


            // ============================================================
            // Create another storage account

            System.out.println("Creating a 2nd Storage Account");

            StorageAccount storageAccount2 = azure.storageAccounts().define(storageAccountName2)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created a Storage Account:");
            Utils.print(storageAccount2);


            // ============================================================
            // List storage accounts

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azure.storageAccounts();

            List<StorageAccount> accounts = storageAccounts.listByResourceGroup(rgName);
            StorageAccount sa;
            for (int i = 0; i < accounts.size(); i++) {
                sa = (StorageAccount) accounts.get(i);
                System.out.println("Storage Account (" + i + ") " + sa.name()
                        + " created @ " + sa.creationTime());
            }

            // ============================================================
            // Delete a storage account

            System.out.println("Deleting a storage account - " + storageAccount.name()
                    + " created @ " + storageAccount.creationTime());

            azure.storageAccounts().deleteById(storageAccount.id());

            System.out.println("Deleted storage account");
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            }
            catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageStorageAccount() {

    }
}