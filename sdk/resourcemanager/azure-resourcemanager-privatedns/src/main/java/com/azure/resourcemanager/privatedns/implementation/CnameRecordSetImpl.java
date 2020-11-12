// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.CnameRecord;
import com.azure.resourcemanager.privatedns.models.CnameRecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link CnameRecordSet}. */
class CnameRecordSetImpl extends PrivateDnsRecordSetImpl implements CnameRecordSet {
    CnameRecordSetImpl(final String name, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.CNAME.toString(), parent, innerModel);
    }

    static CnameRecordSetImpl newRecordSet(final String name, final PrivateDnsZoneImpl parent) {
        return new CnameRecordSetImpl(name, parent, new RecordSetInner().withCnameRecord(new CnameRecord()));
    }

    @Override
    public String canonicalName() {
        if (innerModel().cnameRecord() == null) {
            return null;
        }
        return innerModel().cnameRecord().cname();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }
}
