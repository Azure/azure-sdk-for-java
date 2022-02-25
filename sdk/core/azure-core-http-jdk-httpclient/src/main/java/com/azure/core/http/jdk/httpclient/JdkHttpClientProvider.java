// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpClient.
 * <p>
 * NOTE: This implementation is only available in Java 11+ as that is when {@link java.net.http.HttpClient} was
 * introduced.
 */
public final class JdkHttpClientProvider implements HttpClientProvider {
    private static final AtomicReference<HttpClient> DEFAULT_HTTP_CLIENT =
        new AtomicReference<>(new JdkAsyncHttpClientBuilder().build());
    private static final String FALSE = "false";

    @Override
    public HttpClient createInstance() {
        if (FALSE.equalsIgnoreCase(Configuration.getGlobalConfiguration().get(("AZURE_HTTP_CLIENT_SHARED")))) {
            return new JdkAsyncHttpClientBuilder().build();
        }
        // by default use a singleton instance of http client
        return DEFAULT_HTTP_CLIENT.get();
    }
}
