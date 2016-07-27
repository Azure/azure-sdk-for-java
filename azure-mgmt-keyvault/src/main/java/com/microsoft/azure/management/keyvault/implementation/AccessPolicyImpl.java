/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class AccessPolicyImpl
        extends ChildResourceImpl<
                AccessPolicyEntry,
                VaultImpl>
        implements
            AccessPolicy,
            AccessPolicy.Definition<Vault.DefinitionStages.WithCreate> {
    protected AccessPolicyImpl(String name, AccessPolicyEntry innerObject, VaultImpl parent) {
        super(name, innerObject, parent);
    }

    @Override
    public UUID tenantId() {
        return inner().tenantId();
    }

    @Override
    public UUID objectId() {
        return inner().objectId();
    }

    @Override
    public UUID applicationId() {
        return inner().applicationId();
    }

    @Override
    public Permissions permissions() {
        return inner().permissions();
    }

    @Override
    public String name() {
        return inner().objectId().toString();
    }

    @Override
    public AccessPolicyImpl withApplicationId(UUID applicationId) {
        inner().withApplicationId(applicationId);
        return this;
    }

    @Override
    public AccessPolicyImpl withPermissionToKey(String key) {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().keys() == null) {
            inner().permissions().withKeys(new ArrayList<String>());
        }
        inner().permissions().keys().add(key);
        return this;
    }

    @Override
    public AccessPolicyImpl withPermissionToKeys(List<String> keys) {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().keys() == null) {
            inner().permissions().withKeys(new ArrayList<String>());
        }
        inner().permissions().keys().addAll(keys);
        return this;
    }

    @Override
    public AccessPolicyImpl withPermissionToSecret(String secret) {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().secrets() == null) {
            inner().permissions().withSecrets(new ArrayList<String>());
        }
        inner().permissions().secrets().add(secret);
        return this;
    }

    @Override
    public AccessPolicyImpl withPermissionToSecrets(List<String> secrets) {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().secrets() == null) {
            inner().permissions().withSecrets(new ArrayList<String>());
        }
        inner().permissions().secrets().addAll(secrets);
        return this;
    }

    @Override
    public AccessPolicyImpl withTenantId(UUID tenantId) {
        inner().withTenantId(tenantId);
        return this;
    }

    @Override
    public VaultImpl attach() {
        parent().withAccessPolicy(this);
        return parent();
    }
}
