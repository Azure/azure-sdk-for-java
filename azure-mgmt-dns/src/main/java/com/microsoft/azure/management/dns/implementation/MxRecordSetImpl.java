package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.MxRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link MxRecordSet}
 */
class MxRecordSetImpl
        extends DnsRecordSetImpl<MxRecordSet, MxRecordSetImpl>
        implements MxRecordSet {
    MxRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<MxRecord> records() {
        if (this.inner().mxRecords() != null) {
            return Collections.unmodifiableList(this.inner().mxRecords());
        }
        return Collections.unmodifiableList(new ArrayList<MxRecord>());
    }

    @Override
    public MxRecordSetImpl refresh() {
        this.resetInner();
        return this;
    }

    @Override
    protected Func1<RecordSetInner, MxRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

