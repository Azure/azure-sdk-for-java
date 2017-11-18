/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The implementation for AvailabilitySet and its create and update interfaces.
 */
@LangDefinition
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

    private Set<String> idOfVMsInSet;

    AvailabilitySetImpl(String name, AvailabilitySetInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
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
    public List<InstanceViewStatus> statuses() {
        return Collections.unmodifiableList(this.inner().statuses());
    }

    @Override
    public PagedList<VirtualMachineSize> listVirtualMachineSizes() {
        return Utils.toPagedList(this.manager()
                        .inner()
                        .availabilitySets()
                        .listAvailableSizes(this.resourceGroupName(), this.name()),
                new Func1<VirtualMachineSizeInner, VirtualMachineSize>() {
                    @Override
                    public VirtualMachineSize call(VirtualMachineSizeInner inner) {
                        return new VirtualMachineSizeImpl(inner);
                    }
                });
    }

    @Override
    public Observable<AvailabilitySet> refreshAsync() {
        return super.refreshAsync().map(new Func1<AvailabilitySet, AvailabilitySet>() {
            @Override
            public AvailabilitySet call(AvailabilitySet availabilitySet) {
                AvailabilitySetImpl impl = (AvailabilitySetImpl) availabilitySet;
                impl.idOfVMsInSet = null;
                return impl;
            }
        });
    }

    @Override
    protected Observable<AvailabilitySetInner> getInnerAsync() {
        return this.manager().inner().availabilitySets().getByResourceGroupAsync(this.resourceGroupName(), this.name());
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

    // CreateUpdateTaskGroup.ResourceCreator.createResourceAsync implementation

    @Override
    public Observable<AvailabilitySet> createResourceAsync() {
        final AvailabilitySetImpl self = this;
        if (this.inner().platformFaultDomainCount() == null) {
            this.inner().withPlatformFaultDomainCount(2);
        }
        if (this.inner().platformUpdateDomainCount() == null) {
            this.inner().withPlatformUpdateDomainCount(5);
        }
        return this.manager().inner().availabilitySets().createOrUpdateAsync(resourceGroupName(), name(), inner())
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
