/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.compute.models.AvailabilitySetsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to availability set management API.
 */
@Fluent
public interface AvailabilitySets extends
        SupportsListingByResourceGroup<AvailabilitySet>,
        SupportsGettingByResourceGroup<AvailabilitySet>,
        SupportsGettingById<AvailabilitySet>,
        SupportsListing<AvailabilitySet>,
        SupportsCreating<AvailabilitySet.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<AvailabilitySet>,
        HasManager<ComputeManager>,
        HasInner<AvailabilitySetsInner> {
}
