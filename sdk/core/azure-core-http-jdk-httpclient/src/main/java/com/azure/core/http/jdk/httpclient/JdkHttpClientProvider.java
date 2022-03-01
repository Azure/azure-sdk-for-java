// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpClient.
 * <p>
 * NOTE: This implementation is only available in Java 11+ as that is when {@link java.net.http.HttpClient} was
 * introduced.
 */
public final class JdkHttpClientProvider implements HttpClientProvider {
    // Enum Singleton Pattern
    private enum GlobalJdkAsyncHttpClient {
        HTTP_CLIENT(new JdkAsyncHttpClientBuilder().build());

        private HttpClient httpClient;

        GlobalJdkAsyncHttpClient(HttpClient httpClient) {
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
            return new JdkAsyncHttpClientBuilder().build();
        }
        return GlobalJdkAsyncHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
