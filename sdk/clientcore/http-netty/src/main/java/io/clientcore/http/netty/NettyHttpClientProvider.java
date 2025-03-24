// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

/**
 * Provider class for creating instances of HttpClient using plain Netty.
 */
public final class NettyHttpClientProvider extends HttpClientProvider {
    private enum GlobalNettyHttpClient {
        HTTP_CLIENT(new NettyHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalNettyHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * Creates a new instance of {@link NettyHttpClientProvider}.
     */
    public NettyHttpClientProvider() {
    }

    @Override
    public HttpClient getNewInstance() {
        return new NettyHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return NettyHttpClientProvider.GlobalNettyHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
