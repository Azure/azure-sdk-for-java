// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import okhttp3.ConnectionPool;

import java.util.concurrent.TimeUnit;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on OkHttp.
 */
public final class OkHttpAsyncClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        OkHttpAsyncHttpClientBuilder builder = new OkHttpAsyncHttpClientBuilder();

        if (clientOptions != null) {
            builder = builder.proxy(clientOptions.getProxyOptions())
                .configuration(clientOptions.getConfiguration())
                .writeTimeout(clientOptions.getWriteTimeout())
                .readTimeout(clientOptions.getReadTimeout());

            int maximumConnectionPoolSize = (clientOptions.getMaximumConnectionPoolSize() == 0)
                ? 5 // By default OkHttp uses a maximum idle connection count of 5.
                : clientOptions.getMaximumConnectionPoolSize();

            ConnectionPool connectionPool = new ConnectionPool(maximumConnectionPoolSize,
                clientOptions.getConnectionIdleTimeout().toMillis(), TimeUnit.MILLISECONDS);

            builder = builder.connectionPool(connectionPool);
        }

        return builder.build();
    }
}
