// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.client.traits;

/**
 * An {@link com.typespec.core.client.traits Azure SDK for Java trait} providing a consistent interface for setting
 * service endpoints.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @see com.typespec.core.client.traits
 */
public interface EndpointTrait<T extends EndpointTrait<T>> {
    /**
     * Sets the service endpoint that will be connected to by clients.
     *
     * @param endpoint The URL of the service endpoint.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid URL.
     */
    T endpoint(String endpoint);
}
