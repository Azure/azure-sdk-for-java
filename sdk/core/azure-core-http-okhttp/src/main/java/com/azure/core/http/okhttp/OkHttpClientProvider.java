// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on OkHttp.
 */
public class OkHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }
}
