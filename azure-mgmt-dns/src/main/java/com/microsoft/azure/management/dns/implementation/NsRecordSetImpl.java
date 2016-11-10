package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.NsRecord;
import com.microsoft.azure.management.dns.NsRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link NsRecordSet}
 */
class NsRecordSetImpl
        extends DnsRecordSetImpl<NsRecordSet, NsRecordSetImpl>
        implements
            NsRecordSet,
            NsRecordSet.Definition,
            NsRecordSet.Update {
    NsRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<String> nameServers() {
        List<String> nameServers = new ArrayList<>();
        if (this.inner().aaaaRecords() != null) {
            for (NsRecord nsRecord : this.inner().nsRecords()) {
                nameServers.add(nsRecord.nsdname());
            }
        }
        return Collections.unmodifiableList(nameServers);
    }

    @Override
    public NsRecordSetImpl refresh() {
        this.refreshInner();
        return this;
    }

    @Override
    public NsRecordSetImpl withNameServer(String nameServerHostName) {
        if (this.inner().nsRecords() == null) {
            this.inner().withNsRecords(new ArrayList<NsRecord>());
        }
        this.inner().nsRecords().add(new NsRecord().withNsdname(nameServerHostName));
        return this;
    }

    @Override
    public NsRecordSetImpl withoutNameServer(String nameServerHostName) {
        if (this.inner().nsRecords() != null) {
            for (NsRecord record : this.inner().nsRecords()) {
                if (record.nsdname().equalsIgnoreCase(nameServerHostName)) {
                    this.inner().nsRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, NsRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

