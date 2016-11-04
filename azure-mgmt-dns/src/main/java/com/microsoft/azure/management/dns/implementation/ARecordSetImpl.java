package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.ARecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ARecordSet}.
 */
class ARecordSetImpl
        extends DnsRecordSetImpl
        implements ARecordSet {
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
    protected RecordSetInner merge(RecordSetInner resource, RecordSetInner recordSetRemoveInfo) {
        if (this.inner().aRecords() != null && this.inner().aRecords().size() > 0) {
            if (resource.aRecords() == null) {
                resource.withARecords(new ArrayList<ARecord>());
            }

            for (ARecord recordToAdd : this.inner().aRecords()) {
                resource.aRecords().add(recordToAdd);
            }
        }

        if (recordSetRemoveInfo.aRecords().size() > 0) {
            if (resource.aRecords() != null) {
                for (ARecord recordToRemove : recordSetRemoveInfo.aRecords()) {
                    for (ARecord record : resource.aRecords()) {
                        if (record.ipv4Address().equalsIgnoreCase(recordToRemove.ipv4Address())) {
                            resource.aRecords().remove(record);
                            break;
                        }
                    }
                }
            }
        }
        return resource;
    }
}
