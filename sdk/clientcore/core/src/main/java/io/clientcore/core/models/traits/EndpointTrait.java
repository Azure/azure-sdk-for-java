// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.traits;

/**
 * A {@link io.clientcore.core.models.traits trait} providing a consistent interface for setting service endpoints.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 * return the concrete type, rather than the trait type.
 *
 * @see io.clientcore.core.models.traits
 */
public interface EndpointTrait<T extends EndpointTrait<T>> {
    /**
     * Sets the service endpoint that will be connected to by clients.
     *
     * @param endpoint The URI of the service endpoint.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     *
     * @throws NullPointerException If {@code endpoint} is null.
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid URI.
     */
    T endpoint(String endpoint);
}
