/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of ARecordSet.
 */
@LangDefinition
class ARecordSetImpl
        extends DnsRecordSetImpl
        implements ARecordSet {
    ARecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static ARecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new ARecordSetImpl(parent,
                new RecordSetInner()
                    .withName(name)
                    .withType(RecordType.A.toString())
                    .withARecords(new ArrayList<ARecord>()));
    }

    @Override
    public List<String> ipv4Addresses() {
        List<String> ipv4Addresses = new ArrayList<>();
        if (this.inner().aRecords() != null) {
            for (ARecord aRecord : this.inner().aRecords()) {
                ipv4Addresses.add(aRecord.ipv4Address());
            }
        }
        return Collections.unmodifiableList(ipv4Addresses);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().aRecords() != null && this.inner().aRecords().size() > 0) {
            if (resource.aRecords() == null) {
                resource.withARecords(new ArrayList<ARecord>());
            }

            resource.aRecords().addAll(this.inner().aRecords());
            this.inner().aRecords().clear();
        }

        if (this.recordSetRemoveInfo.aRecords().size() > 0) {
            if (resource.aRecords() != null) {
                for (ARecord recordToRemove : this.recordSetRemoveInfo.aRecords()) {
                    for (ARecord record : resource.aRecords()) {
                        if (record.ipv4Address().equalsIgnoreCase(recordToRemove.ipv4Address())) {
                            resource.aRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.aRecords().clear();
        }
        return resource;
    }
}
