package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to availability set management API.
 */
@Fluent
public interface AvailabilitySets extends
        SupportsListingByGroup<AvailabilitySet>,
        SupportsGettingByGroup<AvailabilitySet>,
        SupportsGettingById<AvailabilitySet>,
        SupportsListing<AvailabilitySet>,
        SupportsCreating<AvailabilitySet.DefinitionStages.Blank>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<AvailabilitySet> {
}
