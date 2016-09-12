/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * The implementation for {@link AvailabilitySets}.
 */
class AvailabilitySetsImpl
    extends GroupableResourcesImpl<
        AvailabilitySet,
        AvailabilitySetImpl,
        AvailabilitySetInner,
        AvailabilitySetsInner,
        ComputeManager>
    implements AvailabilitySets {

    AvailabilitySetsImpl(
            final AvailabilitySetsInner client,
            final ComputeManager computeManager) {
        super(client, computeManager);
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return new GroupPagedList<AvailabilitySet>(this.myManager.resourceManager().resourceGroups().list()) {
            @Override
            public List<AvailabilitySet> listNextGroup(String resourceGroupName) throws RestException, IOException {
                return wrapList(innerCollection.list(resourceGroupName));
            }
        };
    }

    @Override
    public PagedList<AvailabilitySet> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public AvailabilitySetImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        AvailabilitySetInner response = this.innerCollection.get(groupName, name);
        return wrapModel(response);
    }

    @Override
    public AvailabilitySetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected AvailabilitySetImpl wrapModel(String name) {
        return new AvailabilitySetImpl(name,
                new AvailabilitySetInner(),
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected AvailabilitySetImpl wrapModel(AvailabilitySetInner availabilitySetInner) {
        return new AvailabilitySetImpl(availabilitySetInner.name(),
                availabilitySetInner,
                this.innerCollection,
                this.myManager);
    }
}
