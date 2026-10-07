// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.validation.http.HttpClientOptionsProviderTests;
import okhttp3.Protocol;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link OkHttpAsyncClientProvider}.
 */
public class OkHttpAsyncClientProviderTests extends HttpClientOptionsProviderTests {
    @Test
    public void nullOptionsReturnsBaseClient() {
        OkHttpAsyncHttpClient httpClient = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider().createInstance(null);

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
        OkHttpAsyncHttpClient httpClient
            = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider().createInstance(new HttpClientOptions());

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

        OkHttpAsyncHttpClient httpClient
            = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider().createInstance(clientOptions);

        // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
        ProxySelector proxySelector = httpClient.httpClient.proxySelector();
        assertNotNull(proxySelector);
        assertEquals(proxyOptions.getAddress(), proxySelector.select(null).get(0).address());
    }

    @Test
    public void optionsWithTimeouts() {
        long expectedTimeout = 15000;
        Duration timeout = Duration.ofMillis(expectedTimeout);
        HttpClientOptions clientOptions = new HttpClientOptions().setConnectTimeout(timeout)
            .setWriteTimeout(timeout)
            .setResponseTimeout(timeout)
            .setReadTimeout(timeout);

        OkHttpAsyncHttpClient httpClient
            = (OkHttpAsyncHttpClient) new OkHttpAsyncClientProvider().createInstance(clientOptions);

        assertEquals(expectedTimeout, httpClient.httpClient.connectTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.writeTimeoutMillis());
        assertEquals(expectedTimeout, httpClient.httpClient.readTimeoutMillis());
    }

    @Override
    protected HttpClientProvider createProvider(Configuration configuration) {
        return new OkHttpAsyncClientProvider(configuration);
    }

    @Override
    protected void assertMaximumHttpVersion(HttpClient client, HttpProtocolVersion version) {
        OkHttpAsyncHttpClient okHttpClient = assertInstanceOf(OkHttpAsyncHttpClient.class, client);
        assertEquals(version == HttpProtocolVersion.HTTP_1_1
            ? Collections.singletonList(Protocol.HTTP_1_1)
            : Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1), okHttpClient.httpClient.protocols());
    }

    @Override
    protected void closeHttpClient(HttpClient client) {
        okhttp3.OkHttpClient nativeClient = ((OkHttpAsyncHttpClient) client).httpClient;
        nativeClient.connectionPool().evictAll();
        nativeClient.dispatcher().executorService().shutdown();
    }
}
