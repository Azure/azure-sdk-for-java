/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.google.common.base.Splitter;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.DnsRecordSets;
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

    abstract protected PagedList<RecordSetT> listIntern(String recordSetNameSuffix, Integer pageSize);
    abstract protected Observable<RecordSetT> listInternAsync(String recordSetNameSuffix, Integer pageSize);
}
