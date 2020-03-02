// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk11.httpclient.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.jdk11.httpclient.Jdk11AsyncHttpClientBuilder;

public class Jdk11HttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new Jdk11AsyncHttpClientBuilder().build();
    }
}
