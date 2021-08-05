// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.models.PtrRecordSet;
import com.azure.resourcemanager.dns.models.PtrRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of PtrRecordSets. */
class PtrRecordSetsImpl extends DnsRecordSetsBaseImpl<PtrRecordSet, PtrRecordSetImpl> implements PtrRecordSets {

    PtrRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.PTR);
    }

    @Override
    public PtrRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<PtrRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<PtrRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
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
                        recordType,
                        pageSize,
                        recordSetNameSuffix,
                        Context.NONE));
    }

    @Override
    protected PagedFlux<PtrRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .serviceClient()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected PtrRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new PtrRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
