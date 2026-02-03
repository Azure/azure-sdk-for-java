// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.DnsRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/** The base implementation for Dns Record sets. */
abstract class DnsRecordSetsBaseImpl<RecordSetT, RecordSetImplT extends RecordSetT>
    extends ReadableWrappersImpl<RecordSetT, RecordSetImplT, RecordSetInner> implements DnsRecordSets<RecordSetT> {
    /** the parent DNS zone of the record set. */
    protected final DnsZoneImpl dnsZone;
    /** the record type in the record set. */
    protected final RecordType recordType;

    /**
     * Creates DnsRecordSetsBaseImpl.
     *
     * @param parent the parent DNS zone of the record set
     * @param recordType the record type in the record set
     */
    DnsRecordSetsBaseImpl(DnsZoneImpl parent, RecordType recordType) {
        this.dnsZone = parent;
        this.recordType = recordType;
    }

    @Override
    public PagedIterable<RecordSetT> list() {
        return listIntern(null, null);
    }

    @Override
    public PagedIterable<RecordSetT> list(String recordSetNameSuffix) {
        return listIntern(recordSetNameSuffix, null);
    }

    @Override
    public PagedIterable<RecordSetT> list(int pageSize) {
        return listIntern(null, pageSize);
    }

    @Override
    public PagedIterable<RecordSetT> list(String recordSetNameSuffix, int pageSize) {
        return listIntern(recordSetNameSuffix, pageSize);
    }

    @Override
    public PagedFlux<RecordSetT> listAsync() {
        return listInternAsync(null, null);
    }

    @Override
    public PagedFlux<RecordSetT> listAsync(String recordSetNameSuffix) {
        return listInternAsync(recordSetNameSuffix, null);
    }

    @Override
    public PagedFlux<RecordSetT> listAsync(int pageSize) {
        return listInternAsync(null, pageSize);
    }

    @Override
    public PagedFlux<RecordSetT> listAsync(String recordSetNameSuffix, int pageSize) {
        return listInternAsync(recordSetNameSuffix, pageSize);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    protected abstract PagedIterable<RecordSetT> listIntern(String recordSetNameSuffix, Integer pageSize);

    protected abstract PagedFlux<RecordSetT> listInternAsync(String recordSetNameSuffix, Integer pageSize);
}
