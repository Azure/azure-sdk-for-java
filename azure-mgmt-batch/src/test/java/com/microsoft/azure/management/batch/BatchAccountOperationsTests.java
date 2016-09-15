/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BatchAccountOperationsTests extends BatchManagementTestBase {
    private static final String RG_NAME = "javacbatch385";
    private static final String BATCH_NAME = "javacsmsa385";
    private static final String SA_NAME = "javacsmsa385";

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
    public void canCRUDBatchAccount() throws Exception {
        // Create
        BatchAccount batchAccount = batchManager.batchAccounts()
                .define(BATCH_NAME)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(RG_NAME)
                .createAsync()
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, batchAccount.resourceGroupName());
        Assert.assertNull(batchAccount.autoStorage());
        // List
        List<BatchAccount> accounts = batchManager.batchAccounts().listByGroup(RG_NAME);
        boolean found = false;
        for (BatchAccount account : accounts) {
            if (account.name().equals(BATCH_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        batchAccount = batchManager.batchAccounts().getByGroup(RG_NAME, BATCH_NAME);
        Assert.assertNotNull(batchAccount);

        // Get Keys
        BatchAccountKeys keys = batchAccount.keys();
        Assert.assertNotNull(keys.primary());
        Assert.assertNotNull(keys.secondary());

        BatchAccountKeys newKeys = batchAccount.regenerateKeys(AccountKeyType.PRIMARY);
        Assert.assertNotNull(newKeys.primary());
        Assert.assertNotNull(newKeys.secondary());

        Assert.assertNotEquals(newKeys.primary(), keys.primary());
        Assert.assertEquals(newKeys.secondary(), keys.secondary());

        batchAccount = batchAccount.update()
                .withNewStorageAccount(SA_NAME)
                .apply();

        Assert.assertNotNull(batchAccount.autoStorage().storageAccountId());
        Assert.assertNotNull(batchAccount.autoStorage().lastKeySync());

        DateTime lastSync = batchAccount.autoStorage().lastKeySync();

        batchAccount.synchronizeAutoStorageKeys();
        batchAccount.refresh();

        Assert.assertNotEquals(lastSync, batchAccount.autoStorage().lastKeySync());

        batchManager.batchAccounts().delete(batchAccount.resourceGroupName(), batchAccount.name());
        try {
            batchManager.batchAccounts().getById(batchAccount.id());
            Assert.assertTrue(false);
        }
        catch (CloudException exception) {
            Assert.assertEquals(exception.getResponse().code(), 404);
        }
    }
}
