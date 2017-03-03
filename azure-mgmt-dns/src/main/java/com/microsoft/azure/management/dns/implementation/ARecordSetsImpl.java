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
import rx.Observable;

/**
 * Implementation of ARecordSets.
 */
@LangDefinition
class ARecordSetsImpl
        extends ReadableWrappersImpl<ARecordSet, ARecordSetImpl, RecordSetInner>
        implements ARecordSets {

    private final DnsZoneImpl dnsZone;

    ARecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public ARecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.A);
        return new ARecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<ARecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(this.parent().resourceGroupName(),
                this.parent().name(),
                RecordType.A));
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<ARecordSet> listAsync() {
        return super.wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                RecordType.A));
    }
}
