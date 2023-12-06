// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.models.HttpClientOptions;
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
    public void nullOptionsReturnsBaseClient() {
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider()
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
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider()
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
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider()
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
            .setConnectTimeout(timeout)
            .setWriteTimeout(timeout)
            .setResponseTimeout(timeout)
            .setReadTimeout(timeout);
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider()
            .createInstance(clientOptions);

        assertEquals(expectedTimeout, httpClient.httpClient.connectTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.writeTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.readTimeoutMillis());
    }
}
