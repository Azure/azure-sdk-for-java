package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Dns zone Soa record set management API.
 */
@Fluent
public interface SoaRecordSets extends
        DnsRecordSets<SoaRecordSet>,
        SupportsListing<SoaRecordSet>,
        SupportsGettingById<SoaRecordSet> {
}

