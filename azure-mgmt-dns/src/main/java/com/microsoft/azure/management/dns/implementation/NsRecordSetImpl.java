package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.NsRecord;
import com.microsoft.azure.management.dns.NsRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link NsRecordSet}.
 */
class NsRecordSetImpl
        extends DnsRecordSetImpl
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
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().nsRecords() != null && this.inner().nsRecords().size() > 0) {
            if (resource.nsRecords() == null) {
                resource.withNsRecords(new ArrayList<NsRecord>());
            }

            for (NsRecord recordToAdd : this.inner().nsRecords()) {
                resource.nsRecords().add(recordToAdd);
            }
            this.inner().nsRecords().clear();
        }

        if (this.recordSetRemoveInfo.nsRecords().size() > 0) {
            if (resource.nsRecords() != null) {
                for (NsRecord recordToRemove : this.recordSetRemoveInfo.nsRecords()) {
                    for (NsRecord record : resource.nsRecords()) {
                        if (record.nsdname().equalsIgnoreCase(recordToRemove.nsdname())) {
                            resource.nsRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.nsRecords().clear();
        }
        return resource;
    }
}

