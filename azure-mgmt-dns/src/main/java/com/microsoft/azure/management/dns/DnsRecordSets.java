/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.dns;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import rx.Observable;

/**
 * Base interface for all record sets.
 *
 * @param <RecordSetT> the record set type
 */
@Fluent
public interface DnsRecordSets<RecordSetT> extends
        SupportsListing<RecordSetT>,
        SupportsGettingByName<RecordSetT>,
        HasParent<DnsZone> {
    /**
     * Lists all the record sets with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return list of record sets
     */
    PagedList<RecordSetT> list(String recordSetNameSuffix);
    /**
     * Lists all the record sets, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return list of record sets
     */
    PagedList<RecordSetT> list(int pageSize);
    /**
     * Lists all the record sets with the given suffix, also limits the number of entries
     * per page to the given page size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedList<RecordSetT> list(String recordSetNameSuffix, int pageSize);
    /**
     * Lists all the record sets with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return an observable that emits record sets
     */
    Observable<RecordSetT> listAsync(String recordSetNameSuffix);
    /**
     * Lists all the record sets, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return an observable that emits record sets
     */
    Observable<RecordSetT> listAsync(int pageSize);
    /**
     * Lists all the record sets with the given suffix, also limits the number of entries
     * per page to the given page size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return an observable that emits record sets
     */
    Observable<RecordSetT> listAsync(String recordSetNameSuffix, int pageSize);
}
