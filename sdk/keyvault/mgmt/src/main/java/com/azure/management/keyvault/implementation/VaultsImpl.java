/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.azure.management.keyvault.implementation;

import java.util.UUID;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.CheckNameAvailabilityResult;
import com.azure.management.keyvault.CreateMode;
import com.azure.management.keyvault.DeletedVault;
import com.azure.management.keyvault.Sku;
import com.azure.management.keyvault.SkuName;
import com.azure.management.keyvault.Vault;
import com.azure.management.keyvault.VaultCreateOrUpdateParameters;
import com.azure.management.keyvault.VaultProperties;
import com.azure.management.keyvault.Vaults;
import com.azure.management.keyvault.models.DeletedVaultInner;
import com.azure.management.keyvault.models.VaultInner;
import com.azure.management.keyvault.models.VaultsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation of Vaults and its parent interfaces.
 */
class VaultsImpl extends GroupableResourcesImpl<Vault, VaultImpl, VaultInner, VaultsInner, KeyVaultManager>
        implements Vaults {
    private final GraphRbacManager graphRbacManager;
    private final String tenantId;
    

    VaultsImpl(final KeyVaultManager keyVaultManager, final GraphRbacManager graphRbacManager, final String tenantId) {
        super(keyVaultManager.inner().vaults(), keyVaultManager);
        this.graphRbacManager = graphRbacManager;
        this.tenantId = tenantId;
    }

    @Override
    public PagedIterable<Vault> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<Vault> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName, null));
    }

    @Override
    protected Mono<VaultInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    @Override
    public VaultImpl define(String name) {
        return wrapModel(name).withSku(SkuName.STANDARD).withEmptyAccessPolicy();
    }

    @Override
    protected VaultImpl wrapModel(String name) {
        VaultInner inner = new VaultInner().setProperties(new VaultProperties());
        inner.getProperties().setTenantId(UUID.fromString(tenantId));
        return new VaultImpl(name, inner, this.manager(), graphRbacManager);
    }

    @Override
    protected VaultImpl wrapModel(VaultInner vaultInner) {
        if (vaultInner == null) {
            return null;
        }
        return new VaultImpl(vaultInner.getName(), vaultInner, this.manager(), graphRbacManager);
    }

    @Override
    public PagedIterable<DeletedVault> listDeleted() {
        return this.inner().listDeleted().mapPage(DeletedVaultImpl::new);
    }

    @Override
    public DeletedVault getDeleted(String vaultName, String location) {
        DeletedVaultInner deletedVault = inner().getDeleted(vaultName, location);
        if (deletedVault == null) {
            return null;
        }
        return new DeletedVaultImpl(deletedVault);
    }

    @Override
    public void purgeDeleted(String vaultName, String location) {
        inner().purgeDeleted(vaultName, location);
    }

    @Override
    public Mono<DeletedVault> getDeletedAsync(String vaultName, String location) {
        VaultsInner client = this.inner();
        return client.getDeletedAsync(vaultName, location).map(DeletedVaultImpl::new);
    }

    @Override
    public Mono<Void> purgeDeletedAsync(String vaultName, String location) {
        return this.inner().purgeDeletedAsync(vaultName, location);
    }

    @Override
    public PagedFlux<DeletedVault> listDeletedAsync() {
        VaultsInner client = this.inner();
        return client.listDeletedAsync().mapPage(DeletedVaultImpl::new);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return new CheckNameAvailabilityResultImpl(inner().checkNameAvailability(name));
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return inner().checkNameAvailabilityAsync(name).map(CheckNameAvailabilityResultImpl::new);
    }

    @Override
    public Vault recoverSoftDeletedVault(String resourceGroupName, String vaultName, String location) {
        return recoverSoftDeletedVaultAsync(resourceGroupName, vaultName, location).block();
    }
    
    @Override
    public Mono<Vault> recoverSoftDeletedVaultAsync(final String resourceGroupName, final String vaultName, String location) {
        final KeyVaultManager manager = this.manager();
        return getDeletedAsync(vaultName, location).flatMap(deletedVault -> {
            VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters();
            parameters.setLocation(deletedVault.location());
            parameters.setTags(deletedVault.inner().getProperties().getTags());
            parameters.setProperties(new VaultProperties()
                    .setCreateMode(CreateMode.RECOVER)
                    .setSku(new Sku().setName(SkuName.STANDARD))
                    .setTenantId(UUID.fromString(tenantId))
            );
            return inner().createOrUpdateAsync(resourceGroupName, vaultName, parameters).map(inner -> (Vault) new VaultImpl(inner.getId(), inner, manager, graphRbacManager));
        });
    }
}
