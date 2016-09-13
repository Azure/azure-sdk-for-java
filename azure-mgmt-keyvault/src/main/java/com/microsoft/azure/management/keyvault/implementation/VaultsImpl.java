/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.VaultProperties;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.UUID;

/**
 * The implementation of Vaults and its parent interfaces.
 */
class VaultsImpl
        extends GroupableResourcesImpl<
            Vault,
            VaultImpl,
            VaultInner,
            VaultsInner,
            KeyVaultManager>
        implements Vaults {
    private final GraphRbacManager graphRbacManager;
    private final String tenantId;

    VaultsImpl(
            final VaultsInner client,
            final KeyVaultManager keyVaultManager,
            final GraphRbacManager graphRbacManager,
            final String tenantId) {
        super(client, keyVaultManager);
        this.graphRbacManager = graphRbacManager;
        this.tenantId = tenantId;
    }

    @Override
    public PagedList<Vault> list() throws RestException, IOException {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<Vault> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public Vault getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public VaultImpl define(String name) {
        return wrapModel(name)
                .withSku(SkuName.STANDARD)
                .withEmptyAccessPolicy();
    }

    @Override
    protected VaultImpl wrapModel(String name) {
        VaultInner inner = new VaultInner().withProperties(new VaultProperties());
        inner.properties().withTenantId(UUID.fromString(tenantId));
        return new VaultImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager,
                graphRbacManager);
    }

    @Override
    protected VaultImpl wrapModel(VaultInner vaultInner) {
        return new VaultImpl(
                vaultInner.name(),
                vaultInner,
                this.innerCollection,
                super.myManager,
                graphRbacManager);
    }
}
