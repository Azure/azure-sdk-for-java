/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ExpressRouteCrossConnectionPeeringsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for express route cross connection peerings management API in Azure.
 */
@Fluent
public interface ExpressRouteCrossConnectionPeerings extends
        SupportsListing<ExpressRouteCrossConnectionPeering>,
        SupportsGettingByName<ExpressRouteCrossConnectionPeering>,
        SupportsGettingById<ExpressRouteCrossConnectionPeering>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasInner<ExpressRouteCrossConnectionPeeringsInner>,
        HasParent<ExpressRouteCrossConnection> {
    /**
     * Begins definition of Azure private peering.
     * @return next peering definition stage
     */
    ExpressRouteCrossConnectionPeering.DefinitionStages.Blank defineAzurePrivatePeering();

    /**
     * Begins definition of Microsoft peering.
     * @return next peering definition stage
     */
    ExpressRouteCrossConnectionPeering.DefinitionStages.WithAdvertisedPublicPrefixes defineMicrosoftPeering();
}