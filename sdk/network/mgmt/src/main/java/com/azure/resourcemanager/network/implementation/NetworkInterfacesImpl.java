// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkInterface;
import com.azure.resourcemanager.network.NetworkInterfaceDnsSettings;
import com.azure.resourcemanager.network.NetworkInterfaces;
import com.azure.resourcemanager.network.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.models.NetworkInterfaceInner;
import com.azure.resourcemanager.network.models.NetworkInterfacesInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import java.util.ArrayList;

/** Implementation for {@link NetworkInterfaces}. */
class NetworkInterfacesImpl
    extends TopLevelModifiableResourcesImpl<
        NetworkInterface, NetworkInterfaceImpl, NetworkInterfaceInner, NetworkInterfacesInner, NetworkManager>
    implements NetworkInterfaces {

    NetworkInterfacesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().networkInterfaces(), networkManager);
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineScaleSetInstanceId(
        String resourceGroupName, String scaleSetName, String instanceId, String name) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces =
            new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName, scaleSetName, this.manager());
        return scaleSetNetworkInterfaces.getByVirtualMachineInstanceId(instanceId, name);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(
        String resourceGroupName, String scaleSetName) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces =
            new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName, scaleSetName, this.manager());
        return scaleSetNetworkInterfaces.list();
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetId(String id) {
        return this
            .listByVirtualMachineScaleSet(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceId(
        String resourceGroupName, String scaleSetName, String instanceId) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces =
            new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName, scaleSetName, this.manager());
        return scaleSetNetworkInterfaces.listByVirtualMachineInstanceId(instanceId);
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceIdAsync(
        String resourceGroupName, String scaleSetName, String instanceId) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces =
            new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName, scaleSetName, this.manager());
        return scaleSetNetworkInterfaces.listByVirtualMachineInstanceIdAsync(instanceId);
    }

    @Override
    public NetworkInterfaceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(String name) {
        NetworkInterfaceInner inner = new NetworkInterfaceInner();
        inner.withIpConfigurations(new ArrayList<NetworkInterfaceIpConfigurationInner>());
        inner.withDnsSettings(new NetworkInterfaceDnsSettings());
        return new NetworkInterfaceImpl(name, inner, super.manager());
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkInterfaceImpl(inner.name(), inner, this.manager());
    }
}
