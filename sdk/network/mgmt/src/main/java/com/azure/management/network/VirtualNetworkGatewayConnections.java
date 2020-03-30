/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.VirtualNetworkGatewayConnectionsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for virtual network gateway connections management API in Azure.
 */
@Fluent
public interface VirtualNetworkGatewayConnections extends
        SupportsCreating<VirtualNetworkGatewayConnection.DefinitionStages.Blank>,
        SupportsListing<VirtualNetworkGatewayConnection>,
        SupportsGettingByName<VirtualNetworkGatewayConnection>,
        SupportsGettingById<VirtualNetworkGatewayConnection>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<VirtualNetworkGatewayConnectionsInner>,
        HasParent<VirtualNetworkGateway> {
}
