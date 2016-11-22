/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.NsRecordSet;
import com.microsoft.azure.management.dns.NsRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link NsRecordSets}.
 */
@LangDefinition
class NsRecordSetsImpl
        extends ReadableWrappersImpl<NsRecordSet, NsRecordSetImpl, RecordSetInner>
        implements NsRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    NsRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public NsRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.NS);
        return new NsRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<NsRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.NS));
    }

    @Override
    protected NsRecordSetImpl wrapModel(RecordSetInner inner) {
        return new NsRecordSetImpl(this.dnsZone, inner, this.client);
    }
}