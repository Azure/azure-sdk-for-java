// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

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
     * @return specific resource.
     */
    InnerT getByResourceGroup(String resourceGroupName, String resourceName);

    /**
     * Returns the specific resource asynchronously.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription.
     * @param resourceName The name of the resource within specified resource group.
     * @return a {@link Mono} emits the found resource asynchronously.
     */
    Mono<InnerT> getByResourceGroupAsync(String resourceGroupName, String resourceName);
}
