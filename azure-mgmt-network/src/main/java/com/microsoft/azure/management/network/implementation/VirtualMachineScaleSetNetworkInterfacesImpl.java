/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * Implementation for VirtualMachineScaleSetNetworkInterfaces.
 */
@LangDefinition
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
        return new VirtualMachineScaleSetNetworkInterfaceImpl(inner.name(),
                this.scaleSetName,
                this.resourceGroupName,
                inner,
                this.manager());
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineInstanceId(String instanceId, String name) {
        NetworkInterfaceInner networkInterfaceInner = this.inner().getVirtualMachineScaleSetNetworkInterface(this.resourceGroupName,
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
        return super.wrapList(this.inner().listVirtualMachineScaleSetNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName));
    }

    @Override
    public Observable<VirtualMachineScaleSetNetworkInterface> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId) {
        return super.wrapList(this.inner().listVirtualMachineScaleSetVMNetworkInterfaces(this.resourceGroupName,
                this.scaleSetName,
                instanceId));
    }
}