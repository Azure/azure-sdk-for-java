/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class RedisResourceOperationsTests extends RedisManagementTestBase {
    private static final String RG_NAME = "javacsmrg374";
    private static final String RR_NAME = "javacsmsa374";
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
    public void canCRUDRedisCache() throws Exception {
        // Create
        RedisResource redisResource = redisManager.redisResources()
                .define(RR_NAME)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(RG_NAME)
                .withSku(SkuName.STANDARD, SkuFamily.C)
                .create();
        Assert.assertEquals(RG_NAME, redisResource.resourceGroupName());
        Assert.assertEquals(SkuName.STANDARD, redisResource.sku().name());

        redisManager.redisResources().delete(redisResource.id());
        /*
        // List
        List<StorageAccount> accounts = storageManager.storageAccounts().listByGroup(RG_NAME);
        boolean found = false;
        for (StorageAccount account : accounts) {
            if (account.name().equals(RR_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        storageAccount = storageManager.storageAccounts().getByGroup(RG_NAME, RR_NAME);
        Assert.assertNotNull(storageAccount);

        // Get Keys
        List<StorageAccountKey> keys = storageAccount.keys();
        Assert.assertTrue(keys.size() > 0);

        // Regen key
        StorageAccountKey oldKey = keys.get(0);
        List<StorageAccountKey> updatedKeys = storageAccount.regenerateKey(oldKey.keyName());
        Assert.assertTrue(updatedKeys.size() > 0);
        for (StorageAccountKey updatedKey : updatedKeys) {
            if (updatedKey.keyName().equalsIgnoreCase(oldKey.keyName())) {
                Assert.assertNotEquals(oldKey.value(), updatedKey.value());
                break;
            }
        }

        // Update
        try {
            storageAccount.update()
                    .withAccessTier(AccessTier.COOL)
                    .apply();
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
        storageAccount = storageAccount.update()
                .withSku(SkuName.STANDARD_LRS)
                .apply();
        Assert.assertEquals(SkuName.STANDARD_LRS, storageAccount.sku().name());
        */
    }
}
