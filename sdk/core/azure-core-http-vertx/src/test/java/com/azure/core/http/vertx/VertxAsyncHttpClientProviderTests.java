// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.azure.core.http.vertx.VertxAsyncClientTestHelper.getVertxInternalHttpClient;
import static com.azure.core.http.vertx.VertxAsyncClientTestHelper.getVertxInternalProxyFilter;
import static io.vertx.core.net.SocketAddress.inetSocketAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link VertxAsyncHttpClientProvider}.
 */
public class VertxAsyncHttpClientProviderTests {

    @Test
    public void nullOptionsReturnsBaseClient() {
        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) new VertxAsyncHttpClientProvider()
            .createInstance(null);

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        HttpClientImpl vertxHttpClient = getVertxInternalHttpClient(httpClient);
        io.vertx.core.http.HttpClientOptions options = vertxHttpClient.getOptions();
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
        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) new VertxAsyncHttpClientProvider()
            .createInstance(new HttpClientOptions());

        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());
        HttpClientImpl vertxHttpClient = getVertxInternalHttpClient(httpClient);
        io.vertx.core.http.HttpClientOptions options = vertxHttpClient.getOptions();
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

        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) new VertxAsyncHttpClientProvider()
            .createInstance(clientOptions);

        HttpClientImpl vertxHttpClient = getVertxInternalHttpClient(httpClient);
        io.vertx.core.http.HttpClientOptions options = vertxHttpClient.getOptions();

        io.vertx.core.net.ProxyOptions vertxProxyOptions = options.getProxyOptions();
        assertNotNull(vertxProxyOptions);
        assertEquals(proxyOptions.getAddress().getHostName(), vertxProxyOptions.getHost());
        assertEquals(proxyOptions.getAddress().getPort(), vertxProxyOptions.getPort());
        assertEquals(proxyOptions.getType().name(), vertxProxyOptions.getType().name());

        Predicate<SocketAddress> proxyFilter = getVertxInternalProxyFilter(vertxHttpClient);
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
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setConnectTimeout(timeout)
            .setConnectionIdleTimeout(timeout)
            .setReadTimeout(timeout)
            .setWriteTimeout(timeout);

        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) new VertxAsyncHttpClientProvider()
            .createInstance(clientOptions);

        HttpClientImpl vertxHttpClient = getVertxInternalHttpClient(httpClient);
        io.vertx.core.http.HttpClientOptions options = vertxHttpClient.getOptions();

        assertEquals(timeout.toMillis(), options.getConnectTimeout());
        assertEquals(timeout.getSeconds(), options.getIdleTimeout());
        assertEquals(timeout.getSeconds(), options.getReadIdleTimeout());
        assertEquals(timeout.getSeconds(), options.getWriteIdleTimeout());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void vertxProvider() throws Exception {
        Vertx vertx = Vertx.vertx();

        ServiceLoader mockServiceLoader = mock(ServiceLoader.class);
        VertxProvider mockVertxProvider = mock(VertxProvider.class);

        try (MockedStatic<ServiceLoader> serviceLoader = mockStatic(ServiceLoader.class)) {
            Set<VertxProvider> providers = new HashSet<>();
            providers.add(mockVertxProvider);

            Class<?> providerClass = VertxProvider.class;
            serviceLoader.when(() -> ServiceLoader.load(providerClass, providerClass.getClassLoader()))
                .thenReturn(mockServiceLoader);

            Mockito.when(mockServiceLoader.iterator()).thenReturn(providers.iterator());
            Mockito.when(mockVertxProvider.createVertx()).thenReturn(vertx);

            HttpClient httpClient = new VertxAsyncHttpClientProvider().createInstance();
            assertNotNull(httpClient);

            verify(mockServiceLoader, times(1)).iterator();
            verify(mockVertxProvider, times(1)).createVertx();
        } finally {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void multipleVertxProviders() throws Exception {
        Vertx vertx = Vertx.vertx();

        ServiceLoader mockServiceLoader = mock(ServiceLoader.class);
        VertxProvider mockVertxProviderA = mock(VertxProvider.class);
        VertxProvider mockVertxProviderB = mock(VertxProvider.class);

        try (MockedStatic<ServiceLoader> serviceLoader = mockStatic(ServiceLoader.class)) {
            Set<VertxProvider> providers = new LinkedHashSet<>();
            providers.add(mockVertxProviderA);
            providers.add(mockVertxProviderB);

            Class<?> providerClass = VertxProvider.class;
            serviceLoader.when(() -> ServiceLoader.load(providerClass, providerClass.getClassLoader()))
                .thenReturn(mockServiceLoader);

            Mockito.when(mockServiceLoader.iterator()).thenReturn(providers.iterator());
            Mockito.when(mockVertxProviderA.createVertx()).thenReturn(vertx);

            HttpClient httpClient = new VertxAsyncHttpClientProvider().createInstance();
            assertNotNull(httpClient);

            verify(mockServiceLoader, times(1)).iterator();
            verify(mockVertxProviderA, times(1)).createVertx();

            // Only the first provider should have been invoked
            verify(mockVertxProviderB, never()).createVertx();
        } finally {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }
}
