/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.CnameRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link CnameRecordSets}.
 */
@LangDefinition
class CnameRecordSetsImpl
        extends ReadableWrappersImpl<CnameRecordSet, CnameRecordSetImpl, RecordSetInner>
        implements CnameRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    CnameRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public CnameRecordSet getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.CNAME);
        return new CnameRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<CnameRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.CNAME));
    }

    @Override
    protected CnameRecordSetImpl wrapModel(RecordSetInner inner) {
        return new CnameRecordSetImpl(this.dnsZone, inner, this.client);
    }
}