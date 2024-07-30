// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on OkHttp.
 */
public final class OkHttpHttpClientProvider extends HttpClientProvider {
    @Override
    public HttpClient getNewInstance() {
        return new OkHttpHttpClientBuilder().build();
    }
}
