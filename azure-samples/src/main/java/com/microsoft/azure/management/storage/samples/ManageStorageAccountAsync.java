/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.rest.LogLevel;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;
import java.util.List;

/**
 * Azure Storage sample for managing storage accounts -
 *  - Create two storage account
 *  - List storage accounts and regenerate storage account access keys
 *  - Delete both storage account.
 */

public final class ManageStorageAccountAsync {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        final String storageAccountName = Utils.createRandomName("sa");
        final String storageAccountName2 = Utils.createRandomName("sa2");
        final String rgName = Utils.createRandomName("rgSTMS");
        try {

            // ============================================================
            // Create storage accounts

            System.out.println("Creating a Storage Accounts");

            Observable.merge(
                    azure.storageAccounts().define(storageAccountName)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync(),
                    azure.storageAccounts().define(storageAccountName2)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync())
                    .map(new Func1<Indexable, Indexable>() {
                        @Override
                        public Indexable call(Indexable indexable) {
                            if (indexable instanceof StorageAccount) {
                                StorageAccount storageAccount = (StorageAccount) indexable;

                                System.out.println("Created a Storage Account:");
                                Utils.print(storageAccount);
                            }
                            return indexable;
                        }
                    }).toBlocking().last();

            // ============================================================
            // List storage accounts and regenerate storage account access keys

            System.out.println("Listing storage accounts");

            StorageAccounts storageAccounts = azure.storageAccounts();

            storageAccounts.listByResourceGroupAsync(rgName)
                    .flatMap(new Func1<StorageAccount, Observable<List<StorageAccountKey>>>() {
                        @Override
                        public Observable<List<StorageAccountKey>> call(final StorageAccount storageAccount) {
                            System.out.println("Getting storage account access keys for Storage Account "
                                    + storageAccount.name() + " created @ " + storageAccount.creationTime());

                            return storageAccount.getKeysAsync()
                                    .flatMap(new Func1<List<StorageAccountKey>, Observable<List<StorageAccountKey>>>() {
                                    @Override
                                    public Observable<List<StorageAccountKey>> call(List<StorageAccountKey> storageAccountKeys) {
                                        System.out.println("Regenerating first storage account access key");
                                        return storageAccount.regenerateKeyAsync(storageAccountKeys.get(0).keyName());
                                    }
                                });
                        }
                    })
                    .map(new Func1<List<StorageAccountKey>, List<StorageAccountKey>>() {
                        @Override
                        public List<StorageAccountKey> call(List<StorageAccountKey> storageAccountKeys) {
                            Utils.print(storageAccountKeys);
                            return storageAccountKeys;
                        }
                    }).toBlocking().last();

            // ============================================================
            // Delete storage accounts

            storageAccounts.listByResourceGroupAsync(rgName)
                    .flatMap(new Func1<StorageAccount, Observable<Void>>() {
                        @Override
                        public Observable<Void> call(StorageAccount storageAccount) {
                            System.out.println("Deleting a storage account - " + storageAccount.name()
                                    + " created @ " + storageAccount.creationTime());
                            return azure.storageAccounts().deleteByIdAsync(storageAccount.id()).toObservable();
                        }
                    }).toCompletable().await();

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName)
                        .await();
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

    private ManageStorageAccountAsync() {

    }
}