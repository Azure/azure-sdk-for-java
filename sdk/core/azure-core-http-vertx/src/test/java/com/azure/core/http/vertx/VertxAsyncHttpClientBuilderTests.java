// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.azure.core.http.vertx.VertxAsyncClientTestHelper.getVertxInternalProxyFilter;
import static io.vertx.core.net.SocketAddress.inetSocketAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link VertxAsyncHttpClientBuilder}.
 */
public class VertxAsyncHttpClientBuilderTests {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @Test
    public void buildWithConfigurationNone() {
        HttpClient httpClient = new VertxAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        String defaultPath = "/default";
        WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
    }

    @Test
    public void buildWithDefaultConnectionOptions() {
        VertxAsyncHttpClientBuilder builder = new VertxAsyncHttpClientBuilder();
        HttpClient httpClient = builder.build();

        io.vertx.core.http.HttpClient client = ((VertxAsyncHttpClient) httpClient).client;
        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) client).getOptions();

        String defaultPath = "/default";
        WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();

            assertEquals(10000, options.getConnectTimeout());
            assertEquals(60, options.getIdleTimeout());
            assertEquals(60, options.getReadIdleTimeout());
            assertEquals(60, options.getWriteIdleTimeout());
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
    }

    @Test
    public void buildWithConnectionOptions() {
        VertxAsyncHttpClientBuilder builder = new VertxAsyncHttpClientBuilder();
        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) builder.connectTimeout(Duration.ofSeconds(10))
            .idleTimeout(Duration.ofSeconds(20))
            .readIdleTimeout(Duration.ofSeconds(30))
            .writeIdleTimeout(Duration.ofSeconds(40))
            .build();

        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).getOptions();

        String defaultPath = "/default";
        WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();

            assertEquals(10000, options.getConnectTimeout());
            assertEquals(20, options.getIdleTimeout());
            assertEquals(30, options.getReadIdleTimeout());
            assertEquals(40, options.getWriteIdleTimeout());
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ProxyOptions.Type.class)
    public void buildWithAllProxyTypes(ProxyOptions.Type type) throws Exception {
        if (type.equals(ProxyOptions.Type.SOCKS5)) {
            return;
        }

        String proxyUser = "user";
        String proxyPassword = "secret";

        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        ProxyOptions proxyOptions = new ProxyOptions(type, address);
        proxyOptions.setCredentials("user", "secret");
        proxyOptions.setNonProxyHosts("foo.*|*bar.com|microsoft.com");

        VertxAsyncHttpClient httpClient = (VertxAsyncHttpClient) new VertxAsyncHttpClientBuilder()
            .proxy(proxyOptions)
            .build();

        HttpClientImpl vertxHttpClientImpl = (HttpClientImpl) httpClient.client;
        io.vertx.core.http.HttpClientOptions options = vertxHttpClientImpl.getOptions();

        io.vertx.core.net.ProxyOptions vertxProxyOptions = options.getProxyOptions();
        assertNotNull(vertxProxyOptions);
        assertEquals(address.getHostName(), vertxProxyOptions.getHost());
        assertEquals(address.getPort(), vertxProxyOptions.getPort());
        assertEquals(type.name(), vertxProxyOptions.getType().name());
        assertEquals(proxyUser, vertxProxyOptions.getUsername());
        assertEquals(proxyPassword, vertxProxyOptions.getPassword());

        Predicate<SocketAddress> proxyFilter = getVertxInternalProxyFilter(vertxHttpClientImpl);
        assertFalse(proxyFilter.test(inetSocketAddress(80, "foo.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "foo.bar.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "test.bar.com")));
        assertFalse(proxyFilter.test(inetSocketAddress(80, "microsoft.com")));
        assertTrue(proxyFilter.test(inetSocketAddress(80, "allowed.host.com")));
    }

    @Test
    public void buildWithHttpProxy() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .proxy(clientProxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTP_PROXY, "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
                    .put("java.net.useSystemProxies", "true"))
                .build();

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .configuration(configuration)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxyFromExplicitConfiguration() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder()
                .putProperty("http.proxy.hostname", proxyEndpoint.getHost())
                .putProperty("http.proxy.port", String.valueOf(proxyEndpoint.getPort()))
                .build();

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .configuration(configuration)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithCustomVertx() throws Exception {
        Vertx vertx = Vertx.vertx();

        HttpClient httpClient = new VertxAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .vertx(vertx)
            .build();

        String defaultPath = "/default";
        WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }

            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    public void buildWithCustomHttpClientOptions() {
        HttpClientOptions options = new HttpClientOptions();
        options.setConnectTimeout(30000);
        options.setIdleTimeout(50);
        options.setReadIdleTimeout(60);
        options.setWriteIdleTimeout(70);

        HttpClient httpClient = new VertxAsyncHttpClientBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .idleTimeout(Duration.ofSeconds(20))
            .readIdleTimeout(Duration.ofSeconds(30))
            .writeIdleTimeout(Duration.ofSeconds(40))
            .httpClientOptions(options)
            .build();

        // Verify the original configuration was preserved and not overwritten
        assertEquals(30000, options.getConnectTimeout());
        assertEquals(50, options.getIdleTimeout());
        assertEquals(60, options.getReadIdleTimeout());
        assertEquals(70, options.getWriteIdleTimeout());

        String defaultPath = "/default";
        WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
    }

    @Test
    public void buildWithNullProxyAddress() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            proxyServer.start();

            ProxyOptions mockPoxyOptions = Mockito.mock(ProxyOptions.class);
            Mockito.when(mockPoxyOptions.getType()).thenReturn(ProxyOptions.Type.HTTP);
            Mockito.when(mockPoxyOptions.getAddress()).thenReturn(null);

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .proxy(mockPoxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .verifyError(ConnectException.class);
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithInvalidProxyType() {
        ProxyOptions.Type mockProxyType = Mockito.mock(ProxyOptions.Type.class);
        Mockito.when(mockProxyType.name()).thenReturn("INVALID");

        ProxyOptions clientProxyOptions = new ProxyOptions(mockProxyType,
            new InetSocketAddress("test.com", 8080));

        assertThrows(IllegalArgumentException.class, () -> {
            new VertxAsyncHttpClientBuilder()
                .proxy(clientProxyOptions)
                .build();
        });
    }

    @Test
    public void buildWithNullProxyType() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions mockPoxyOptions = Mockito.mock(ProxyOptions.class);
            Mockito.when(mockPoxyOptions.getType()).thenReturn(null);
            Mockito.when(mockPoxyOptions.getAddress()).thenReturn(new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .proxy(mockPoxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithoutProxyAuthentication() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD,
            new String[] {SERVICE_ENDPOINT});

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

            HttpClient httpClient = new VertxAsyncHttpClientBuilder()
                .proxy(clientProxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }
}
