// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.NetworkInterfacesClient;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkInterfaceDnsSettings;
import com.azure.resourcemanager.network.models.NetworkInterfaces;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;

import java.util.ArrayList;
import java.util.function.Function;

/** Implementation for {@link NetworkInterfaces}. */
public class NetworkInterfacesImpl
    extends TopLevelModifiableResourcesImpl<
        NetworkInterface, NetworkInterfaceImpl, NetworkInterfaceInner, NetworkInterfacesClient, NetworkManager>
    implements NetworkInterfaces {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    public NetworkInterfacesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getNetworkInterfaces(), networkManager);
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

    @Override
    public Accepted<Void> beginDeleteById(String id) {
        return beginDeleteByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        return AcceptedImpl.newAccepted(logger,
            manager().inner(),
            () -> this.inner().deleteWithResponseAsync(resourceGroupName, name).block(),
            Function.identity(),
            Void.class,
            null);
    }
}
