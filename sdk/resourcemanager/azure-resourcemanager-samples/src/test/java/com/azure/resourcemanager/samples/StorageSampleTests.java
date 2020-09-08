// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;


import com.azure.resourcemanager.storage.samples.ManageStorageAccount;
import com.azure.resourcemanager.storage.samples.ManageStorageAccountAsync;
import com.azure.resourcemanager.storage.samples.ManageStorageAccountNetworkRules;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageSampleTests extends SamplesTestBase {
    @Test
    public void testManageStorageAccount() throws Exception {
        Assertions.assertTrue(ManageStorageAccount.runSample(azure));
    }

    @Test
    public void testManageStorageAccountAsync() throws Exception {
        Assertions.assertTrue(ManageStorageAccountAsync.runSample(azure));
    }

    @Test
    public void testManageStorageAccountNetworkRules() throws Exception {
        Assertions.assertTrue(ManageStorageAccountNetworkRules.runSample(azure));
    }
}
