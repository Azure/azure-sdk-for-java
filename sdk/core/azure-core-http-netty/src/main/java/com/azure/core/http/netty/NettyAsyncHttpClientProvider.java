// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import reactor.netty.resources.ConnectionProvider;

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
public final class NettyAsyncHttpClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING
        = Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;
    private static final int DEFAULT_MAX_CONNECTIONS = 500;

    private static final ClientLogger LOGGER = new ClientLogger(NettyAsyncHttpClientProvider.class);

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

    /**
     * For testing purpose only, assigning 'AZURE_ENABLE_HTTP_CLIENT_SHARING' to 'enableHttpClientSharing' for 'final'
     * modifier.
     */
    public NettyAsyncHttpClientProvider() {
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    NettyAsyncHttpClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalNettyHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new NettyAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder();
        builder = builder.proxy(clientOptions.getProxyOptions())
            .configuration(clientOptions.getConfiguration())
            .connectTimeout(clientOptions.getConnectTimeout())
            .writeTimeout(clientOptions.getWriteTimeout())
            .responseTimeout(clientOptions.getResponseTimeout())
            .readTimeout(clientOptions.getReadTimeout());

        ConnectionProvider.Builder connectionProviderBuilder = ConnectionProvider.builder("azure-sdk");
        connectionProviderBuilder.maxIdleTime(clientOptions.getConnectionIdleTimeout());

        // Only configure the maximum connections if it has been set in the options.
        Integer maximumConnectionPoolSize = clientOptions.getMaximumConnectionPoolSize();
        if (maximumConnectionPoolSize != null && maximumConnectionPoolSize > 0) {
            LOGGER.verbose(
                "Setting Reactor Netty ConnectionProvider's maximum connections to " + maximumConnectionPoolSize + ".");
            connectionProviderBuilder.maxConnections(maximumConnectionPoolSize);
        } else {
            // reactor-netty (as of version 1.0.13) uses different default values for maxConnections when creating
            // HttpClient with and without a ConnectionProvider.
            // https://github.com/reactor/reactor-netty/blob/v1.0.13/reactor-netty-core/src/main/java/reactor/netty/tcp/TcpResources.java#L398

            // HttpClient.create() uses 500 connections (called when HttpClientOptions is not set)
            // HttpClient.create(ConnectionProvider) uses maxAvailableProcessors * 2 (minimum of 16) (called
            // when HttpClientOptions is set). This number can be very small depending on the host on which
            // applications run and can lead to issues like this -
            // https://github.com/Azure/azure-sdk-for-java/issues/26027

            // So, we need to unfortunately hardcode the maxConnections to 500 (when user doesn't set it) to have
            // consistent configuration whether HttpClientOptions is set.
            connectionProviderBuilder.maxConnections(DEFAULT_MAX_CONNECTIONS);
        }

        builder = builder.connectionProviderInternal(connectionProviderBuilder.build());

        return builder.build();
    }
}
