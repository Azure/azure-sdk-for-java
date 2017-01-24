/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

import java.util.List;

import static org.junit.Assert.fail;

public class StorageAccountOperationsTests extends StorageManagementTest {
    private static String RG_NAME = "";
    private static String SA_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        SA_NAME = generateRandomResourceName("javacsmsa", 15);

        super.initializeClients(restClient, defaultSubscription, domain);
    }
    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRUDStorageAccount() throws Exception {
        // Name available
        CheckNameAvailabilityResult result = storageManager.storageAccounts()
                .checkNameAvailability(SA_NAME);
        Assert.assertEquals(true, result.isAvailable());
        // Create
        Observable<Indexable> resourceStream = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(RG_NAME)
                .createAsync();
        StorageAccount storageAccount = Utils.<StorageAccount>rootResource(resourceStream)
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, storageAccount.resourceGroupName());
        Assert.assertEquals(SkuName.STANDARD_GRS, storageAccount.sku().name());
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

        // Get Keys
        List<StorageAccountKey> keys = storageAccount.getKeys();
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
    }
}
