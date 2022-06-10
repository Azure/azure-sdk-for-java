// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.MxRecordSet;
import com.azure.resourcemanager.dns.models.MxRecord;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of MxRecordSet. */
class MxRecordSetImpl extends DnsRecordSetImpl implements MxRecordSet {
    MxRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.MX.toString(), parent, innerModel);
    }

    static MxRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new MxRecordSetImpl(name, parent, new RecordSetInner().withMxRecords(new ArrayList<>()));
    }

    @Override
    public List<MxRecord> records() {
        if (this.innerModel().mxRecords() != null) {
            return Collections.unmodifiableList(this.innerModel().mxRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.innerModel().mxRecords() != null && this.innerModel().mxRecords().size() > 0) {
            if (resource.mxRecords() == null) {
                resource.withMxRecords(new ArrayList<>());
            }

            resource.mxRecords().addAll(this.innerModel().mxRecords());
            this.innerModel().mxRecords().clear();
        }

        if (this.recordSetRemoveInfo.mxRecords().size() > 0) {
            if (resource.mxRecords() != null) {
                for (MxRecord recordToRemove : this.recordSetRemoveInfo.mxRecords()) {
                    for (MxRecord record : resource.mxRecords()) {
                        if (record.exchange().equalsIgnoreCase(recordToRemove.exchange())
                            && (record.preference() != null
                                && record.preference().equals(recordToRemove.preference()))) {
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
