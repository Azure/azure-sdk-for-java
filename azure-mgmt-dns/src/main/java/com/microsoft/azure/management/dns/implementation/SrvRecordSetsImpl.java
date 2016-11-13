package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.SrvRecordSets;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link SrvRecordSets}.
 */
class SrvRecordSetsImpl
        extends ReadableWrappersImpl<SrvRecordSet, SrvRecordSetImpl, RecordSetInner>
        implements SrvRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    SrvRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public SrvRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.SRV);
        return new SrvRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<SrvRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.SRV));
    }

    @Override
    protected SrvRecordSetImpl wrapModel(RecordSetInner inner) {
        return new SrvRecordSetImpl(this.dnsZone, inner, this.client);
    }
}