// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VirtualNetworkPeeringsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** Entry point to network peering management API. */
@Fluent
public interface NetworkPeerings
    extends SupportsCreating<NetworkPeering.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<NetworkPeering>,
        SupportsBatchCreation<NetworkPeering>,
        SupportsDeletingByParent,
        SupportsListing<NetworkPeering>,
        HasManager<NetworkManager>,
        HasInner<VirtualNetworkPeeringsClient> {

    /**
     * Finds the peering, if any, that is associated with the specified network.
     *
     * <p>(Note that this makes a separate call to Azure.)
     *
     * @param network an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering getByRemoteNetwork(Network network);

    /**
     * Finds the peering, if any, that is associated with the specified network.
     *
     * <p>(Note that this makes a separate call to Azure.)
     *
     * @param remoteNetworkResourceId the resource ID of an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering getByRemoteNetwork(String remoteNetworkResourceId);

    /**
     * Asynchronously finds the peering, if any, that is associated with the specified network.
     *
     * <p>(Note that this makes a separate call to Azure.)
     *
     * @param network an existing network
     * @return a representation of the future computation of this call, evaluating to null if no such peering is found
     */
    Mono<NetworkPeering> getByRemoteNetworkAsync(Network network);

    /**
     * Asynchronously finds the peering, if any, that is associated with the specified network.
     *
     * <p>(Note that this makes a separate call to Azure.)
     *
     * @param remoteNetworkResourceId the resource ID of an existing network
     * @return a representation of the future computation of this call, evaluating to null if no such peering is found
     */
    Mono<NetworkPeering> getByRemoteNetworkAsync(String remoteNetworkResourceId);
}
