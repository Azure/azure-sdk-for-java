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
import rx.Observable;

/**
 * Implementation of AaaaRecordSets.
 */
@LangDefinition
class AaaaRecordSetsImpl
        extends ReadableWrappersImpl<AaaaRecordSet, AaaaRecordSetImpl, RecordSetInner>
        implements AaaaRecordSets {

    private final DnsZoneImpl dnsZone;

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public AaaaRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                RecordType.AAAA);
        return new AaaaRecordSetImpl(this.dnsZone, inner);
    }

    @Override
    public PagedList<AaaaRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.AAAA));
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        return new AaaaRecordSetImpl(this.dnsZone, inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<AaaaRecordSet> listAsync() {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.AAAA));
    }
}
