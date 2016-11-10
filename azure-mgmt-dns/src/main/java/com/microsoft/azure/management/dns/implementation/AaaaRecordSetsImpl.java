package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link AaaaRecordSets}.
 */
class AaaaRecordSetsImpl
        extends CreatableWrappersImpl<AaaaRecordSet, AaaaRecordSetImpl, RecordSetInner>
        implements AaaaRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public AaaaRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.AAAA);
        return new AaaaRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public AaaaRecordSetImpl getById(String id) {
        return null;
    }

    @Override
    public AaaaRecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<AaaaRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.AAAA));
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.AAAA.toString());
        return wrapModel(inner);
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        return new AaaaRecordSetImpl(this.dnsZone, inner, this.client);
    }
}
