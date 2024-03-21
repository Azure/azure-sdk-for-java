// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.implementation.util.Providers;
import com.generic.core.util.configuration.Configuration;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Sends the provided request synchronously with contextual information.
     *
     * @param request The HTTP request to send.
     *
     * @return The response.
     */
    Response<?> send(HttpRequest request);

    /**
     * Creates an instance of the default {@link HttpClient} implementation, entirely unrelated to all other instances
     * that were created previously.
     *
     * @return An instance of the default {@link HttpClient} implementation, entirely unrelated to all other instances
     * that were created previously.
     */
    static HttpClient getDefault() {
        return new DefaultHttpClientBuilder().build();
    }

    /**
     * Returns a new instance of the {@link HttpClient} that the default {@link HttpClientProvider} is configured to
     * create. If no {@link HttpClientProvider} can be found on the classpath, an instance of the default
     * {@link HttpClient} implementation will be created instead, in which it would effectively be the same as calling
     * {@link #getDefault()}.
     *
     * @return A new {@link HttpClient} instance, entirely unrelated to all other instances that were created
     * previously.
     */
    static HttpClient getInstance() {
        final String NO_DEFAULT_PROVIDER_MESSAGE = "A request was made to load the default HttpClient provider "
            + "but one could not be found on the classpath. If you are using a dependency manager, consider including "
            + "a dependency on azure-core-http-netty or azure-core-http-okhttp. Depending on your existing "
            + "dependencies, you have the choice of Netty or OkHttp implementations. Additionally, refer to "
            + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

        final Providers<HttpClientProvider, HttpClient> HTTP_CLIENT_PROVIDERS =
            new Providers<>(HttpClientProvider.class,
                Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_HTTP_CLIENT_IMPLEMENTATION),
                NO_DEFAULT_PROVIDER_MESSAGE);

        return HTTP_CLIENT_PROVIDERS.create(HttpClientProvider::createInstance, new DefaultHttpClientBuilder().build(),
            null);
    }
}
