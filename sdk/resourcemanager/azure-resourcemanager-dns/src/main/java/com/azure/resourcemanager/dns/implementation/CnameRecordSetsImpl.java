// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.models.CnameRecordSet;
import com.azure.resourcemanager.dns.models.CnameRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import reactor.core.publisher.Mono;

/** Implementation of CnameRecordSets. */
class CnameRecordSetsImpl extends DnsRecordSetsBaseImpl<CnameRecordSet, CnameRecordSetImpl> implements CnameRecordSets {

    CnameRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.CNAME);
    }

    @Override
    public CnameRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<CnameRecordSet> getByNameAsync(String name) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getRecordSets()
            .getAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), name, this.recordType)
            .map(this::wrapModel);
    }

    @Override
    protected PagedIterable<CnameRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
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
    protected PagedFlux<CnameRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            this
                .parent()
                .manager()
                .serviceClient()
                .getRecordSets()
                .listByTypeAsync(this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected CnameRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new CnameRecordSetImpl(inner.name(), this.dnsZone, inner);
    }
}
