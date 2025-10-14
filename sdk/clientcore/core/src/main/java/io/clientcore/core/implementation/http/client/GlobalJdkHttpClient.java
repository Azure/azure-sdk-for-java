// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;

/**
 * An enum containing the global {@link JdkHttpClient}.
 */
public enum GlobalJdkHttpClient {
    HTTP_CLIENT(new JdkHttpClientBuilder().build());

    private final HttpClient httpClient;

    GlobalJdkHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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
