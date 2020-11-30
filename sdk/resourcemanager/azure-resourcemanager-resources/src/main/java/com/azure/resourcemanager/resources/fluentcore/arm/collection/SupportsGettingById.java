// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its resource ID.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resource collection
 */
public interface SupportsGettingById<T> {
    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param id the id of the resource.
     * @return an immutable representation of the resource
     */
    T getById(String id);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param id the id of the resource.
     * @return a {@link Mono} that emits the found resource asynchronously
     */
    Mono<T> getByIdAsync(String id);
}
