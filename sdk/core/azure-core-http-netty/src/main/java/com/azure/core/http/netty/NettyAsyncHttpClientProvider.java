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
    private static final int DEFAULT_MAX_CONNECTIONS = 500;

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
            } else {
                // reactor-netty (as of version 1.0.13) uses different default values for maxConnections when creating
                // HttpClient with and without a ConnectionProvider.
                // https://github.com/reactor/reactor-netty/blob/v1.0.13/reactor-netty-core/src/main/java/reactor/netty/tcp/TcpResources.java#L398

                // HttpClient.create() uses 500 connections (called when HttpClientOptions is not set)
                // HttpClient.create(ConnectionProvider) uses maxAvailableProcessors * 2 (minimum of 16) (called
                // when HttpClientOptions is set). This number can be very small depending on the host on which
                // applications run and can lead to issues like this - https://github.com/Azure/azure-sdk-for-java/issues/26027

                // So, we need to unfortunately hardcode the maxConnections to 500 (when user doesn't set it) to have
                // consistent configuration whether or not HttpClientOptions is set.
                connectionProviderBuilder.maxConnections(DEFAULT_MAX_CONNECTIONS);
            }

            builder = builder.connectionProvider(connectionProviderBuilder.build());
        }

        return builder.build();
    }
}
