// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;

import java.util.concurrent.TimeUnit;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on Apache Http.
 */
public final class ApacheHttpAsyncClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalOkHttpClient {
        HTTP_CLIENT(new ApacheHttpAsyncHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalOkHttpClient(HttpClient httpClient) {
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
    public ApacheHttpAsyncClientProvider() {
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    ApacheHttpAsyncClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalOkHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new ApacheHttpAsyncHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        ApacheHttpAsyncHttpClientBuilder builder = new ApacheHttpAsyncHttpClientBuilder();
        builder = builder.proxy(clientOptions.getProxyOptions())
                      .configuration(clientOptions.getConfiguration());

        return builder.build();
    }
}
