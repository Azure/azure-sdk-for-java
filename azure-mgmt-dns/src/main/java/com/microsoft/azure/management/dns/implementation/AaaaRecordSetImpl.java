package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.AaaaRecord;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link AaaaRecordSet}.
 */
class AaaaRecordSetImpl
        extends DnsRecordSetImpl<AaaaRecordSet, AaaaRecordSetImpl>
        implements
            AaaaRecordSet,
            AaaaRecordSet.Definition,
            AaaaRecordSet.Update {

    AaaaRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<String> ipv6Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        if (this.inner().aaaaRecords() != null) {
            for (AaaaRecord aaaaRecord : this.inner().aaaaRecords()) {
                ipv6Addresses.add(aaaaRecord.ipv6Address());
            }
        }
        return Collections.unmodifiableList(ipv6Addresses);
    }

    @Override
    public AaaaRecordSetImpl refresh() {
        this.refreshInner();
        return this;
    }

    @Override
    public AaaaRecordSetImpl withIpv6Address(String ipv6Address) {
        if (this.inner().aaaaRecords() == null) {
            this.inner().withAaaaRecords(new ArrayList<AaaaRecord>());
        }
        this.inner().aaaaRecords().add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public AaaaRecordSetImpl withoutIpv6Address(String ipv6Address) {
        if (this.inner().aaaaRecords() != null) {
            for (AaaaRecord record : this.inner().aaaaRecords()) {
                if (record.ipv6Address().equalsIgnoreCase(ipv6Address)) {
                    this.inner().aaaaRecords().remove(record);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    protected Func1<RecordSetInner, AaaaRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}
