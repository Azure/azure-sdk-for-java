// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.MxRecord;
import com.azure.resourcemanager.privatedns.models.MxRecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link MxRecordSet}. */
class MxRecordSetImpl extends PrivateDnsRecordSetImpl implements MxRecordSet {
    MxRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.MX.toString(), parent, innerModel);
    }

    static MxRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new MxRecordSetImpl(name, parent, new RecordSetInner().withMxRecords(new ArrayList<>()));
    }

    @Override
    public List<MxRecord> records() {
        if (innerModel().mxRecords() != null) {
            return Collections.unmodifiableList(innerModel().mxRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (innerModel().mxRecords() != null && !innerModel().mxRecords().isEmpty()) {
            if (resource.mxRecords() == null) {
                resource.withMxRecords(new ArrayList<>());
            }
            resource.mxRecords().addAll(innerModel().mxRecords());
            innerModel().mxRecords().clear();
        }

        if (!recordSetRemoveInfo.mxRecords().isEmpty()) {
            if (resource.mxRecords() != null) {
                for (MxRecord recordToRemove : recordSetRemoveInfo.mxRecords()) {
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
            recordSetRemoveInfo.mxRecords().clear();
        }
        return resource;
    }
}
