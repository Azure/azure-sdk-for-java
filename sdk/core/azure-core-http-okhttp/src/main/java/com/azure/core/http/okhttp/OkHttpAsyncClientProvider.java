// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import okhttp3.ConnectionPool;

import java.util.concurrent.TimeUnit;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on OkHttp.
 */
public final class OkHttpAsyncClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalOkHttpClient {
        HTTP_CLIENT(new OkHttpAsyncHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalOkHttpClient(HttpClient httpClient) {
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
    public OkHttpAsyncClientProvider() {
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    OkHttpAsyncClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalOkHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new OkHttpAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        OkHttpAsyncHttpClientBuilder builder = new OkHttpAsyncHttpClientBuilder();
        builder = builder.proxy(clientOptions.getProxyOptions())
            .configuration(clientOptions.getConfiguration())
            .connectionTimeout(clientOptions.getConnectTimeout())
            .writeTimeout(clientOptions.getWriteTimeout())
            .readTimeout(clientOptions.getReadTimeout());

        Integer poolSize = clientOptions.getMaximumConnectionPoolSize();
        int maximumConnectionPoolSize = (poolSize != null && poolSize > 0)
            ? poolSize
            : 5; // By default, OkHttp uses a maximum idle connection count of 5.

        ConnectionPool connectionPool = new ConnectionPool(maximumConnectionPoolSize,
            clientOptions.getConnectionIdleTimeout().toMillis(), TimeUnit.MILLISECONDS);

        builder = builder.connectionPool(connectionPool);

        return builder.build();
    }
}
