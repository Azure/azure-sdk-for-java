/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;


import com.azure.management.storage.samples.ManageStorageAccount;
import com.azure.management.storage.samples.ManageStorageAccountAsync;
import com.azure.management.storage.samples.ManageStorageAccountNetworkRules;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageSampleTests extends SamplesTestBase {
    @Test
    public void testManageStorageAccount() {
        Assertions.assertTrue(ManageStorageAccount.runSample(azure));
    }

    @Test
    public void testManageStorageAccountAsync() {
        Assertions.assertTrue(ManageStorageAccountAsync.runSample(azure));
    }

    @Test
    public void testManageStorageAccountNetworkRules() {
        Assertions.assertTrue(ManageStorageAccountNetworkRules.runSample(azure));
    }
}
