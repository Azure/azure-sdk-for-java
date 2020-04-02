/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.LocalNetworkGatewaysInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to local network gateways management API in Azure.
 */
@Fluent
public interface LocalNetworkGateways extends
        SupportsCreating<LocalNetworkGateway.DefinitionStages.Blank>,
        SupportsListing<LocalNetworkGateway>,
        SupportsListingByResourceGroup<LocalNetworkGateway>,
        SupportsGettingByResourceGroup<LocalNetworkGateway>,
        SupportsGettingById<LocalNetworkGateway>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        HasManager<NetworkManager>,
        HasInner<LocalNetworkGatewaysInner> {
}