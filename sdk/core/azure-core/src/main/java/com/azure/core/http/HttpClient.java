// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.http.HttpClientProviders;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send.
     * @return A {@link Mono} that emits response asynchronously.
     */
    Mono<HttpResponse> send(HttpRequest request);

    /**
     * Initializes the Http client with the given context.
     *
     * @param context The context to initialize this Http client with.
     * @return The update instance of this {@link HttpClient}.
     */
    default HttpClient initContext(Context context) {
        return this;
    }

    /**
     * Create default {@link HttpClient} instance.
     *
     * @return A new instance of the {@link HttpClient}.
     */
    static HttpClient createDefault() {
        return createDefault(Context.NONE);
    }

    /**
     * Creates default {@link HttpClient} instance with additional context.
     *
     * @param context The context to initialize the {@link HttpClient} with.
     * @return A new instance of the {@link HttpClient}.
     */
    static HttpClient createDefault(Context context) {
        return HttpClientProviders.createInstance(context);
    }
}
