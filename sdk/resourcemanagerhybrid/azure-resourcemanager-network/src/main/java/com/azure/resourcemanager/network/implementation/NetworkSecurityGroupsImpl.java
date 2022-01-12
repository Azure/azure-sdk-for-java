// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.NetworkSecurityGroupsClient;
import com.azure.resourcemanager.network.fluent.models.NetworkSecurityGroupInner;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation for NetworkSecurityGroups. */
public class NetworkSecurityGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        NetworkSecurityGroup,
        NetworkSecurityGroupImpl,
        NetworkSecurityGroupInner,
        NetworkSecurityGroupsClient,
        NetworkManager>
    implements NetworkSecurityGroups {

    public NetworkSecurityGroupsImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getNetworkSecurityGroups(), networkManager);
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        // Clear NIC references if any
        NetworkSecurityGroupImpl nsg = (NetworkSecurityGroupImpl) getByResourceGroup(resourceGroupName, name);
        if (nsg != null) {
            for (String nicRef : nsg.networkInterfaceIds()) {
                NetworkInterface nic = this.manager().networkInterfaces().getById(nicRef);
                if (nic == null) {
                    continue;
                } else if (!nsg.id().equalsIgnoreCase(nic.networkSecurityGroupId())) {
                    continue;
                } else {
                    nic.update().withoutNetworkSecurityGroup().apply();
                }
            }
        }

        return this.deleteInnerAsync(resourceGroupName, name);
    }

    @Override
    public NetworkSecurityGroupImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkSecurityGroupImpl wrapModel(String name) {
        NetworkSecurityGroupInner inner = new NetworkSecurityGroupInner();
        return new NetworkSecurityGroupImpl(name, inner, this.manager());
    }

    @Override
    protected NetworkSecurityGroupImpl wrapModel(NetworkSecurityGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkSecurityGroupImpl(inner.name(), inner, this.manager());
    }
}
