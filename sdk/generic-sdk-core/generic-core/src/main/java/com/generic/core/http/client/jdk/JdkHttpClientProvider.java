// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.jdk;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;
import com.generic.core.http.models.HttpClientOptions;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpClient.
 * <p>
 * NOTE: This implementation is only available in Java 11+ as that is when {@link java.net.http.HttpClient} was
 * introduced.
 */
public final class JdkHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new JdkHttpClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        JdkHttpClientBuilder builder = new JdkHttpClientBuilder();
        builder = builder
            .configuration(clientOptions.getConfiguration())
            .connectionTimeout(clientOptions.getConnectTimeout());

        return builder.build();
    }
}
