/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaceDnsSettings;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;

import java.util.ArrayList;

/**
 *  Implementation for {@link NetworkInterfaces}.
 */
@LangDefinition
class NetworkInterfacesImpl
        extends GroupableResourcesImpl<
            NetworkInterface,
            NetworkInterfaceImpl,
            NetworkInterfaceInner,
            NetworkInterfacesInner,
            NetworkManager>
        implements NetworkInterfaces {

    NetworkInterfacesImpl(
            final NetworkInterfacesInner client,
            final NetworkManager networkManager) {
        super(client, networkManager);
    }

    @Override
    public PagedList<NetworkInterface> list() {
        return wrapList(innerCollection.listAll());
    }

    @Override
    public PagedList<NetworkInterface> listByGroup(String groupName) {
        return wrapList(innerCollection.list(groupName));
    }

    @Override
    public NetworkInterface getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getByVirtualMachineScaleSetInstanceId(String resourceGroupName,
                                                                                        String scaleSetName,
                                                                                        String instanceId,
                                                                                        String name) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName,
                scaleSetName,
                this.innerCollection,
                this.myManager);
        return scaleSetNetworkInterfaces.getByVirtualMachineInstanceId(instanceId, name);
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(String resourceGroupName,
                                                                                          String scaleSetName) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName,
                scaleSetName,
                this.innerCollection,
                this.myManager);
        return scaleSetNetworkInterfaces.list();
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetId(String id) {
        return this.listByVirtualMachineScaleSet(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceId(String resourceGroupName,
                                                                                                    String scaleSetName,
                                                                                                    String instanceId) {
        VirtualMachineScaleSetNetworkInterfacesImpl scaleSetNetworkInterfaces = new VirtualMachineScaleSetNetworkInterfacesImpl(resourceGroupName,
                scaleSetName,
                this.innerCollection,
                this.myManager);
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
        return new NetworkInterfaceImpl(name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkInterfaceImpl(inner.name(),
                inner,
                this.innerCollection,
                super.myManager);
    }
}
