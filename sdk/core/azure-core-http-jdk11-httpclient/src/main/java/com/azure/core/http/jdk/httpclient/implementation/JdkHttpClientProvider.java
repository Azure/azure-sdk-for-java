// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder;

public class JdkHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new JdkAsyncHttpClientBuilder().build();
    }
}
