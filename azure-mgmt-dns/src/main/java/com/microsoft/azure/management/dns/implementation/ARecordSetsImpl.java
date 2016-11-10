package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.ARecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link ARecordSets}.
 */
class ARecordSetsImpl
        extends CreatableWrappersImpl<ARecordSet, ARecordSetImpl, RecordSetInner>
        implements ARecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    ARecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public ARecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.A);
        return new ARecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public ARecordSetImpl getById(String id) {
        return null;
    }

    @Override
    public ARecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<ARecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.A));
    }

    @Override
    protected ARecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.A.toString());
        return wrapModel(inner);
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(this.dnsZone, inner, this.client);
    }
}
