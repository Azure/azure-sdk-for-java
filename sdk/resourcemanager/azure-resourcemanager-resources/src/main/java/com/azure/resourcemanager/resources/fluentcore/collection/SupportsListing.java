// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

/**
 * Provides access to listing Azure resources of a specific type in a subscription.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
public interface SupportsListing<T> {
    /**
     * Lists all the resources of the specified type in the currently selected subscription.
     *
     * @return A {@link PagedIterable} of resources
     */
    PagedIterable<T> list();

    /**
     * Lists all the resources of the specified type in the currently selected subscription.
     *
     * @return A {@link PagedFlux} of resources
     */
    PagedFlux<T> listAsync();
}
