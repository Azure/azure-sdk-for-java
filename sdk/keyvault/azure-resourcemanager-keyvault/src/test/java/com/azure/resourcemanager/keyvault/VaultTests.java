// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.PublicNetworkAccess;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class VaultTests extends KeyVaultManagementTest {
    @Test
    public void canCRUDVault() throws Exception {
        // Create user service principal
        //        String sp = generateRandomResourceName("sp", 20);
        //        String us = generateRandomResourceName("us", 20);
        // issue: https://github.com/Azure/azure-sdk-for-java/issues/47117
        //        ServicePrincipal servicePrincipal
        //            = authorizationManager.servicePrincipals().define(sp).withNewApplication().create();

        // Status code 403, "{"error":{"code":"Authorization_RequestDenied","message":"Insufficient privileges to complete the operation."
        //        ActiveDirectoryUser user
        //            = authorizationManager.users().define(us).withEmailAlias(us).withPassword(password()).create();

        // CREATE
        Vault vault = keyVaultManager.vaults()
            .define(vaultName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            //                .defineAccessPolicy()
            //                .forServicePrincipal(sp)
            //                .allowKeyPermissions(KeyPermissions.LIST)
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET)
            //                .attach()
            //                .defineAccessPolicy()
            //                .forUser(us)
            //                .allowKeyAllPermissions()
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST,
            //                    CertificatePermissions.CREATE)
            //                .attach()
            // .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)
            .withEmptyAccessPolicy()
            .withAccessFromAzureServices()
            .withAccessFromIpAddress("0.0.0.0/0")
            .create();
        Assertions.assertNotNull(vault);
        //Assertions.assertFalse(vault.softDeleteEnabled());
        Assertions.assertEquals(vault.networkRuleSet().bypass(), NetworkRuleBypassOptions.AZURE_SERVICES);

        // GET
        vault = keyVaultManager.vaults().getByResourceGroup(rgName, vaultName);
        Assertions.assertNotNull(vault);
        //            for (AccessPolicy policy : vault.accessPolicies()) {
        //                if (policy.objectId().equals(servicePrincipal.id())) {
        //                    Assertions.assertArrayEquals(new KeyPermissions[] { KeyPermissions.LIST },
        //                        policy.permissions().keys().toArray());
        //                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
        //                    Assertions.assertArrayEquals(new CertificatePermissions[] { CertificatePermissions.GET },
        //                        policy.permissions().certificates().toArray());
        //                }
        //                if (policy.objectId().equals(user.id())) {
        //                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
        //                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
        //                    Assertions.assertEquals(3, policy.permissions().certificates().size());
        //                }
        //            }
        // LIST
        PagedIterable<Vault> vaults = keyVaultManager.vaults().listByResourceGroup(rgName);
        for (Vault v : vaults) {
            if (vaultName.equals(v.name())) {
                vault = v;
                break;
            }
        }
        Assertions.assertNotNull(vault);
        // UPDATE
        vault.update()
            //                .updateAccessPolicy(user.id())
            //                .allowKeyAllPermissions()
            //                .disallowSecretAllPermissions()
            //                .allowCertificateAllPermissions()
            //                .parent()
            .withTag("foo", "bar")
            .apply();
        //            for (AccessPolicy policy : vault.accessPolicies()) {
        //                if (policy.objectId().equals(user.id())) {
        //                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
        //                    Assertions.assertEquals(0, policy.permissions().secrets().size());
        //                    Assertions.assertEquals(CertificatePermissions.values().size(),
        //                        policy.permissions().certificates().size());
        //                }
        //            }

        // DELETE
        keyVaultManager.vaults().deleteById(vault.id());
        //ResourceManagerUtils.sleep(Duration.ofSeconds(20));
        //assertVaultDeleted(vaultName, Region.US_WEST.toString());
    }

    @Test
    void canCRUDVaultWithRbac() {
        Vault vault = keyVaultManager.vaults()
            .define(vaultName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            .withRoleBasedAccessControl()
            .create();

        Assertions.assertTrue(vault.roleBasedAccessControlEnabled());

        vault.update().withoutRoleBasedAccessControl().apply();

        Assertions.assertFalse(vault.roleBasedAccessControlEnabled());
    }

    @Test
    public void canCRUDVaultAsync() throws Exception {
        // Create user service principal
        //        String sp = generateRandomResourceName("sp", 20);
        //        String us = generateRandomResourceName("us", 20);
        // issue: https://github.com/Azure/azure-sdk-for-java/issues/47117
        //        ServicePrincipal servicePrincipal
        //            = authorizationManager.servicePrincipals().define(sp).withNewApplication().create();

        // Status code 403, "{"error":{"code":"Authorization_RequestDenied","message":"Insufficient privileges to complete the operation."
        //        ActiveDirectoryUser user
        //            = authorizationManager.users().define(us).withEmailAlias(us).withPassword(password()).create();

        // CREATE
        Vault vault = keyVaultManager.vaults()
            .define(vaultName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            //                .defineAccessPolicy()
            //                .forServicePrincipal(sp)
            //                .allowKeyPermissions(KeyPermissions.LIST)
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET)
            //                .attach()
            //                .defineAccessPolicy()
            //                .forUser(us)
            //                .allowKeyAllPermissions()
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST,
            //                    CertificatePermissions.CREATE)
            //                .attach()
            .withEmptyAccessPolicy()
            .create();
        Assertions.assertNotNull(vault);
        //Assertions.assertFalse(vault.softDeleteEnabled());
        // GET
        vault = keyVaultManager.vaults().getByResourceGroupAsync(rgName, vaultName).block();
        Assertions.assertNotNull(vault);
        //            for (AccessPolicy policy : vault.accessPolicies()) {
        //                if (policy.objectId().equals(servicePrincipal.id())) {
        //                    Assertions.assertArrayEquals(new KeyPermissions[] { KeyPermissions.LIST },
        //                        policy.permissions().keys().toArray());
        //                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
        //                    Assertions.assertArrayEquals(new CertificatePermissions[] { CertificatePermissions.GET },
        //                        policy.permissions().certificates().toArray());
        //                }
        //                if (policy.objectId().equals(user.id())) {
        //                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
        //                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
        //                    Assertions.assertEquals(3, policy.permissions().certificates().size());
        //                }
        //            }
        // LIST
        PagedIterable<Vault> vaults = new PagedIterable<>(keyVaultManager.vaults().listByResourceGroupAsync(rgName));
        for (Vault v : vaults) {
            if (vaultName.equals(v.name())) {
                vault = v;
                break;
            }
        }
        Assertions.assertNotNull(vault);
        // UPDATE
        vault.update()
            //                .updateAccessPolicy(user.id())
            //                .allowKeyAllPermissions()
            //                .disallowSecretAllPermissions()
            //                .allowCertificateAllPermissions()
            //                .parent()
            .withTag("foo", "bar")
            .apply();
        //        for (AccessPolicy policy : vault.accessPolicies()) {
        //                if (policy.objectId().equals(user.id())) {
        //                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
        //                    Assertions.assertEquals(0, policy.permissions().secrets().size());
        //                    Assertions.assertEquals(CertificatePermissions.values().size(),
        //                        policy.permissions().certificates().size());
        //                }
        //        }

        // DELETE
        keyVaultManager.vaults().deleteByIdAsync(vault.id()).block();
        //ResourceManagerUtils.sleep(Duration.ofSeconds(20));
        //assertVaultDeleted(vaultName, Region.US_WEST.toString());
    }

    @Test
    public void canEnableSoftDeleteAndPurge() throws InterruptedException {
        String otherVaultName = vaultName + "other";
        //        String sp = generateRandomResourceName("sp", 20);
        //        String us = generateRandomResourceName("us", 20);

        // issue: https://github.com/Azure/azure-sdk-for-java/issues/47117
        //        ServicePrincipal servicePrincipal
        //            = authorizationManager.servicePrincipals().define(sp).withNewApplication().create();

        //        ActiveDirectoryUser user
        //            = authorizationManager.users().define(us).withEmailAlias(us).withPassword(password()).create();

        Vault vault = keyVaultManager.vaults()
            .define(otherVaultName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            //                .defineAccessPolicy()
            //                .forServicePrincipal(sp)
            //                .allowKeyPermissions(KeyPermissions.LIST)
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET)
            //                .attach()
            //                .defineAccessPolicy()
            //                .forUser(us)
            //                .allowKeyAllPermissions()
            //                .allowSecretAllPermissions()
            //                .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST,
            //                    CertificatePermissions.CREATE)
            //                .attach()
            .withEmptyAccessPolicy()
            .create();
        Assertions.assertTrue(vault.softDeleteEnabled());

        keyVaultManager.vaults().deleteByResourceGroup(rgName, otherVaultName);

        ResourceManagerUtils.sleep(Duration.ofSeconds(20));
        // Can still see deleted vault.
        Assertions.assertNotNull(keyVaultManager.vaults().getDeleted(otherVaultName, Region.US_WEST.toString()));

        keyVaultManager.vaults().purgeDeleted(otherVaultName, Region.US_WEST.toString());
        ResourceManagerUtils.sleep(Duration.ofSeconds(20));
        // Vault is purged
        assertVaultDeleted(otherVaultName, Region.US_WEST.toString());
    }

    @Test
    public void canDisablePublicNetworkAccess() {
        Vault vault = keyVaultManager.vaults()
            .define(vaultName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            .withEmptyAccessPolicy()
            .disablePublicNetworkAccess()
            .create();

        Assertions.assertEquals(PublicNetworkAccess.DISABLED, vault.publicNetworkAccess());
        Assertions.assertEquals(PublicNetworkAccess.DISABLED,
            keyVaultManager.vaults().getById(vault.id()).publicNetworkAccess());

        vault.update().enablePublicNetworkAccess().apply();

        Assertions.assertEquals(PublicNetworkAccess.ENABLED, vault.publicNetworkAccess());
        Assertions.assertEquals(PublicNetworkAccess.ENABLED,
            keyVaultManager.vaults().getById(vault.id()).publicNetworkAccess());
    }

    private void assertVaultDeleted(String name, String location) {
        boolean deleted = false;
        try {
            keyVaultManager.vaults().getDeleted(name, location);
        } catch (ManagementException exception) {
            if (exception.getResponse().getStatusCode() == 404) {
                deleted = true;
            }
        }
        Assertions.assertTrue(deleted);
    }
}
