// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.resourcemanager.storage.models.StorageService;

import java.util.List;
import java.util.Map;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String storageAccountName = azure.sdkContext().randomResourceName("sa", 8);
        final String storageAccountName2 = azure.sdkContext().randomResourceName("sa2", 8);
        final String storageAccountName3 = azure.sdkContext().randomResourceName("sa3", 8);
        final String rgName = azure.sdkContext().randomResourceName("rgSTMS", 8);
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
            // Update storage account by enabling encryption

            System.out.println("Enabling blob encryption for the storage account: " + storageAccount2.name());

            storageAccount2.update()
                    .withBlobEncryption()
                    .apply();

            for (Map.Entry<StorageService, StorageAccountEncryptionStatus> encryptionStatus : storageAccount2.encryptionStatuses().entrySet()) {
                String status = encryptionStatus.getValue().isEnabled() ? "Enabled" : "Not enabled";
                System.out.println("Encryption status of the service " + encryptionStatus.getKey() + ":" + status);
            }

            // Create a V2 storage account

            System.out.println("Creating a V2 Storage Account");

            azure.storageAccounts().define(storageAccountName3)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withGeneralPurposeAccountKindV2()
                .create();

            System.out.println("Created V2 Storage Account");

            // ============================================================
            // List storage accounts

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azure.storageAccounts();

            PagedIterable<StorageAccount> accounts = storageAccounts.listByResourceGroup(rgName);
            for (StorageAccount sa : accounts) {
                System.out.println("Storage Account " + sa.name()
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
                azure.resourceGroups().beginDeleteByName(rgName);
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
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
