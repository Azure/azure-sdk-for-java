// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.PrivateDnsRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;

/** The base implementation for Private Dns Record sets. */
abstract class PrivateDnsRecordSetsBaseImpl<PrivateRecordSetT, PrivateRecordSetImplT extends PrivateRecordSetT>
    extends ReadableWrappersImpl<PrivateRecordSetT, PrivateRecordSetImplT, RecordSetInner>
    implements PrivateDnsRecordSets<PrivateRecordSetT> {
    protected final PrivateDnsZoneImpl privateDnsZone;
    protected final RecordType recordType;

    PrivateDnsRecordSetsBaseImpl(PrivateDnsZoneImpl parent, RecordType recordType) {
        this.privateDnsZone = parent;
        this.recordType = recordType;
    }

    @Override
    public PagedIterable<PrivateRecordSetT> list() {
        return listIntern(null, null);
    }

    @Override
    public PagedIterable<PrivateRecordSetT> list(String recordSetNameSuffix) {
        return listIntern(recordSetNameSuffix, null);
    }

    @Override
    public PagedIterable<PrivateRecordSetT> list(int pageSize) {
        return listIntern(null, pageSize);
    }

    @Override
    public PagedIterable<PrivateRecordSetT> list(String recordSetNameSuffix, int pageSize) {
        return listIntern(recordSetNameSuffix, pageSize);
    }

    @Override
    public PagedFlux<PrivateRecordSetT> listAsync() {
        return listInternAsync(null, null);
    }

    @Override
    public PagedFlux<PrivateRecordSetT> listAsync(String recordSetNameSuffix) {
        return listInternAsync(recordSetNameSuffix, null);
    }

    @Override
    public PagedFlux<PrivateRecordSetT> listAsync(int pageSize) {
        return listInternAsync(null, pageSize);
    }

    @Override
    public PagedFlux<PrivateRecordSetT> listAsync(String recordSetNameSuffix, int pageSize) {
        return listInternAsync(recordSetNameSuffix, pageSize);
    }

    @Override
    public PrivateRecordSetT getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<PrivateRecordSetT> getByNameAsync(String name) {
        return parent().manager().inner().getRecordSets()
            .getAsync(parent().resourceGroupName(), parent().name(), recordType, name)
            .map(this::wrapModel);
    }

    @Override
    public PrivateDnsZoneImpl parent() {
        return this.privateDnsZone;
    }

    protected PagedIterable<PrivateRecordSetT> listIntern(String recordSetNameSuffix, Integer pageSize) {
        return new PagedIterable<>(listInternAsync(recordSetNameSuffix, pageSize));
    }

    protected PagedFlux<PrivateRecordSetT> listInternAsync(String recordSetNameSuffix, Integer pageSize) {
        return wrapPageAsync(
            parent().manager().inner().getRecordSets().listByTypeAsync(
                parent().resourceGroupName(),
                parent().name(),
                recordType,
                pageSize,
                recordSetNameSuffix));
    }
}
