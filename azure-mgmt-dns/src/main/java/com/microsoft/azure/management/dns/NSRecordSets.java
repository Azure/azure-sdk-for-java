/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to NS record sets in an Azure DNS zone.
 */
@Fluent
public interface NSRecordSets extends
        SupportsListing<NSRecordSet>,
        SupportsGettingByName<NSRecordSet>,
        HasParent<DnsZone> {
}