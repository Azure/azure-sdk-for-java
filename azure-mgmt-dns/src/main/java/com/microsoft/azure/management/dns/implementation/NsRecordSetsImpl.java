package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.NsRecordSet;
import com.microsoft.azure.management.dns.NsRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link NsRecordSets}.
 */
class NsRecordSetsImpl
        extends CreatableWrappersImpl<NsRecordSet, NsRecordSetImpl, RecordSetInner>
        implements NsRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    NsRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public NsRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.NS);
        return new NsRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public NsRecordSetImpl getById(String id) {
        return null;
    }

    @Override
    public NsRecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<NsRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.NS));
    }

    @Override
    protected NsRecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.NS.toString());
        return wrapModel(inner);
    }

    @Override
    protected NsRecordSetImpl wrapModel(RecordSetInner inner) {
        return new NsRecordSetImpl(this.dnsZone, inner, this.client);
    }
}