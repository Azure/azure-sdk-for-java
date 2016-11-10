package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.dns.implementation.RecordSetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of a record set in Azure Dns Zone.
 */
public interface DnsRecordSet<FluentModelT, ParentT> extends
    ExternalChildResource<FluentModelT, ParentT>,
    HasTags,
    Wrapper<RecordSetInner> {

    /**
     * @return the type of records in this record set
     */
    RecordType recordType();

    /**
     * @return TTL of the records in this record set
     */
    long timeToLive();
}
