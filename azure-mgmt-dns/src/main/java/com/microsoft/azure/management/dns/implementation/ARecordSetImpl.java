package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.RecordType;

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

    static ARecordSetImpl newRecordSet(final String name, final DnsZoneImpl parentDnsZone, final RecordSetsInner client) {
        return new ARecordSetImpl(parentDnsZone,
                new RecordSetInner()
                    .withName(name)
                    .withType(RecordType.A.toString())
                    .withARecords(new ArrayList<ARecord>()),
                client);
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
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().aRecords() != null && this.inner().aRecords().size() > 0) {
            if (resource.aRecords() == null) {
                resource.withARecords(new ArrayList<ARecord>());
            }

            for (ARecord recordToAdd : this.inner().aRecords()) {
                resource.aRecords().add(recordToAdd);
            }
            this.inner().aRecords().clear();
        }

        if (this.recordSetRemoveInfo.aRecords().size() > 0) {
            if (resource.aRecords() != null) {
                for (ARecord recordToRemove : this.recordSetRemoveInfo.aRecords()) {
                    for (ARecord record : resource.aRecords()) {
                        if (record.ipv4Address().equalsIgnoreCase(recordToRemove.ipv4Address())) {
                            resource.aRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.aRecords().clear();
        }
        return resource;
    }
}
