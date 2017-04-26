/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import rx.Observable;


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
     * @return Observable of list of resources.
     */
    Observable<Page<InnerT>> listAsync();

    /**
     * Returns the observable for the page list of all resources of specific type in specified resource group.
     *
     * @param resourceGroup name of the resource group.
     * @return Observable of list of resources.
     */
    Observable<Page<InnerT>> listByResourceGroupAsync(String resourceGroup);

    /**
     * Lists the page list of all resources of specific type available in subscription.
     *
     * @return the paged list of resources if successful.
     */
    PagedList<InnerT> list();

    /**
     * Lists the page list of all resources of specific type in specified resource group.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @return the paged list of resources if successful.
     */
    PagedList<InnerT> listByResourceGroup(String resourceGroupName);
}
