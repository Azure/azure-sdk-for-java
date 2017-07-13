/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.DnsRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * The base implementation for Dns Record sets.
 */
@LangDefinition
abstract class DnsRecordSetsBaseImpl<RecordSetT, RecordSetImplT extends RecordSetT>
    extends
        ReadableWrappersImpl<RecordSetT, RecordSetImplT, RecordSetInner>
    implements
        DnsRecordSets<RecordSetT> {
    /**
     * the parent DNS zone of the record set.
     */
    protected final DnsZoneImpl dnsZone;
    /**
     * the record type in the record set.
     */
    protected  final RecordType recordType;

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
    public PagedList<RecordSetT> list() {
        return listIntern(null, null);
    }

    @Override
    public PagedList<RecordSetT> list(String recordSetNameSuffix) {
        return listIntern(recordSetNameSuffix, null);
    }

    @Override
    public PagedList<RecordSetT> list(int pageSize) {
        return listIntern(null, pageSize);
    }

    @Override
    public PagedList<RecordSetT> list(String recordSetNameSuffix, int pageSize) {
        return listIntern(recordSetNameSuffix, pageSize);
    }

    @Override
    public Observable<RecordSetT> listAsync() {
        return listInternAsync(null, null);
    }

    @Override
    public Observable<RecordSetT> listAsync(String recordSetNameSuffix) {
        return listInternAsync(recordSetNameSuffix, null);
    }

    @Override
    public Observable<RecordSetT> listAsync(int pageSize) {
        return listInternAsync(null, pageSize);
    }

    @Override
    public Observable<RecordSetT> listAsync(String recordSetNameSuffix, int pageSize) {
        return listInternAsync(recordSetNameSuffix, pageSize);
    }

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    protected abstract PagedList<RecordSetT> listIntern(String recordSetNameSuffix, Integer pageSize);
    protected abstract Observable<RecordSetT> listInternAsync(String recordSetNameSuffix, Integer pageSize);
}
