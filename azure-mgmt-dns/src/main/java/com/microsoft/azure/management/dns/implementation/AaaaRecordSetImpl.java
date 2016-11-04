package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.AaaaRecord;
import com.microsoft.azure.management.dns.AaaaRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link AaaaRecordSet}.
 */
class AaaaRecordSetImpl
        extends DnsRecordSetImpl
        implements AaaaRecordSet {

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
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().aaaaRecords() != null && this.inner().aaaaRecords().size() > 0) {
            if (resource.aaaaRecords() == null) {
                resource.withAaaaRecords(new ArrayList<AaaaRecord>());
            }

            for (AaaaRecord recordToAdd : this.inner().aaaaRecords()) {
                resource.aaaaRecords().add(recordToAdd);
            }
            this.inner().aaaaRecords().clear();
        }

        if (this.recordSetRemoveInfo.aaaaRecords().size() > 0) {
            if (resource.aaaaRecords() != null) {
                for (AaaaRecord recordToRemove : this.recordSetRemoveInfo.aaaaRecords()) {
                    for (AaaaRecord record : resource.aaaaRecords()) {
                        if (record.ipv6Address().equalsIgnoreCase(recordToRemove.ipv6Address())) {
                            resource.aaaaRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.aaaaRecords().clear();
        }
        return resource;
    }
}
