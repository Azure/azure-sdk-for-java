// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point for express route cross connection peerings management API in Azure. */
@Fluent
public interface ExpressRouteCrossConnectionPeerings
    extends SupportsListing<ExpressRouteCrossConnectionPeering>,
        SupportsGettingByName<ExpressRouteCrossConnectionPeering>,
        SupportsGettingById<ExpressRouteCrossConnectionPeering>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasParent<ExpressRouteCrossConnection> {
    /**
     * Begins definition of Azure private peering.
     *
     * @return next peering definition stage
     */
    ExpressRouteCrossConnectionPeering.DefinitionStages.Blank defineAzurePrivatePeering();

    /**
     * Begins definition of Microsoft peering.
     *
     * @return next peering definition stage
     */
    ExpressRouteCrossConnectionPeering.DefinitionStages.WithAdvertisedPublicPrefixes defineMicrosoftPeering();
}
