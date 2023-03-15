// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.ProvisioningState;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ManagedHsmTests extends KeyVaultManagementTest {
    @Test
    public void canOperateManagedHsmAndKeys() {
        String resourceGroupName = "rg-name";
        String hsmName = "my-hsm";

        // listByResourceGroups
        PagedIterable<ManagedHsm> hsms = keyVaultManager.managedHsms()
            .listByResourceGroup(resourceGroupName);
        // getByResourceGroup
        ManagedHsm hsm = keyVaultManager.managedHsms()
            .getByResourceGroup(resourceGroupName, hsmName);

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
        NetworkRuleSet ruleSet = hsm.networkRuleSet();
        // Provisioning state.
        ProvisioningState provisioningState = hsm.state();

        // give local RBAC role for "/" permission to the service principal
        // this is data-plane operation, which involves a new client library azure-security-keyvault-administration
        // should we provide this convenience layer for data-plane?
        hsm.localRbacRoles()
            .define("global-role-for-admin")
            .forObjectId(initialAdminObjectIds.get(0))
            .withGlobalScope()
            .create();

        // key operations, same interface as the key vault
        Keys keys = hsm.keys();
        String keyName = generateRandomResourceName("key", 10);
        Key key = keys.define(keyName)
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeySize(4096)
            .create();

        // give local RBAC role for "/key/<key-name>" permission to the service principal
        // should we provide this convenience layer for data-plane?
        hsm.localRbacRoles()
            .define("global-role-for-admin")
            .forObjectId(initialAdminObjectIds.get(0))
            .withKeyScope(keyName)
            .create();

        keys.deleteById(key.id());
    }
}
