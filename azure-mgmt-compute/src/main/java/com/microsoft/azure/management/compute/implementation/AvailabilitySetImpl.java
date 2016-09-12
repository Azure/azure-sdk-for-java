/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation for {@link AvailabilitySet} and its create and update interfaces.
 */
class AvailabilitySetImpl
    extends
        GroupableResourceImpl<
            AvailabilitySet,
            AvailabilitySetInner,
            AvailabilitySetImpl,
            ComputeManager>
    implements
        AvailabilitySet,
        AvailabilitySet.Definition,
        AvailabilitySet.Update {

    private List<String> idOfVMsInSet;

    // The client to make AvailabilitySet Management API calls
    private final AvailabilitySetsInner client;

    AvailabilitySetImpl(String name, AvailabilitySetInner innerModel,
                               final AvailabilitySetsInner client,
                               final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        this.client = client;
    }

    @Override
    public int updateDomainCount() {
        return this.inner().platformUpdateDomainCount();
    }

    @Override
    public int faultDomainCount() {
        return this.inner().platformFaultDomainCount();
    }

    @Override
    public List<String> virtualMachineIds() {
        if (idOfVMsInSet == null) {
            idOfVMsInSet = new ArrayList<>();
            for (SubResource resource : this.inner().virtualMachines()) {
                idOfVMsInSet.add(resource.id());
            }
        }

        return Collections.unmodifiableList(idOfVMsInSet);
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return Collections.unmodifiableList(this.inner().statuses());
    }

    @Override
    public AvailabilitySet refresh() throws Exception {
        AvailabilitySetInner response = client.get(this.resourceGroupName(), this.name());
        this.setInner(response);
        this.idOfVMsInSet = null;
        return this;
    }

    @Override
    public AvailabilitySetImpl withUpdateDomainCount(int updateDomainCount) {
        this.inner().withPlatformUpdateDomainCount(updateDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl withFaultDomainCount(int faultDomainCount) {
        this.inner().withPlatformFaultDomainCount(faultDomainCount);
        return this;
    }

    @Override
    public Observable<AvailabilitySet> applyAsync() {
        return this.createAsync();
    }

    // CreatorTaskGroup.ResourceCreator implementation

    @Override
    public Observable<AvailabilitySet> createResourceAsync() {
        final AvailabilitySetImpl self = this;
        return this.client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(new Func1<AvailabilitySetInner, AvailabilitySet>() {
                    @Override
                    public AvailabilitySet call(AvailabilitySetInner availabilitySetInner) {
                        self.setInner(availabilitySetInner);
                        idOfVMsInSet = null;
                        return self;
                    }
                });
    }
}
