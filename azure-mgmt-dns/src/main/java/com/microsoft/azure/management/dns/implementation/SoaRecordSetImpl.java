/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SoaRecord;
import com.microsoft.azure.management.dns.SoaRecordSet;

/**
 * Implementation of SoaRecordSet.
 */
@LangDefinition
class SoaRecordSetImpl
        extends DnsRecordSetImpl
        implements SoaRecordSet {
    SoaRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static SoaRecordSetImpl newRecordSet(final DnsZoneImpl parent) {
        return new SoaRecordSetImpl(parent,
                new RecordSetInner()
                        .withName("@")
                        .withType(RecordType.SOA.toString())
                        .withSoaRecord(new SoaRecord()));
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
