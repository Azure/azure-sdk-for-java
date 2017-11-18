/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.PtrRecord;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link PtrRecordSet}.
 */
@LangDefinition
class PtrRecordSetImpl
        extends DnsRecordSetImpl
        implements PtrRecordSet {
    PtrRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static PtrRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new PtrRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.PTR.toString())
                        .withPtrRecords(new ArrayList<PtrRecord>()));
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
                resource.withPtrRecords(new ArrayList<PtrRecord>());
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

