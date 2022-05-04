// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;

/**
 * {@link HttpClientProvider} backed by the Vert.x {@link io.vertx.core.http.HttpClient}
 */
public class VertxAsyncHttpClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalVertxHttpClient {
        HTTP_CLIENT(new VertxAsyncHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalVertxHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * For testing purpose only, assigning 'AZURE_ENABLE_HTTP_CLIENT_SHARING' to 'enableHttpClientSharing' for
     * 'final' modifier.
     */
    public VertxAsyncHttpClientProvider() {
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    VertxAsyncHttpClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalVertxHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new VertxAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        return new VertxAsyncHttpClientBuilder()
            .proxy(clientOptions.getProxyOptions())
            .configuration(clientOptions.getConfiguration())
            .connectTimeout(clientOptions.getConnectTimeout())
            .idleTimeout(clientOptions.getConnectionIdleTimeout())
            .writeIdleTimeout(clientOptions.getWriteTimeout())
            .readIdleTimeout(clientOptions.getReadTimeout())
            .build();
    }
}
