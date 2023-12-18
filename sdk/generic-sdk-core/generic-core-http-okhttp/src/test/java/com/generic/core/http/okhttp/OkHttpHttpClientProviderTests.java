// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.util.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link OkHttpHttpClientProvider}.
 */
public class OkHttpHttpClientProviderTests {
    @Test
    public void getBaseClient() {
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider()
            .createInstance();

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        if (environmentProxy == null) {
            assertNull(httpClient.httpClient.proxy());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            ProxySelector proxySelector = httpClient.httpClient.proxySelector();

            assertNotNull(proxySelector);
            assertEquals(environmentProxy.getAddress(), proxySelector.select(null).get(0).address());
        }
    }

    @Test
    public void optionsWithAProxy() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888));
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new AnotherOkHttpHttpClientProvider()
            .createInstance(proxyOptions);

        // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
        ProxySelector proxySelector = httpClient.httpClient.proxySelector();

        assertNotNull(proxySelector);
        assertEquals(proxyOptions.getAddress(), proxySelector.select(null).get(0).address());
    }

    @Test
    public void optionsWithTimeouts() {
        long expectedTimeout = 15000;
        Duration timeout = Duration.ofMillis(expectedTimeout);
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new AnotherOkHttpHttpClientProvider()
            .createInstance(timeout);

        assertEquals(expectedTimeout, httpClient.httpClient.connectTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.writeTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.readTimeoutMillis());
    }

    class AnotherOkHttpHttpClientProvider implements HttpClientProvider {
        @Override
        public HttpClient createInstance() {
            return new OkHttpHttpClientBuilder().build();
        }

        public HttpClient createInstance(ProxyOptions proxyOptions) {
            return new OkHttpHttpClientBuilder().proxy(proxyOptions).build();
        }

        public HttpClient createInstance(Duration timeout) {
            return new OkHttpHttpClientBuilder()
                .connectionTimeout(timeout)
                .writeTimeout(timeout)
                .readTimeout(timeout)
                .callTimeout(timeout)
                .build();
        }
    }
}
