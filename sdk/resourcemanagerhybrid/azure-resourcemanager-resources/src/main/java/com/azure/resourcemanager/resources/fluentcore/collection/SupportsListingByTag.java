// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

/**
 * Provides access to listing Azure resources of a specific type based on their tag.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
public interface SupportsListingByTag<T> {
    /**
     * Lists all the resources with the specified tag.
     *
     * @param tagName tag's name as the key
     * @param tagValue tag's value
     * @return a {@link PagedIterable} of resources
     */
    PagedIterable<T> listByTag(String tagName, String tagValue);

    /**
     * Lists all the resources with the specified tag.
     *
     * @param tagName tag's name as the key
     * @param tagValue tag's value
     * @return a representation of the deferred computation of this call, returning the requested resources
     */
    PagedFlux<T> listByTagAsync(String tagName, String tagValue);
}
