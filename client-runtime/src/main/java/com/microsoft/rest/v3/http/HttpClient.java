/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import reactor.core.publisher.Mono;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public abstract class HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send
     * @return A {@link Mono} that emits response asynchronously
     */
    public abstract Mono<HttpResponse> send(HttpRequest request);

    /**
     * Create default HttpClient instance.
     *
     * @return the HttpClient
     */
    public static HttpClient createDefault() {
        return new ReactorNettyClient(null);
    }

    /**
     * Create default HttpClient instance with the provided configuration applied.
     *
     * @param configuration The configuration to apply to the HttpClient
     * @return the HttpClient
     */
    public static HttpClient createDefault(HttpClientConfiguration configuration) {
        return new ReactorNettyClient(configuration);
    }
}
