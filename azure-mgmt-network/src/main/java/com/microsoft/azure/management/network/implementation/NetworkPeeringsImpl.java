/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkPeering;
import com.microsoft.azure.management.network.NetworkPeerings;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 *  Implementation for network peerings.
 */
@LangDefinition
class NetworkPeeringsImpl
    extends IndependentChildrenImpl<
        NetworkPeering,
        NetworkPeeringImpl,
        VirtualNetworkPeeringInner,
        VirtualNetworkPeeringsInner,
        NetworkManager,
        Network>

    implements NetworkPeerings {

    private final NetworkImpl network;
    private final PagedList<NetworkPeering> members;

    // Constructor to use from the context of a parent
    NetworkPeeringsImpl(final NetworkImpl parent) {
        super(parent.manager().inner().virtualNetworkPeerings(), parent.manager());
        this.network = parent;
        this.members = this.wrapList(parent.inner().virtualNetworkPeerings());
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
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return this.inner().deleteAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public Observable<NetworkPeering> getByParentAsync(String resourceGroup, String parentName, String name) {
        return this.inner().getAsync(resourceGroup, parentName, name).map(new Func1<VirtualNetworkPeeringInner, NetworkPeering>() {
            @Override
            public NetworkPeering call(VirtualNetworkPeeringInner inner) {
                return wrapModel(inner);
            }
        });
    }

    @Override
    public PagedList<NetworkPeering> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.inner().list(resourceGroupName, parentName));
    }

    @Override
    public PagedList<NetworkPeering> list() {
        return this.members;
    }

    @Override
    public Observable<NetworkPeering> listAsync() {
        return Observable.from(this.members);
    }

    @Override
    public NetworkPeering associatedWithRemoteNetwork(Network network) {
        return (network != null) ? this.associatedWithRemoteNetwork(network.id()) : null;
    }

    @Override
    public NetworkPeering associatedWithRemoteNetwork(String resourceId) {
        if (resourceId != null) {
            for (NetworkPeering peering : this.members) {
                if (peering.remoteNetworkId().equalsIgnoreCase(resourceId)) {
                    return peering;
                }
            }
        }
        return null;
    }
}
