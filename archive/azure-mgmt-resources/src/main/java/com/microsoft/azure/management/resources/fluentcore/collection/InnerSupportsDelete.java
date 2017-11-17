/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.rest.ServiceResponse;
import rx.Observable;

/**
 * Provides access to delete Azure resources of a specific type in a subscription.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <ResponseT> Response type for delete.
 */
public interface InnerSupportsDelete<ResponseT> {
    /**
     * Deletes a resource asynchronously.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @param resourceName The name of the resource within specified resource group.
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ResponseT> deleteAsync(String resourceGroupName, String resourceName);
}
