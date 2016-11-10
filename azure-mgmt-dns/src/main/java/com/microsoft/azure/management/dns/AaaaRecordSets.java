package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Dns zone Aaaa record set management API.
 */
@Fluent
public interface AaaaRecordSets extends
        DnsRecordSets<AaaaRecordSet>,
        SupportsListing<AaaaRecordSet>,
        SupportsGettingByName<AaaaRecordSet>,
        SupportsGettingById<AaaaRecordSet>,
        SupportsCreating<AaaaRecordSet.DefinitionStages.Blank>,
        SupportsDeletingById {
}
