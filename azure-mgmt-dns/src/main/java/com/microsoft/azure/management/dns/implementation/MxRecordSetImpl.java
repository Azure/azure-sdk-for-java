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
        implements
            MxRecordSet,
            MxRecordSet.Definition,
            MxRecordSet.Update {
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
        this.refreshInner();
        return this;
    }

    @Override
    public MxRecordSetImpl withMailExchange(String mailExchangeHostName, int priority) {
        if (this.inner().mxRecords() == null) {
            this.inner().withMxRecords(new ArrayList<MxRecord>());
        }
        this.inner().mxRecords().add(new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public MxRecordSetImpl withoutMailExchange(String mailExchangeHostName, int priority) {
        if (this.inner().mxRecords() != null) {
            for (MxRecord record : this.inner().mxRecords()) {
                if (record.exchange().equalsIgnoreCase(mailExchangeHostName) && record.preference() == priority) {
                    this.inner().mxRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, MxRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

