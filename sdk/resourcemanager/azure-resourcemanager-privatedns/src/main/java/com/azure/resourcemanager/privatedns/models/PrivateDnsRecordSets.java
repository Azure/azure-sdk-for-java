// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/**
 * Base interface for all record sets.
 *
 * @param <PrivateRecordSetT> the record set type
 */
@Fluent
public interface PrivateDnsRecordSets<PrivateRecordSetT>
    extends SupportsListing<PrivateRecordSetT>,
        SupportsGettingByName<PrivateRecordSetT>,
        HasParent<PrivateDnsZone> {
    /**
     * Lists all the record sets with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return list of record sets
     */
    PagedIterable<PrivateRecordSetT> list(String recordSetNameSuffix);

    /**
     * Lists all the record sets, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return list of record sets
     */
    PagedIterable<PrivateRecordSetT> list(int pageSize);

    /**
     * Lists all the record sets with the given suffix, also limits the number of entries per page to the given page
     * size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return the record sets
     */
    PagedIterable<PrivateRecordSetT> list(String recordSetNameSuffix, int pageSize);

    /**
     * Lists all the record sets with the given suffix.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @return A {@link PagedFlux} of record sets
     */
    PagedFlux<PrivateRecordSetT> listAsync(String recordSetNameSuffix);

    /**
     * Lists all the record sets, with number of entries in each page limited to given size.
     *
     * @param pageSize the maximum number of record sets in a page
     * @return A {@link PagedFlux} of record sets
     */
    PagedFlux<PrivateRecordSetT> listAsync(int pageSize);

    /**
     * Lists all the record sets with the given suffix, also limits the number of entries per page to the given page
     * size.
     *
     * @param recordSetNameSuffix the record set name suffix
     * @param pageSize the maximum number of record sets in a page
     * @return A {@link PagedFlux} of record sets
     */
    PagedFlux<PrivateRecordSetT> listAsync(String recordSetNameSuffix, int pageSize);
}
