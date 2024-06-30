// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.implementation.util.Providers;
import io.clientcore.core.util.configuration.Configuration;

/**
 * An interface to be implemented by any core plugin that wishes to provide an alternate {@link HttpClient}
 * implementation.
 */
public abstract class HttpClientProvider {
    enum GlobalDefaultHttpClient {
        HTTP_CLIENT(new DefaultHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalDefaultHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        HttpClient getHttpClient() {
            return httpClient;
        }
    }

    static final String NO_DEFAULT_PROVIDER_MESSAGE = "A request was made to load the default HttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including "
        + "a dependency on io.clientcore:http-okhttp3 or io.clientcore:http-jdk-httpclient. Additionally, refer to "
        + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

    private static Providers<HttpClientProvider, HttpClient> providers;

    /**
     * Gets a new instance of the {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     *
     * @return A new instance of {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     */
    public abstract HttpClient getNewInstance();

    /**
     * Gets a shared instance of the {@link HttpClient} that this {@link HttpClientProvider} is configured to create.
     *
     * @return A shared instance of {@link HttpClient} that this {@link HttpClientProvider} is configured to
     * create.
     */
    public abstract HttpClient getSharedInstance();

    static Providers<HttpClientProvider, HttpClient> getProviders() {
        if (providers == null) {
            providers = new Providers<>(HttpClientProvider.class,
                Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_HTTP_CLIENT_IMPLEMENTATION),
                NO_DEFAULT_PROVIDER_MESSAGE);
        }

        return providers;
    }
}
