// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.client;

import com.generic.core.http.client.DefaultHttpClientBuilder;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpURLConnection.
 */
public class DefaultHttpClientProvider extends HttpClientProvider {
    @Override
    public HttpClient getNewInstance() {
        return new DefaultHttpClientBuilder().build();
    }
}
