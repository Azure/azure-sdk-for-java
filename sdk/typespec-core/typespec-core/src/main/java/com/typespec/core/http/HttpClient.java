// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.implementation.http.HttpClientProviders;
import com.typespec.core.util.Context;
import com.typespec.core.util.HttpClientOptions;
import reactor.core.publisher.Mono;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send.
     * @return A {@link Mono} that emits the response asynchronously.
     */
    Mono<HttpResponse> send(HttpRequest request);

    /**
     * Sends the provided request asynchronously with contextual information.
     *
     * @param request The HTTP request to send.
     * @param context Contextual information about the request.
     * @return A {@link Mono} that emits the response asynchronously.
     */
    default Mono<HttpResponse> send(HttpRequest request, Context context) {
        return send(request);
    }

    /**
     * Sends the provided request synchronously with contextual information.
     *
     * @param request The HTTP request to send.
     * @param context Contextual information about the request.
     * @return The response.
     */
    default HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault() {
        return createDefault(null);
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @param clientOptions Configuration options applied to the created {@link HttpClient}.
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault(HttpClientOptions clientOptions) {
        return HttpClientProviders.createInstance(clientOptions);
    }
}
