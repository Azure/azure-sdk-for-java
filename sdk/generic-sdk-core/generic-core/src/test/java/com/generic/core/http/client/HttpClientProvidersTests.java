// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.client.httpurlconnection.HttpUrlConnectionClient;
import com.generic.core.http.client.httpurlconnection.HttpUrlConnectionClientProvider;
import com.generic.core.http.models.HttpClientOptions;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.util.configuration.Configuration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpClientProvidersTests {
    @Test
    public void testHttpUrlConnectionAsDefaultProvider() {
        HttpClientOptions options = new HttpClientOptions();
        options.setHttpClientProvider(HttpUrlConnectionClientProvider.class);
        // sanity check
        HttpClient httpClient = HttpClient.createDefault(options);
        assertInstanceOf(HttpUrlConnectionClient.class, httpClient);
    }

    @Test
    public void testNettyAsExplicitProvider() {
        HttpClientOptions options = new HttpClientOptions();
        options.setHttpClientProvider(HttpUrlConnectionClientProvider.class);
        // sanity check
        HttpClient httpClient = HttpClient.createDefault(options);
        assertInstanceOf(HttpUrlConnectionClient.class, httpClient);
    }

    @Test
    public void testIncorrectExplicitProvider() {
        HttpClientOptions options = new HttpClientOptions();
        options.setHttpClientProvider(AnotherHttpClientProvider.class);
        assertThrows(IllegalStateException.class, () -> HttpClient.createDefault(options));
    }

    class AnotherHttpClientProvider implements HttpClientProvider {
        @Override
        public HttpClient createInstance() {
            throw new IllegalStateException("should never be called");
        }
    }
}
