package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Dns zone Srv record set management API.
 */
@Fluent
public interface SrvRecordSets extends
        DnsRecordSets<SrvRecordSet>,
        SupportsListing<SrvRecordSet>,
        SupportsGettingByName<SrvRecordSet>,
        SupportsGettingById<SrvRecordSet>,
        SupportsCreating<SrvRecordSet.DefinitionStages.Blank>,
        SupportsDeletingById {
}