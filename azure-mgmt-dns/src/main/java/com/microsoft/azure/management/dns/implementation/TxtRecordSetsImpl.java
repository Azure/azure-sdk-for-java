/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.dns.TxtRecordSets;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link TxtRecordSets}.
 */
@LangDefinition
class TxtRecordSetsImpl
        extends ReadableWrappersImpl<TxtRecordSet, TxtRecordSetImpl, RecordSetInner>
        implements TxtRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    TxtRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public TxtRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.TXT);
        return new TxtRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<TxtRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.TXT));
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        return new TxtRecordSetImpl(this.dnsZone, inner, this.client);
    }
}