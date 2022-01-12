// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.ARecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of ARecordSets. */
class ARecordSetsImpl extends DnsRecordSetsBaseImpl<ARecordSet, ARecordSetImpl> implements ARecordSets {

    ARecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.A);
    }

    @Override
    public ARecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<ARecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<ARecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super
            .wrapList(
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getRecordSets()
                    .listByType(
                        this.dnsZone.resourceGroupName(),
                        this.dnsZone.name(),
                        this.recordType,
                        pageSize,
                        recordSetNameSuffix,
                        Context.NONE));
    }

    @Override
    protected PagedFlux<ARecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .serviceClient()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        return new ARecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
