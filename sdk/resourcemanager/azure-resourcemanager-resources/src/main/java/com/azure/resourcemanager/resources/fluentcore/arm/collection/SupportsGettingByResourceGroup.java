// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its name and resource group.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resource to get.
 */
public interface SupportsGettingByResourceGroup<T> {
    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @return an immutable representation of the resource
     */
    T getByResourceGroup(String resourceGroupName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @return a {@link Mono} that emits the found resource asynchronously.
     */
    Mono<T> getByResourceGroupAsync(String resourceGroupName, String name);
}
