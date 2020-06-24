// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.VirtualNetworkGatewayConnectionsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for virtual network gateway connections management API in Azure. */
@Fluent
public interface VirtualNetworkGatewayConnections
    extends SupportsCreating<VirtualNetworkGatewayConnection.DefinitionStages.Blank>,
        SupportsListing<VirtualNetworkGatewayConnection>,
        SupportsGettingByName<VirtualNetworkGatewayConnection>,
        SupportsGettingById<VirtualNetworkGatewayConnection>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<VirtualNetworkGatewayConnectionsClient>,
        HasParent<VirtualNetworkGateway> {
}
