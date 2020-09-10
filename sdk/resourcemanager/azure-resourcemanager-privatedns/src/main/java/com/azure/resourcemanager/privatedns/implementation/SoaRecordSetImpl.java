// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SoaRecord;
import com.azure.resourcemanager.privatedns.models.SoaRecordSet;

/** Implementation of {@link SoaRecordSet}. */
class SoaRecordSetImpl extends PrivateDnsRecordSetImpl implements SoaRecordSet {
    SoaRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.SOA.toString(), parent, innerModel);
    }

    static SoaRecordSetImpl newRecordSet(final PrivateDnsZoneImpl parent) {
        return new SoaRecordSetImpl("@", parent, new RecordSetInner().withSoaRecord(new SoaRecord()));
    }

    @Override
    public SoaRecord record() {
        return inner().soaRecord();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (resource.soaRecord() == null) {
            resource.withSoaRecord(new SoaRecord());
        }
        if (inner().soaRecord().email() != null) {
            resource.soaRecord().withEmail(inner().soaRecord().email());
        }
        if (inner().soaRecord().expireTime() != null) {
            resource.soaRecord().withExpireTime(inner().soaRecord().expireTime());
        }
        if (inner().soaRecord().minimumTtl() != null) {
            resource.soaRecord().withMinimumTtl(inner().soaRecord().minimumTtl());
        }
        if (inner().soaRecord().refreshTime() != null) {
            resource.soaRecord().withRefreshTime(inner().soaRecord().refreshTime());
        }
        if (inner().soaRecord().retryTime() != null) {
            resource.soaRecord().withRetryTime(inner().soaRecord().retryTime());
        }
        if (inner().soaRecord().serialNumber() != null) {
            resource.soaRecord().withSerialNumber(inner().soaRecord().serialNumber());
        }
        inner().withSoaRecord(new SoaRecord());
        return resource;
    }
}
