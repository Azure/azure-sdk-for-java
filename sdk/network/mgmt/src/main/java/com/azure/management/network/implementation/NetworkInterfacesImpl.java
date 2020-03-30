/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.models.NetworkInterfaceIPConfigurationInner;
import com.azure.management.network.models.NetworkInterfaceInner;
import com.azure.management.network.models.NetworkInterfacesInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NetworkInterfaceDnsSettings;
import com.azure.management.network.NetworkInterfaces;
import com.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

import java.util.ArrayList;

/**
 *  Implementation for {@link NetworkInterfaces}.
 */
class NetworkInterfacesImpl
    extends TopLevelModifiableResourcesImpl<
            NetworkInterface,
            NetworkInterfaceImpl,
            NetworkInterfaceInner,
            NetworkInterfacesInner,
            NetworkManager>
    implements NetworkInterfaces {

    NetworkInterfacesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().networkInterfaces(), networkManager);
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineScaleSetInstanceId(String resourceGroupName,
                                                                                        String scaleSetName,
                                                                                        String instanceId,
                                                                                        String name) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(
                resourceGroupName,
                scaleSetName,
                this.manager());
        return scaleSetNetworkInterfaces.getByVirtualMachineInstanceId(instanceId, name);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(String resourceGroupName,
                                                                                              String scaleSetName) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(
                resourceGroupName,
                scaleSetName,
                this.manager());
        return scaleSetNetworkInterfaces.list();
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetId(String id) {
        return this.listByVirtualMachineScaleSet(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceId(
            String resourceGroupName,
            String scaleSetName,
            String instanceId) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(
                resourceGroupName,
                scaleSetName,
                this.manager());
        return scaleSetNetworkInterfaces.listByVirtualMachineInstanceId(instanceId);
    }

    @Override
    public NetworkInterfaceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(String name) {
        NetworkInterfaceInner inner = new NetworkInterfaceInner();
        inner.withIpConfigurations(new ArrayList<NetworkInterfaceIPConfigurationInner>());
        inner.withDnsSettings(new NetworkInterfaceDnsSettings());
        return new NetworkInterfaceImpl(name, inner, super.manager());
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkInterfaceImpl(inner.getName(), inner, this.manager());
    }
}
