// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.client.traits;

import com.azure.core.util.ServiceVersion;

/**
 * An {@link com.azure.core.client.traits Azure SDK for Java trait} providing a consistent interface for setting
 * service version.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @param <V> The type of the {@link ServiceVersion}.
 * @see com.azure.core.client.traits
 */
public interface ServiceVersionTrait <T extends ServiceVersionTrait<T, V>, V extends ServiceVersion> {
    /**
     * Sets the service version that will be used by clients.
     * <p>
     * If {@code serviceVersion} is null the builder will use a default service version. Generally, this is the latest
     * service version that the client supports.
     *
     * @param serviceVersion The service version.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T serviceVersion(V serviceVersion);
}
