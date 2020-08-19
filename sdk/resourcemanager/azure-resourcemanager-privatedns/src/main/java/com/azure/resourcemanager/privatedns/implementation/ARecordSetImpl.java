// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.ARecord;
import com.azure.resourcemanager.privatedns.models.ARecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link ARecordSet}. */
class ARecordSetImpl extends PrivateDnsRecordSetImpl implements ARecordSet {
    ARecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.A.toString(), parent, innerModel);
    }

    static ARecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new ARecordSetImpl(name, parent, new RecordSetInner().withARecords(new ArrayList<>()));
    }

    @Override
    public List<String> ipv4Addresses() {
        List<String> ipv4Addresses = new ArrayList<>();
        if (inner().aRecords() != null) {
            for (ARecord aRecord : inner().aRecords()) {
                ipv4Addresses.add(aRecord.ipv4Address());
            }
        }
        return Collections.unmodifiableList(ipv4Addresses);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (inner().aRecords() != null && !inner().aRecords().isEmpty()) {
            if (resource.aRecords() == null) {
                resource.withARecords(new ArrayList<>());
            }
            resource.aRecords().addAll(inner().aRecords());
            inner().aRecords().clear();
        }
        if (!recordSetRemoveInfo.aRecords().isEmpty()) {
            if (resource.aRecords() != null) {
                for (ARecord recordToRemove : recordSetRemoveInfo.aRecords()) {
                    for (ARecord record : resource.aRecords()) {
                        if (record.ipv4Address().equalsIgnoreCase(recordToRemove.ipv4Address())) {
                            resource.aRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            recordSetRemoveInfo.aRecords().clear();
        }
        return resource;
    }
}
