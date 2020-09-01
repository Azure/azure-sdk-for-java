// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.TxtRecord;
import com.azure.resourcemanager.privatedns.models.TxtRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link TxtRecordSet}. */
class TxtRecordSetImpl extends PrivateDnsRecordSetImpl implements TxtRecordSet {
    TxtRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.TXT.toString(), parent, innerModel);
    }

    static TxtRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new TxtRecordSetImpl(name, parent, new RecordSetInner().withTxtRecords(new ArrayList<>()));
    }

    @Override
    public List<TxtRecord> records() {
        if (inner().txtRecords() != null) {
            return Collections.unmodifiableList(inner().txtRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (inner().txtRecords() != null && inner().txtRecords().size() > 0) {
            if (resource.txtRecords() == null) {
                resource.withTxtRecords(new ArrayList<>());
            }
            resource.txtRecords().addAll(inner().txtRecords());
            inner().txtRecords().clear();
        }
        if (recordSetRemoveInfo.txtRecords().size() > 0) {
            if (resource.txtRecords() != null) {
                for (TxtRecord recordToRemove : recordSetRemoveInfo.txtRecords()) {
                    for (TxtRecord record : resource.txtRecords()) {
                        boolean exists = record.value().equals(recordToRemove.value());
                        if (exists) {
                            resource.txtRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            recordSetRemoveInfo.txtRecords().clear();
        }
        return resource;
    }
}
