// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.TxtRecord;
import com.azure.resourcemanager.dns.models.TxtRecordSet;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of TxtRecordSet. */
class TxtRecordSetImpl extends DnsRecordSetImpl implements TxtRecordSet {
    TxtRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.TXT.toString(), parent, innerModel);
    }

    static TxtRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new TxtRecordSetImpl(name, parent, new RecordSetInner().withTxtRecords(new ArrayList<>()));
    }

    @Override
    public List<TxtRecord> records() {
        if (this.innerModel().txtRecords() != null) {
            return Collections.unmodifiableList(this.innerModel().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.innerModel().txtRecords() != null && this.innerModel().txtRecords().size() > 0) {
            if (resource.txtRecords() == null) {
                resource.withTxtRecords(new ArrayList<>());
            }

            resource.txtRecords().addAll(this.innerModel().txtRecords());
            this.innerModel().txtRecords().clear();
        }

        if (this.recordSetRemoveInfo.txtRecords().size() > 0) {
            if (resource.txtRecords() != null) {
                for (TxtRecord recordToRemove : this.recordSetRemoveInfo.txtRecords()) {
                    for (TxtRecord record : resource.txtRecords()) {
                        boolean exists = record.value().equals(recordToRemove.value());
                        if (exists) {
                            resource.txtRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.txtRecords().clear();
        }
        return resource;
    }
}
