// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to express route circuits management API in Azure. */
@Fluent
public interface ExpressRouteCircuits extends SupportsCreating<ExpressRouteCircuit.DefinitionStages.Blank>,
    SupportsListing<ExpressRouteCircuit>, SupportsListingByResourceGroup<ExpressRouteCircuit>,
    SupportsGettingByResourceGroup<ExpressRouteCircuit>, SupportsGettingById<ExpressRouteCircuit>, SupportsDeletingById,
    SupportsDeletingByResourceGroup, SupportsBatchCreation<ExpressRouteCircuit>, HasManager<NetworkManager> {
}
