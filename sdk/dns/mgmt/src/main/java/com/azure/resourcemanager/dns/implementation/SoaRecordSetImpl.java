// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.SoaRecord;
import com.azure.resourcemanager.dns.models.SoaRecordSet;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;

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
        return this.inner().soaRecord();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (resource.soaRecord() == null) {
            resource.withSoaRecord(new SoaRecord());
        }

        if (this.inner().soaRecord().email() != null) {
            resource.soaRecord().withEmail(this.inner().soaRecord().email());
        }

        if (this.inner().soaRecord().expireTime() != null) {
            resource.soaRecord().withExpireTime(this.inner().soaRecord().expireTime());
        }

        if (this.inner().soaRecord().minimumTtl() != null) {
            resource.soaRecord().withMinimumTtl(this.inner().soaRecord().minimumTtl());
        }

        if (this.inner().soaRecord().refreshTime() != null) {
            resource.soaRecord().withRefreshTime(this.inner().soaRecord().refreshTime());
        }

        if (this.inner().soaRecord().retryTime() != null) {
            resource.soaRecord().withRetryTime(this.inner().soaRecord().retryTime());
        }

        if (this.inner().soaRecord().serialNumber() != null) {
            resource.soaRecord().withSerialNumber(this.inner().soaRecord().serialNumber());
        }

        this.inner().withSoaRecord(new SoaRecord());
        return resource;
    }
}
