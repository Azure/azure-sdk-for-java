package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.MxRecordSet;
import com.microsoft.azure.management.dns.MxRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link MxRecordSets}.
 */
class MxRecordSetsImpl
        extends CreatableWrappersImpl<MxRecordSet, MxRecordSetImpl, RecordSetInner>
        implements MxRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    MxRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public MxRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.MX);
        return new MxRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public MxRecordSetImpl getById(String id) {
        return null;
    }

    @Override
    public MxRecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<MxRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.MX));
    }

    @Override
    protected MxRecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.MX.toString());
        return wrapModel(inner);
    }

    @Override
    protected MxRecordSetImpl wrapModel(RecordSetInner inner) {
        return new MxRecordSetImpl(this.dnsZone, inner, this.client);
    }
}