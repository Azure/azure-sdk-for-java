// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.PtrRecord;
import com.azure.resourcemanager.privatedns.models.PtrRecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link PtrRecordSet}. */
class PtrRecordSetImpl extends PrivateDnsRecordSetImpl implements PtrRecordSet {
    PtrRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.PTR.toString(), parent, innerModel);
    }

    static PtrRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new PtrRecordSetImpl(name, parent, new RecordSetInner().withPtrRecords(new ArrayList<>()));
    }

    @Override
    public List<String> targetDomainNames() {
        List<String> targetDomainNames = new ArrayList<>();
        if (innerModel().ptrRecords() != null) {
            for (PtrRecord ptrRecord : this.innerModel().ptrRecords()) {
                targetDomainNames.add(ptrRecord.ptrdname());
            }
        }
        return Collections.unmodifiableList(targetDomainNames);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (innerModel().ptrRecords() != null && !innerModel().ptrRecords().isEmpty()) {
            if (resource.ptrRecords() == null) {
                resource.withPtrRecords(new ArrayList<>());
            }
            resource.ptrRecords().addAll(innerModel().ptrRecords());
            innerModel().ptrRecords().clear();
        }
        if (!recordSetRemoveInfo.ptrRecords().isEmpty()) {
            if (resource.ptrRecords() != null) {
                for (PtrRecord recordToRemove : recordSetRemoveInfo.ptrRecords()) {
                    for (PtrRecord record : resource.ptrRecords()) {
                        if (record.ptrdname().equalsIgnoreCase(recordToRemove.ptrdname())) {
                            resource.ptrRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            recordSetRemoveInfo.ptrRecords().clear();
        }
        return resource;
    }
}
