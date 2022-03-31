// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

/**
 * An {@link com.azure.core.client.traits Azure SDK for Java trait} providing a consistent interface for
 * setting connection strings.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @see com.azure.core.client.traits
 */
public interface IdentifierTrait<T extends IdentifierTrait<T>> {
    /**
     * Sets the identifier to the client.
     *
     * @param identifier Identifier for the client.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T identifier(String identifier);
}
