// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;

/**
 * A default http client provider.
 */
public final class DefaultHttpProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return HttpClient.createDefault();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        return HttpClient.createDefault(clientOptions);
    }
}
