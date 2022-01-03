// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link OkHttpAsyncClientProvider}.
 */
public class OkHttpAsyncClientProviderTests {
    @Test
    public void nullOptionsReturnsBaseClient() {
        OkHttpAsyncHttpClient httpClient = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider()
            .createInstance(null);

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
    public void defaultOptionsReturnsBaseClient() {
        OkHttpAsyncHttpClient httpClient = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider()
            .createInstance(new HttpClientOptions());

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
        HttpClientOptions clientOptions = new HttpClientOptions().setProxyOptions(proxyOptions);

        OkHttpAsyncHttpClient httpClient = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider()
            .createInstance(clientOptions);

        // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
        ProxySelector proxySelector = httpClient.httpClient.proxySelector();
        assertNotNull(proxySelector);
        assertEquals(proxyOptions.getAddress(), proxySelector.select(null).get(0).address());
    }

    @Test
    public void optionsWithTimeouts() {
        long expectedTimeout = 15000;
        Duration timeout = Duration.ofMillis(expectedTimeout);
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setWriteTimeout(timeout)
            .setResponseTimeout(timeout)
            .setReadTimeout(timeout);

        OkHttpAsyncHttpClient httpClient = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider()
            .createInstance(clientOptions);

        assertEquals(expectedTimeout, httpClient.httpClient.writeTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.readTimeoutMillis());
    }
}
