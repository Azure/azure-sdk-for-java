// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link NettyHttpClientProvider}.
 */
@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class NettyHttpClientProviderTests {
    @Test
    public void testGetSharedClient() {
        NettyHttpClient httpClient = (NettyHttpClient) new NettyHttpClientProvider().getSharedInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        assertSame(httpClient, new NettyHttpClientProvider().getSharedInstance());

        if (environmentProxy == null) {
            assertNull(httpClient.getProxyOptions());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            ProxyOptions proxyOptions = httpClient.getProxyOptions();

            assertNotNull(proxyOptions);
            assertEquals(environmentProxy.getAddress(), proxyOptions.getAddress());
            assertEquals(environmentProxy.getNonProxyHosts(), proxyOptions.getNonProxyHosts());
            assertEquals(environmentProxy.getPassword(), proxyOptions.getPassword());
            assertEquals(environmentProxy.getUsername(), proxyOptions.getUsername());
            assertEquals(environmentProxy.getType(), proxyOptions.getType());
        }
    }

    @Test
    public void testGetNewClient() {
        NettyHttpClient httpClient = (NettyHttpClient) new NettyHttpClientProvider().getNewInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        assertNotSame(httpClient, new NettyHttpClientProvider().getSharedInstance());
        assertNotSame(httpClient, new NettyHttpClientProvider().getNewInstance());

        if (environmentProxy == null) {
            assertNull(httpClient.getProxyOptions());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            ProxyOptions proxyOptions = httpClient.getProxyOptions();

            assertNotNull(proxyOptions);
            assertEquals(environmentProxy.getAddress(), proxyOptions.getAddress());
            assertEquals(environmentProxy.getNonProxyHosts(), proxyOptions.getNonProxyHosts());
            assertEquals(environmentProxy.getPassword(), proxyOptions.getPassword());
            assertEquals(environmentProxy.getUsername(), proxyOptions.getUsername());
            assertEquals(environmentProxy.getType(), proxyOptions.getType());
        }
    }
}
