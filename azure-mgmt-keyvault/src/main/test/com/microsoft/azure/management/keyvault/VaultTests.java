/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class VaultTests extends KeyVaultManagementTestBase {
    private static final String RG_NAME = "javacsmrg901";
    private static final String VAULT_NAME = "java-keyvault-901";

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
        ServicePrincipal sp = graphRbacManager.servicePrincipals().getByServicePrincipalName("app-123");
        User user = graphRbacManager.users().getByUserPrincipalName("azuresdk@outlook.com");
        Vault vault = keyVaultManager.vaults().define(VAULT_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .defineAccessPolicy()
                    .forServicePrincipal(sp)
                    .allowKeyGetting()
                    .allowKeyListing()
                    .allowKeyCreating()
                    .allowSecretAllPermissions()
                    .attach()
                .create();
        Assert.assertNotNull(vault);
        // GET
        vault = keyVaultManager.vaults().getByGroup(RG_NAME, VAULT_NAME);
        Assert.assertNotNull(vault);
        // LIST
        List<Vault> vaults = keyVaultManager.vaults().listByGroup(RG_NAME);
        for (Vault v : vaults) {
            if (VAULT_NAME.equals(v.name())) {
                vault = v;
                break;
            }
        }
        Assert.assertNotNull(vault);
        // UPDATE
        vault.update()
                .updateAccessPolicy(vault.accessPolicies().get(0).objectId().toString())
                    .allowKeyAllPermissions()
                    .disallowSecretAllPermissions()
                    .parent()
                .apply();
        vault.update()
                .defineAccessPolicy()
                    .forUser(user)
                    .allowKeyAllPermissions()
                    .allowSecretGetting()
                    .allowSecretListing()
                    .attach()
                .apply();
        vault.update()
                .withoutAccessPolicy(sp.objectId())
                .apply();
        // AUTHORIZE
        vault.defineAppAuthorization("app-id")
                .allowKeyListing()
                .allowKeyGetting()
                .allowSecretListing()
                .allowSecretGetting()
                .authorize();
    }
}
