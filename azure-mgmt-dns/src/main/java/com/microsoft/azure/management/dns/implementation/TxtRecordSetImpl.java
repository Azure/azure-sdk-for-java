/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link TxtRecordSet}.
 */
@LangDefinition
class TxtRecordSetImpl
        extends DnsRecordSetImpl
        implements TxtRecordSet {
    TxtRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parent, innerModel, client);
    }

    static TxtRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent, final RecordSetsInner client) {
        return new TxtRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.TXT.toString())
                        .withTxtRecords(new ArrayList<TxtRecord>()),
                    client);
    }

    @Override
    public List<TxtRecord> records() {
        if (this.inner().txtRecords() != null) {
            return Collections.unmodifiableList(this.inner().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<TxtRecord>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().txtRecords() != null && this.inner().txtRecords().size() > 0) {
            if (resource.txtRecords() == null) {
                resource.withTxtRecords(new ArrayList<TxtRecord>());
            }

            resource.txtRecords().addAll(this.inner().txtRecords());
            this.inner().txtRecords().clear();
        }

        if (this.recordSetRemoveInfo.txtRecords().size() > 0) {
            if (resource.txtRecords() != null) {
                for (TxtRecord recordToRemove : this.recordSetRemoveInfo.txtRecords()) {
                    for (TxtRecord record : resource.txtRecords()) {
                        if (record.value().size() != 0 && record.value().get(0).equalsIgnoreCase(recordToRemove.value().get(0))) {
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


