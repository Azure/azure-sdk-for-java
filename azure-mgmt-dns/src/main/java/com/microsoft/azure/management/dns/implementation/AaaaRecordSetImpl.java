/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.AaaaRecord;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of AaaaRecordSet.
 */
@LangDefinition
class AaaaRecordSetImpl
        extends DnsRecordSetImpl
        implements AaaaRecordSet {

    AaaaRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static AaaaRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new AaaaRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.AAAA.toString())
                        .withAaaaRecords(new ArrayList<AaaaRecord>()));
    }

    @Override
    public List<String> ipv6Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        if (this.inner().aaaaRecords() != null) {
            for (AaaaRecord aaaaRecord : this.inner().aaaaRecords()) {
                ipv6Addresses.add(aaaaRecord.ipv6Address());
            }
        }
        return Collections.unmodifiableList(ipv6Addresses);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.inner().aaaaRecords() != null && this.inner().aaaaRecords().size() > 0) {
            if (resource.aaaaRecords() == null) {
                resource.withAaaaRecords(new ArrayList<AaaaRecord>());
            }

            resource.aaaaRecords().addAll(this.inner().aaaaRecords());
            this.inner().aaaaRecords().clear();
        }

        if (this.recordSetRemoveInfo.aaaaRecords().size() > 0) {
            if (resource.aaaaRecords() != null) {
                for (AaaaRecord recordToRemove : this.recordSetRemoveInfo.aaaaRecords()) {
                    for (AaaaRecord record : resource.aaaaRecords()) {
                        if (record.ipv6Address().equalsIgnoreCase(recordToRemove.ipv6Address())) {
                            resource.aaaaRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.aaaaRecords().clear();
        }
        return resource;
    }
}
