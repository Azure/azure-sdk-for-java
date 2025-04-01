// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.client.GlobalJdkHttpClient;
import io.clientcore.core.models.binarydata.BinaryData;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Sends the provided request.
     *
     * @param request The HTTP request to send.
     * @return The response.
     * @throws IOException If an I/O error occurs during sending the request or receiving the response.
     */
    Response<BinaryData> send(HttpRequest request) throws IOException;

    /**
     * Sends the provided request asynchronously.
     * <p>
     * If an error occurs while sending the request or receiving the response, the returned {@link CompletableFuture}
     * will complete exceptionally.
     *
     * @param request The HTTP request to send.
     * @return A CompletableFuture that will complete with the response or error.
     */
    default CompletableFuture<Response<BinaryData>> sendAsync(HttpRequest request) {
        CompletableFuture<Response<BinaryData>> completableFuture = new CompletableFuture<>();
        try {
            completableFuture.complete(send(request));
        } catch (Exception ex) {
            completableFuture.completeExceptionally(ex);
        }

        return completableFuture;
    }

    /**
     * Get a new instance of the {@link HttpClient} that the {@link HttpClientProvider} loaded from the classpath is
     * configured to create.
     *
     * <p>If no {@link HttpClientProvider} can be found on the classpath, a new instance of the default
     * {@link HttpClient} implementation will be returned instead.
     *
     * @return A new instance of {@link HttpClient} that the {@link HttpClientProvider} loaded from the classpath is
     * configured to create.
     */
    static HttpClient getNewInstance() {
        return HttpClientProvider.getProviders()
            .create(HttpClientProvider::getNewInstance, () -> new JdkHttpClientBuilder().build(), null);
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
    static HttpClient getSharedInstance() {
        return HttpClientProvider.getProviders()
            .create(HttpClientProvider::getSharedInstance, GlobalJdkHttpClient.HTTP_CLIENT::getHttpClient, null);
    }
}
