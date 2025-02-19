// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.client.DefaultHttpClientProvider;
import io.clientcore.core.utils.SharedExecutorService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
    Response<?> send(HttpRequest request) throws IOException;

    /**
     * Sends the provided request asynchronously.
     * <p>
     * If an I/O error occurs while sending the request or receiving the response, the returned
     * {@link CompletableFuture} will complete exceptionally.
     *
     * @param request The HTTP request to send.
     * @return A CompletableFuture that will complete with the response.
     */
    default CompletableFuture<Response<?>> sendAsync(HttpRequest request) {
        // TODO (alzimmer): Few thoughts here.
        //  Is this API checked with IOException as the synchronous API is? Or is it mentioned in docs that getting the
        //  result of the CompletableFuture may throw IOException if one occurred while sending the request?
        //  Does this API need to accept an ExecutorService to manage which thread the CompletableFuture is executed on?
        //  Or, does this return an extension type of CompletableFuture that can set the ExecutorService after the fact?
        return CompletableFuture.supplyAsync(() -> {
            try {
                return send(request);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, SharedExecutorService.getInstance());
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
            .create(HttpClientProvider::getSharedInstance, new DefaultHttpClientProvider()::getSharedInstance, null);
    }
}
