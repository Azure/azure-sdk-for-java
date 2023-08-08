// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.AaaaRecord;
import com.azure.resourcemanager.dns.models.AaaaRecordSet;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of AaaaRecordSet. */
class AaaaRecordSetImpl extends DnsRecordSetImpl implements AaaaRecordSet {

    AaaaRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.AAAA.toString(), parent, innerModel);
    }

    static AaaaRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new AaaaRecordSetImpl(name, parent, new RecordSetInner().withAaaaRecords(new ArrayList<>()));
    }

    @Override
    public List<String> ipv6Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        if (this.innerModel().aaaaRecords() != null) {
            for (AaaaRecord aaaaRecord : this.innerModel().aaaaRecords()) {
                ipv6Addresses.add(aaaaRecord.ipv6Address());
            }
        }
        return Collections.unmodifiableList(ipv6Addresses);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.innerModel().aaaaRecords() != null && this.innerModel().aaaaRecords().size() > 0) {
            if (resource.aaaaRecords() == null) {
                resource.withAaaaRecords(new ArrayList<>());
            }

            resource.aaaaRecords().addAll(this.innerModel().aaaaRecords());
            this.innerModel().aaaaRecords().clear();
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
