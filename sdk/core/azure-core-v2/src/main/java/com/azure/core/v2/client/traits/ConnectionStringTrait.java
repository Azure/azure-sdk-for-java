// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.client.traits;

/**
 * An {@link com.azure.core.client.traits Azure SDK for Java trait} providing a consistent interface for
 * setting connection strings.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 * @see com.azure.core.client.traits
 */
public interface ConnectionStringTrait<T extends ConnectionStringTrait<T>> {
    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the service.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T connectionString(String connectionString);
}
