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
     * Send the provided request along with context asynchronously.
     *
     * @param pipelineCallContext The HTTP pipeline call context.
     * @return A {@link Mono} that emits response asynchronously.
     */
    default Mono<HttpResponse> send(HttpPipelineCallContext pipelineCallContext) {
        return send(pipelineCallContext.getHttpRequest());
    }

    /**
     * Send the provided request along with context asynchronously.
     *
     * @param request The HTTP pipeline call context.
     * @param context Additional context for sending this request.
     * @return A {@link Mono} that emits response asynchronously.
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
