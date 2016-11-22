/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link AaaaRecordSets}.
 */
@LangDefinition
class AaaaRecordSetsImpl
        extends ReadableWrappersImpl<AaaaRecordSet, AaaaRecordSetImpl, RecordSetInner>
        implements AaaaRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public AaaaRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.AAAA);
        return new AaaaRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<AaaaRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.AAAA));
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        return new AaaaRecordSetImpl(this.dnsZone, inner, this.client);
    }
}
