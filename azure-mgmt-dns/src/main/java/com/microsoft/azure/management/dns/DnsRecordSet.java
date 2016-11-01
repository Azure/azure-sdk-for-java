package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.dns.implementation.RecordSetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

public interface DnsRecordSet<FluentModelT, ParentT> extends
    ExternalChildResource<FluentModelT, ParentT>,
    HasTags,
    Wrapper<RecordSetInner> {

    /**
     * @return the type of records in this record set
     */
    String recordType();

    /**
     * @return TTL of the records in this record set
     */
    long timeToLive();
}
