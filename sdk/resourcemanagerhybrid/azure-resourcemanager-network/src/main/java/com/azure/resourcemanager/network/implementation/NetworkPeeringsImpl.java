// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VirtualNetworkPeeringsClient;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkPeering;
import com.azure.resourcemanager.network.models.NetworkPeerings;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import reactor.core.publisher.Mono;

/** Implementation for network peerings. */
class NetworkPeeringsImpl
    extends IndependentChildrenImpl<
        NetworkPeering,
        NetworkPeeringImpl,
        VirtualNetworkPeeringInner,
        VirtualNetworkPeeringsClient,
        NetworkManager,
        Network>
    implements NetworkPeerings {

    private final NetworkImpl network;

    // Constructor to use from the context of a parent
    NetworkPeeringsImpl(final NetworkImpl parent) {
        super(parent.manager().serviceClient().getVirtualNetworkPeerings(), parent.manager());
        this.network = parent;
    }

    @Override
    public NetworkPeeringImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkPeeringImpl wrapModel(String name) {
        VirtualNetworkPeeringInner inner = new VirtualNetworkPeeringInner().withName(name);
        return new NetworkPeeringImpl(inner, this.network);
    }

    @Override
    protected NetworkPeeringImpl wrapModel(VirtualNetworkPeeringInner inner) {
        return (inner != null) ? new NetworkPeeringImpl(inner, this.network) : null;
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, final String name) {
        return this
            .manager()
            .networks()
            // Get the parent network of the peering to delete
            .getByResourceGroupAsync(groupName, parentName)

            // Then find the local peering to delete
            .flatMap(
                localNetwork -> {
                    if (localNetwork == null) {
                        return Mono.empty(); // Missing local network, so nothing else to do
                    } else {
                        String peeringId = localNetwork.id() + "/peerings/" + name;
                        return localNetwork.peerings().getByIdAsync(peeringId);
                    }
                })
            .flux()

            // Then get the remote peering if available and possible to delete
            .flatMap(
                localPeering -> {
                    if (localPeering == null) {
                        return Mono.empty();
                    } else if (!localPeering.isSameSubscription()) {
                        return Mono.just(localPeering);
                    } else {
                        return Mono.just(localPeering).concatWith(localPeering.getRemotePeeringAsync());
                    }
                })

            // Then delete each peering (this will be called for each of the peerings, so at least once for the local
            // peering, and second time for the remote one if any
            .flatMap(
                peering -> {
                    if (peering == null) {
                        return Mono.empty();
                    } else {
                        String networkName = ResourceUtils.nameFromResourceId(peering.networkId());
                        return peering
                            .manager()
                            .serviceClient()
                            .getVirtualNetworkPeerings()
                            .deleteAsync(peering.resourceGroupName(), networkName, peering.name());
                    }
                })

            // Then continue till the last peering is deleted
            .then();
    }

    @Override
    public Mono<NetworkPeering> getByParentAsync(String resourceGroup, String parentName, String name) {
        return this.innerModel().getAsync(resourceGroup, parentName, name).map(inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<NetworkPeering> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.innerModel().list(resourceGroupName, parentName));
    }

    @Override
    public PagedIterable<NetworkPeering> list() {
        return this.wrapList(this.innerModel().list(this.network.resourceGroupName(), this.network.name()));
    }

    @Override
    public PagedFlux<NetworkPeering> listAsync() {
        return this.wrapPageAsync(this.innerModel().listAsync(this.network.resourceGroupName(), this.network.name()));
    }

    @Override
    public NetworkPeering getByRemoteNetwork(Network network) {
        return (network != null) ? this.getByRemoteNetwork(network.id()) : null;
    }

    @Override
    public NetworkPeering getByRemoteNetwork(String remoteNetworkResourceId) {
        if (remoteNetworkResourceId != null) {
            for (NetworkPeering peering : this.list()) {
                if (peering.remoteNetworkId().equalsIgnoreCase(remoteNetworkResourceId)) {
                    return peering;
                }
            }
        }
        return null;
    }

    @Override
    public Mono<NetworkPeering> getByRemoteNetworkAsync(Network network) {
        if (network != null) {
            return this.getByRemoteNetworkAsync(network.id());
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<NetworkPeering> getByRemoteNetworkAsync(final String remoteNetworkResourceId) {
        if (remoteNetworkResourceId == null) {
            return Mono.empty();
        } else {
            return this
                .listAsync()
                .filter(
                    peering -> {
                        if (peering == null) {
                            return false;
                        } else {
                            return remoteNetworkResourceId.equalsIgnoreCase(peering.remoteNetworkId());
                        }
                    })
                .last();
        }
    }
}
