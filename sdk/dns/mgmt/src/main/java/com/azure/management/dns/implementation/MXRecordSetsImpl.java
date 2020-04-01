/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.MXRecordSet;
import com.azure.management.dns.MXRecordSets;
import com.azure.management.dns.RecordType;
import reactor.core.publisher.Mono;

/**
 * Implementation of MXRecordSets.
 */
class MXRecordSetsImpl
        extends DnsRecordSetsBaseImpl<MXRecordSet, MXRecordSetImpl>
        implements MXRecordSets {

    MXRecordSetsImpl(DnsZoneImpl dnsZone) {
        super(dnsZone, RecordType.MX);
    }

    @Override
    public MXRecordSet getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<MXRecordSet> getByNameAsync(String name) {
        return this.parent().manager().inner().recordSets().getAsync(this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                name,
                this.recordType)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    public PagedIterable<MXRecordSet> list() {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(), this.dnsZone.name(), this.recordType));
    }

    @Override
    protected PagedIterable<MXRecordSet> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return super.wrapList(this.parent().manager().inner().recordSets().listByType(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType,
                pageSize,
                recordSetNameSuffix));
    }

    @Override
    protected PagedFlux<MXRecordSet> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(this.parent().manager().inner().recordSets().listByTypeAsync(
                this.dnsZone.resourceGroupName(),
                this.dnsZone.name(),
                this.recordType));
    }

    @Override
    protected MXRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new MXRecordSetImpl(inner.getName(), this.dnsZone, inner);
    }
}