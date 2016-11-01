package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Dns zone Ptr record set management API.
 */
@Fluent
public interface PtrRecordSets extends
        DnsRecordSets<PtrRecordSet>,
        SupportsListing<PtrRecordSet>,
        SupportsGettingById<PtrRecordSet>,
        SupportsCreating<PtrRecordSet.DefinitionStages.Blank>,
        SupportsDeletingById {
}
