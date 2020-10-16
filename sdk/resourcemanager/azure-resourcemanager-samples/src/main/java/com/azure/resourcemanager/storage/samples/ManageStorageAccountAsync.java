// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import reactor.core.publisher.Flux;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final AzureResourceManager azureResourceManager) {
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "sa", 8);
        final String storageAccountName2 = Utils.randomResourceName(azureResourceManager, "sa2", 8);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSTMS", 8);
        try {

            // ============================================================
            // Create storage accounts

            System.out.println("Creating a Storage Accounts");

            Flux.merge(
                    azureResourceManager.storageAccounts().define(storageAccountName)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync(),
                    azureResourceManager.storageAccounts().define(storageAccountName2)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync())
                    .map(storageAccount -> {
                        System.out.println("Created a Storage Account:");
                        Utils.print(storageAccount);
                        return storageAccount;
                    }).blockLast();

            // ============================================================
            // List storage accounts and regenerate storage account access keys

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azureResourceManager.storageAccounts();

            storageAccounts.listByResourceGroupAsync(rgName)
                    .flatMap(storageAccount -> {
                        System.out.println("Getting storage account access keys for Storage Account "
                                + storageAccount.name() + " created @ " + storageAccount.creationTime());

                        return storageAccount.getKeysAsync()
                                .flatMap(storageAccountKeys -> {
                                    System.out.println("Regenerating first storage account access key");
                                    return storageAccount.regenerateKeyAsync(storageAccountKeys.get(0).keyName());
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
                        return azureResourceManager.storageAccounts().deleteByIdAsync(storageAccount.id());
                    }).blockLast();

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().deleteByNameAsync(rgName).block();
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

    private ManageStorageAccountAsync() {

    }
}
