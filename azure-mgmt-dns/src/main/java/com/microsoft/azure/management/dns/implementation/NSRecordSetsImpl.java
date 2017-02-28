/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.NSRecordSet;
import com.microsoft.azure.management.dns.NSRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * Implementation of NSRecordSets.
 */
@LangDefinition
class NSRecordSetsImpl
        extends ReadableWrappersImpl<NSRecordSet, NSRecordSetImpl, RecordSetInner>
        implements NSRecordSets {

    private final DnsZoneImpl dnsZone;

    NSRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public NSRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.NS);
        return new NSRecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<NSRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.NS));
    }

    @Override
    protected NSRecordSetImpl wrapModel(RecordSetInner inner) {
        return new NSRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<NSRecordSet> listAsync() {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.NS));
    }
}