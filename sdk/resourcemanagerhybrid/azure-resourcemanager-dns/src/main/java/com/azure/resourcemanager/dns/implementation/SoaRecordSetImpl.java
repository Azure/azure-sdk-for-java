// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.SoaRecord;
import com.azure.resourcemanager.dns.models.SoaRecordSet;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;

/** Implementation of SoaRecordSet. */
class SoaRecordSetImpl extends DnsRecordSetImpl implements SoaRecordSet {
    SoaRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.SOA.toString(), parent, innerModel);
    }

    static SoaRecordSetImpl newRecordSet(final DnsZoneImpl parent) {
        return new SoaRecordSetImpl("@", parent, new RecordSetInner().withSoaRecord(new SoaRecord()));
    }

    @Override
    public SoaRecord record() {
        return this.innerModel().soaRecord();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (resource.soaRecord() == null) {
            resource.withSoaRecord(new SoaRecord());
        }

        if (this.innerModel().soaRecord().email() != null) {
            resource.soaRecord().withEmail(this.innerModel().soaRecord().email());
        }

        if (this.innerModel().soaRecord().expireTime() != null) {
            resource.soaRecord().withExpireTime(this.innerModel().soaRecord().expireTime());
        }

        if (this.innerModel().soaRecord().minimumTtl() != null) {
            resource.soaRecord().withMinimumTtl(this.innerModel().soaRecord().minimumTtl());
        }

        if (this.innerModel().soaRecord().refreshTime() != null) {
            resource.soaRecord().withRefreshTime(this.innerModel().soaRecord().refreshTime());
        }

        if (this.innerModel().soaRecord().retryTime() != null) {
            resource.soaRecord().withRetryTime(this.innerModel().soaRecord().retryTime());
        }

        if (this.innerModel().soaRecord().serialNumber() != null) {
            resource.soaRecord().withSerialNumber(this.innerModel().soaRecord().serialNumber());
        }

        this.innerModel().withSoaRecord(new SoaRecord());
        return resource;
    }
}
