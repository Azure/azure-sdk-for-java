/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.VaultProperties;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

import java.util.UUID;

/**
 * The implementation of Vaults and its parent interfaces.
 */
@LangDefinition
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
    public PagedList<Vault> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<Vault> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public Vault getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
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
        if (vaultInner == null) {
            return null;
        }
        return new VaultImpl(
                vaultInner.name(),
                vaultInner,
                this.innerCollection,
                super.myManager,
                graphRbacManager);
    }
}
