/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.Sku;
import com.microsoft.azure.management.keyvault.SkuFamily;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.VaultProperties;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class VaultImpl
        extends GroupableResourceImpl<
            Vault,
            VaultInner,
            VaultImpl,
            KeyVaultManager>
        implements
        Vault,
        Vault.Definition,
        Vault.Update {
    private VaultsInner client;

    protected VaultImpl(String key, VaultInner innerObject, VaultsInner client, KeyVaultManager manager) {
        super(key, innerObject, manager);
        this.client = client;
    }

    @Override
    public String vaultUri() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().vaultUri();
    }

    @Override
    public UUID tenantId() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().tenantId();
    }

    @Override
    public Sku sku() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().sku();
    }

    @Override
    public List<AccessPolicyEntry> accessPolicies() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().accessPolicies();
    }

    @Override
    public Boolean enabledForDeployment() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().enabledForDeployment();
    }

    @Override
    public Boolean enabledForDiskEncryption() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().enabledForDiskEncryption();
    }

    @Override
    public Boolean enabledForTemplateDeployment() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().enabledForTemplateDeployment();
    }

    @Override
    public VaultImpl withEmptyAccessPolicy() {
        if (inner().properties() == null) {
            inner().withProperties(new VaultProperties());
        }
        inner().properties().withAccessPolicies(new ArrayList<AccessPolicyEntry>());
        return this;
    }

    @Override
    public VaultImpl withAccessPolicy(AccessPolicy accessPolicy) {
        if (inner().properties() == null) {
            inner().withProperties(new VaultProperties());
        }
        if (inner().properties().accessPolicies() == null) {
            inner().properties().withAccessPolicies(new ArrayList<AccessPolicyEntry>());
        }
        inner().properties().accessPolicies().add(accessPolicy.inner());
        return this;
    }

    @Override
    public AccessPolicyImpl defineAccessPolicy(String objectId) {
        return new AccessPolicyImpl(objectId, new AccessPolicyEntry(), this);
    }

    @Override
    public VaultImpl enabledForDeployment(boolean enabled) {
        inner().properties().withEnabledForDeployment(enabled);
        return this;
    }

    @Override
    public VaultImpl enabledForDiskEncryption(boolean enabled) {
        inner().properties().withEnabledForDiskEncryption(enabled);
        return this;
    }

    @Override
    public VaultImpl enabledForTemplateDeployment(boolean enabled) {
        inner().properties().withEnabledForTemplateDeployment(enabled);
        return this;
    }

    @Override
    public VaultImpl withSku(SkuName skuName) {
        inner().properties().withSku(new Sku().withName(skuName).withFamily(SkuFamily.A));
        return this;
    }

    @Override
    public VaultImpl apply() throws Exception {
        return createResource();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<Vault> callback) {
        return null;
    }

    @Override
    public ServiceCall createResourceAsync(ServiceCallback<Resource> serviceCallback) {
        return null;
    }

    @Override
    public VaultImpl createResource() throws Exception {
        VaultCreateOrUpdateParametersInner parameters = new VaultCreateOrUpdateParametersInner();
        parameters.withLocation(regionName());
        parameters.withProperties(inner().properties());
        parameters.withTags(inner().getTags());
        this.setInner(client.createOrUpdate(resourceGroupName(), name(), parameters).getBody());
        return this;
    }

    @Override
    public VaultImpl refresh() throws Exception {
        return null;
    }
}
