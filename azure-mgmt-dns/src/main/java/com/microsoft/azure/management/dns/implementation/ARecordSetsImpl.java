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
import rx.Observable;

/**
 * Implementation of ARecordSets.
 */
@LangDefinition
class ARecordSetsImpl
        extends DnsRecordSetsBaseImpl<ARecordSet, ARecordSetImpl>
        implements ARecordSets {

    ARecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.A);
    }

    @Override
    public ARecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType);
        return new ARecordSetImpl(this.dnsZone, inner);
    }

    @Override
    protected PagedList<ARecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected Observable<ARecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(this.dnsZone, inner);
    }
}
