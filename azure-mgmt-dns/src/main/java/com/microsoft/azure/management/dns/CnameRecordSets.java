package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Cname record sets in a Dns zone.
 */
@Fluent
public interface CnameRecordSets extends
        SupportsListing<CnameRecordSet>,
        SupportsGettingByName<CnameRecordSet> {
}
