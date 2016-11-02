package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link TxtRecordSet}.
 */
class TxtRecordSetImpl
        extends DnsRecordSetImpl<TxtRecordSet, TxtRecordSetImpl>
        implements
            TxtRecordSet,
            TxtRecordSet.Definition,
            TxtRecordSet.Update {
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
        this.refreshInner();
        return this;
    }

    @Override
    public TxtRecordSetImpl withText(String text) {
        if (this.inner().txtRecords() == null) {
            this.inner().withTxtRecords(new ArrayList<TxtRecord>());
        }
        List<String> value = new ArrayList<>();
        value.add(text);
        this.inner().txtRecords().add(new TxtRecord().withValue(value));
        return this;
    }

    @Override
    public TxtRecordSetImpl withoutText(String text) {
        if (this.inner().txtRecords() != null) {
            for (TxtRecord record : this.inner().txtRecords()) {
                if (record.value() != null && record.value().size() != 0) {
                    if (record.value().get(0).equalsIgnoreCase(text)) {
                        this.inner().txtRecords().remove(record);
                        break;
                    }
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, TxtRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}


