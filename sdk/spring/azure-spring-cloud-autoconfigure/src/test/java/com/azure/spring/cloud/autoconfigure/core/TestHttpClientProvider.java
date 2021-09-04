// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;

public class TestHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new TestHttpClient();
    }
}
