package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.SrvRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link SrvRecordSet}.
 */
class SrvRecordSetImpl
        extends DnsRecordSetImpl
        implements SrvRecordSet {
    SrvRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parent, innerModel, client);
    }

    static SrvRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent, final RecordSetsInner client) {
        return new SrvRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.SRV.toString())
                        .withSrvRecords(new ArrayList<SrvRecord>()),
                client);
    }

    @Override
    public List<SrvRecord> records() {
        if (this.inner().srvRecords() != null) {
            return Collections.unmodifiableList(this.inner().srvRecords());
        }
        return Collections.unmodifiableList(new ArrayList<SrvRecord>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().srvRecords() != null && this.inner().srvRecords().size() > 0) {
            if (resource.srvRecords() == null) {
                resource.withSrvRecords(new ArrayList<SrvRecord>());
            }

            resource.srvRecords().addAll(this.inner().srvRecords());
            this.inner().srvRecords().clear();
        }

        if (this.recordSetRemoveInfo.srvRecords().size() > 0) {
            if (resource.srvRecords() != null) {
                for (SrvRecord recordToRemove : this.recordSetRemoveInfo.srvRecords()) {
                    for (SrvRecord record : resource.srvRecords()) {
                        if (record.target().equalsIgnoreCase(recordToRemove.target())
                                && (record.port().intValue() == recordToRemove.port().intValue())
                                && (record.weight().intValue() == recordToRemove.weight().intValue())
                                && (record.priority().intValue() == recordToRemove.priority().intValue())) {
                            resource.srvRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.srvRecords().clear();
        }
        return resource;
    }
}
