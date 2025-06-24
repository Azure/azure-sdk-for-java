// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.AsyncHttpClient;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;

/**
 * An enum containing the global {@link JdkHttpClient}.
 */
public enum GlobalJdkHttpClient {
    HTTP_CLIENT(new JdkHttpClientBuilder().build());

    private final AsyncHttpClient asyncHttpClient;
    private final HttpClient httpClient;

    <T extends AsyncHttpClient & HttpClient> GlobalJdkHttpClient(T httpClient) {
        this.asyncHttpClient = httpClient;
        this.httpClient = httpClient;
    }

    /**
     * Get the global {@link AsyncHttpClient} instance.
     *
     * @return The global {@link AsyncHttpClient} instance.
     */
    public AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    /**
     * Get the global {@link JdkHttpClient} instance.
     *
     * @return The global {@link JdkHttpClient} instance.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
