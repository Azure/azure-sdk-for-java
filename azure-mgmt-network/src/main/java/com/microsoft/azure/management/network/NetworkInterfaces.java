package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to network interface management.
 */
@Fluent()
public interface NetworkInterfaces  extends
        SupportsCreating<NetworkInterface.DefinitionStages.Blank>,
        SupportsListing<NetworkInterface>,
        SupportsListingByGroup<NetworkInterface>,
        SupportsGettingByGroup<NetworkInterface>,
        SupportsGettingById<NetworkInterface>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<NetworkInterface> {
}
