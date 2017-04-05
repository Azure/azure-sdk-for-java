/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.management.dns.implementation.ZonesInner;
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
 * Entry point to DNS zone management API in Azure.
 */
@Fluent
public interface DnsZones extends
        SupportsCreating<DnsZone.DefinitionStages.Blank>,
        SupportsListing<DnsZone>,
        SupportsListingByResourceGroup<DnsZone>,
        SupportsGettingByResourceGroup<DnsZone>,
        SupportsGettingById<DnsZone>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<DnsZone>,
        SupportsBatchDeletion,
        HasManager<DnsZoneManager>,
        HasInner<ZonesInner> {
}
