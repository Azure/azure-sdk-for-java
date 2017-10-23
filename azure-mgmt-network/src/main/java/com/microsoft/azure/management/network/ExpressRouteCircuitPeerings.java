/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ExpressRouteCircuitPeeringsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for express route circuit peerings management API in Azure.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface ExpressRouteCircuitPeerings extends
        SupportsListing<ExpressRouteCircuitPeering>,
        SupportsGettingByName<ExpressRouteCircuitPeering>,
        SupportsGettingById<ExpressRouteCircuitPeering>,
        SupportsGettingByNameAsync<ExpressRouteCircuitPeering>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<ExpressRouteCircuitPeeringsInner>,
        HasParent<ExpressRouteCircuit> {
    /**
     * Begins definition of Azure private peering.
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.Blank defineAzurePrivatePeering();

    /**
     * Begins definition of Azure public peering.
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.Blank defineAzurePublicPeering();

    /**
     * Begins definition of Microsoft peering.
     * @return next peering definition stage
     */
    ExpressRouteCircuitPeering.DefinitionStages.WithAdvertisedPublicPrefixes defineMicrosoftPeering();
}
