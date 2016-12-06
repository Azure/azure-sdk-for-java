/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.ARecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link ARecordSets}.
 */
@LangDefinition
class ARecordSetsImpl
        extends ReadableWrappersImpl<ARecordSet, ARecordSetImpl, RecordSetInner>
        implements ARecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    ARecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public ARecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.A);
        return new ARecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<ARecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.A));
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(this.dnsZone, inner, this.client);
    }
}
