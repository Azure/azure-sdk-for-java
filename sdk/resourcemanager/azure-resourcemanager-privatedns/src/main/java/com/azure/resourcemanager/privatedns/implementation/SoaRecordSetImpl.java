// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
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
        return innerModel().soaRecord();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (resource.soaRecord() == null) {
            resource.withSoaRecord(new SoaRecord());
        }
        if (innerModel().soaRecord().email() != null) {
            resource.soaRecord().withEmail(innerModel().soaRecord().email());
        }
        if (innerModel().soaRecord().expireTime() != null) {
            resource.soaRecord().withExpireTime(innerModel().soaRecord().expireTime());
        }
        if (innerModel().soaRecord().minimumTtl() != null) {
            resource.soaRecord().withMinimumTtl(innerModel().soaRecord().minimumTtl());
        }
        if (innerModel().soaRecord().refreshTime() != null) {
            resource.soaRecord().withRefreshTime(innerModel().soaRecord().refreshTime());
        }
        if (innerModel().soaRecord().retryTime() != null) {
            resource.soaRecord().withRetryTime(innerModel().soaRecord().retryTime());
        }
        if (innerModel().soaRecord().serialNumber() != null) {
            resource.soaRecord().withSerialNumber(innerModel().soaRecord().serialNumber());
        }
        innerModel().withSoaRecord(new SoaRecord());
        return resource;
    }
}
