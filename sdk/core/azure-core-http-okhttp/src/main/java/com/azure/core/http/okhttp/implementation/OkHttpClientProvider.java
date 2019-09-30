// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;

public class OkHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }
}
