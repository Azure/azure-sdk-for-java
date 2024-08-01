// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on OkHttp.
 */
public final class OkHttpHttpClientProvider extends HttpClientProvider {
    // Enum Singleton Pattern
    private enum GlobalOkHttpHttpClient {
        HTTP_CLIENT(new OkHttpHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalOkHttpHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    @Override
    public HttpClient getNewInstance() {
        return new OkHttpHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return GlobalOkHttpHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
