// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.NSRecordSet;
import com.azure.resourcemanager.dns.models.NSRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of NSRecordSets. */
class NSRecordSetsImpl extends DnsRecordSetsBaseImpl<NSRecordSet, NSRecordSetImpl> implements NSRecordSets {

    NSRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.NS);
    }

    @Override
    public NSRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<NSRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<NSRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
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
                        this.recordType,
                        pageSize,
                        recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<NSRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .inner()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected NSRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new NSRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
