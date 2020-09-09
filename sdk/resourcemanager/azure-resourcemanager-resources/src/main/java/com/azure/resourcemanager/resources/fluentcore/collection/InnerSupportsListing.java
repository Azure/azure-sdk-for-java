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
 * @param <InnerT> the fluent type of the resource
 */
public interface InnerSupportsListing<InnerT> {
    /**
     * Returns the observable for the page list of all resources of specific type in subscription.
     *
     * @return the {@link PagedFlux} of resources if successful.
     */
    PagedFlux<InnerT> listAsync();

    /**
     * Returns the observable for the page list of all resources of specific type in specified resource group.
     *
     * @param resourceGroup name of the resource group.
     * @return the {@link PagedFlux} of resources if successful.
     */
    PagedFlux<InnerT> listByResourceGroupAsync(String resourceGroup);

    /**
     * Lists the page list of all resources of specific type available in subscription.
     *
     * @return the {@link PagedIterable} of resources if successful.
     */
    PagedIterable<InnerT> list();

    /**
     * Lists the page list of all resources of specific type in specified resource group.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @return the {@link PagedIterable} of resources if successful.
     */
    PagedIterable<InnerT> listByResourceGroup(String resourceGroupName);
}
