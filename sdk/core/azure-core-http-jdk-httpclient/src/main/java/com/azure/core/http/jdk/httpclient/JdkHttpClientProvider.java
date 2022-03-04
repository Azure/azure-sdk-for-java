// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpClient.
 * <p>
 * NOTE: This implementation is only available in Java 11+ as that is when {@link java.net.http.HttpClient} was
 * introduced.
 */
public final class JdkHttpClientProvider implements HttpClientProvider {
    private static final boolean AZURE_DISABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_DISABLE_HTTP_CLIENT_SHARING", Boolean.TRUE);
    private final boolean disableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalJdkAsyncHttpClient {
        HTTP_CLIENT(new JdkAsyncHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalJdkAsyncHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * For testing purpose only, assigning 'AZURE_DISABLE_HTTP_CLIENT_SHARING' to 'disableHttpClientSharing' for
     * 'final' modifier.
     */
    public JdkHttpClientProvider() {
        disableHttpClientSharing = AZURE_DISABLE_HTTP_CLIENT_SHARING;
    }

    JdkHttpClientProvider(Configuration configuration) {
        disableHttpClientSharing = configuration.get("AZURE_DISABLE_HTTP_CLIENT_SHARING", Boolean.TRUE);
    }

    @Override
    public HttpClient createInstance() {
        if (disableHttpClientSharing) {
            return new JdkAsyncHttpClientBuilder().build();
        }
        return GlobalJdkAsyncHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
