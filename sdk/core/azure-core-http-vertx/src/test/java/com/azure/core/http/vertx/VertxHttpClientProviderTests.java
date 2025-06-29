// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link VertxHttpClientProvider}.
 */
public class VertxHttpClientProviderTests {

    @Test
    public void nullOptionsReturnsBaseClient() {
        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientProvider().createInstance(null);

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        io.vertx.core.net.ProxyOptions proxyOptions = httpClient.buildOptions.getProxyOptions();
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
        io.vertx.core.net.ProxyOptions proxyOptions = httpClient.buildOptions.getProxyOptions();
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

        io.vertx.core.net.ProxyOptions vertxProxyOptions = httpClient.buildOptions.getProxyOptions();
        assertNotNull(vertxProxyOptions);
        assertEquals(proxyOptions.getAddress().getHostName(), vertxProxyOptions.getHost());
        assertEquals(proxyOptions.getAddress().getPort(), vertxProxyOptions.getPort());
        assertEquals(proxyOptions.getType().name(), vertxProxyOptions.getType().name());
    }

    @Test
    public void optionsWithTimeouts() {
        Duration timeout = Duration.ofMillis(15000);
        HttpClientOptions clientOptions = new HttpClientOptions().setConnectTimeout(timeout)
            .setConnectionIdleTimeout(timeout)
            .setReadTimeout(timeout)
            .setWriteTimeout(timeout);

        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientProvider().createInstance(clientOptions);

        assertEquals(timeout.toMillis(), httpClient.buildOptions.getConnectTimeout());
        assertEquals(timeout.toMillis(), httpClient.buildOptions.getReadIdleTimeout());
        assertEquals(timeout.toMillis(), httpClient.buildOptions.getWriteIdleTimeout());
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
            vertx.close().andThen(event -> latch.countDown());
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
            vertx.close().andThen(event -> latch.countDown());
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
