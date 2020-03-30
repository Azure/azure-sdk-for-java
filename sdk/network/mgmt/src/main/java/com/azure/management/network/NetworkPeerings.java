/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;

import com.azure.management.network.models.VirtualNetworkPeeringsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 *  Entry point to network peering management API.
 */
@Fluent
public interface NetworkPeerings extends
        SupportsCreating<NetworkPeering.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<NetworkPeering>,
        SupportsBatchCreation<NetworkPeering>,
        SupportsDeletingByParent,
        SupportsListing<NetworkPeering>,
        HasManager<NetworkManager>,
        HasInner<VirtualNetworkPeeringsInner> {

    /**
     * Finds the peering, if any, that is associated with the specified network.
     * <p>
     * (Note that this makes a separate call to Azure.)
     * @param network an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering getByRemoteNetwork(Network network);

    /**
     * Finds the peering, if any, that is associated with the specified network.
     * <p>
     * (Note that this makes a separate call to Azure.)
     * @param remoteNetworkResourceId the resource ID of an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering getByRemoteNetwork(String remoteNetworkResourceId);

    /**
     * Asynchronously finds the peering, if any, that is associated with the specified network.
     * <p>
     * (Note that this makes a separate call to Azure.)
     * @param network an existing network
     * @return a representation of the future computation of this call, evaluating to null if no such peering is found
     */
    Mono<NetworkPeering> getByRemoteNetworkAsync(Network network);

    /**
     * Asynchronously finds the peering, if any, that is associated with the specified network.
     * <p>
     * (Note that this makes a separate call to Azure.)
     * @param remoteNetworkResourceId the resource ID of an existing network
     * @return a representation of the future computation of this call, evaluating to null if no such peering is found
     */
    Mono<NetworkPeering> getByRemoteNetworkAsync(String remoteNetworkResourceId);
}
