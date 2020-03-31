/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.ExpressRouteCrossConnectionsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to express route crosss connections management API in Azure.
 */
@Fluent
public interface ExpressRouteCrossConnections extends
        SupportsListing<ExpressRouteCrossConnection>,
        SupportsListingByResourceGroup<ExpressRouteCrossConnection>,
        SupportsGettingByResourceGroup<ExpressRouteCrossConnection>,
        SupportsGettingById<ExpressRouteCrossConnection>,
        HasManager<NetworkManager>,
        HasInner<ExpressRouteCrossConnectionsInner> {
}