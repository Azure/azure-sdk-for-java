// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk11.httpclient.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.jdk11.httpclient.Jdk11AsyncHttpClientBuilder;
import com.azure.core.test.implementation.RestProxyTests;

public class RestProxyWithJdk11HttpClientTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return new Jdk11AsyncHttpClientBuilder().build();
    }
}
