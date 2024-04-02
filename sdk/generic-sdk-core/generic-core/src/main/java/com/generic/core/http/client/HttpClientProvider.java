// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.implementation.util.Providers;
import com.generic.core.util.configuration.Configuration;

/**
 * An interface to be implemented by any generic-core plugin that wishes to provide an alternate {@link HttpClient}
 * implementation.
 */
public abstract class HttpClientProvider {
    final static String NO_DEFAULT_PROVIDER_MESSAGE = "A request was made to load the default HttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including "
        + "a dependency on io.clientcore:http-okhttp. Additionally, refer to "
        + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

    private static HttpClient httpClient;
    private static Providers<HttpClientProvider, HttpClient> providers;

    /**
     * Gets a new instance of the {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     */
    public abstract HttpClient getNewInstance();

    /**
     * Gets a shared instance of the {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     */
    public HttpClient getSharedInstance() {
        if (httpClient == null) {
            httpClient = getNewInstance();
        }

        return httpClient;
    }

    static Providers<HttpClientProvider, HttpClient> getProviders() {
        if (providers == null) {
            providers = new Providers<>(HttpClientProvider.class,
                Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_HTTP_CLIENT_IMPLEMENTATION),
                NO_DEFAULT_PROVIDER_MESSAGE);
        }

        return providers;
    }
}
