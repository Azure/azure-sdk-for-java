/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.RecordType;
import com.azure.management.dns.TxtRecord;
import com.azure.management.dns.TxtRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of TxtRecordSet.
 */
class TxtRecordSetImpl
        extends DnsRecordSetImpl
        implements TxtRecordSet {
    TxtRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.TXT.toString(), parent, innerModel);
    }

    static TxtRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new TxtRecordSetImpl(name, parent,
                new RecordSetInner()
                        .withTxtRecords(new ArrayList<>()));
    }

    @Override
    public List<TxtRecord> records() {
        if (this.inner().txtRecords() != null) {
            return Collections.unmodifiableList(this.inner().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().txtRecords() != null && this.inner().txtRecords().size() > 0) {
            if (resource.txtRecords() == null) {
                resource.withTxtRecords(new ArrayList<>());
            }

            resource.txtRecords().addAll(this.inner().txtRecords());
            this.inner().txtRecords().clear();
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


