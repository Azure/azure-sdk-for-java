// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.spi;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.policy.spi.AfterRetryPolicyProvider;
import com.azure.core.implementation.http.policy.spi.BeforeRetryPolicyProvider;
import com.azure.core.implementation.http.policy.spi.PolicyProvider;
import com.azure.core.implementation.http.spi.HttpClientProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

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

        return defaultProvider.createNewInstance();
    }
}
