package com.microsoft.azure.management.dns;

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
 * Entry point to Dns zone management API in Azure.
 */
@Fluent()
public interface DnsZones extends
        SupportsCreating<DnsZone.DefinitionStages.Blank>,
        SupportsListing<DnsZone>,
        SupportsListingByGroup<DnsZone>,
        SupportsGettingByGroup<DnsZone>,
        SupportsGettingById<DnsZone>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<DnsZone> {
}
