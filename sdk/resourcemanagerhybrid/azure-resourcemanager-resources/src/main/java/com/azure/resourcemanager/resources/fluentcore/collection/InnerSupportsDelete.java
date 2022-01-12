// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to deleting Azure resources of a specific type in a subscription.
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
     * @return the {@link Mono} object if successful.
     */
    Mono<ResponseT> deleteAsync(String resourceGroupName, String resourceName);
}
