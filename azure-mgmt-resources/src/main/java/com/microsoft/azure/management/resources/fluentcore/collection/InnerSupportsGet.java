/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;


/**
 * Provides access to listing Azure resources of a specific type in a subscription.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <InnerT> the fluent type of the resource
 */
public interface InnerSupportsGet<InnerT> {
    /**
     * Returns the specific resource.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @param resourceName The name of the resource within specified resource group.
     *
     * @return specific resource.
     */
    InnerT getByResourceGroup(String resourceGroupName, String resourceName);

    /**
     * Returns the specific resource asynchronously.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @param resourceName The name of the resource within specified resource group.
     *
     * @return Observable to specific resource.
     */
    Observable<InnerT> getByResourceGroupAsync(String resourceGroupName, String resourceName);

    /**
     * Returns the specific resource asynchronously.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @param resourceName The name of the resource within specified resource group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.

     * @return ServiceFuture to specific resource.
     */
    ServiceFuture<InnerT> getByResourceGroupAsync(String resourceGroupName, String resourceName, ServiceCallback<InnerT> serviceCallback);
}
