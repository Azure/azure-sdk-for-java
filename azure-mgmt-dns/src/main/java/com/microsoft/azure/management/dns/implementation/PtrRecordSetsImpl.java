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
import rx.Observable;

/**
 * Implementation of PtrRecordSets.
 */
@LangDefinition
class PtrRecordSetsImpl
        extends DnsRecordSetsBaseImpl<PtrRecordSet, PtrRecordSetImpl>
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
    protected PagedList<PtrRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.parent().resourceGroupName(),
                this.parent().name(),
                RecordType.PTR,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected Observable<PtrRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                RecordType.PTR));
    }

    @Override
    protected PtrRecordSetImpl wrapModel(RecordSetInner inner) {
        return new PtrRecordSetImpl(this.parent(), inner);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }
}