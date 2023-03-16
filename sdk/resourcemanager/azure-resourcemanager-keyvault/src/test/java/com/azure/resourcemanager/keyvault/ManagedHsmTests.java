// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuFamily;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuName;
import com.azure.resourcemanager.keyvault.models.MhsmNetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.ProvisioningState;
import com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ManagedHsmTests extends KeyVaultManagementTest {
    @Test
    public void canOperateManagedHsmAndKeys() {
        ManagedHsm managedHsm = getManagedHsm();

        try {

            KeyVaultAccessControlAsyncClient accessControlAsyncClient =
                new KeyVaultAccessControlClientBuilder()
                    .pipeline(keyVaultManager.httpPipeline())
                    .vaultUrl(managedHsm.hsmUri())
                    .buildAsyncClient();

            prepareManagedHsm(managedHsm, accessControlAsyncClient);

            // listByResourceGroups
            PagedIterable<ManagedHsm> hsms = keyVaultManager.managedHsms()
                .listByResourceGroup(rgName);
            // getByResourceGroup
            ManagedHsm hsm = keyVaultManager.managedHsms()
                .getByResourceGroup(rgName, managedHsm.name());

            // ManagedHsm properties
            // The Azure Active Directory tenant ID that should be used for authenticating requests to the managed HSM pool.
            String tenantId  = hsm.tenantId();
            ManagedHsmSku sku = hsm.sku();
            // Array of initial administrators object ids for this managed hsm pool.
            List<String> initialAdminObjectIds = hsm.initialAdminObjectIds();
            // The URI of the managed hsm pool for performing operations on keys.
            String hsmUri = hsm.hsmUri();
            // Property to specify whether the 'soft delete' functionality is enabled for this managed HSM pool.
            boolean softDelete = hsm.isSoftDeleteEnabled();
            // softDelete data retention days. It accepts >=7 and <=90.
            Integer softDeleteRetentionDays = hsm.softDeleteRetentionDays();
            // Property specifying whether protection against purge is enabled for this managed HSM pool.
            boolean purgeProtectionEnabled = hsm.isPurgeProtectionEnabled();
            // he create mode to indicate whether the resource is being created or is being recovered from a deleted resource.
            CreateMode createMode = hsm.createMode();
            // Rules governing the accessibility of the key vault from specific network locations.
            MhsmNetworkRuleSet ruleSet = hsm.networkRuleSet();
            // Provisioning state.
            ProvisioningState provisioningState = hsm.state();

            // key operations, same interface as the key vault
            Keys keys = hsm.keys();
            String keyName = generateRandomResourceName("key", 10);
            Key key = keys.define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(4096)
                .create();

            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.fromString(String.format("/keys/%s", keyName)), "21dbd100-6940-42c2-9190-5d6cb909625b", managedHsm.initialAdminObjectIds().get(0)).block();
            keys.deleteById(key.id());
        } finally {
            keyVaultManager.managedHsms().deleteById(managedHsm.id());
            keyVaultManager.serviceClient().getManagedHsms().purgeDeleted(managedHsm.name(), managedHsm.regionName());
        }
    }

    private void prepareManagedHsm(ManagedHsm managedHsm, KeyVaultAccessControlAsyncClient accessControlAsyncClient) {
        accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.KEYS, "21dbd100-6940-42c2-9190-5d6cb909625b", managedHsm.initialAdminObjectIds().get(0)).block();
    }

    private ManagedHsm getManagedHsm() {
        String objectId = authorizationManager
            .users()
            .getByNameAsync(clientIdFromFile())
            .block()
            .id();
        ManagedHsmInner inner =  keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(
                rgName,
                generateRandomResourceName("mhsm", 10),
                new ManagedHsmInner()
                    .withLocation("westus")
                    .withSku(
                        new ManagedHsmSku().withFamily(ManagedHsmSkuFamily.B).withName(ManagedHsmSkuName.STANDARD_B1))
                    .withProperties(
                        new ManagedHsmProperties()
                            .withTenantId(UUID.fromString(authorizationManager.tenantId()))
                            .withInitialAdminObjectIds(Arrays.asList(objectId))
                            .withEnableSoftDelete(true)
                            .withSoftDeleteRetentionInDays(90)
                            .withEnablePurgeProtection(false)),
                Context.NONE);

        return null;
    }
}
