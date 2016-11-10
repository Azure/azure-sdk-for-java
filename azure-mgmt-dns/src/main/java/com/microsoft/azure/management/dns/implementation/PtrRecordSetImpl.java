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
        implements
            PtrRecordSet,
            PtrRecordSet.Definition,
            PtrRecordSet.Update {
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
        this.refreshInner();
        return this;
    }

    @Override
    public PtrRecordSetImpl withTargetDomain(String targetDomainName) {
        if (this.inner().ptrRecords() == null) {
            this.inner().withPtrRecords(new ArrayList<PtrRecord>());
        }
        this.inner().ptrRecords().add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public PtrRecordSetImpl withoutTargetDomain(String targetDomainName) {
        if (this.inner().nsRecords() != null) {
            for (PtrRecord record : this.inner().ptrRecords()) {
                if (record.ptrdname().equalsIgnoreCase(targetDomainName)) {
                    this.inner().ptrRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, PtrRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

