// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.RecordType;
import com.azure.management.dns.SrvRecordSet;
import com.azure.management.dns.SrvRecordSets;
import com.azure.management.dns.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of SrvRecordSets. */
class SrvRecordSetsImpl extends DnsRecordSetsBaseImpl<SrvRecordSet, SrvRecordSetImpl> implements SrvRecordSets {

    SrvRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.SRV);
    }

    @Override
    public SrvRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SrvRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .inner()
            .recordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .onErrorResume(e -> Mono.empty())
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<SrvRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super
            .wrapList(
                this
                    .parent()
                    .manager()
                    .inner()
                    .recordSets()
                    .listByType(
                        this.dnsZone.resourceGroupName(),
                        this.dnsZone.name(),
                        this.recordType,
                        pageSize,
                        recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<SrvRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .inner()
                .recordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected SrvRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new SrvRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
