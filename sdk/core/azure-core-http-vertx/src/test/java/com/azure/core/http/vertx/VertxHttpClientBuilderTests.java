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
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.azure.core.http.vertx.VertxClientTestHelper.getVertxInternalProxyFilter;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.PROXY_PASSWORD;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.PROXY_USERNAME;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.SERVICE_ENDPOINT;
import static io.vertx.core.net.SocketAddress.inetSocketAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link VertxHttpClientBuilder}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class VertxHttpClientBuilderTests {
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static final String SERVER_HTTP_URI = VertxHttpClientLocalTestServer.getServer().getHttpUri();
    private static final int PROXY_SERVER_HTTP_PORT = VertxHttpClientLocalTestServer.getProxyServer().getHttpPort();

    @Test
    public void buildWithConfigurationNone() {
        HttpClient httpClient = new VertxHttpClientBuilder().configuration(Configuration.NONE).build();

        String defaultUrl = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void buildWithDefaultConnectionOptions() {
        HttpClient httpClient = new VertxHttpClientBuilder().build();

        io.vertx.core.http.HttpClient client = ((VertxHttpClient) httpClient).client;
        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) client).options();

        String defaultUrl = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertEquals(10000, options.getConnectTimeout());
        assertEquals(60000, options.getReadIdleTimeout());
        assertEquals(60000, options.getWriteIdleTimeout());
    }

    @Test
    public void buildWithConnectionOptions() {
        VertxHttpClientBuilder builder = new VertxHttpClientBuilder();
        VertxHttpClient httpClient = (VertxHttpClient) builder.connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(40))
            .build();

        io.vertx.core.http.HttpClientOptions options = ((HttpClientImpl) httpClient.client).options();

        String defaultUrl = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertEquals(10000, options.getConnectTimeout());
        assertEquals(30000, options.getReadIdleTimeout());
        assertEquals(40000, options.getWriteIdleTimeout());
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

        VertxHttpClient httpClient = (VertxHttpClient) new VertxHttpClientBuilder().proxy(proxyOptions).build();

        HttpClientImpl vertxHttpClientImpl = (HttpClientImpl) httpClient.client;
        io.vertx.core.http.HttpClientOptions options = vertxHttpClientImpl.options();

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
        ProxyOptions clientProxyOptions
            = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", PROXY_SERVER_HTTP_PORT))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

        HttpClient httpClient = new VertxHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource()
                .put(Configuration.PROPERTY_HTTP_PROXY,
                    "http://" + PROXY_USER_INFO + "localhost:" + PROXY_SERVER_HTTP_PORT)
                .put("java.net.useSystemProxies", "true")).build();

        HttpClient httpClient = new VertxHttpClientBuilder().configuration(configuration).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void buildWithHttpProxyFromExplicitConfiguration() {
        Configuration configuration = new ConfigurationBuilder().putProperty("http.proxy.hostname", "localhost")
            .putProperty("http.proxy.port", String.valueOf(PROXY_SERVER_HTTP_PORT))
            .build();

        HttpClient httpClient = new VertxHttpClientBuilder().configuration(configuration).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void buildWithCustomVertx() throws Exception {
        Vertx vertx = Vertx.vertx();

        HttpClient httpClient = new VertxHttpClientBuilder().configuration(Configuration.NONE).vertx(vertx).build();

        String defaultUrl = SERVER_HTTP_URI + SERVICE_ENDPOINT;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(event -> latch.countDown());
            // Wait 60 seconds, same as production code.
            assertTrue(latch.await(60, TimeUnit.SECONDS));
        }
    }

    @Test
    public void buildWithCustomHttpClientOptions() {
        HttpClientOptions options = new HttpClientOptions();
        options.setConnectTimeout(30000);
        options.setIdleTimeout(50);
        options.setReadIdleTimeout(60);
        options.setWriteIdleTimeout(70);

        HttpClient httpClient = new VertxHttpClientBuilder().connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(40))
            .httpClientOptions(options)
            .build();

        // Verify the original configuration was preserved and not overwritten
        assertEquals(30000, options.getConnectTimeout());
        assertEquals(60, options.getReadIdleTimeout());
        assertEquals(70, options.getWriteIdleTimeout());

        String defaultUrl = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void buildWithNullProxyAddress() {
        ProxyOptions mockPoxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, null);

        HttpClient httpClient = new VertxHttpClientBuilder().proxy(mockPoxyOptions).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .verifyError(ConnectException.class);
    }

    @Test
    public void buildWithNullProxyType() {
        ProxyOptions mockPoxyOptions
            = new ProxyOptions(null, new InetSocketAddress("localhost", PROXY_SERVER_HTTP_PORT));

        HttpClient httpClient = new VertxHttpClientBuilder().proxy(mockPoxyOptions).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void buildWithoutProxyAuthentication() {
        ProxyOptions clientProxyOptions
            = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", PROXY_SERVER_HTTP_PORT));

        HttpClient httpClient = new VertxHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }
}
