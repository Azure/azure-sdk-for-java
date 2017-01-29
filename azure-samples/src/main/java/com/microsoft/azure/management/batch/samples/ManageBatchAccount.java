/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.batch.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.batch.AccountKeyType;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Azure Batch sample for managing batch accounts -
 *  - Get subscription batch account quota for a particular location.
 *  - List all the batch accounts, look if quota allows you to create a new batch account at specified location by counting batch accounts in that particular location.
 *  - Create a batch account with new application and application package, along with new storage account.
 *  - Get the keys for batch account.
 *  - Regenerate keys for batch account
 *  - Regenerate the keys of storage accounts, sync with batch account.
 *  - Update application's display name.
 *  - Create another batch account using existing storage account.
 *  - List the batch account.
 *  - Delete the batch account.
 *      - Delete the application packages.
 *      - Delete applications.
 */

public final class ManageBatchAccount {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String batchAccountName = Utils.createRandomName("ba");
        final String storageAccountName = Utils.createRandomName("sa");
        final String applicationName = "application";
        final String applicationDisplayName = "My application display name";
        final String applicationPackageName = "app_package";
        final String batchAccountName2 = Utils.createRandomName("ba2");
        final String rgName = Utils.createRandomName("rgBAMB");
        final Region region = Region.AUSTRALIA_SOUTHEAST;
        final Region region2 = Region.US_WEST;

        try {

            // ===========================================================
            // Get how many batch accounts can be created in specified region.

            int allowedNumberOfBatchAccounts = azure.batchAccounts().getBatchAccountQuotaByLocation(region);

            // ===========================================================
            // List all the batch accounts in subscription.

            List<BatchAccount> batchAccounts = azure.batchAccounts().list();
            int batchAccountsAtSpecificRegion = 0;
            for (BatchAccount batchAccount: batchAccounts) {
                if (batchAccount.region() == region) {
                    batchAccountsAtSpecificRegion++;
                }
            }

            if (batchAccountsAtSpecificRegion >= allowedNumberOfBatchAccounts) {
                System.out.println("No more batch accounts can be created at "
                        + region + " region, this region already have "
                        + batchAccountsAtSpecificRegion
                        + " batch accounts, current quota to create batch account in "
                        + region + " region is " +  allowedNumberOfBatchAccounts + ".");
                return false;
            }

            // ============================================================
            // Create a batch account

            System.out.println("Creating a batch Account");

            BatchAccount batchAccount = azure.batchAccounts().define(batchAccountName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .defineNewApplication(applicationName)
                        .defineNewApplicationPackage(applicationPackageName)
                        .withAllowUpdates(true)
                        .withDisplayName(applicationDisplayName)
                        .attach()
                    .withNewStorageAccount(storageAccountName)
                    .create();

            System.out.println("Created a batch Account:");
            Utils.print(batchAccount);

            // ============================================================
            // Get | regenerate batch account access keys

            System.out.println("Getting batch account access keys");

            BatchAccountKeys batchAccountKeys = batchAccount.getKeys();

            Utils.print(batchAccountKeys);

            System.out.println("Regenerating primary batch account primary access key");

            batchAccountKeys = batchAccount.regenerateKeys(AccountKeyType.PRIMARY);

            Utils.print(batchAccountKeys);

            // ============================================================
            // Regenerate the keys for storage account
            StorageAccount storageAccount = azure.storageAccounts().getByGroup(rgName, storageAccountName);
            List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();

            Utils.print(storageAccountKeys);

            System.out.println("Regenerating first storage account access key");

            storageAccountKeys = storageAccount.regenerateKey(storageAccountKeys.get(0).keyName());

            Utils.print(storageAccountKeys);

            // ============================================================
            // Synchronize storage account keys with batch account

            batchAccount.synchronizeAutoStorageKeys();

            // ============================================================
            // Update name of application.
            batchAccount
                    .update()
                    .updateApplication(applicationName)
                    .withDisplayName("New application display name")
                    .parent()
                    .apply();

            batchAccount.refresh();
            Utils.print(batchAccount);

            // ============================================================
            // Create another batch account

            System.out.println("Creating another Batch Account");

            allowedNumberOfBatchAccounts = azure.batchAccounts().getBatchAccountQuotaByLocation(region2);

            // ===========================================================
            // List all the batch accounts in subscription.

            batchAccounts = azure.batchAccounts().list();
            batchAccountsAtSpecificRegion = 0;
            for (BatchAccount batch: batchAccounts) {
                if (batch.region() == region2) {
                    batchAccountsAtSpecificRegion++;
                }
            }

            BatchAccount batchAccount2 = null;
            if (batchAccountsAtSpecificRegion < allowedNumberOfBatchAccounts) {
                batchAccount2 = azure.batchAccounts().define(batchAccountName2)
                        .withRegion(region2)
                        .withExistingResourceGroup(rgName)
                        .withExistingStorageAccount(storageAccount)
                        .create();

                System.out.println("Created second Batch Account:");
                Utils.print(batchAccount2);
            }

            // ============================================================
            // List batch accounts

            System.out.println("Listing Batch accounts");

            List<BatchAccount> accounts = azure.batchAccounts().listByGroup(rgName);
            BatchAccount ba;
            for (int i = 0; i < accounts.size(); i++) {
                ba = accounts.get(i);
                System.out.println("Batch Account (" + i + ") " + ba.name());
            }

            // ============================================================
            // Refresh a batch account.
            batchAccount.refresh();
            Utils.print(batchAccount);

            // ============================================================
            // Delete a batch account

            System.out.println("Deleting a batch account - " + batchAccount.name());

            for (Map.Entry<String, Application> applicationEntry: batchAccount.applications().entrySet()) {
                for (Map.Entry<String, ApplicationPackage> applicationPackageEntry: applicationEntry.getValue().applicationPackages().entrySet()) {
                    System.out.println("Deleting a application package - " + applicationPackageEntry.getKey());
                    applicationPackageEntry.getValue().delete();
                }
                System.out.println("Deleting a application - " + applicationEntry.getKey());
                batchAccount.update().withoutApplication(applicationEntry.getKey()).apply();
            }

            azure.batchAccounts().deleteById(batchAccount.id());

            System.out.println("Deleted batch account");

            if (batchAccount2 != null) {
                System.out.println("Deleting second batch account - " + batchAccount2.name());
                azure.batchAccounts().deleteById(batchAccount2.id());
                System.out.println("Deleted second batch account");
            }

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
                    .withLogLevel(LogLevel.BASIC)
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

    private ManageBatchAccount() {
    }
}