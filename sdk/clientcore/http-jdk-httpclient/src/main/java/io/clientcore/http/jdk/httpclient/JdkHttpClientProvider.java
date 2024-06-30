// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpClient.
 * <p>
 * NOTE: This implementation is only available in Java 11+ as that is when {@link java.net.http.HttpClient} was
 * introduced.
 */
public final class JdkHttpClientProvider extends HttpClientProvider {
    // Enum Singleton Pattern
    private enum GlobalJdkHttpClient {
        HTTP_CLIENT(new JdkHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalJdkHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    @Override
    public HttpClient getNewInstance() {
        return new JdkHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return GlobalJdkHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
