// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.keyvault.models.AccessPolicy;
import com.azure.resourcemanager.keyvault.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VaultTests extends KeyVaultManagementTest {
    @Test
    public void canCRUDVault() throws Exception {
        // Create user service principal
        String sp = sdkContext.randomResourceName("sp", 20);
        String us = sdkContext.randomResourceName("us", 20);
        ServicePrincipal servicePrincipal =
            authorizationManager.servicePrincipals().define(sp).withNewApplication("http://" + sp).create();

        ActiveDirectoryUser user =
            authorizationManager.users().define(us).withEmailAlias(us).withPassword("P@$$w0rd").create();

        try {
            // CREATE
            Vault vault =
                keyVaultManager
                    .vaults()
                    .define(vaultName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .defineAccessPolicy()
                    .forServicePrincipal("http://" + sp)
                    .allowKeyPermissions(KeyPermissions.LIST)
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(CertificatePermissions.GET)
                    .attach()
                    .defineAccessPolicy()
                    .forUser(us)
                    .allowKeyAllPermissions()
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(
                        CertificatePermissions.GET, CertificatePermissions.LIST, CertificatePermissions.CREATE)
                    .attach()
                    // .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)
                    .withAccessFromAzureServices()
                    .withAccessFromIpAddress("0.0.0.0/0")
                    .create();
            Assertions.assertNotNull(vault);
            Assertions.assertFalse(vault.softDeleteEnabled());
            Assertions.assertEquals(vault.networkRuleSet().bypass(), NetworkRuleBypassOptions.AZURE_SERVICES);

            // GET
            vault = keyVaultManager.vaults().getByResourceGroup(rgName, vaultName);
            Assertions.assertNotNull(vault);
            for (AccessPolicy policy : vault.accessPolicies()) {
                if (policy.objectId().equals(servicePrincipal.id())) {
                    Assertions
                        .assertArrayEquals(
                            new KeyPermissions[] {KeyPermissions.LIST}, policy.permissions().keys().toArray());
                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
                    Assertions
                        .assertArrayEquals(
                            new CertificatePermissions[] {CertificatePermissions.GET},
                            policy.permissions().certificates().toArray());
                }
                if (policy.objectId().equals(user.id())) {
                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
                    Assertions.assertEquals(3, policy.permissions().certificates().size());
                }
            }
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
            vault
                .update()
                .updateAccessPolicy(servicePrincipal.id())
                .allowKeyAllPermissions()
                .disallowSecretAllPermissions()
                .allowCertificateAllPermissions()
                .parent()
                .withTag("foo", "bar")
                .apply();
            for (AccessPolicy policy : vault.accessPolicies()) {
                if (policy.objectId().equals(servicePrincipal.id())) {
                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
                    Assertions.assertEquals(0, policy.permissions().secrets().size());
                    Assertions
                        .assertEquals(
                            CertificatePermissions.values().size(), policy.permissions().certificates().size());
                }
            }

            // DELETE
            keyVaultManager.vaults().deleteById(vault.id());
            SdkContext.sleep(20000);
            assertVaultDeleted(vaultName, Region.US_WEST.toString());
        } finally {
            authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
            //            graphRbacManager.users().deleteById(user.id());
        }
    }

    @Test
    public void canCRUDVaultAsync() throws Exception {
        // Create user service principal
        String sp = sdkContext.randomResourceName("sp", 20);
        String us = sdkContext.randomResourceName("us", 20);
        ServicePrincipal servicePrincipal =
            authorizationManager.servicePrincipals().define(sp).withNewApplication("http://" + sp).create();

        ActiveDirectoryUser user =
            authorizationManager.users().define(us).withEmailAlias(us).withPassword("P@$$w0rd").create();

        try {
            // CREATE
            Vault vault =
                keyVaultManager
                    .vaults()
                    .define(vaultName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .defineAccessPolicy()
                    .forServicePrincipal("http://" + sp)
                    .allowKeyPermissions(KeyPermissions.LIST)
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(CertificatePermissions.GET)
                    .attach()
                    .defineAccessPolicy()
                    .forUser(us)
                    .allowKeyAllPermissions()
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(
                        CertificatePermissions.GET, CertificatePermissions.LIST, CertificatePermissions.CREATE)
                    .attach()
                    .create();
            Assertions.assertNotNull(vault);
            Assertions.assertFalse(vault.softDeleteEnabled());
            // GET
            vault = keyVaultManager.vaults().getByResourceGroupAsync(rgName, vaultName).block();
            Assertions.assertNotNull(vault);
            for (AccessPolicy policy : vault.accessPolicies()) {
                if (policy.objectId().equals(servicePrincipal.id())) {
                    Assertions
                        .assertArrayEquals(
                            new KeyPermissions[] {KeyPermissions.LIST}, policy.permissions().keys().toArray());
                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
                    Assertions
                        .assertArrayEquals(
                            new CertificatePermissions[] {CertificatePermissions.GET},
                            policy.permissions().certificates().toArray());
                }
                if (policy.objectId().equals(user.id())) {
                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
                    Assertions.assertEquals(SecretPermissions.values().size(), policy.permissions().secrets().size());
                    Assertions.assertEquals(3, policy.permissions().certificates().size());
                }
            }
            // LIST
            PagedIterable<Vault> vaults =
                new PagedIterable<>(keyVaultManager.vaults().listByResourceGroupAsync(rgName));
            for (Vault v : vaults) {
                if (vaultName.equals(v.name())) {
                    vault = v;
                    break;
                }
            }
            Assertions.assertNotNull(vault);
            // UPDATE
            vault
                .update()
                .updateAccessPolicy(servicePrincipal.id())
                .allowKeyAllPermissions()
                .disallowSecretAllPermissions()
                .allowCertificateAllPermissions()
                .parent()
                .withTag("foo", "bar")
                .apply();
            for (AccessPolicy policy : vault.accessPolicies()) {
                if (policy.objectId().equals(servicePrincipal.id())) {
                    Assertions.assertEquals(KeyPermissions.values().size(), policy.permissions().keys().size());
                    Assertions.assertEquals(0, policy.permissions().secrets().size());
                    Assertions
                        .assertEquals(
                            CertificatePermissions.values().size(), policy.permissions().certificates().size());
                }
            }

            // DELETE
            keyVaultManager.vaults().deleteByIdAsync(vault.id()).block();
            SdkContext.sleep(20000);
            assertVaultDeleted(vaultName, Region.US_WEST.toString());
        } finally {
            authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
            //            graphRbacManager.users().deleteById(user.id());
        }
    }

    @Test
    public void canEnableSoftDeleteAndPurge() throws InterruptedException {
        String otherVaultName = vaultName + "other";
        String sp = sdkContext.randomResourceName("sp", 20);
        String us = sdkContext.randomResourceName("us", 20);

        ServicePrincipal servicePrincipal =
            authorizationManager.servicePrincipals().define(sp).withNewApplication("http://" + sp).create();

        ActiveDirectoryUser user =
            authorizationManager.users().define(us).withEmailAlias(us).withPassword("P@$$w0rd").create();

        try {
            Vault vault =
                keyVaultManager
                    .vaults()
                    .define(otherVaultName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .defineAccessPolicy()
                    .forServicePrincipal("http://" + sp)
                    .allowKeyPermissions(KeyPermissions.LIST)
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(CertificatePermissions.GET)
                    .attach()
                    .defineAccessPolicy()
                    .forUser(us)
                    .allowKeyAllPermissions()
                    .allowSecretAllPermissions()
                    .allowCertificatePermissions(
                        CertificatePermissions.GET, CertificatePermissions.LIST, CertificatePermissions.CREATE)
                    .attach()
                    .withSoftDeleteEnabled()
                    .create();
            Assertions.assertTrue(vault.softDeleteEnabled());

            keyVaultManager.vaults().deleteByResourceGroup(rgName, otherVaultName);

            SdkContext.sleep(20000);
            // Can still see deleted vault.
            Assertions.assertNotNull(keyVaultManager.vaults().getDeleted(otherVaultName, Region.US_WEST.toString()));

            keyVaultManager.vaults().purgeDeleted(otherVaultName, Region.US_WEST.toString());
            SdkContext.sleep(20000);
            // Vault is purged
            assertVaultDeleted(otherVaultName, Region.US_WEST.toString());
        } finally {
            authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
            // graphRbacManager.users().deleteById(user.id());
        }
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
