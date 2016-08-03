/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class VaultsImpl
        extends GroupableResourcesImpl<
            Vault,
            VaultImpl,
            VaultInner,
            VaultsInner,
            KeyVaultManager>
        implements Vaults {

    VaultsImpl(
            final VaultsInner client,
            final KeyVaultManager keyVaultManager) {
        super(client, keyVaultManager);
    }

    @Override
    public PagedList<Vault> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.list().getBody());
    }

    @Override
    public PagedList<Vault> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.listByResourceGroup(groupName).getBody());
    }

    @Override
    public Vault getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name).getBody());
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
        return new VaultImpl(
                name,
                new VaultInner(),
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected VaultImpl wrapModel(VaultInner storageAccountInner) {
        return new VaultImpl(
                storageAccountInner.name(),
                storageAccountInner,
                this.innerCollection,
                super.myManager);
    }
}
