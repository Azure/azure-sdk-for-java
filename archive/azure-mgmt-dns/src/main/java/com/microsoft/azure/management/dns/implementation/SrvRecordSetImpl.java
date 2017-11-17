/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.SrvRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of SrvRecordSet.
 */
@LangDefinition
class SrvRecordSetImpl
        extends DnsRecordSetImpl
        implements SrvRecordSet {
    SrvRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static SrvRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new SrvRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.SRV.toString())
                        .withSrvRecords(new ArrayList<SrvRecord>()));
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
