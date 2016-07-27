/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class VaultTests extends KeyVaultManagementTestBase {
    private static final String RG_NAME = "javacsmrg901";
    private static final String VAULT_NAME = "java-keyvault-901";
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
    public void canCRUDVault() throws Exception {
        // CREATE
        Vault vault = keyVaultManager.vaults().define(VAULT_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withTenantId(UUID.fromString("72f988bf-86f1-41af-91ab-2d7cd011db47"))
                .create();
        Assert.assertNotNull(vault);
        // GET
    }
}
