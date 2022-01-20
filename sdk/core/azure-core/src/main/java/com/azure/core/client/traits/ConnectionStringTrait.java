// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

/**
 * The interface for client builders that support a connection string.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface ConnectionStringTrait<TBuilder extends ConnectionStringTrait<TBuilder>> {
    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the service.
     * @return the updated {@code TBuilder}.
     */
    TBuilder connectionString(String connectionString);
}
