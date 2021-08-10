// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link NettyAsyncHttpClientProvider}.
 */
public class NettyAsyncHttpClientProviderTests {
    @Test
    public void nullOptionsReturnsBaseClient() {
        NettyAsyncHttpClient httpClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientProvider()
            .createInstance(null);

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        if (environmentProxy == null) {
            assertFalse(httpClient.nettyClient.configuration().hasProxy());
        } else {
            assertTrue(httpClient.nettyClient.configuration().hasProxy());

            ProxyProvider proxyProvider = httpClient.nettyClient.configuration().proxyProvider();
            assertEquals(environmentProxy.getAddress(), proxyProvider.getAddress().get());
        }
    }

    @Test
    public void defaultOptionsReturnsBaseClient() {
        NettyAsyncHttpClient httpClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientProvider()
            .createInstance(new HttpClientOptions());

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        if (environmentProxy == null) {
            assertFalse(httpClient.nettyClient.configuration().hasProxy());
        } else {
            assertTrue(httpClient.nettyClient.configuration().hasProxy());

            ProxyProvider proxyProvider = httpClient.nettyClient.configuration().proxyProvider();
            assertEquals(environmentProxy.getAddress(), proxyProvider.getAddress().get());
        }
    }

    @Test
    public void optionsWithAProxy() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888));
        HttpClientOptions clientOptions = new HttpClientOptions().setProxyOptions(proxyOptions);

        NettyAsyncHttpClient httpClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientProvider()
            .createInstance(clientOptions);

        assertTrue(httpClient.nettyClient.configuration().hasProxy());

        ProxyProvider proxyProvider = httpClient.nettyClient.configuration().proxyProvider();
        assertEquals(proxyOptions.getAddress(), proxyProvider.getAddress().get());
    }

    @Test
    public void optionsWithTimeouts() {
        long expectedTimeout = 15000;
        Duration timeout = Duration.ofMillis(expectedTimeout);
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setWriteTimeout(timeout)
            .setResponseTimeout(timeout)
            .setReadTimeout(timeout);

        NettyAsyncHttpClient httpClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientProvider()
            .createInstance(clientOptions);

        assertEquals(expectedTimeout, httpClient.writeTimeout);
        assertEquals(expectedTimeout, httpClient.responseTimeout);
        assertEquals(expectedTimeout, httpClient.readTimeout);
    }
}
