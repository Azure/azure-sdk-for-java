// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.CNameRecordSet;
import com.azure.resourcemanager.dns.models.CNameRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of CNameRecordSets. */
class CNameRecordSetsImpl extends DnsRecordSetsBaseImpl<CNameRecordSet, CNameRecordSetImpl> implements CNameRecordSets {

    CNameRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.CNAME);
    }

    @Override
    public CNameRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<CNameRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<CNameRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
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
    protected PagedFlux<CNameRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .inner()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected CNameRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new CNameRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
