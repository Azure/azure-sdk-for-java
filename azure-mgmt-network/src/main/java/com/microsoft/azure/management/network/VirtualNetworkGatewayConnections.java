/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayConnectionsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for virtual network gateway connections management API in Azure.
 */
@Fluent
@Beta
public interface VirtualNetworkGatewayConnections extends
        SupportsCreating<VirtualNetworkGatewayConnection.DefinitionStages.Blank>,
        SupportsListing<VirtualNetworkGatewayConnection>,
        SupportsGettingByName<VirtualNetworkGatewayConnection>,
        SupportsGettingById<VirtualNetworkGatewayConnection>,
        SupportsGettingByNameAsync<VirtualNetworkGatewayConnection>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<VirtualNetworkGatewayConnectionsInner>,
        HasParent<VirtualNetworkGateway> {
}
