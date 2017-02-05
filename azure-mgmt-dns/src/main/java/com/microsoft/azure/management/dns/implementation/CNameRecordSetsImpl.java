/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.CNameRecordSet;
import com.microsoft.azure.management.dns.CNameRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link CNameRecordSets}.
 */
@LangDefinition
class CNameRecordSetsImpl
        extends ReadableWrappersImpl<CNameRecordSet, CNameRecordSetImpl, RecordSetInner>
        implements CNameRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    CNameRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public CNameRecordSet getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.CNAME);
        return new CNameRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<CNameRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.CNAME));
    }

    @Override
    protected CNameRecordSetImpl wrapModel(RecordSetInner inner) {
        return new CNameRecordSetImpl(this.dnsZone, inner, this.client);
    }
}