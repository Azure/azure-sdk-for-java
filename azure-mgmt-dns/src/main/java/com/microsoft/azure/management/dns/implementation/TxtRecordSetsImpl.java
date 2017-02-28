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
import rx.Observable;

/**
 * Implementation of TxtRecordSets.
 */
@LangDefinition
class TxtRecordSetsImpl
        extends ReadableWrappersImpl<TxtRecordSet, TxtRecordSetImpl, RecordSetInner>
        implements TxtRecordSets {

    private final DnsZoneImpl dnsZone;

    TxtRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public TxtRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.TXT);
        return new TxtRecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<TxtRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.TXT));
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        return new TxtRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<TxtRecordSet> listAsync() {
        return super.wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.TXT));
    }
}