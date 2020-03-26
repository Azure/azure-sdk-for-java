/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.AvailabilitySetInner;
import com.azure.management.compute.models.AvailabilitySetsInner;
import com.azure.management.compute.AvailabilitySet;
import com.azure.management.compute.AvailabilitySetSkuTypes;
import com.azure.management.compute.AvailabilitySets;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for AvailabilitySets.
 */
class AvailabilitySetsImpl
    extends GroupableResourcesImpl<
        AvailabilitySet,
        AvailabilitySetImpl,
        AvailabilitySetInner,
        AvailabilitySetsInner,
        ComputeManager>
    implements AvailabilitySets {

    AvailabilitySetsImpl(final ComputeManager computeManager) {
        super(computeManager.inner().availabilitySets(), computeManager);
    }

    @Override
    public PagedIterable<AvailabilitySet> list() {
        //TODO validate in tests
        return manager().inner().availabilitySets().list()
                .mapPage(this::wrapModel);
    }

    @Override
    public PagedFlux<AvailabilitySet> listAsync() {
        //TODO validate in tests
        return this.manager().inner().availabilitySets().listAsync()
                .mapPage(this::wrapModel);
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
        return new AvailabilitySetImpl(name,
                new AvailabilitySetInner(),
                this.manager());
    }

    @Override
    protected AvailabilitySetImpl wrapModel(AvailabilitySetInner availabilitySetInner) {
        if (availabilitySetInner == null) {
            return null;
        }
        return new AvailabilitySetImpl(availabilitySetInner.getName(),
                availabilitySetInner,
                this.manager());
    }
}
