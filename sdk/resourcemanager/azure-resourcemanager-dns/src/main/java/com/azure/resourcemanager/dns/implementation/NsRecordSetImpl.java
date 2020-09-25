// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.NsRecordSet;
import com.azure.resourcemanager.dns.models.NsRecord;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation of NsRecordSet. */
class NsRecordSetImpl extends DnsRecordSetImpl implements NsRecordSet {
    NsRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.NS.toString(), parent, innerModel);
    }

    static NsRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new NsRecordSetImpl(name, parent, new RecordSetInner().withNsRecords(new ArrayList<NsRecord>()));
    }

    @Override
    public List<String> nameServers() {
        List<String> nameServers = new ArrayList<>();
        if (this.innerModel().nsRecords() != null) {
            for (NsRecord nsRecord : this.innerModel().nsRecords()) {
                nameServers.add(nsRecord.nsdname());
            }
        }
        return Collections.unmodifiableList(nameServers);
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (this.innerModel().nsRecords() != null && this.innerModel().nsRecords().size() > 0) {
            if (resource.nsRecords() == null) {
                resource.withNsRecords(new ArrayList<NsRecord>());
            }

            for (NsRecord record : this.innerModel().nsRecords()) {
                resource.nsRecords().add(record);
            }
            this.innerModel().nsRecords().clear();
        }

        if (this.recordSetRemoveInfo.nsRecords().size() > 0) {
            if (resource.nsRecords() != null) {
                for (NsRecord recordToRemove : this.recordSetRemoveInfo.nsRecords()) {
                    for (NsRecord record : resource.nsRecords()) {
                        if (record.nsdname().equalsIgnoreCase(recordToRemove.nsdname())) {
                            resource.nsRecords().remove(record);
                            break;
                        }
                    }
                }
            }
            this.recordSetRemoveInfo.nsRecords().clear();
        }
        return resource;
    }
}
