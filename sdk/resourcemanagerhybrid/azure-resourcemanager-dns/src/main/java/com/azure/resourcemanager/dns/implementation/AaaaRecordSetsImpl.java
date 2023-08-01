// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.AaaaRecordSet;
import com.azure.resourcemanager.dns.models.AaaaRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of AaaaRecordSets. */
class AaaaRecordSetsImpl extends DnsRecordSetsBaseImpl<AaaaRecordSet, AaaaRecordSetImpl> implements AaaaRecordSets {

    AaaaRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.AAAA);
    }

    @Override
    public AaaaRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<AaaaRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<AaaaRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return new PagedIterable<>(listInternAsync(recordSetNameSuffix, pageSize));
    }

    @Override
    protected PagedFlux<AaaaRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .serviceClient()
                .getRecordSets()
                .listByTypeAsync(
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
        return new AaaaRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
