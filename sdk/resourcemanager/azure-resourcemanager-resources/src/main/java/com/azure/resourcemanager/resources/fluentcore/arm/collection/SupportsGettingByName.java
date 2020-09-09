// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.resourcemanager.resources.fluentcore.arm.collection;


import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its name within the current resource group.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resource collection
 */
public interface SupportsGettingByName<T> {
    /**
     * Gets the information about a resource from Azure based on the resource name within the current resource group.
     *
     * @param name the name of the resource. (Note, this is not the resource ID.)
     * @return An immutable representation of the resource
     */
    T getByName(String name);


    /**
     * Gets the information about a resource based on the resource name ithin the current resource group.
     *
     * @param name The name of the resource. (Note, this is not the resource ID.)
     * @return A {@link Mono} that emits the found resource asynchronously
     */
    Mono<T> getByNameAsync(String name);
}
