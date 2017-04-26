/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.SrvRecordSets;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * Implementation of SrvRecordSets.
 */
@LangDefinition
class SrvRecordSetsImpl
        extends ReadableWrappersImpl<SrvRecordSet, SrvRecordSetImpl, RecordSetInner>
        implements SrvRecordSets {

    private final DnsZoneImpl dnsZone;

    SrvRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public SrvRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.SRV);
        return new SrvRecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<SrvRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(),
                this.parent().name(),
                RecordType.SRV));
    }

    @Override
    protected SrvRecordSetImpl wrapModel(RecordSetInner inner) {
        return new SrvRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<SrvRecordSet> listAsync() {
        return super.wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                RecordType.SRV));
    }
}