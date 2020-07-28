// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.PtrRecord;
import com.azure.resourcemanager.dns.models.PtrRecordSet;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link PtrRecordSet}. */
class PtrRecordSetImpl extends DnsRecordSetImpl implements PtrRecordSet {
    PtrRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.PTR.toString(), parent, innerModel);
    }

    static PtrRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new PtrRecordSetImpl(name, parent, new RecordSetInner().withPtrRecords(new ArrayList<>()));
    }

    @Override
    public List<String> targetDomainNames() {
        List<String> targetDomainNames = new ArrayList<>();
        if (this.inner().ptrRecords() != null) {
            for (PtrRecord ptrRecord : this.inner().ptrRecords()) {
                targetDomainNames.add(ptrRecord.ptrdname());
            }
        }
        return Collections.unmodifiableList(targetDomainNames);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().ptrRecords() != null && this.inner().ptrRecords().size() > 0) {
            if (resource.ptrRecords() == null) {
                resource.withPtrRecords(new ArrayList<>());
            }

            resource.ptrRecords().addAll(this.inner().ptrRecords());
            this.inner().ptrRecords().clear();
        }

        if (this.recordSetRemoveInfo.ptrRecords().size() > 0) {
            if (resource.ptrRecords() != null) {
                for (PtrRecord recordToRemove : this.recordSetRemoveInfo.ptrRecords()) {
                    for (PtrRecord record : resource.ptrRecords()) {
                        if (record.ptrdname().equalsIgnoreCase(recordToRemove.ptrdname())) {
                            resource.ptrRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.ptrRecords().clear();
        }
        return resource;
    }
}
