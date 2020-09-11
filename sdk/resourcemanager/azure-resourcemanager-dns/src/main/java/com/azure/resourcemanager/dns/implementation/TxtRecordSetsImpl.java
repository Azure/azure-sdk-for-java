// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.TxtRecordSet;
import com.azure.resourcemanager.dns.models.TxtRecordSets;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of TxtRecordSets. */
class TxtRecordSetsImpl extends DnsRecordSetsBaseImpl<TxtRecordSet, TxtRecordSetImpl> implements TxtRecordSets {

    TxtRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.TXT);
    }

    @Override
    public TxtRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<TxtRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<TxtRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super
            .wrapList(
                this
                    .parent()
                    .manager()
                    .inner()
                    .getRecordSets()
                    .listByType(
                        this.dnsZone.resourceGroupName(),
                        this.dnsZone.name(),
                        recordType,
                        pageSize,
                        recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<TxtRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .inner()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), recordType));
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new TxtRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
