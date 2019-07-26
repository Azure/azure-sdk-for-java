// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.spi;

import com.azure.core.http.HttpClient;

import java.util.ServiceLoader;

/**
 * This class handles loading available HTTP clients
 */
public final class HttpClientProviders {

    private static ServiceLoader<HttpClientProvider> serviceLoader;
    static {
        serviceLoader = ServiceLoader.load(HttpClientProvider.class);
    }

    private static HttpClientProvider defaultProvider;

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        if (defaultProvider == null) {
            defaultProvider = serviceLoader.iterator().next();
        }

        // we return the first item found in the service loader iterator
        if (defaultProvider == null) {
            // FIXME throw error
        }

        return defaultProvider.createInstance();
    }
}
