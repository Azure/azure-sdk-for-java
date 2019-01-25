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
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     * @param request The HTTP request to send.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    public abstract Mono<HttpResponse> sendRequestAsync(HttpRequest request);

    private static final class DefaultHttpClientHolder {
        // Putting this field in an inner class makes it so it is only instantiated when
        // one of the createDefault() methods instead of instantiating when any members are accessed.
        private static HttpClientFactory defaultHttpClientFactory = new NettyClient.Factory();
    }

    /**
     * Create an instance of the default HttpClient type.
     * @return an instance of the default HttpClient type.
     */
    public static HttpClient createDefault() {
        return createDefault(null);
    }

    /**
     * Create an instance of the default HttpClient type with the provided configuration.
     * @param configuration The configuration to apply to the HttpClient.
     * @return an instance of the default HttpClient type.
     */
    public static HttpClient createDefault(HttpClientConfiguration configuration) {
        return DefaultHttpClientHolder.defaultHttpClientFactory.create(configuration);
    }
}
