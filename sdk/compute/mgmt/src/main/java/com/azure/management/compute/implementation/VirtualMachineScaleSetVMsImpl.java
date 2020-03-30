/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineScaleSetVMInner;
import com.azure.management.compute.models.VirtualMachineScaleSetVMsInner;
import com.azure.management.compute.models.VirtualMachineScaleSetsInner;
import com.azure.management.compute.VirtualMachineScaleSetVM;
import com.azure.management.compute.VirtualMachineScaleSetVMs;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implementation for {@link VirtualMachineScaleSetVMs}.
 */
class VirtualMachineScaleSetVMsImpl
        extends
        ReadableWrappersImpl<VirtualMachineScaleSetVM, VirtualMachineScaleSetVMImpl, VirtualMachineScaleSetVMInner>
        implements
        VirtualMachineScaleSetVMs {

    private final VirtualMachineScaleSetImpl scaleSet;
    private final VirtualMachineScaleSetVMsInner client;
    private final ComputeManager computeManager;

    VirtualMachineScaleSetVMsImpl(VirtualMachineScaleSetImpl scaleSet,
                                  VirtualMachineScaleSetVMsInner client,
                                  ComputeManager computeManager) {
        this.scaleSet = scaleSet;
        this.client = client;
        this.computeManager = computeManager;
    }

    @Override
    protected VirtualMachineScaleSetVMImpl wrapModel(VirtualMachineScaleSetVMInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineScaleSetVMImpl(inner, this.scaleSet, this.client, this.computeManager);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetVM> list() {
        return super.wrapList(this.client.list(this.scaleSet.resourceGroupName(), this.scaleSet.name()));
    }

    @Override
    public VirtualMachineScaleSetVMsInner inner() {
        return this.client;
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetVM> listAsync() {
        return super.wrapPageAsync(this.client.listAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name()));
    }

    @Override
    public Mono<Void> deleteInstancesAsync(Collection<String> instanceIds) {
        if (instanceIds == null || instanceIds.size() == 0) {
            return Mono.empty();
        }
        List<String> instanceIdList = new ArrayList<>();
        for (String instanceId : instanceIds) {
            instanceIdList.add(instanceId);
        }
        VirtualMachineScaleSetsInner scaleSetInnerManager = this.scaleSet.manager().virtualMachineScaleSets().inner();
        return scaleSetInnerManager.deleteInstancesAsync(this.scaleSet.resourceGroupName(),
                this.scaleSet.name(), instanceIdList);
    }

    @Override
    public Mono<Void> deleteInstancesAsync(String... instanceIds) {
        return this.deleteInstancesAsync(new ArrayList<String>(Arrays.asList(instanceIds)));
    }

    @Override
    public void deleteInstances(String... instanceIds) {
        this.deleteInstancesAsync(instanceIds).block();
    }

    @Override
    public VirtualMachineScaleSetVM getInstance(String instanceId) {
        return this.wrapModel(client.get(this.scaleSet.resourceGroupName(), this.scaleSet.name(), instanceId));
    }

    @Override
    public Mono<VirtualMachineScaleSetVM> getInstanceAsync(String instanceId) {
        return this.client.getAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(), instanceId)
                .map(this::wrapModel);
    }

    @Override
    public Mono<Void> updateInstancesAsync(Collection<String> instanceIds) {
        if (instanceIds == null || instanceIds.size() == 0) {
            return Mono.empty();
        }
        List<String> instanceIdList = new ArrayList<>();
        for (String instanceId : instanceIds) {
            instanceIdList.add(instanceId);
        }
        VirtualMachineScaleSetsInner scaleSetInnerManager = this.scaleSet.manager().virtualMachineScaleSets().inner();
        return scaleSetInnerManager.updateInstancesAsync(this.scaleSet.resourceGroupName(),
                this.scaleSet.name(), instanceIdList);
    }

    @Override
    public Mono<Void> updateInstancesAsync(String... instanceIds) {
        return this.updateInstancesAsync(new ArrayList<String>(Arrays.asList(instanceIds)));
    }

    @Override
    public void updateInstances(String... instanceIds) {
        this.updateInstancesAsync(instanceIds).block();
    }
}
