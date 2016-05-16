package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public interface AvailabilitySets extends
        SupportsListing<AvailabilitySet>,
        SupportsListingByGroup<AvailabilitySet>,
        SupportsGettingByGroup<AvailabilitySet>,
        SupportsCreating<AvailabilitySet.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    interface InGroup extends
            SupportsListing<AvailabilitySet>,
            SupportsCreating<AvailabilitySet.DefinitionCreatable>,
            SupportsDeleting {}
}
