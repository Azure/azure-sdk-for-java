package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Soa record sets in a Dns zone.
 */
@Fluent
public interface SoaRecordSets extends
        SupportsListing<SoaRecordSet>,
        SupportsGettingById<SoaRecordSet> {
}

