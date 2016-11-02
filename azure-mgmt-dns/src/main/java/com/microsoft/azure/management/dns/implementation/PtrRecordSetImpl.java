package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.PtrRecord;
import com.microsoft.azure.management.dns.PtrRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link PtrRecordSet}
 */
class PtrRecordSetImpl
        extends DnsRecordSetImpl<PtrRecordSet, PtrRecordSetImpl>
        implements PtrRecordSet {
    PtrRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<String> targetDomainNames() {
        List<String> targetDomainNames = new ArrayList<>();
        if (this.inner().aaaaRecords() != null) {
            for (PtrRecord ptrRecord : this.inner().ptrRecords()) {
                targetDomainNames.add(ptrRecord.ptrdname());
            }
        }
        return Collections.unmodifiableList(targetDomainNames);
    }

    @Override
    public PtrRecordSetImpl refresh() {
        this.resetInner();
        return this;
    }

    @Override
    protected Func1<RecordSetInner, PtrRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

