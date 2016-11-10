package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.dns.TxtRecordSets;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link TxtRecordSets}.
 */
class TxtRecordSetsImpl
        extends CreatableWrappersImpl<TxtRecordSet, TxtRecordSetImpl, RecordSetInner>
        implements TxtRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    TxtRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public TxtRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.TXT);
        return new TxtRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public TxtRecordSetImpl getById(String id) {
        return null;
    }

    @Override
    public TxtRecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<TxtRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.TXT));
    }

    @Override
    protected TxtRecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.TXT.toString());
        return wrapModel(inner);
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        return new TxtRecordSetImpl(this.dnsZone, inner, this.client);
    }
}