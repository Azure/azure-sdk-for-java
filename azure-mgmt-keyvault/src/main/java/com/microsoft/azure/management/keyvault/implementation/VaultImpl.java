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
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
    public List<AccessPolicy> accessPolicies() {
        if (inner().properties() == null) {
            return null;
        }
        if (inner().properties().accessPolicies() == null) {
            return null;
        }
        List<AccessPolicy> accessPolicies = new ArrayList<>();
        for (AccessPolicyEntry entry : inner().properties().accessPolicies()) {
            accessPolicies.add(new AccessPolicyImpl(entry, this));
        }
        return accessPolicies;
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
    public Update withoutAccessPolicy(String objectId) {
        return null;
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
    public AccessPolicyImpl defineAccessPolicy() {
        return new AccessPolicyImpl(new AccessPolicyEntry(), this);
    }

    @Override
    public AccessPolicyImpl updateAccessPolicy(String objectId) {
        if (inner().properties() == null) {
            return null;
        }
        if (inner().properties().accessPolicies() == null) {
            return null;
        }
        for (AccessPolicyEntry entry : inner().properties().accessPolicies()) {
            if (entry.objectId().toString().equals(objectId)) {
                return new AccessPolicyImpl(entry, this);
            }
        }
        throw new NoSuchElementException(String.format("Identity %s not found in the access policies.", objectId));
    }

    @Override
    public VaultImpl enableDeployment() {
        inner().properties().withEnabledForDeployment(true);
        return this;
    }

    @Override
    public VaultImpl enableDiskEncryption() {
        inner().properties().withEnabledForDiskEncryption(true);
        return this;
    }

    @Override
    public VaultImpl enableTemplateDeployment() {
        inner().properties().withEnabledForTemplateDeployment(true);
        return this;
    }

    @Override
    public VaultImpl disableDeployment() {
        inner().properties().withEnabledForDeployment(false);
        return this;
    }

    @Override
    public VaultImpl disableDiskEncryption() {
        inner().properties().withEnabledForDiskEncryption(false);
        return this;
    }

    @Override
    public VaultImpl disableTemplateDeployment() {
        inner().properties().withEnabledForTemplateDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withSku(SkuName skuName) {
        if (inner().properties() == null) {
            inner().withProperties(new VaultProperties());
        }
        inner().properties().withSku(new Sku().withName(skuName).withFamily(SkuFamily.A));
        return this;
    }

    @Override
    public VaultImpl apply() throws Exception {
        return create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<Vault> callback) {
        return createAsync(callback);
    }

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> serviceCallback) {
        VaultCreateOrUpdateParametersInner parameters = new VaultCreateOrUpdateParametersInner();
        parameters.withLocation(regionName());
        parameters.withProperties(inner().properties());
        parameters.withTags(inner().getTags());
        final VaultImpl self = this;
        return client.createOrUpdateAsync(resourceGroupName(), name(), parameters, new ServiceCallback<VaultInner>() {
            @Override
            public void failure(Throwable t) {
                serviceCallback.failure(t);
            }

            @Override
            public void success(ServiceResponse<VaultInner> result) {
                setInner(result.getBody());
                serviceCallback.success(new ServiceResponse<Resource>(self, result.getResponse()));
            }
        });
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
        setInner(client.get(resourceGroupName(), name()).getBody());
        return this;
    }
}
