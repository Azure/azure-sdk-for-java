// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.http.spi.HttpClientProviders;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send
     * @return A {@link Mono} that emits response asynchronously
     */
    Mono<HttpResponse> send(HttpRequest request);

    /**
     * Create default HttpClient instance.
     *
     * @return the HttpClient
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }

    /**
     * Apply the provided proxy configuration to the HttpClient.
     *
     * @param proxyOptions the proxy configuration supplier
     * @return a HttpClient with proxy applied
     */
    HttpClient proxy(Supplier<ProxyOptions> proxyOptions);

    /**
     * Apply or remove a wire logger configuration.
     *
     * @param enableWiretap wiretap config
     * @return a HttpClient with wire logging enabled or disabled
     */
    HttpClient wiretap(boolean enableWiretap);

    /**
     * Set the port that client should connect to.
     *
     * @param port the port
     * @return a HttpClient with port applied
     */
    HttpClient port(int port);
}
