// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.spi;

import com.azure.core.http.HttpClient;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class handles loading available HTTP clients
 */
public final class HttpClientProviders {
    private static HttpClientProvider defaultProvider;

    static {
        ServiceLoader<HttpClientProvider> serviceLoader = ServiceLoader.load(HttpClientProvider.class);

        // Use the first provider found in the service loader iterator.
        Iterator<HttpClientProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        }
    }

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        if (defaultProvider == null) {
            throw new IllegalStateException(
                "Cannot find any HttpClient provider on the classpath - unable to create a default HttpClient instance");
        }

        return defaultProvider.createInstance();
    }
}
