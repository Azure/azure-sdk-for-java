/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.implementation.CheckNameAvailabilityResult;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class StorageAccountOperationsTests extends StorageManagementTestBase {
    private static final String RG_NAME = "javacsmrg7";
    private static final String SA_NAME = "javacsmsa2";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCRUDStorageAccount() throws Exception {
        // Name available
        CheckNameAvailabilityResult result = storageManager.storageAccounts()
                .checkNameAvailability(SA_NAME);
        Assert.assertEquals(CheckNameAvailabilityResult.AVAILABLE, result);
        // Create
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.ASIA_EAST)
                .withNewGroup(RG_NAME)
                .withAccountType(AccountType.STANDARD_LRS)
                .create();
        Assert.assertEquals(RG_NAME, storageAccount.resourceGroupName());
        Assert.assertEquals(AccountType.STANDARD_LRS, storageAccount.accountType());
        // List
        List<StorageAccount> accounts = storageManager.storageAccounts().listByGroup(RG_NAME);
        boolean found = false;
        for (StorageAccount account : accounts) {
            if (account.name().equals(SA_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        storageAccount = storageManager.storageAccounts().getByGroup(RG_NAME, SA_NAME);
        Assert.assertNotNull(storageAccount);
        // Update
        storageAccount = storageAccount.update()
                .withAccountType(AccountType.STANDARD_GRS)
                .apply();
        Assert.assertEquals(AccountType.STANDARD_GRS, storageAccount.accountType());
    }
}
