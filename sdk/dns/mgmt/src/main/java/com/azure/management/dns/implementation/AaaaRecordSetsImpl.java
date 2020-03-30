/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.AaaaRecordSet;
import com.azure.management.dns.AaaaRecordSets;
import com.azure.management.dns.RecordType;
import reactor.core.publisher.Mono;

/**
 * Implementation of AaaaRecordSets.
 */
class AaaaRecordSetsImpl
        extends DnsRecordSetsBaseImpl<AaaaRecordSet, AaaaRecordSetImpl>
        implements AaaaRecordSets {

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.AAAA);
    }

    @Override
    public AaaaRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<AaaaRecordSet> getByNameAsync(String name) {
        return this.parent().manager().inner().recordSets().getAsync(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<AaaaRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return new PagedIterable<>(listInternAsync(recordSetNameSuffix, pageSize));
    }

    @Override
    protected PagedFlux<AaaaRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new AaaaRecordSetImpl(inner.getName(), this.dnsZone, inner);
    }
}
