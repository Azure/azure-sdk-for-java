/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.RouteTablesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to route table management.
 */
@Fluent()
public interface RouteTables extends
    SupportsCreating<RouteTable.DefinitionStages.Blank>,
    SupportsListing<RouteTable>,
    SupportsListingByResourceGroup<RouteTable>,
    SupportsGettingByResourceGroup<RouteTable>,
    SupportsGettingById<RouteTable>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsBatchCreation<RouteTable>,
    SupportsBatchDeletion,
    HasManager<NetworkManager>,
    HasInner<RouteTablesInner> {
}
