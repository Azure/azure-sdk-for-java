/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import rx.Observable;

import java.util.List;

/**
 * The implementation for {@link AvailabilitySets}.
 */
@LangDefinition
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
    public PagedList<AvailabilitySet> list() {
        return new GroupPagedList<AvailabilitySet>(this.myManager.resourceManager().resourceGroups().list()) {
            @Override
            public List<AvailabilitySet> listNextGroup(String resourceGroupName) {
                return wrapList(innerCollection.list(resourceGroupName));
            }
        };
    }

    @Override
    public PagedList<AvailabilitySet> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public AvailabilitySetImpl getByGroup(String groupName, String name) {
        AvailabilitySetInner response = this.innerCollection.get(groupName, name);
        return wrapModel(response);
    }

    @Override
    public AvailabilitySetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
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
        if (availabilitySetInner == null) {
            return null;
        }
        return new AvailabilitySetImpl(availabilitySetInner.name(),
                availabilitySetInner,
                this.innerCollection,
                this.myManager);
    }
}
