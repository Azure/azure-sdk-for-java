/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.RecordType;
import com.azure.management.dns.TxtRecordSet;
import com.azure.management.dns.TxtRecordSets;
import reactor.core.publisher.Mono;

/**
 * Implementation of TxtRecordSets.
 */
class TxtRecordSetsImpl
        extends DnsRecordSetsBaseImpl<TxtRecordSet, TxtRecordSetImpl>
        implements TxtRecordSets {

    TxtRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.TXT);
    }

    @Override
    public TxtRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<TxtRecordSet> getByNameAsync(String name) {
        return this.parent().manager().inner().recordSets().getAsync(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<TxtRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<TxtRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
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
        return new TxtRecordSetImpl(inner.getName(), this.dnsZone, inner);
    }
}