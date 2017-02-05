/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.MXRecordSet;
import com.microsoft.azure.management.dns.MXRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * Implementation of {@link MXRecordSets}.
 */
@LangDefinition
class MXRecordSetsImpl
        extends ReadableWrappersImpl<MXRecordSet, MXRecordSetImpl, RecordSetInner>
        implements MXRecordSets {

    private final DnsZoneImpl dnsZone;
    private final RecordSetsInner client;

    MXRecordSetsImpl(DnsZoneImpl dnsZone, RecordSetsInner client) {
        this.dnsZone = dnsZone;
        this.client = client;
    }

    @Override
    public MXRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.client.get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.MX);
        return new MXRecordSetImpl(this.dnsZone, inner, this.client);
    }

    @Override
    public PagedList<MXRecordSet> list() {
        return super.wrapList(this.client.listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), RecordType.MX));
    }

    @Override
    protected MXRecordSetImpl wrapModel(RecordSetInner inner) {
        return new MXRecordSetImpl(this.dnsZone, inner, this.client);
    }
}