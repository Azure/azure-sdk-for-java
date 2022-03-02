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

    @Override
    public HttpClient createInstance() {
        final String disableDefaultSharingHttpClient =
            Configuration.getGlobalConfiguration().get("AZURE_DISABLE_DEFAULT_SHARING_HTTP_CLIENT");
        if ("true".equalsIgnoreCase(disableDefaultSharingHttpClient)) {
            return new OkHttpAsyncHttpClientBuilder().build();
        }
        return GlobalOkHttpClient.HTTP_CLIENT.getHttpClient();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        OkHttpAsyncHttpClientBuilder builder = new OkHttpAsyncHttpClientBuilder();
        builder = builder.proxy(clientOptions.getProxyOptions())
                      .configuration(clientOptions.getConfiguration())
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
