/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.azure.management.network.VirtualMachineScaleSetNetworkInterfaces;
import com.azure.management.network.models.NetworkInterfaceInner;
import com.azure.management.network.models.NetworkInterfacesInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation for VirtualMachineScaleSetNetworkInterfaces.
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
    private final NetworkManager networkManager;

    VirtualMachineScaleSetNetworkInterfacesImpl(
            String resourceGroupName,
            String scaleSetName,
            NetworkManager networkManager) {
        this.resourceGroupName = resourceGroupName;
        this.scaleSetName = scaleSetName;
        this.networkManager = networkManager;
    }

    @Override
    public NetworkInterfacesInner inner() {
        return this.manager().inner().networkInterfaces();
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
        return new VirtualMachineScaleSetNetworkInterfaceImpl(inner.getName(),
                this.scaleSetName,
                this.resourceGroupName,
                inner,
                this.manager());
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineInstanceId(String instanceId, String name) {
        // FIXME: parameter - expand
        NetworkInterfaceInner networkInterfaceInner = this.inner().getVirtualMachineScaleSetNetworkInterface(this.resourceGroupName,
                this.scaleSetName,
                instanceId,
                name, null);
        if (networkInterfaceInner == null) {
            return null;
        }
        return this.wrapModel(networkInterfaceInner);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> list() {
        return super.wrapList(this.inner().listVirtualMachineScaleSetNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName));
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId) {
        return super.wrapList(this.inner().listVirtualMachineScaleSetVMNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName,
                instanceId));
    }
}