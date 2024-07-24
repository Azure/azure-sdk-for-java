// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.azure.core.http.vertx.VertxAsyncClientTestHelper.getVertxInternalProxyFilter;
import static io.vertx.core.net.SocketAddress.inetSocketAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link VertxHttpClientProvider}.
 */
public class VertxHttpClientProviderTests {

    @Test
    public void nullOptionsReturnsBaseClient() {
        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientProvider().createInstance(null);

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).options();
        io.vertx.core.net.ProxyOptions proxyOptions = options.getProxyOptions();
        if (environmentProxy == null) {
            assertNull(proxyOptions);
        } else {
            assertNotNull(proxyOptions);
            assertEquals(environmentProxy.getAddress().getHostName(), proxyOptions.getHost());
        }
    }

    @Test
    public void defaultOptionsReturnsBaseClient() {
        VertxHttpClient httpClient
            = (VertxHttpClient) new VertxHttpClientProvider().createInstance(new HttpClientOptions());

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).options();
        io.vertx.core.net.ProxyOptions proxyOptions = options.getProxyOptions();
        if (environmentProxy == null) {
            assertNull(proxyOptions);
        } else {
            assertNotNull(proxyOptions);
            assertEquals(environmentProxy.getAddress().getHostName(), proxyOptions.getHost());
        }
    }

    @Test
    public void optionsWithAProxy() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888));
        proxyOptions.setNonProxyHosts("foo.*|bar.*|cheese.com|wine.org");

        HttpClientOptions clientOptions = new HttpClientOptions().setProxyOptions(proxyOptions);

        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientProvider().createInstance(clientOptions);

        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).options();

        io.vertx.core.net.ProxyOptions vertxProxyOptions = options.getProxyOptions();
        assertNotNull(vertxProxyOptions);
        assertEquals(proxyOptions.getAddress().getHostName(), vertxProxyOptions.getHost());
        assertEquals(proxyOptions.getAddress().getPort(), vertxProxyOptions.getPort());
        assertEquals(proxyOptions.getType().name(), vertxProxyOptions.getType().name());

        Predicate<SocketAddress> proxyFilter = getVertxInternalProxyFilter((HttpClientImpl) httpClient.client);
        assertFalse(proxyFilter.test(inetSocketAddress(80, "foo.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "foo.bar.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "bar.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "cheese.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "wine.org")));
        assertTrue(proxyFilter.test(inetSocketAddress(80, "allowed.host.com")));
    }

    @Test
    public void optionsWithTimeouts() {
        Duration timeout = Duration.ofMillis(15000);
        HttpClientOptions clientOptions = new HttpClientOptions().setConnectTimeout(timeout)
            .setConnectionIdleTimeout(timeout)
            .setReadTimeout(timeout)
            .setWriteTimeout(timeout);

        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientProvider().createInstance(clientOptions);

        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).options();

        assertEquals(timeout.toMillis(), options.getConnectTimeout());
        assertEquals(timeout.toMillis(), options.getReadIdleTimeout());
        assertEquals(timeout.toMillis(), options.getWriteIdleTimeout());
    }

    @Test
    public void vertxProvider() throws Exception {
        Vertx vertx = Vertx.vertx();

        CreateCountVertxProvider mockVertxProvider = new CreateCountVertxProvider(vertx);

        try {
            Vertx vertxSelectedByBuilder = VertxHttpClientBuilder
                .getVertx(Collections.singletonList((VertxProvider) mockVertxProvider).iterator());

            assertEquals(1, mockVertxProvider.getCreateCount());
            assertSame(vertx, vertxSelectedByBuilder);
        } finally {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void multipleVertxProviders() throws Exception {
        Vertx vertx = Vertx.vertx();

        CreateCountVertxProvider mockVertxProviderA = new CreateCountVertxProvider(vertx);
        CreateCountVertxProvider mockVertxProviderB = new CreateCountVertxProvider(vertx);

        try {
            Vertx vertxSelectedByBuilder = VertxHttpClientBuilder
                .getVertx(Arrays.asList((VertxProvider) mockVertxProviderA, mockVertxProviderB).iterator());

            // Only the first provider should have been invoked
            assertEquals(1, mockVertxProviderA.getCreateCount());
            assertEquals(0, mockVertxProviderB.getCreateCount());

            assertSame(vertx, vertxSelectedByBuilder);
        } finally {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    private static final class CreateCountVertxProvider implements VertxProvider {
        private final AtomicInteger createCount = new AtomicInteger();
        private final Vertx vertx;

        CreateCountVertxProvider(Vertx vertx) {
            this.vertx = vertx;
        }

        @Override
        public Vertx createVertx() {
            createCount.incrementAndGet();
            return vertx;
        }

        int getCreateCount() {
            return createCount.get();
        }
    }
}
