/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.MxRecordSet;
import com.microsoft.azure.management.dns.MxRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link MxRecordSets}.
 */
@LangDefinition
class MxRecordSetsImpl
        extends ReadableWrappersImpl<MxRecordSet, MxRecordSetImpl, RecordSetInner>
        implements MxRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    MxRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public MxRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.MX);
        return new MxRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<MxRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.MX));
    }

    @Override
    protected MxRecordSetImpl wrapModel(RecordSetInner inner) {
        return new MxRecordSetImpl(this.dnsZone, inner, this.client);
    }
}