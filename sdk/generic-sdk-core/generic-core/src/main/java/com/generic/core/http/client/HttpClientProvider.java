// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.annotation.ServiceClient;

/**
 * An interface to be implemented by any generic-core plugin that wishes to provide an alternate {@link HttpClient}
 * implementation.
 */
@FunctionalInterface
public interface HttpClientProvider {
    /**
     * Gets an instance of the {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     *
     * @return A new {@link HttpClient} instance, entirely unrelated to all other instances that were created previously
     * If {@code ENABLE_HTTP_CLIENT_SHARING} is not specified or set to {@code false}. Otherwise, return a singleton
     * {@link HttpClient} that can be shared amongst different {@link ServiceClient} instances.
     */
    HttpClient getInstance();
}
