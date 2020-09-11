// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroup;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.Sku;
import com.azure.resourcemanager.compute.models.VirtualMachineSize;
import com.azure.resourcemanager.compute.fluent.inner.AvailabilitySetInner;
import com.azure.resourcemanager.compute.fluent.inner.ProximityPlacementGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import reactor.core.publisher.Mono;

/** The implementation for AvailabilitySet and its create and update interfaces. */
class AvailabilitySetImpl
    extends GroupableResourceImpl<AvailabilitySet, AvailabilitySetInner, AvailabilitySetImpl, ComputeManager>
    implements AvailabilitySet, AvailabilitySet.Definition, AvailabilitySet.Update {

    private Set<String> idOfVMsInSet;
    // Name of the new proximity placement group
    private String newProximityPlacementGroupName;
    // Type fo the new proximity placement group
    private ProximityPlacementGroupType newProximityPlacementGroupType;

    AvailabilitySetImpl(String name, AvailabilitySetInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        newProximityPlacementGroupName = null;
        newProximityPlacementGroupType = null;
        newProximityPlacementGroupType = null;
    }

    @Override
    public int updateDomainCount() {
        return Utils.toPrimitiveInt(this.inner().platformUpdateDomainCount());
    }

    @Override
    public int faultDomainCount() {
        return Utils.toPrimitiveInt(this.inner().platformFaultDomainCount());
    }

    @Override
    public AvailabilitySetSkuTypes sku() {
        if (this.inner().sku() != null && this.inner().sku().name() != null) {
            return AvailabilitySetSkuTypes.fromString(this.inner().sku().name());
        }
        return null;
    }

    @Override
    public Set<String> virtualMachineIds() {
        if (idOfVMsInSet == null) {
            idOfVMsInSet = new HashSet<>();
            for (SubResource resource : this.inner().virtualMachines()) {
                idOfVMsInSet.add(resource.id());
            }
        }
        return Collections.unmodifiableSet(idOfVMsInSet);
    }

    @Override
    public ProximityPlacementGroup proximityPlacementGroup() {
        if (inner().proximityPlacementGroup() == null) {
            return null;
        } else {
            ResourceId id = ResourceId.fromString(inner().proximityPlacementGroup().id());
            ProximityPlacementGroupInner plgInner =
                manager().inner().getProximityPlacementGroups().getByResourceGroup(id.resourceGroupName(), id.name());
            if (plgInner == null) {
                return null;
            } else {
                return new ProximityPlacementGroupImpl(plgInner);
            }
        }
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return Collections.unmodifiableList(this.inner().statuses());
    }

    @Override
    public PagedIterable<VirtualMachineSize> listVirtualMachineSizes() {
        return manager()
            .inner()
            .getAvailabilitySets()
            .listAvailableSizes(resourceGroupName(), name())
            .mapPage(virtualMachineSizeInner -> new VirtualMachineSizeImpl(virtualMachineSizeInner));
    }

    @Override
    public Mono<AvailabilitySet> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                availabilitySet -> {
                    AvailabilitySetImpl impl = (AvailabilitySetImpl) availabilitySet;
                    impl.idOfVMsInSet = null;
                    return impl;
                });
    }

    @Override
    protected Mono<AvailabilitySetInner> getInnerAsync() {
        return this.manager().inner().getAvailabilitySets()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
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
    public AvailabilitySetImpl withSku(AvailabilitySetSkuTypes skuType) {
        if (this.inner().sku() == null) {
            this.inner().withSku(new Sku());
        }
        this.inner().sku().withName(skuType.toString());
        return this;
    }

    @Override
    public AvailabilitySetImpl withProximityPlacementGroup(String proximityPlacementGroupId) {
        this.inner().withProximityPlacementGroup(new SubResource().withId(proximityPlacementGroupId));
        this.newProximityPlacementGroupType = null;
        this.newProximityPlacementGroupName = null;
        return this;
    }

    @Override
    public AvailabilitySetImpl withNewProximityPlacementGroup(
        String proximityPlacementGroupName, ProximityPlacementGroupType type) {
        this.newProximityPlacementGroupName = proximityPlacementGroupName;
        this.newProximityPlacementGroupType = type;

        this.inner().withProximityPlacementGroup(null);

        return this;
    }

    @Override
    public AvailabilitySetImpl withoutProximityPlacementGroup() {
        this.inner().withProximityPlacementGroup(null);
        return this;
    }

    // CreateUpdateTaskGroup.ResourceCreator.createResourceAsync implementation

    @Override
    public Mono<AvailabilitySet> createResourceAsync() {
        final AvailabilitySetImpl self = this;
        if (this.inner().platformFaultDomainCount() == null) {
            this.inner().withPlatformFaultDomainCount(2);
        }
        if (this.inner().platformUpdateDomainCount() == null) {
            this.inner().withPlatformUpdateDomainCount(5);
        }
        return this
            .createNewProximityPlacementGroupAsync()
            .flatMap(
                availabilitySet ->
                    manager()
                        .inner()
                        .getAvailabilitySets()
                        .createOrUpdateAsync(resourceGroupName(), name(), inner())
                        .map(
                            availabilitySetInner -> {
                                self.setInner(availabilitySetInner);
                                idOfVMsInSet = null;
                                return self;
                            }));
    }

    private Mono<AvailabilitySetImpl> createNewProximityPlacementGroupAsync() {
        if (isInCreateMode()) {
            if (this.newProximityPlacementGroupName != null && !this.newProximityPlacementGroupName.isEmpty()) {
                ProximityPlacementGroupInner plgInner = new ProximityPlacementGroupInner();
                plgInner.withProximityPlacementGroupType(this.newProximityPlacementGroupType);
                plgInner.withLocation(this.inner().location());
                return this
                    .manager()
                    .inner()
                    .getProximityPlacementGroups()
                    .createOrUpdateAsync(this.resourceGroupName(), this.newProximityPlacementGroupName, plgInner)
                    .map(
                        createdPlgInner -> {
                            this.inner().withProximityPlacementGroup(new SubResource().withId(createdPlgInner.id()));
                            return this;
                        });
            }
        }
        return Mono.just(this);
    }
}
