// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCrossConnectionsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point to express route crosss connections management API in Azure. */
@Fluent
public interface ExpressRouteCrossConnections
    extends SupportsListing<ExpressRouteCrossConnection>,
        SupportsListingByResourceGroup<ExpressRouteCrossConnection>,
        SupportsGettingByResourceGroup<ExpressRouteCrossConnection>,
        SupportsGettingById<ExpressRouteCrossConnection>,
        HasManager<NetworkManager>,
        HasInner<ExpressRouteCrossConnectionsClient> {
}
