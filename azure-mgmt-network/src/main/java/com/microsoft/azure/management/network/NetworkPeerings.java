/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkPeeringsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import rx.Observable;

/**
 *  Entry point to network peering management API.
 */
@Fluent
@Beta(SinceVersion.V1_3_0)
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
    Observable<NetworkPeering> getByRemoteNetworkAsync(Network network);

    /**
     * Asynchronously finds the peering, if any, that is associated with the specified network.
     * <p>
     * (Note that this makes a separate call to Azure.)
     * @param remoteNetworkResourceId the resource ID of an existing network
     * @return a representation of the future computation of this call, evaluating to null if no such peering is found
     */
    Observable<NetworkPeering> getByRemoteNetworkAsync(String remoteNetworkResourceId);
}
