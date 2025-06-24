// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.client;

import io.clientcore.core.implementation.utils.Providers;
import io.clientcore.core.utils.configuration.Configuration;

/**
 * An interface to be implemented by any core plugin that wishes to provide an alternate {@link AsyncHttpClient}
 * implementation.
 */
public abstract class AsyncHttpClientProvider {
    static final String NO_DEFAULT_PROVIDER_MESSAGE = "A request was made to load the default AsyncHttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on supported implementations of HTTP clients. Please refer to "
        + "https://aka.ms/java/docs/httpclients for more information.";

    private static Providers<AsyncHttpClientProvider, AsyncHttpClient> providers;

    /**
     * Creates a new instance of {@link AsyncHttpClientProvider}.
     */
    public AsyncHttpClientProvider() {
    }

    /**
     * Gets a new {@link AsyncHttpClient} instance that this {@link AsyncHttpClientProvider} is configured to create.
     *
     * @return A new {@link AsyncHttpClient} instance that this {@link AsyncHttpClientProvider} is configured to create.
     */
    public abstract AsyncHttpClient getNewInstance();

    /**
     * Gets a shared {@link AsyncHttpClient} instance that this {@link AsyncHttpClientProvider} is configured to create.
     *
     * @return A shared {@link AsyncHttpClient} instance that this {@link AsyncHttpClientProvider} is configured to
     * create.
     */
    public abstract AsyncHttpClient getSharedInstance();

    static Providers<AsyncHttpClientProvider, AsyncHttpClient> getProviders() {
        if (providers == null) {
            providers = new Providers<>(AsyncHttpClientProvider.class,
                Configuration.getGlobalConfiguration().get(Configuration.HTTP_CLIENT_IMPLEMENTATION),
                NO_DEFAULT_PROVIDER_MESSAGE);
        }

        return providers;
    }
}
