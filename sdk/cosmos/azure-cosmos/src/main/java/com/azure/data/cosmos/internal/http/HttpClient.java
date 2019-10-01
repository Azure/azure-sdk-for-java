// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.http;

import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;

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
     * Create fixed HttpClient with {@link HttpClientConfig}
     *
     * @return the HttpClient
     */
    static HttpClient createFixed(HttpClientConfig httpClientConfig) {
        if (httpClientConfig.getConfigs() == null) {
            throw new IllegalArgumentException("HttpClientConfig is null");
        }

        if (httpClientConfig.getMaxPoolSize() == null) {
            return new ReactorNettyClient(ConnectionProvider.fixed(httpClientConfig.getConfigs().getReactorNettyConnectionPoolName()), httpClientConfig);
        }
        return new ReactorNettyClient(ConnectionProvider.fixed(httpClientConfig.getConfigs().getReactorNettyConnectionPoolName(), httpClientConfig.getMaxPoolSize()), httpClientConfig);
    }

    /**
     * Shutdown the Http Client and clean up resources
     */
    void shutdown();
}
