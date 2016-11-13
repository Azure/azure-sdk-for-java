package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.PtrRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link PtrRecordSets}.
 */
class PtrRecordSetsImpl
        extends ReadableWrappersImpl<PtrRecordSet, PtrRecordSetImpl, RecordSetInner>
        implements PtrRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    PtrRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public PtrRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.PTR);
        return new PtrRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<PtrRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.PTR));
    }

    @Override
    protected PtrRecordSetImpl wrapModel(RecordSetInner inner) {
        return new PtrRecordSetImpl(this.dnsZone, inner, this.client);
    }
}