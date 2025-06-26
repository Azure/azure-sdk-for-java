// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.client.GlobalJdkHttpClient;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.concurrent.CompletableFuture;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface AsyncHttpClient {

    /**
     * Sends the provided request with contextual information.
     *
     * @param request The HTTP request to send.
     * @return A {@link CompletableFuture} that will complete with the response, or fail with an exception if the
     * request fails.
     * @throws CoreException If any error occurs during sending the request or receiving the response.
     */
    CompletableFuture<Response<BinaryData>> sendAsync(HttpRequest request);

    /**
     * Gets a new {@link AsyncHttpClient} instance that the {@link AsyncHttpClientProvider} loaded from the classpath is
     * configured to create.
     *
     * <p>If no {@link AsyncHttpClientProvider} can be found on the classpath, a new instance of the default
     * {@link AsyncHttpClient} implementation will be returned instead.
     *
     * @return A new {@link AsyncHttpClient} instance that the {@link HttpClientProvider} loaded from the classpath is
     * configured to create.
     */
    static AsyncHttpClient getNewInstance() {
        return AsyncHttpClientProvider.getProviders()
            .create(AsyncHttpClientProvider::getNewInstance, () -> new JdkHttpClientBuilder().build(), null);
    }

    /**
     * Get a shared instance of the {@link HttpClient} that the {@link HttpClientProvider} loaded from the classpath is
     * configured to create.
     *
     * <p>If no {@link HttpClientProvider} can be found on the classpath, a shared instance of the default
     * {@link HttpClient} implementation will be returned instead.
     *
     * @return A shared instance of {@link HttpClient} that the {@link HttpClientProvider} loaded from the classpath is
     * configured to create.
     */
    static AsyncHttpClient getSharedInstance() {
        return AsyncHttpClientProvider.getProviders()
            .create(AsyncHttpClientProvider::getSharedInstance, GlobalJdkHttpClient.HTTP_CLIENT::getAsyncHttpClient,
                null);
    }
}
