// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SrvRecord;
import com.azure.resourcemanager.privatedns.models.SrvRecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of {@link SrvRecordSet}. */
class SrvRecordSetImpl extends PrivateDnsRecordSetImpl implements SrvRecordSet {
    SrvRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.SRV.toString(), parent, innerModel);
    }

    static SrvRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new SrvRecordSetImpl(name, parent, new RecordSetInner().withSrvRecords(new ArrayList<>()));
    }

    @Override
    public List<SrvRecord> records() {
        if (innerModel().srvRecords() != null) {
            return Collections.unmodifiableList(innerModel().srvRecords());
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (innerModel().srvRecords() != null && innerModel().srvRecords().size() > 0) {
            if (resource.srvRecords() == null) {
                resource.withSrvRecords(new ArrayList<>());
            }
            resource.srvRecords().addAll(innerModel().srvRecords());
            innerModel().srvRecords().clear();
        }
        if (recordSetRemoveInfo.srvRecords().size() > 0) {
            if (resource.srvRecords() != null) {
                for (SrvRecord recordToRemove : recordSetRemoveInfo.srvRecords()) {
                    for (SrvRecord record : resource.srvRecords()) {
                        if (record.target().equalsIgnoreCase(recordToRemove.target())
                            && (record.port().intValue() == recordToRemove.port().intValue())
                            && (record.weight().intValue() == recordToRemove.weight().intValue())
                            && (record.priority().intValue() == recordToRemove.priority().intValue())) {
                            resource.srvRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            recordSetRemoveInfo.srvRecords().clear();
        }
        return resource;
    }
}
