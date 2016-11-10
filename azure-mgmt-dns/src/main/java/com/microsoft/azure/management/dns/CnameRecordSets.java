package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to Dns zone CName record set management API.
 */
@Fluent
public interface CnameRecordSets extends
        DnsRecordSets<CnameRecordSet>,
        SupportsListing<CnameRecordSet>,
        SupportsGettingByName<CnameRecordSet>,
        SupportsGettingById<CnameRecordSet>,
        SupportsCreating<CnameRecordSet.DefinitionStages.Blank>,
        SupportsDeletingById {
}
