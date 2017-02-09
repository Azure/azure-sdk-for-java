/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.CnameRecord;
import com.microsoft.azure.management.dns.CNameRecordSet;
import com.microsoft.azure.management.dns.RecordType;

/**
 * Implementation of CNameRecordSet.
 */
@LangDefinition
class CNameRecordSetImpl
        extends DnsRecordSetImpl
        implements CNameRecordSet {
    CNameRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(parent, innerModel);
    }

    static CNameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent) {
        return new CNameRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.CNAME.toString())
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
