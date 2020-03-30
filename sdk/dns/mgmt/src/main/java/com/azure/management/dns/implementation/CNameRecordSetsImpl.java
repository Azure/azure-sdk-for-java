/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.CNameRecordSet;
import com.azure.management.dns.CNameRecordSets;
import com.azure.management.dns.RecordType;
import reactor.core.publisher.Mono;

/**
 * Implementation of CNameRecordSets.
 */
class CNameRecordSetsImpl
        extends DnsRecordSetsBaseImpl<CNameRecordSet, CNameRecordSetImpl>
        implements CNameRecordSets {

    CNameRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.CNAME);
    }

    @Override
    public CNameRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<CNameRecordSet> getByNameAsync(String name) {
        return this.parent().manager().inner().recordSets().getAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<CNameRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<CNameRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected CNameRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new CNameRecordSetImpl(inner.getName(), this.dnsZone, inner);
    }
}