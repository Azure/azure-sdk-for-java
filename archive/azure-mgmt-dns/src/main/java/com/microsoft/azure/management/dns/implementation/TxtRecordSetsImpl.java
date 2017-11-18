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
import rx.Observable;

/**
 * Implementation of TxtRecordSets.
 */
@LangDefinition
class TxtRecordSetsImpl
        extends DnsRecordSetsBaseImpl<TxtRecordSet, TxtRecordSetImpl>
        implements TxtRecordSets {

    TxtRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.TXT);
    }

    @Override
    public TxtRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                recordType);
        return new TxtRecordSetImpl(this.dnsZone, inner);
    }

    @Override
    protected PagedList<TxtRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected Observable<TxtRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                recordType));
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new TxtRecordSetImpl(this.dnsZone, inner);
    }
}