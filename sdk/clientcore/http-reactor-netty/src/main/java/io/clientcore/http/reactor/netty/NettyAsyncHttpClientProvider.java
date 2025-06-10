// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.reactor.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

/**
 * <p>
 * The NettyAsyncHttpClientProvider class is an implementation of the HttpClientProvider interface that provides
 * an instance of HttpClient based on Netty. Instances are either shared or a newly created based
 * on the configuration.
 * </p>
 *
 * @see com.azure.core.http.netty
 * @see NettyAsyncHttpClient
 * @see HttpClient
 */
public final class NettyAsyncHttpClientProvider extends HttpClientProvider {

    // Enum Singleton Pattern
    private enum GlobalNettyHttpClient {
        HTTP_CLIENT(new NettyAsyncHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalNettyHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    @Override
    public HttpClient getNewInstance() {
        return new NettyAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return NettyAsyncHttpClientProvider.GlobalNettyHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
