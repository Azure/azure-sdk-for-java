package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link TxtRecordSet}
 */
class TxtRecordSetImpl
        extends DnsRecordSetImpl<TxtRecordSet, TxtRecordSetImpl>
        implements TxtRecordSet {
    TxtRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<TxtRecord> records() {
        if (this.inner().txtRecords() != null) {
            return Collections.unmodifiableList(this.inner().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<TxtRecord>());
    }

    @Override
    public TxtRecordSetImpl refresh() {
        this.resetInner();
        return this;
    }

    @Override
    protected Func1<RecordSetInner, TxtRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}


