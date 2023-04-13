// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.ManagedHsmsClient;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.models.ManagedHsms;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation for ManagedHsms and its parent interfaces.
 */
public class ManagedHsmsImpl
    extends GroupableResourcesImpl<ManagedHsm, ManagedHsmImpl, ManagedHsmInner, ManagedHsmsClient, KeyVaultManager>
    implements ManagedHsms {
    private final String tenantId;

    public ManagedHsmsImpl(KeyVaultManager manager, String tenantId) {
        super(manager.serviceClient().getManagedHsms(), manager);
        this.tenantId = tenantId;
    }

    @Override
    protected ManagedHsmImpl wrapModel(String name) {
        ManagedHsmInner inner = new ManagedHsmInner().withProperties(new ManagedHsmProperties());
        inner.properties().withTenantId(UUID.fromString(tenantId));
        return new ManagedHsmImpl(name, inner, this.manager());
    }

    @Override
    protected Mono<ManagedHsmInner> getInnerAsync(String resourceGroupName, String name) {
        return inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    protected ManagedHsmImpl wrapModel(ManagedHsmInner inner) {
        if (inner == null) {
            return null;
        }
        return new ManagedHsmImpl(inner.name(), inner, this.manager());
    }

    @Override
    public PagedIterable<ManagedHsm> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<ManagedHsm> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroupName));
    }
}
