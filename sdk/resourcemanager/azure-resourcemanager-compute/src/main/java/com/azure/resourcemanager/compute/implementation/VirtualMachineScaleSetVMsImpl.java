// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetVMsClient;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetsClient;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetVMInner;
import com.azure.resourcemanager.compute.models.InstanceViewTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMExpandType;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMInstanceIDs;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMInstanceRequiredIDs;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Implementation for {@link VirtualMachineScaleSetVMs}. */
class VirtualMachineScaleSetVMsImpl
    extends ReadableWrappersImpl<VirtualMachineScaleSetVM, VirtualMachineScaleSetVMImpl, VirtualMachineScaleSetVMInner>
    implements VirtualMachineScaleSetVMs {

    private final VirtualMachineScaleSetImpl scaleSet;
    private final VirtualMachineScaleSetVMsClient client;
    private final ComputeManager computeManager;

    VirtualMachineScaleSetVMsImpl(
        VirtualMachineScaleSetImpl scaleSet, VirtualMachineScaleSetVMsClient client, ComputeManager computeManager) {
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
        return new PagedIterable<>(this.listAsync());
    }

    public VirtualMachineScaleSetVMsClient inner() {
        return this.client;
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetVM> listAsync() {
        return super.wrapPageAsync(this.client.listAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name()));
    }

    @Override
    public Mono<Void> deleteInstancesAsync(Collection<String> instanceIds) {
        return this.scaleSet.manager().virtualMachineScaleSets()
            .deleteInstancesAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(), instanceIds, false);
    }

    @Override
    public Mono<Void> deleteInstancesAsync(String... instanceIds) {
        return this.deleteInstancesAsync(new ArrayList<>(Arrays.asList(instanceIds)));
    }

    @Override
    public void deleteInstances(String... instanceIds) {
        this.deleteInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> deleteInstancesAsync(Collection<String> instanceIds, boolean forceDeletion) {
        return this.scaleSet.manager().virtualMachineScaleSets().deleteInstancesAsync(this.scaleSet.resourceGroupName(),
            this.scaleSet.name(), instanceIds, forceDeletion);
    }

    @Override
    public void deleteInstances(Collection<String> instanceIds, boolean forceDeletion) {
        this.deleteInstancesAsync(instanceIds, forceDeletion).block();
    }

    @Override
    public void deallocateInstances(Collection<String> instanceIds) {
        this.deallocateInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> deallocateInstancesAsync(Collection<String> instanceIds) {
        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().deallocateAsync(
            this.scaleSet.resourceGroupName(), this.scaleSet.name(),
            new VirtualMachineScaleSetVMInstanceIDs().withInstanceIds(new ArrayList<>(instanceIds)));
    }

    @Override
    public void powerOffInstances(Collection<String> instanceIds, boolean skipShutdown) {
        this.powerOffInstancesAsync(instanceIds, skipShutdown).block();
    }

    @Override
    public Mono<Void> powerOffInstancesAsync(Collection<String> instanceIds, boolean skipShutdown) {
        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().powerOffAsync(
            this.scaleSet.resourceGroupName(), this.scaleSet.name(), skipShutdown,
            new VirtualMachineScaleSetVMInstanceIDs().withInstanceIds(new ArrayList<>(instanceIds)));
    }

    @Override
    public void startInstances(Collection<String> instanceIds) {
        this.startInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> startInstancesAsync(Collection<String> instanceIds) {
        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().startAsync(
            this.scaleSet.resourceGroupName(), this.scaleSet.name(),
            new VirtualMachineScaleSetVMInstanceIDs().withInstanceIds(new ArrayList<>(instanceIds)));
    }

    @Override
    public void restartInstances(Collection<String> instanceIds) {
        this.restartInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> restartInstancesAsync(Collection<String> instanceIds) {
        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().restartAsync(
            this.scaleSet.resourceGroupName(), this.scaleSet.name(),
            new VirtualMachineScaleSetVMInstanceIDs().withInstanceIds(new ArrayList<>(instanceIds)));
    }

    @Override
    public void redeployInstances(Collection<String> instanceIds) {
        this.redeployInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> redeployInstancesAsync(Collection<String> instanceIds) {
        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().redeployAsync(
            this.scaleSet.resourceGroupName(), this.scaleSet.name(),
            new VirtualMachineScaleSetVMInstanceIDs().withInstanceIds(new ArrayList<>(instanceIds)));
    }

//    @Override
//    public void reimageInstances(Collection<String> instanceIds) {
//        this.reimageInstancesAsync(instanceIds).block();
//    }
//
//    @Override
//    public Mono<Void> reimageInstancesAsync(Collection<String> instanceIds) {
//        return this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets().reimageAsync(
//            this.scaleSet.resourceGroupName(), this.scaleSet.name(),
//            new VirtualMachineScaleSetReimageParameters().withInstanceIds(new ArrayList<>(instanceIds)));
//    }

    @Override
    public VirtualMachineScaleSetVM getInstance(String instanceId) {
        return this.getInstanceAsync(instanceId).block();
    }

    @Override
    public Mono<VirtualMachineScaleSetVM> getInstanceAsync(String instanceId) {
        return this
            .client
            .getAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(), instanceId,
                InstanceViewTypes.INSTANCE_VIEW)
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
        VirtualMachineScaleSetsClient scaleSetInnerManager =
            this.scaleSet.manager().serviceClient().getVirtualMachineScaleSets();
        return scaleSetInnerManager
            .updateInstancesAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(),
                new VirtualMachineScaleSetVMInstanceRequiredIDs().withInstanceIds(instanceIdList));
    }

    @Override
    public Mono<Void> updateInstancesAsync(String... instanceIds) {
        return this.updateInstancesAsync(new ArrayList<>(Arrays.asList(instanceIds)));
    }

    @Override
    public void updateInstances(String... instanceIds) {
        this.updateInstancesAsync(instanceIds).block();
    }

    @Override
    public Mono<Void> simulateEvictionAsync(String instanceId) {
        return this.client.simulateEvictionAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(), instanceId);
    }

    @Override
    public void simulateEviction(String instanceId) {
        this.simulateEvictionAsync(instanceId).block();
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetVM> list(String filter, VirtualMachineScaleSetVMExpandType expand) {
        return new PagedIterable<>(this.listAsync(filter, expand));
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetVM> listAsync(String filter, VirtualMachineScaleSetVMExpandType expand) {
        return super.wrapPageAsync(this.client.listAsync(this.scaleSet.resourceGroupName(), this.scaleSet.name(),
            filter, null, expand.toString()));
    }
}
