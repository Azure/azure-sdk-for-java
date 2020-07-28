// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;

/**
 * Provides access to listing Azure resources of a specific type based on their region.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
public interface SupportsListingByRegion<T> {
    /**
     * Lists all the resources of the specified type in the specified region.
     *
     * @param region the selected Azure region
     * @return a {@link PagedIterable} of resources
     */
    PagedIterable<T> listByRegion(Region region);

    /**
     * List all the resources of the specified type in the specified region.
     *
     * @param regionName the name of an Azure region
     * @return a {@link PagedIterable} list of resources
     */
    PagedIterable<T> listByRegion(String regionName);

    /**
     * Lists all the resources of the specified type in the specified region.
     *
     * @param region the selected Azure region
     * @return a representation of the deferred computation of this call, returning the requested resources
     */
    PagedFlux<T> listByRegionAsync(Region region);

    /**
     * List all the resources of the specified type in the specified region.
     *
     * @param regionName the name of an Azure region
     * @return a representation of the deferred computation of this call, returning the requested resources
     */
    PagedFlux<T> listByRegionAsync(String regionName);
}
