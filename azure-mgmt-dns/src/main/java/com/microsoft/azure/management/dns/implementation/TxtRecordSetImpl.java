package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link TxtRecordSet}.
 */
class TxtRecordSetImpl
        extends DnsRecordSetImpl
        implements TxtRecordSet {
    TxtRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    @Override
    public List<TxtRecord> records() {
        if (this.inner().txtRecords() != null) {
            return Collections.unmodifiableList(this.inner().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<TxtRecord>());
    }

    @Override
    protected RecordSetInner merge(RecordSetInner resource, RecordSetInner recordSetRemoveInfo) {
        if (this.inner().txtRecords() != null && this.inner().txtRecords().size() > 0) {
            if (resource.txtRecords() == null) {
                resource.withTxtRecords(new ArrayList<TxtRecord>());
            }

            for (TxtRecord recordToAdd : this.inner().txtRecords()) {
                resource.txtRecords().add(recordToAdd);
            }
        }

        if (recordSetRemoveInfo.txtRecords().size() > 0) {
            if (resource.txtRecords() != null) {
                for (TxtRecord recordToRemove : recordSetRemoveInfo.txtRecords()) {
                    for (TxtRecord record : resource.txtRecords()) {
                        if (record.value().size() != 0 && record.value().get(0).equalsIgnoreCase(recordToRemove.value().get(0))) {
                            resource.txtRecords().remove(record);
                            break;
                        }
                    }
                }
            }
        }
        return resource;
    }
}


