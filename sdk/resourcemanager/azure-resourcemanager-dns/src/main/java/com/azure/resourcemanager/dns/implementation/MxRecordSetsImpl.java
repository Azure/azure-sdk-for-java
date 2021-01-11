// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.models.MxRecordSet;
import com.azure.resourcemanager.dns.models.MxRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of MxRecordSets. */
class MxRecordSetsImpl extends DnsRecordSetsBaseImpl<MxRecordSet, MxRecordSetImpl> implements MxRecordSets {

    MxRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.MX);
    }

    @Override
    public MxRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<MxRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    public PagedIterable<MxRecordSet> list() {
        return super
            .wrapList(
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getRecordSets()
                    .listByType(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected PagedIterable<MxRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
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
    protected PagedFlux<MxRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .serviceClient()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected MxRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new MxRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
