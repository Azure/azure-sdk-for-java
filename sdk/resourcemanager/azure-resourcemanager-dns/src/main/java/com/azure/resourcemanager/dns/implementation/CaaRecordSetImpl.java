// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.CaaRecord;
import com.azure.resourcemanager.dns.models.CaaRecordSet;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of CaaRecordSet. */
class CaaRecordSetImpl extends DnsRecordSetImpl implements CaaRecordSet {
    CaaRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.CAA.toString(), parent, innerModel);
    }

    static CaaRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new CaaRecordSetImpl(name, parent, new RecordSetInner().withCaaRecords(new ArrayList<>()));
    }

    @Override
    public List<CaaRecord> records() {
        if (this.innerModel().caaRecords() != null) {
            return Collections.unmodifiableList(this.innerModel().caaRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.innerModel().caaRecords() != null && this.innerModel().caaRecords().size() > 0) {
            if (resource.caaRecords() == null) {
                resource.withCaaRecords(new ArrayList<>());
            }

            resource.caaRecords().addAll(this.innerModel().caaRecords());
            this.innerModel().caaRecords().clear();
        }

        if (this.recordSetRemoveInfo.caaRecords().size() > 0) {
            if (resource.caaRecords() != null) {
                for (CaaRecord recordToRemove : this.recordSetRemoveInfo.caaRecords()) {
                    for (CaaRecord record : resource.caaRecords()) {
                        if (record.value().equalsIgnoreCase(recordToRemove.value())
                            && (record.flags().intValue() == recordToRemove.flags().intValue())
                            && (record.tag().equalsIgnoreCase(recordToRemove.tag()))) {
                            resource.caaRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.caaRecords().clear();
        }
        return resource;
    }
}
