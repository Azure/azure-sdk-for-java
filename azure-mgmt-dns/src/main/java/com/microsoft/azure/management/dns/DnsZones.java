/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.management.dns.implementation.ZonesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroupAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingAsync;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to DNS zone management API in Azure.
 */
@Fluent
public interface DnsZones extends
        SupportsCreating<DnsZone.DefinitionStages.Blank>,
        SupportsListingAsync<DnsZone>,
        SupportsListingByGroupAsync<DnsZone>,
        SupportsGettingByGroup<DnsZone>,
        SupportsGettingById<DnsZone>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<DnsZone>,
        HasManager<DnsZoneManager>,
        HasInner<ZonesInner> {
}
