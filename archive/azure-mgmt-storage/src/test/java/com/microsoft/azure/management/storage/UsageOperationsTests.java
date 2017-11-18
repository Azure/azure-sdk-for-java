/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import org.junit.Test;

import java.util.List;

public class UsageOperationsTests extends StorageManagementTest {
    @Test
    public void canGetUsages() throws Exception {
        List<StorageUsage> usages = storageManager.usages().list();
        System.out.println(usages.size());
    }


    @Override
    protected void cleanUpResources() {
    }
}
