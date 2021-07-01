// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.VaultsClient;
import com.azure.resourcemanager.keyvault.fluent.models.DeletedVaultInner;
import com.azure.resourcemanager.keyvault.fluent.models.VaultInner;
import com.azure.resourcemanager.keyvault.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.DeletedVault;
import com.azure.resourcemanager.keyvault.models.Sku;
import com.azure.resourcemanager.keyvault.models.SkuFamily;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.keyvault.models.VaultCreateOrUpdateParameters;
import com.azure.resourcemanager.keyvault.models.VaultProperties;
import com.azure.resourcemanager.keyvault.models.Vaults;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.UUID;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Vaults and its parent interfaces. */
public class VaultsImpl extends GroupableResourcesImpl<Vault, VaultImpl, VaultInner, VaultsClient, KeyVaultManager>
    implements Vaults {
    private final AuthorizationManager authorizationManager;
    private final String tenantId;

    public VaultsImpl(
        final KeyVaultManager keyVaultManager, final AuthorizationManager authorizationManager, final String tenantId) {
        super(keyVaultManager.serviceClient().getVaults(), keyVaultManager);
        this.authorizationManager = authorizationManager;
        this.tenantId = tenantId;
    }

    @Override
    public PagedIterable<Vault> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<Vault> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
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
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public VaultImpl define(String name) {
        return wrapModel(name).withSku(SkuName.STANDARD).withEmptyAccessPolicy();
    }

    @Override
    protected VaultImpl wrapModel(String name) {
        VaultInner inner = new VaultInner().withProperties(new VaultProperties());
        inner.properties().withTenantId(UUID.fromString(tenantId));
        return new VaultImpl(name, inner, this.manager(), authorizationManager);
    }

    @Override
    protected VaultImpl wrapModel(VaultInner vaultInner) {
        if (vaultInner == null) {
            return null;
        }
        return new VaultImpl(vaultInner.name(), vaultInner, this.manager(), authorizationManager);
    }

    @Override
    public PagedIterable<DeletedVault> listDeleted() {
        return PagedConverter.mapPage(this.inner().listDeleted(), DeletedVaultImpl::new);
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
        return this.inner().getDeletedAsync(vaultName, location).map(DeletedVaultImpl::new);
    }

    @Override
    public Mono<Void> purgeDeletedAsync(String vaultName, String location) {
        return this.inner().purgeDeletedAsync(vaultName, location);
    }

    @Override
    public PagedFlux<DeletedVault> listDeletedAsync() {
        return PagedConverter.mapPage(this.inner().listDeletedAsync(), DeletedVaultImpl::new);
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
    public Mono<Vault> recoverSoftDeletedVaultAsync(
        final String resourceGroupName, final String vaultName, String location) {
        final KeyVaultManager manager = this.manager();
        return getDeletedAsync(vaultName, location)
            .flatMap(
                deletedVault -> {
                    VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters();
                    parameters.withLocation(deletedVault.location());
                    parameters.withTags(deletedVault.innerModel().properties().tags());
                    parameters
                        .withProperties(
                            new VaultProperties()
                                .withCreateMode(CreateMode.RECOVER)
                                .withSku(new Sku().withName(SkuName.STANDARD).withFamily(SkuFamily.A))
                                .withTenantId(UUID.fromString(tenantId)));
                    return inner()
                        .createOrUpdateAsync(resourceGroupName, vaultName, parameters)
                        .map(inner -> (Vault) new VaultImpl(inner.id(), inner, manager, authorizationManager));
                });
    }
}
