// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.CNameRecordSet;
import com.azure.resourcemanager.dns.models.CnameRecord;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;

/** Implementation of CNameRecordSet. */
class CNameRecordSetImpl extends DnsRecordSetImpl implements CNameRecordSet {
    CNameRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.CNAME.toString(), parent, innerModel);
    }

    static CNameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new CNameRecordSetImpl(name, parent, new RecordSetInner().withCnameRecord(new CnameRecord()));
    }

    @Override
    public String canonicalName() {
        if (this.inner().cnameRecord() != null) {
            return this.inner().cnameRecord().cname();
        }
        return null;
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }
}
