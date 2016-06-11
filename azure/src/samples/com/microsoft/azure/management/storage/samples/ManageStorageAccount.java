/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.storage.samples;

import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import java.io.File;

/**
 * Azure Storage sample for managing storage accounts -
 *  - Create a storage account
 *  - Set a default storage account
 *  - Create storage account access keys
 *  - Create another storage account
 *  - List storage accounts
 *  - Delete a storage account
 */

public class ManageStorageAccount {

    public static void main(String[] args) {

        try {

            final File credFile = new File("my.azureauth");

            Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

            System.out.println(String.valueOf(azure.resourceGroups().list().size()));

            Azure.configure().withLogLevel(Level.BASIC).authenticate(credFile);
            System.out.println("Selected subscription: " + azure.subscriptionId());
            System.out.println(String.valueOf(azure.resourceGroups().list().size()));

            final String storageAccountName = Utils.createRandomName("sa");

            // Create a storage account
            StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                    .withRegion(Region.US_EAST)
                    .withNewGroup()
                    .withAccountType(AccountType.PREMIUM_LRS)
                    .create();


            // Set a default storage account


            // Create storage account access keys


            // Create another storage account


            // List storage accounts


            // Delete a storage account

        } catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }

}