/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.CnameRecord;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.RecordType;

/**
 * Implementation of {@link CnameRecordSet}.
 */
@LangDefinition
class CnameRecordSetImpl
        extends DnsRecordSetImpl
        implements CnameRecordSet {
    CnameRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parent, innerModel, client);
    }

    static CnameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parent, final RecordSetsInner client) {
        return new CnameRecordSetImpl(parent,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.CNAME.toString())
                        .withCnameRecord(new CnameRecord()),
                client);
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
