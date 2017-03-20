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
import rx.Observable;

/**
 * Implementation of MXRecordSets.
 */
@LangDefinition
class MXRecordSetsImpl
        extends ReadableWrappersImpl<MXRecordSet, MXRecordSetImpl, RecordSetInner>
        implements MXRecordSets {

    private final DnsZoneImpl dnsZone;

    MXRecordSetsImpl(DnsZoneImpl dnsZone) {
        this.dnsZone = dnsZone;
    }

    @Override
    public MXRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(this.parent().resourceGroupName(),
                this.parent().name(),
                name,
                RecordType.MX);
        return new MXRecordSetImpl(this.parent(), inner);
    }

    @Override
    public PagedList<MXRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(), this.parent().name(), RecordType.MX));
    }

    @Override
    protected MXRecordSetImpl wrapModel(RecordSetInner inner) {
        return new MXRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Observable<MXRecordSet> listAsync() {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.parent().resourceGroupName(),
                this.parent().name(), RecordType.MX));
    }
}