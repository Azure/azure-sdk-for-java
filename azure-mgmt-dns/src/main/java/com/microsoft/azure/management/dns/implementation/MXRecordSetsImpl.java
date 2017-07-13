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
import rx.Observable;

/**
 * Implementation of MXRecordSets.
 */
@LangDefinition
class MXRecordSetsImpl
        extends DnsRecordSetsBaseImpl<MXRecordSet, MXRecordSetImpl>
        implements MXRecordSets {

    MXRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.MX);
    }

    @Override
    public MXRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType);
        return new MXRecordSetImpl(this.dnsZone, inner);
    }

    @Override
    public PagedList<MXRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected PagedList<MXRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected Observable<MXRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected MXRecordSetImpl wrapModel(RecordSetInner inner) {
        return new MXRecordSetImpl(this.dnsZone, inner);
    }
}