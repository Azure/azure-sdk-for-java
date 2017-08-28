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
     * @param network an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering associatedWithRemoteNetwork(Network network);

    /**
     * Finds the peering, if any, that is associated with the specified network.
     * @param resourceId the resource ID of an existing network
     * @return a network peering, or null if none exists
     */
    NetworkPeering associatedWithRemoteNetwork(String resourceId);
}
