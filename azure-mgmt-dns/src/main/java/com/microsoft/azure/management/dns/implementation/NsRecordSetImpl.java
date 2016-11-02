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
        implements NsRecordSet {
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
        this.resetInner();
        return this;
    }

    @Override
    protected Func1<RecordSetInner, NsRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}

