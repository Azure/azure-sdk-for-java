package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.MxRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link MxRecordSet}.
 */
class MxRecordSetImpl
        extends DnsRecordSetImpl
        implements MxRecordSet {
    MxRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<MxRecord> records() {
        if (this.inner().mxRecords() != null) {
            return Collections.unmodifiableList(this.inner().mxRecords());
        }
        return Collections.unmodifiableList(new ArrayList<MxRecord>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().mxRecords() != null && this.inner().mxRecords().size() > 0) {
            if (resource.mxRecords() == null) {
                resource.withMxRecords(new ArrayList<MxRecord>());
            }

            for (MxRecord recordToAdd : this.inner().mxRecords()) {
                resource.mxRecords().add(recordToAdd);
            }
            this.inner().mxRecords().clear();
        }

        if (this.recordSetRemoveInfo.mxRecords().size() > 0) {
            if (resource.mxRecords() != null) {
                for (MxRecord recordToRemove : this.recordSetRemoveInfo.mxRecords()) {
                    for (MxRecord record : resource.mxRecords()) {
                        if (record.exchange().equalsIgnoreCase(recordToRemove.exchange()) && record.preference() == recordToRemove.preference()) {
                            resource.mxRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.mxRecords().clear();
        }
        return resource;
    }
}

