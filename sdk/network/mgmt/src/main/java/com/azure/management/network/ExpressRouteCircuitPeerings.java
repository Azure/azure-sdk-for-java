/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ExpressRouteCircuitPeeringsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for express route circuit peerings management API in Azure.
 */
@Fluent
public interface ExpressRouteCircuitPeerings extends
        SupportsListing<ExpressRouteCircuitPeering>,
        SupportsGettingByName<ExpressRouteCircuitPeering>,
        SupportsGettingById<ExpressRouteCircuitPeering>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<ExpressRouteCircuitPeeringsInner>,
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
