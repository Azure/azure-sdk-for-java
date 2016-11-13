package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.NsRecord;
import com.microsoft.azure.management.dns.NsRecordSet;
import com.microsoft.azure.management.dns.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link NsRecordSet}.
 */
class NsRecordSetImpl
        extends DnsRecordSetImpl
        implements NsRecordSet {
    NsRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parent, innerModel, client);
    }

    static NsRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent, final RecordSetsInner client) {
        return new NsRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.NS.toString())
                        .withNsRecords(new ArrayList<NsRecord>()),
                client);
    }

    @Override
    public List<String> nameServers() {
        List<String> nameServers = new ArrayList<>();
        if (this.inner().nsRecords() != null) {
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

            for (NsRecord record : this.inner().nsRecords()) {
                resource.nsRecords().add(record);
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

