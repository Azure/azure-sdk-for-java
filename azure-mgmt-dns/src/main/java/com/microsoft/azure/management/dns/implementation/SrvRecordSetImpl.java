package com.microsoft.azure.management.dns.implementation;

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
    SrvRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
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

            for (SrvRecord recordToAdd : this.inner().srvRecords()) {
                resource.srvRecords().add(recordToAdd);
            }
            this.inner().srvRecords().clear();
        }

        if (this.recordSetRemoveInfo.srvRecords().size() > 0) {
            if (resource.srvRecords() != null) {
                for (SrvRecord recordToRemove : this.recordSetRemoveInfo.srvRecords()) {
                    for (SrvRecord record : resource.srvRecords()) {
                        if (record.target().equalsIgnoreCase(recordToRemove.target())
                                && record.port() == recordToRemove.port()
                                && record.weight() == recordToRemove.weight()
                                && record.priority() == recordToRemove.priority()) {
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
