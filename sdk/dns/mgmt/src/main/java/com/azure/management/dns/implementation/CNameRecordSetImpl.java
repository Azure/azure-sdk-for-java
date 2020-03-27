/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.CnameRecord;
import com.azure.management.dns.CNameRecordSet;
import com.azure.management.dns.RecordType;

/**
 * Implementation of CNameRecordSet.
 */
class CNameRecordSetImpl
        extends DnsRecordSetImpl
        implements CNameRecordSet {
    CNameRecordSetImpl(final String name, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, RecordType.CNAME.toString(), parent, innerModel);
    }

    static CNameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new CNameRecordSetImpl(name, parent,
                new RecordSetInner()
                        .withCnameRecord(new CnameRecord()));
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
