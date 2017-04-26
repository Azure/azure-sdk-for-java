/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.PtrRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * Implementation of PtrRecordSets.
 */
@LangDefinition
class PtrRecordSetsImpl
        extends ReadableWrappersImpl<PtrRecordSet, PtrRecordSetImpl, RecordSetInner>
        implements PtrRecordSets {

    private final DnsZoneImpl dnsZone;

    PtrRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public PtrRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.PTR);
        return new PtrRecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<PtrRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.PTR));
    }

    @Override
    protected PtrRecordSetImpl wrapModel(RecordSetInner inner) {
        return new PtrRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<PtrRecordSet> listAsync() {
        return super.wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.PTR));
    }
}