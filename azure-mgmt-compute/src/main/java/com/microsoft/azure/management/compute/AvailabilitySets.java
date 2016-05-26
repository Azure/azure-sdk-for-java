package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.ResourcesInGroup;

/**
 * Entry point to availability set management API.
 */
public interface AvailabilitySets extends 
        ResourcesInGroup<AvailabilitySet, AvailabilitySet.DefinitionBlank>,
        SupportsListingByGroup<AvailabilitySet>,
        SupportsGettingByGroup<AvailabilitySet>,
        SupportsDeletingByGroup {
    
    
    public interface InGroup 
        extends ResourcesInGroup<AvailabilitySet, AvailabilitySet.DefinitionAfterGroup> {
    }
}
