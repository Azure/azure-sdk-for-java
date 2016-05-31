package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.ResourcesInGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to availability set management API.
 */
public interface AvailabilitySets extends 
        SupportsListingByGroup<AvailabilitySet>,
        SupportsGettingByGroup<AvailabilitySet>,
        SupportsListing<AvailabilitySet>,
        SupportsCreating<AvailabilitySet.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    
    
    interface InGroup 
        extends ResourcesInGroup<AvailabilitySet, AvailabilitySet.DefinitionCreatable> {
    }
}
