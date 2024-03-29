// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models.traits;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.ProxyOptions;

/**
 * A {@link com.generic.core.models.traits trait} providing a consistent interface for configuration of proxy-specific
 * settings.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 * return the concrete type, rather than the trait type.
 *
 * @see com.generic.core.models.traits
 * @see ProxyOptions
 */
public interface ProxyTrait<T extends ProxyTrait<T>> {
    /**
     * Sets the {@link ProxyOptions} to use with an {@link HttpClient} when sending and receiving requests to and from
     * the service.
     *
     * @param proxyOptions The {@link ProxyOptions} to use with an {@link HttpClient} when sending and receiving
     * requests to and from the service.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T proxyOptions(ProxyOptions proxyOptions);
}
