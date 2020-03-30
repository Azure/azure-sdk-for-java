/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.ARecordSet;
import com.azure.management.dns.ARecordSets;
import com.azure.management.dns.RecordType;
import reactor.core.publisher.Mono;

/**
 * Implementation of ARecordSets.
 */
class ARecordSetsImpl
        extends DnsRecordSetsBaseImpl<ARecordSet, ARecordSetImpl>
        implements ARecordSets {

    ARecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.A);
    }

    @Override
    public ARecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<ARecordSet> getByNameAsync(String name) {
        return this.parent().manager().inner().recordSets().getAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<ARecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<ARecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(inner.getName(), this.dnsZone, inner);
    }
}
