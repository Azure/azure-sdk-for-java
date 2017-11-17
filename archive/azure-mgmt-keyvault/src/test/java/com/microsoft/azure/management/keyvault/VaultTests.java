/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class VaultTests extends KeyVaultManagementTest {
    @Test
    @Ignore("Need specific setting while recording - owner - Jianghao")
    public void canCRUDVault() throws Exception {
        // CREATE
        Vault vault = keyVaultManager.vaults().define(VAULT_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .defineAccessPolicy()
                    .forServicePrincipal("http://nativeapp")
                    .allowKeyPermissions(KeyPermissions.LIST)
                    .allowSecretAllPermissions()
                    .attach()
                .defineAccessPolicy()
                    .forUser("admin2@azuresdkteam.onmicrosoft.com")
                    .allowKeyAllPermissions()
                    .allowSecretAllPermissions()
                    .attach()
                .create();
        Assert.assertNotNull(vault);
        // GET
        vault = keyVaultManager.vaults().getByResourceGroup(RG_NAME, VAULT_NAME);
        Assert.assertNotNull(vault);
        for (AccessPolicy policy : vault.accessPolicies()) {
            if (policy.objectId().equals("8188d1e8-3090-4e3c-aa76-38cf2b5c7b3a")) {
                Assert.assertArrayEquals(new KeyPermissions[] { KeyPermissions.LIST }, policy.permissions().keys().toArray());
                Assert.assertArrayEquals(new SecretPermissions[] { SecretPermissions.ALL }, policy.permissions().secrets().toArray());
            }
            if (policy.objectId().equals("5963f50c-7c43-405c-af7e-53294de76abd")) {
                Assert.assertArrayEquals(new KeyPermissions[] { KeyPermissions.ALL }, policy.permissions().keys().toArray());
                Assert.assertArrayEquals(new SecretPermissions[] { SecretPermissions.ALL }, policy.permissions().secrets().toArray());
            }
        }
        // LIST
        List<Vault> vaults = keyVaultManager.vaults().listByResourceGroup(RG_NAME);
        for (Vault v : vaults) {
            if (VAULT_NAME.equals(v.name())) {
                vault = v;
                break;
            }
        }
        Assert.assertNotNull(vault);
        // UPDATE
        vault.update()
                .updateAccessPolicy(vault.accessPolicies().get(0).objectId())
                    .allowKeyAllPermissions()
                    .disallowSecretAllPermissions()
                    .parent()
                .withTag("foo", "bar")
                .apply();
        for (AccessPolicy policy : vault.accessPolicies()) {
            if (policy.objectId().equals("8188d1e8-3090-4e3c-aa76-38cf2b5c7b3a")) {
                Assert.assertArrayEquals(new KeyPermissions[] { KeyPermissions.LIST, KeyPermissions.ALL }, policy.permissions().keys().toArray());
                Assert.assertArrayEquals(new SecretPermissions[] { }, policy.permissions().secrets().toArray());
            }
        }
        vault.update()
                .defineAccessPolicy()
                    .forServicePrincipal("https://graphapp")
                    .allowKeyAllPermissions()
                    .attach()
                .apply();
    }
}
