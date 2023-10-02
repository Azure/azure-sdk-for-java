// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.implementation.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpClientProvider;
import com.typespec.core.implementation.util.Providers;
import com.typespec.core.util.ClientOptions;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.HttpClientOptions;

import static com.typespec.core.util.Configuration.PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION;

/**
 * This class handles loading available HTTP clients
 */
public final class HttpClientProviders {
    private static final String NO_DEFAULT_PROVIDER_MESSAGE = "A request was made to load the default HttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-http-netty or azure-core-http-okhttp. Depending on your existing dependencies, you "
        + "have the choice of Netty or OkHttp implementations. Additionally, refer to "
        + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

    private static final Providers<HttpClientProvider, HttpClient> HTTP_CLIENT_PROVIDERS = new Providers<>(HttpClientProvider.class,
        Configuration.getGlobalConfiguration().get(PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION),
        NO_DEFAULT_PROVIDER_MESSAGE);

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        return createInstance(null);
    }

    public static HttpClient createInstance(ClientOptions clientOptions) {
        Class<? extends HttpClientProvider> selectedImplementation = null;
        final HttpClientOptions httpClientOptions;
        if (clientOptions instanceof HttpClientOptions) {
            httpClientOptions = (HttpClientOptions) clientOptions;
            selectedImplementation = httpClientOptions.getHttpClientProvider();
        } else {
            httpClientOptions = null;
        }

        return HTTP_CLIENT_PROVIDERS.create(p -> p.createInstance(httpClientOptions), null, selectedImplementation);
    }
}
