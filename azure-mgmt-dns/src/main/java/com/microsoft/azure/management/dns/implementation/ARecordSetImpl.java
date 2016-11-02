package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.ARecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ARecordSet}.
 */
class ARecordSetImpl
        extends DnsRecordSetImpl<ARecordSet, ARecordSetImpl>
        implements
            ARecordSet,
            ARecordSet.Definition,
            ARecordSet.Update {
    ARecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<String> ipv4Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        if (this.inner().aaaaRecords() != null) {
            for (ARecord aRecord : this.inner().aRecords()) {
                ipv6Addresses.add(aRecord.ipv4Address());
            }
        }
        return Collections.unmodifiableList(ipv6Addresses);
    }

    @Override
    public ARecordSetImpl refresh() {
        this.refreshInner();
        return this;
    }

    @Override
    public ARecordSetImpl withIpv4Address(String ipv4Address) {
        if (this.inner().aRecords() == null) {
            this.inner().withARecords(new ArrayList<ARecord>());
        }
        this.inner().aRecords().add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public ARecordSetImpl withoutIpv4Address(String ipv4Address) {
        if (this.inner().aRecords() != null) {
            for (ARecord record : this.inner().aRecords()) {
                if (record.ipv4Address().equalsIgnoreCase(ipv4Address)) {
                    this.inner().aRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, ARecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}
