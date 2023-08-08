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
import com.azure.resourcemanager.compute.fluent.models.AvailabilitySetInner;
import com.azure.resourcemanager.compute.fluent.models.ProximityPlacementGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

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
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().platformUpdateDomainCount());
    }

    @Override
    public int faultDomainCount() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().platformFaultDomainCount());
    }

    @Override
    public AvailabilitySetSkuTypes sku() {
        if (this.innerModel().sku() != null && this.innerModel().sku().name() != null) {
            return AvailabilitySetSkuTypes.fromString(this.innerModel().sku().name());
        }
        return null;
    }

    @Override
    public Set<String> virtualMachineIds() {
        if (idOfVMsInSet == null) {
            idOfVMsInSet = new HashSet<>();
            for (SubResource resource : this.innerModel().virtualMachines()) {
                idOfVMsInSet.add(resource.id());
            }
        }
        return Collections.unmodifiableSet(idOfVMsInSet);
    }

    @Override
    public ProximityPlacementGroup proximityPlacementGroup() {
        if (innerModel().proximityPlacementGroup() == null) {
            return null;
        } else {
            ResourceId id = ResourceId.fromString(innerModel().proximityPlacementGroup().id());
            ProximityPlacementGroupInner plgInner =
                manager()
                    .serviceClient()
                    .getProximityPlacementGroups()
                    .getByResourceGroup(id.resourceGroupName(), id.name());
            if (plgInner == null) {
                return null;
            } else {
                return new ProximityPlacementGroupImpl(plgInner);
            }
        }
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return Collections.unmodifiableList(this.innerModel().statuses());
    }

    @Override
    public PagedIterable<VirtualMachineSize> listVirtualMachineSizes() {
        return PagedConverter.mapPage(manager()
            .serviceClient()
            .getAvailabilitySets()
            .listAvailableSizes(resourceGroupName(), name()),
            virtualMachineSizeInner -> new VirtualMachineSizeImpl(virtualMachineSizeInner));
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
        return this
            .manager()
            .serviceClient()
            .getAvailabilitySets()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public AvailabilitySetImpl withUpdateDomainCount(int updateDomainCount) {
        this.innerModel().withPlatformUpdateDomainCount(updateDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl withFaultDomainCount(int faultDomainCount) {
        this.innerModel().withPlatformFaultDomainCount(faultDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl withSku(AvailabilitySetSkuTypes skuType) {
        if (this.innerModel().sku() == null) {
            this.innerModel().withSku(new Sku());
        }
        this.innerModel().sku().withName(skuType.toString());
        return this;
    }

    @Override
    public AvailabilitySetImpl withProximityPlacementGroup(String proximityPlacementGroupId) {
        this.innerModel().withProximityPlacementGroup(new SubResource().withId(proximityPlacementGroupId));
        this.newProximityPlacementGroupType = null;
        this.newProximityPlacementGroupName = null;
        return this;
    }

    @Override
    public AvailabilitySetImpl withNewProximityPlacementGroup(
        String proximityPlacementGroupName, ProximityPlacementGroupType type) {
        this.newProximityPlacementGroupName = proximityPlacementGroupName;
        this.newProximityPlacementGroupType = type;

        this.innerModel().withProximityPlacementGroup(null);

        return this;
    }

    @Override
    public AvailabilitySetImpl withoutProximityPlacementGroup() {
        this.innerModel().withProximityPlacementGroup(null);
        return this;
    }

    // CreateUpdateTaskGroup.ResourceCreator.createResourceAsync implementation

    @Override
    public Mono<AvailabilitySet> createResourceAsync() {
        final AvailabilitySetImpl self = this;
        if (this.innerModel().platformFaultDomainCount() == null) {
            this.innerModel().withPlatformFaultDomainCount(2);
        }
        if (this.innerModel().platformUpdateDomainCount() == null) {
            this.innerModel().withPlatformUpdateDomainCount(5);
        }
        return this
            .createNewProximityPlacementGroupAsync()
            .flatMap(
                availabilitySet ->
                    manager()
                        .serviceClient()
                        .getAvailabilitySets()
                        .createOrUpdateAsync(resourceGroupName(), name(), innerModel())
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
                plgInner.withLocation(this.innerModel().location());
                return this
                    .manager()
                    .serviceClient()
                    .getProximityPlacementGroups()
                    .createOrUpdateAsync(this.resourceGroupName(), this.newProximityPlacementGroupName, plgInner)
                    .map(
                        createdPlgInner -> {
                            this
                                .innerModel()
                                .withProximityPlacementGroup(new SubResource().withId(createdPlgInner.id()));
                            return this;
                        });
            }
        }
        return Mono.just(this);
    }
}
