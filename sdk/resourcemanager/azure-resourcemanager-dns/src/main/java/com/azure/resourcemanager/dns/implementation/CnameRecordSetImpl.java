// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.CnameRecordSet;
import com.azure.resourcemanager.dns.models.CnameRecord;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;

/** Implementation of CnameRecordSet. */
class CnameRecordSetImpl extends DnsRecordSetImpl implements CnameRecordSet {
    CnameRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.CNAME.toString(), parent, innerModel);
    }

    static CnameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new CnameRecordSetImpl(name, parent, new RecordSetInner().withCnameRecord(new CnameRecord()));
    }

    @Override
    public String canonicalName() {
        if (this.innerModel().cnameRecord() != null) {
            return this.innerModel().cnameRecord().cname();
        }
        return null;
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }
}
