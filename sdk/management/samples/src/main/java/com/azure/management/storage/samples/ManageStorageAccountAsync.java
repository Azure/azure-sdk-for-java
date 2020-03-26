/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.management.Azure;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccounts;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Azure Storage sample for managing storage accounts -
 * - Create two storage account
 * - List storage accounts and regenerate storage account access keys
 * - Delete both storage account.
 */

public final class ManageStorageAccountAsync {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        final String storageAccountName = azure.sdkContext().randomResourceName("sa", 8);
        final String storageAccountName2 = azure.sdkContext().randomResourceName("sa2", 8);
        final String rgName = azure.sdkContext().randomResourceName("rgSTMS", 8);
        try {

            // ============================================================
            // Create storage accounts

            System.out.println("Creating a Storage Accounts");

            Flux.merge(
                    azure.storageAccounts().define(storageAccountName)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync(),
                    azure.storageAccounts().define(storageAccountName2)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync())
                    .map(indexable -> {
                        if (indexable instanceof StorageAccount) {
                            StorageAccount storageAccount = (StorageAccount) indexable;

                            System.out.println("Created a Storage Account:");
                            Utils.print(storageAccount);
                        }
                        return indexable;
                    }).blockLast();

            // ============================================================
            // List storage accounts and regenerate storage account access keys

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azure.storageAccounts();

            storageAccounts.listByResourceGroupAsync(rgName)
                    .flatMap(storageAccount -> {
                        System.out.println("Getting storage account access keys for Storage Account "
                                + storageAccount.name() + " created @ " + storageAccount.creationTime());

                        return storageAccount.getKeysAsync()
                                .flatMap(storageAccountKeys -> {
                                    System.out.println("Regenerating first storage account access key");
                                    return storageAccount.regenerateKeyAsync(storageAccountKeys.get(0).getKeyName());
                                });
                    })
                    .map(storageAccountKeys -> {
                        Utils.print(storageAccountKeys);
                        return storageAccountKeys;
                    }).blockLast();

            // ============================================================
            // Delete storage accounts

            storageAccounts.listByResourceGroupAsync(rgName)
                    .flatMap(storageAccount -> {
                        System.out.println("Deleting a storage account - " + storageAccount.name()
                                + " created @ " + storageAccount.creationTime());
                        return azure.storageAccounts().deleteByIdAsync(storageAccount.id());
                    }).blockLast();

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName).block();
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
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

    private ManageStorageAccountAsync() {

    }
}