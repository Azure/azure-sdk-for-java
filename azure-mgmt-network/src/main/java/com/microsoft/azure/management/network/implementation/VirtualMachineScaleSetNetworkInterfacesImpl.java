/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation for {@link VirtualMachineScaleSetNetworkInterfaces}.
 */
class VirtualMachineScaleSetNetworkInterfacesImpl
        extends
        ReadableWrappersImpl<VirtualMachineScaleSetNetworkInterface,
                VirtualMachineScaleSetNetworkInterfaceImpl,
                NetworkInterfaceInner>
        implements
        VirtualMachineScaleSetNetworkInterfaces {
    private final String resourceGroupName;
    private final String scaleSetName;
    private final NetworkInterfacesInner client;
    private final NetworkManager networkManager;

    VirtualMachineScaleSetNetworkInterfacesImpl(String resourceGroupName,
                                                String scaleSetName,
                                                NetworkInterfacesInner client,
                                                NetworkManager networkManager) {
        this.resourceGroupName = resourceGroupName;
        this.scaleSetName = scaleSetName;
        this.client = client;
        this.networkManager = networkManager;
    }

    @Override
    protected VirtualMachineScaleSetNetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        return new VirtualMachineScaleSetNetworkInterfaceImpl(inner.name(),
                this.scaleSetName,
                this.resourceGroupName,
                inner,
                this.client,
                this.networkManager);
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineInstanceId(String instanceId, String name) {
        NetworkInterfaceInner networkInterfaceInner = this.client.getVirtualMachineScaleSetNetworkInterface(this.resourceGroupName,
                this.scaleSetName,
                instanceId,
                name);
        if (networkInterfaceInner == null) {
            return null;
        }
        return this.wrapModel(networkInterfaceInner);
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> list() {
        return super.wrapList(this.client.listVirtualMachineScaleSetNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName));
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId) {
        return super.wrapList(this.client.listVirtualMachineScaleSetVMNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName,
                instanceId));
    }
}