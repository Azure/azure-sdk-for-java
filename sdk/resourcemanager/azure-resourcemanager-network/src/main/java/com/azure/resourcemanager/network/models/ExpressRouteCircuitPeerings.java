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

/** Entry point for express route circuit peerings management API in Azure. */
@Fluent
public interface ExpressRouteCircuitPeerings
    extends SupportsListing<ExpressRouteCircuitPeering>,
        SupportsGettingByName<ExpressRouteCircuitPeering>,
        SupportsGettingById<ExpressRouteCircuitPeering>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasParent<ExpressRouteCircuit> {
    /**
     * Begins definition of Azure private peering.
     *
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.Blank defineAzurePrivatePeering();

    /**
     * Begins definition of Azure public peering.
     *
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.Blank defineAzurePublicPeering();

    /**
     * Begins definition of Microsoft peering.
     *
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.WithAdvertisedPublicPrefixes defineMicrosoftPeering();
}
