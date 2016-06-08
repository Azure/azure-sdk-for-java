/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.ResourceGroup;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class UsageOperationsTests extends StorageManagementTestBase {
    private static final String RG_NAME = "javacsmrg7";
    private static final String SA_NAME = "javacsmsa2";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @Test
    public void canGetUsages() throws Exception {
        List<Usage> usages = storageManager.usages().list();
        System.out.println(usages.size());
    }
}
