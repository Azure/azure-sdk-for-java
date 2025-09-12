// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

/**
 * Provides access to listing Azure resources of a specific type filtered based on OData syntax.
 * http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part2-url-conventions/odata-v4.0-errata03-os-part2-url-conventions-complete.html#_Toc453752358
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
public interface SupportsListingByFilter<T> {
    /**
     * Lists all the resources of the specified type with specific filter.
     *
     * @param filter the filter based on OData syntax
     * @return a {@link PagedIterable} of resources
     */
    PagedIterable<T> listByFilter(String filter);

    /**
     * Lists all the resources of the specified type with specific filter.
     *
     * @param filter the filter based on OData syntax
     * @return a representation of the deferred computation of this call, returning the requested resources
     */
    PagedFlux<T> listByFilterAsync(String filter);
}
