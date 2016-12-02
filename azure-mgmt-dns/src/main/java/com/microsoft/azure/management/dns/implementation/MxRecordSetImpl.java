/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.MxRecordSet;
import com.microsoft.azure.management.dns.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link MxRecordSet}.
 */
@LangDefinition
class MxRecordSetImpl
        extends DnsRecordSetImpl
        implements MxRecordSet {
    MxRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parent, innerModel, client);
    }

    static MxRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent, final RecordSetsInner client) {
        return new MxRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.MX.toString())
                        .withMxRecords(new ArrayList<MxRecord>()),
                client);
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

            resource.mxRecords().addAll(this.inner().mxRecords());
            this.inner().mxRecords().clear();
        }

        if (this.recordSetRemoveInfo.mxRecords().size() > 0) {
            if (resource.mxRecords() != null) {
                for (MxRecord recordToRemove : this.recordSetRemoveInfo.mxRecords()) {
                    for (MxRecord record : resource.mxRecords()) {
                        if (record.exchange().equalsIgnoreCase(recordToRemove.exchange())
                                && (record.preference() == recordToRemove.preference())) {
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

