// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder;
import com.azure.core.test.implementation.RestProxyTests;

public class RestProxyWithJdkHttpClientTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return new JdkAsyncHttpClientBuilder().build();
    }
}
