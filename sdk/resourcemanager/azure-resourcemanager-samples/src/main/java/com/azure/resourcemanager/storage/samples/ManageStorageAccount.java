// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccounts;

import java.util.List;

/**
 * Azure Storage sample for managing storage accounts -
 * - Create a storage account
 * - Get | regenerate storage account access keys
 * - Create another storage account
 * - Create another storage account of V2 kind
 * - List storage accounts
 * - Delete a storage account.
 */

public final class ManageStorageAccount {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "sa", 8);
        final String storageAccountName2 = Utils.randomResourceName(azureResourceManager, "sa2", 8);
        final String storageAccountName3 = Utils.randomResourceName(azureResourceManager, "sa3", 8);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSTMS", 8);
        try {

            // ============================================================
            // Create a storage account

            System.out.println("Creating a Storage Account");

            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
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

            StorageAccount storageAccount2 = azureResourceManager.storageAccounts().define(storageAccountName2)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created a Storage Account:");
            Utils.print(storageAccount2);

            // Create a V2 storage account

            System.out.println("Creating a V2 Storage Account");

            azureResourceManager.storageAccounts().define(storageAccountName3)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withGeneralPurposeAccountKindV2()
                .create();

            System.out.println("Created V2 Storage Account");

            // ============================================================
            // List storage accounts

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azureResourceManager.storageAccounts();

            PagedIterable<StorageAccount> accounts = storageAccounts.listByResourceGroup(rgName);
            for (StorageAccount sa : accounts) {
                System.out.println("Storage Account " + sa.name()
                        + " created @ " + sa.creationTime());
            }

            // ============================================================
            // Delete a storage account

            System.out.println("Deleting a storage account - " + storageAccount.name()
                    + " created @ " + storageAccount.creationTime());

            azureResourceManager.storageAccounts().deleteById(storageAccount.id());

            System.out.println("Deleted storage account");
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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

    private ManageStorageAccount() {

    }
}
