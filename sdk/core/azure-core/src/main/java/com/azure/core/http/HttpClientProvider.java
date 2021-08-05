// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.HttpClientOptions;

/**
 * An interface to be implemented by any azure-core plugin that wishes to provide an alternate {@link HttpClient}
 * implementation.
 */
@FunctionalInterface
public interface HttpClientProvider {

    /**
     * Creates a new instance of the {@link HttpClient} that this HttpClientProvider is configured to create.
     *
     * @return A new {@link HttpClient} instance, entirely unrelated to all other instances that were created
     * previously.
     */
    HttpClient createInstance();

    /**
     * Creates a new instance of the {@link HttpClient} that this HttpClientProvider is configured to create.
     *
     * @param clientOptions Configuration options applied to the created {@link HttpClient}.
     * @return A new {@link HttpClient} instance, entirely unrelated to all other instances that were created
     * previously.
     */
    default HttpClient createInstance(HttpClientOptions clientOptions) {
        return createInstance();
    }
}
