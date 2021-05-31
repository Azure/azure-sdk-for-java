// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.IpConfigurationProfileInner;
import com.azure.resourcemanager.network.fluent.models.NetworkProfileInner;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.network.models.ContainerNetworkInterfaceConfiguration;
import com.azure.resourcemanager.network.models.NetworkProfile;
import com.azure.resourcemanager.network.models.Subnet;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

final class NetworkProfileImpl extends
    GroupableParentResourceWithTagsImpl<NetworkProfile, NetworkProfileInner, NetworkProfileImpl, NetworkManager>
    implements NetworkProfile, NetworkProfile.Definition, NetworkProfile.Update {

    NetworkProfileImpl(String name, NetworkProfileInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public List<ContainerNetworkInterfaceConfiguration> containerNetworkInterfaceConfigurations() {
        List<ContainerNetworkInterfaceConfiguration> inner =
            this.innerModel().containerNetworkInterfaceConfigurations();
        if (inner != null) {
            return Collections.unmodifiableList(inner);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected Mono<NetworkProfileInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getNetworkProfiles()
            .updateTagsAsync(resourceGroupName(), name(), innerModel().tags());
    }

    @Override
    protected Mono<NetworkProfileInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getNetworkProfiles()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }

    @Override
    protected void initializeChildrenFromInner() {
    }

    @Override
    protected Mono<NetworkProfileInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getNetworkProfiles()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }


    @Override
    public NetworkProfileImpl withContainerNetworkInterfaceConfiguration(
        String name, String ipConfigName, String virtualNetworkId, String subnetName) {
        String subnetId = String.format("%s/subnets/%s", virtualNetworkId, subnetName);
        return withContainerNetworkInterfaceConfiguration(name, ipConfigName, subnetId);
    }

    @Override
    public NetworkProfileImpl withContainerNetworkInterfaceConfiguration(
        String name, String ipConfigName, Subnet subnet) {
        return withContainerNetworkInterfaceConfiguration(name, ipConfigName, subnet.id());
    }

    private NetworkProfileImpl withContainerNetworkInterfaceConfiguration(
        String name, String ipConfigName, String subnetId) {
        this.innerModel().withContainerNetworkInterfaceConfigurations(
            Collections
                .singletonList(
                    new ContainerNetworkInterfaceConfiguration()
                        .withName(name)
                        .withIpConfigurations(
                            Collections
                                .singletonList(
                                    new IpConfigurationProfileInner()
                                        .withName(ipConfigName)
                                        .withSubnet(new SubnetInner().withId(subnetId))))));
        return this;
    }
}
