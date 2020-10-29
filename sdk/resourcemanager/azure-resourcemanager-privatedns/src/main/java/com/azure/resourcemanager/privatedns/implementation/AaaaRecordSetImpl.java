// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.AaaaRecord;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link AaaaRecordSet}. */
class AaaaRecordSetImpl extends PrivateDnsRecordSetImpl implements AaaaRecordSet {

    AaaaRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.AAAA.toString(), parent, innerModel);
    }

    static AaaaRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new AaaaRecordSetImpl(name, parent, new RecordSetInner().withAaaaRecords(new ArrayList<>()));
    }

    @Override
    public List<String> ipv6Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        if (innerModel().aaaaRecords() != null) {
            for (AaaaRecord aaaaRecord : innerModel().aaaaRecords()) {
                ipv6Addresses.add(aaaaRecord.ipv6Address());
            }
        }
        return Collections.unmodifiableList(ipv6Addresses);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (innerModel().aaaaRecords() != null && !innerModel().aaaaRecords().isEmpty()) {
            if (resource.aaaaRecords() == null) {
                resource.withAaaaRecords(new ArrayList<>());
            }
            resource.aaaaRecords().addAll(innerModel().aaaaRecords());
            innerModel().aaaaRecords().clear();
        }
        if (!recordSetRemoveInfo.aaaaRecords().isEmpty()) {
            if (resource.aaaaRecords() != null) {
                for (AaaaRecord recordToRemove : recordSetRemoveInfo.aaaaRecords()) {
                    for (AaaaRecord record : resource.aaaaRecords()) {
                        if (record.ipv6Address().equalsIgnoreCase(recordToRemove.ipv6Address())) {
                            resource.aaaaRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            recordSetRemoveInfo.aaaaRecords().clear();
        }
        return resource;
    }
}
