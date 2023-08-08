// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

/**
 * Provides access to listing Azure resources of a specific type in a specific resource group.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resources listed.
 */
public interface SupportsListingByResourceGroup<T> {
    /**
     * Lists resources of the specified type in the specified resource group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @return the list of resources
     */
    PagedIterable<T> listByResourceGroup(String resourceGroupName);

    /**
     * Lists resources of the specified type in the specified resource group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @return the {@link PagedFlux} of the resources
     */
    PagedFlux<T> listByResourceGroupAsync(String resourceGroupName);
}
