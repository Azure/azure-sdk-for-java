package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.SrvRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link SrvRecordSet}.
 */
class SrvRecordSetImpl
        extends DnsRecordSetImpl<SrvRecordSet, SrvRecordSetImpl>
        implements
            SrvRecordSet,
            SrvRecordSet.Definition,
            SrvRecordSet.Update {
    SrvRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<SrvRecord> records() {
        if (this.inner().srvRecords() != null) {
            return Collections.unmodifiableList(this.inner().srvRecords());
        }
        return Collections.unmodifiableList(new ArrayList<SrvRecord>());
    }

    @Override
    public SrvRecordSetImpl refresh() {
        this.refreshInner();
        return this;
    }

    @Override
    public SrvRecordSetImpl withRecord(String target, int port, int priority, int weight) {
        if (this.inner().srvRecords() == null) {
            this.inner().withSrvRecords(new ArrayList<SrvRecord>());
        }
        this.inner().srvRecords().add(new SrvRecord()
                .withTarget(target)
                .withPort(port)
                .withPriority(priority)
                .withWeight(weight));
        return this;
    }

    @Override
    public SrvRecordSetImpl withoutRecord(String target, int port, int priority, int weight) {
        if (this.inner().srvRecords() != null) {
            for (SrvRecord record : this.inner().srvRecords()) {
                if (record.target().equalsIgnoreCase(target) &&
                        record.port() == port &&
                        record.priority() == priority &&
                        record.weight() == weight) {
                    this.inner().srvRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, SrvRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}
