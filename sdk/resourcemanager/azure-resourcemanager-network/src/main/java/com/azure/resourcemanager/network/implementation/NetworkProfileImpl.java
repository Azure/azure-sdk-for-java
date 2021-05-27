// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.NetworkProfileInner;
import com.azure.resourcemanager.network.models.ContainerNetworkInterface;
import com.azure.resourcemanager.network.models.ContainerNetworkInterfaceConfiguration;
import com.azure.resourcemanager.network.models.NetworkProfile;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

final class NetworkProfileImpl extends
    GroupableParentResourceWithTagsImpl<NetworkProfile, NetworkProfileInner, NetworkProfileImpl, NetworkManager>
    implements NetworkProfile, NetworkProfile.Definition, NetworkProfile.Update {

    NetworkProfileImpl(String name, NetworkProfileInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
    }

    public List<ContainerNetworkInterface> containerNetworkInterfaces() {
        List<ContainerNetworkInterface> inner = this.innerModel().containerNetworkInterfaces();
        if (inner != null) {
            return Collections.unmodifiableList(inner);
        } else {
            return Collections.emptyList();
        }
    }

    public List<ContainerNetworkInterfaceConfiguration> containerNetworkInterfaceConfigurations() {
        List<ContainerNetworkInterfaceConfiguration> inner =
            this.innerModel().containerNetworkInterfaceConfigurations();
        if (inner != null) {
            return Collections.unmodifiableList(inner);
        } else {
            return Collections.emptyList();
        }
    }

    public String resourceGuid() {
        return this.innerModel().resourceGuid();
    }

    public NetworkProfileImpl withContainerNetworkInterfaceConfigurations(
        List<ContainerNetworkInterfaceConfiguration> containerNetworkInterfaceConfigurations) {
        this.innerModel().withContainerNetworkInterfaceConfigurations(containerNetworkInterfaceConfigurations);
        return this;
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


}
