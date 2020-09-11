// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.AvailabilitySets;
import com.azure.resourcemanager.compute.fluent.inner.AvailabilitySetInner;
import com.azure.resourcemanager.compute.fluent.AvailabilitySetsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/** The implementation for AvailabilitySets. */
public class AvailabilitySetsImpl
    extends GroupableResourcesImpl<
        AvailabilitySet, AvailabilitySetImpl, AvailabilitySetInner, AvailabilitySetsClient, ComputeManager>
    implements AvailabilitySets {

    public AvailabilitySetsImpl(final ComputeManager computeManager) {
        super(computeManager.inner().getAvailabilitySets(), computeManager);
    }

    @Override
    public PagedIterable<AvailabilitySet> list() {
        return manager().inner().getAvailabilitySets().list().mapPage(this::wrapModel);
    }

    @Override
    public PagedFlux<AvailabilitySet> listAsync() {
        return this.manager().inner().getAvailabilitySets().listAsync().mapPage(this::wrapModel);
    }

    @Override
    public PagedIterable<AvailabilitySet> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    public PagedFlux<AvailabilitySet> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    protected Mono<AvailabilitySetInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public AvailabilitySetImpl define(String name) {
        return wrapModel(name).withSku(AvailabilitySetSkuTypes.ALIGNED);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected AvailabilitySetImpl wrapModel(String name) {
        return new AvailabilitySetImpl(name, new AvailabilitySetInner(), this.manager());
    }

    @Override
    protected AvailabilitySetImpl wrapModel(AvailabilitySetInner availabilitySetInner) {
        if (availabilitySetInner == null) {
            return null;
        }
        return new AvailabilitySetImpl(availabilitySetInner.name(), availabilitySetInner, this.manager());
    }
}
