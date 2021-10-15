// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import reactor.netty.resources.ConnectionProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on Netty.
 */
public final class NettyAsyncHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new NettyAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder();

        if (clientOptions != null) {
            builder = builder.proxy(clientOptions.getProxyOptions())
                .configuration(clientOptions.getConfiguration())
                .writeTimeout(clientOptions.getWriteTimeout())
                .responseTimeout(clientOptions.getResponseTimeout())
                .readTimeout(clientOptions.getReadTimeout());

            ConnectionProvider.Builder connectionProviderBuilder = ConnectionProvider.builder("azure-sdk");
            connectionProviderBuilder.maxIdleTime(clientOptions.getConnectionIdleTimeout());

            // Only configure the maximum connections if it has been set in the options.
            Integer maximumConnectionPoolSize = clientOptions.getMaximumConnectionPoolSize();
            if (maximumConnectionPoolSize != null && maximumConnectionPoolSize > 0) {
                connectionProviderBuilder.maxConnections(maximumConnectionPoolSize);
            }

            builder = builder.connectionProvider(connectionProviderBuilder.build());
        }

        return builder.build();
    }
}
