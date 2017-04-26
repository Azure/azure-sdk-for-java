/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.storage.samples.ManageStorageAccount;
import com.microsoft.azure.management.storage.samples.ManageStorageAccountAsync;
import org.junit.Assert;
import org.junit.Test;

public class StorageSampleTests extends SamplesTestBase {
    @Test
    public void testManageStorageAccount() {
        Assert.assertTrue(ManageStorageAccount.runSample(azure));
    }

    @Test
    public void testManageStorageAccountAsync() {
        Assert.assertTrue(ManageStorageAccountAsync.runSample(azure));
    }
}
