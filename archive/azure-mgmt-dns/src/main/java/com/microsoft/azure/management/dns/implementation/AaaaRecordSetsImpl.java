/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import rx.Observable;

/**
 * Implementation of AaaaRecordSets.
 */
@LangDefinition
class AaaaRecordSetsImpl
        extends DnsRecordSetsBaseImpl<AaaaRecordSet, AaaaRecordSetImpl>
        implements AaaaRecordSets {

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.AAAA);
    }

    @Override
    public AaaaRecordSetImpl getByName(String name) {
        RecordSetInner inner = this.parent().manager().inner().recordSets().get(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType);
        return new AaaaRecordSetImpl(this.dnsZone, inner);
    }

    @Override
    protected PagedList<AaaaRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected Observable<AaaaRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new AaaaRecordSetImpl(this.dnsZone, inner);
    }
}
