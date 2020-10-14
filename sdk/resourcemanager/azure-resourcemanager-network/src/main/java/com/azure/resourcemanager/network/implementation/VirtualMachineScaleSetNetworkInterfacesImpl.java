// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.NetworkInterfacesClient;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceInner;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterfaces;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/** Implementation for VirtualMachineScaleSetNetworkInterfaces. */
class VirtualMachineScaleSetNetworkInterfacesImpl
    extends ReadableWrappersImpl<
        VirtualMachineScaleSetNetworkInterface, VirtualMachineScaleSetNetworkInterfaceImpl, NetworkInterfaceInner>
    implements VirtualMachineScaleSetNetworkInterfaces {
    private final String resourceGroupName;
    private final String scaleSetName;
    private final NetworkManager networkManager;

    VirtualMachineScaleSetNetworkInterfacesImpl(
        String resourceGroupName, String scaleSetName, NetworkManager networkManager) {
        this.resourceGroupName = resourceGroupName;
        this.scaleSetName = scaleSetName;
        this.networkManager = networkManager;
    }

    public NetworkInterfacesClient inner() {
        return this.manager().serviceClient().getNetworkInterfaces();
    }

    @Override
    public NetworkManager manager() {
        return this.networkManager;
    }

    @Override
    protected VirtualMachineScaleSetNetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineScaleSetNetworkInterfaceImpl(
            inner.name(), this.scaleSetName, this.resourceGroupName, inner, this.manager());
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineInstanceId(String instanceId, String name) {
        NetworkInterfaceInner networkInterfaceInner =
            this
                .inner()
                .getVirtualMachineScaleSetNetworkInterface(this.resourceGroupName, this.scaleSetName, instanceId, name);
        if (networkInterfaceInner == null) {
            return null;
        }
        return this.wrapModel(networkInterfaceInner);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> list() {
        return super
            .wrapList(
                this.inner().listVirtualMachineScaleSetNetworkInterfaces(this.resourceGroupName, this.scaleSetName));
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId) {
        return super
            .wrapList(
                this
                    .inner()
                    .listVirtualMachineScaleSetVMNetworkInterfaces(
                        this.resourceGroupName, this.scaleSetName, instanceId));
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceIdAsync(String instanceId) {
        return super
            .wrapPageAsync(
                this
                    .inner()
                    .listVirtualMachineScaleSetVMNetworkInterfacesAsync(
                        this.resourceGroupName, this.scaleSetName, instanceId));
    }
}
