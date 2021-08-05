// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;


import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.storage.samples.ManageStorageAccount;
import com.azure.resourcemanager.storage.samples.ManageStorageAccountAsync;
import com.azure.resourcemanager.storage.samples.ManageStorageAccountCustomerManagedKey;
import com.azure.resourcemanager.storage.samples.ManageStorageAccountNetworkRules;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageSampleTests extends SamplesTestBase {
    @Test
    public void testManageStorageAccount() {
        Assertions.assertTrue(ManageStorageAccount.runSample(azureResourceManager));
    }

    @Test
    public void testManageStorageAccountAsync() {
        Assertions.assertTrue(ManageStorageAccountAsync.runSample(azureResourceManager));
    }

    @Test
    public void testManageStorageAccountNetworkRules() {
        Assertions.assertTrue(ManageStorageAccountNetworkRules.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true) // requires generate a key
    public void testManageStorageAccountCustomerManagedKey() {
        Assertions.assertTrue(ManageStorageAccountCustomerManagedKey.runSample(azureResourceManager, clientIdFromFile()));
    }
}
