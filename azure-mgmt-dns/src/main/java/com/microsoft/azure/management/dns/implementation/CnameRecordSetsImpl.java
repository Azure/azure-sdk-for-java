package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.CnameRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;

/**
 * Implementation of {@link CnameRecordSets}.
 */
class CnameRecordSetsImpl
        extends CreatableWrappersImpl<CnameRecordSet, CnameRecordSetImpl, RecordSetInner>
        implements CnameRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    CnameRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public CnameRecordSet getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.CNAME);
        return new CnameRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public CnameRecordSet getById(String id) {
        return null;
    }

    @Override
    public CnameRecordSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return null;
    }

    @Override
    public PagedList<CnameRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.CNAME));
    }

    @Override
    protected CnameRecordSetImpl wrapModel(String name) {
        RecordSetInner inner = new RecordSetInner();
        inner.withName(name).withType(RecordType.CNAME.toString());
        return wrapModel(inner);
    }

    @Override
    protected CnameRecordSetImpl wrapModel(RecordSetInner inner) {
        return new CnameRecordSetImpl(this.dnsZone, inner, this.client);
    }
}