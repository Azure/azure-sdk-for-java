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
     * Key for {@link Context} where the value is a boolean flag that indicates whether the {@link HttpResponse} body
     * should be eagerly read and buffered into memory.
     */
    String EAGERLY_READ_RESPONSE_CONTEXT_KEY = "azure-eagerly-read-response";

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
     * Create default {@link HttpClient} instance.
     *
     * @return A new instance of the {@link HttpClient}.
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }
}
