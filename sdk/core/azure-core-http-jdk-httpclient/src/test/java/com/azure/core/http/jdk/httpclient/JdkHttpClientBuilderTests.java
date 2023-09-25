// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import javax.servlet.ServletException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JdkHttpClientBuilder}.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientBuilderTests {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static LocalTestServer server;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            if ("GET".equalsIgnoreCase(req.getMethod()) && SERVICE_ENDPOINT.equals(path)) {
                resp.setStatus(200);
                resp.flushBuffer();
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
            }
        });

        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Tests that an {@link JdkHttpClient} is able to be built from an existing
     * {@link java.net.http.HttpClient.Builder}.
     */
    @Test
    public void buildClientWithExistingClient() {
        final String[] marker = new String[1];
        final java.net.http.HttpClient.Builder existingClientBuilder = java.net.http.HttpClient.newBuilder();
        existingClientBuilder.executor(new Executor() {
            private final ExecutorService executorService = Executors.newFixedThreadPool(2);

            @Override
            public void execute(Runnable command) {
                marker[0] = "on_custom_executor";
                executorService.submit(command);
            }
        });

        final JdkHttpClient client = (JdkHttpClient) new JdkHttpClientBuilder(existingClientBuilder).build();

        final String defaultUrl = server.getHttpUri() + SERVICE_ENDPOINT;

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertNotNull(marker[0]);
        assertEquals(marker[0], "on_custom_executor");
    }

    /**
     * Tests that instantiating an {@link JdkHttpClientBuilder} with a {@code null} {@link JdkHttpClient} will throw a
     * {@link NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder(null));
    }

    /**
     * Tests building a client with a given {@code Executor}.
     */
    @Test
    public void buildWithExecutor() {
        final String[] marker = new String[1];
        final HttpClient httpClient = new JdkHttpClientBuilder()
            .executor(new Executor() {
                private final ExecutorService executorService = Executors.newFixedThreadPool(10);

                @Override
                public void execute(Runnable command) {
                    marker[0] = "on_custom_executor";
                    executorService.submit(command);
                }
            })
            .build();

        final String defaultUrl = server.getHttpUri() + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertNotNull(marker[0]);
        assertEquals(marker[0], "on_custom_executor");
    }

    /**
     * Tests that passing a {@code null} {@code executor} to the builder will throw a {@link NullPointerException}.
     */
    @Test
    public void nullExecutorThrows() {
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder().executor(null));
    }

    /**
     * Tests building a client with a given proxy.
     */
    @Test
    public void buildWithHttpProxy() {
        final SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);
        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

            HttpClient httpClient = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder())
                .proxy(clientProxyOptions)
                .build();
            // Url of the service behind proxy
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
        final SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTP_PROXY, "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
                    .put("java.net.useSystemProxies", "true"))
                .build();

            configurationProxyTest(configuration);
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxyFromExplicitConfiguration() {
        final SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder()
                .putProperty("http.proxy.hostname", proxyEndpoint.getHost())
                .putProperty("http.proxy.port", String.valueOf(proxyEndpoint.getPort()))
                .build();

            configurationProxyTest(configuration);
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithConfigurationNone() {
        final HttpClient httpClient = new JdkHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        final String defaultUrl = server.getHttpUri() + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithNonProxyConfigurationProxy(Configuration configuration) {
        final HttpClient httpClient = new JdkHttpClientBuilder()
            .configuration(configuration)
            .build();

        final String defaultUrl = server.getHttpUri() + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<Arguments> buildWithExplicitConfigurationProxySupplier() {
        List<Arguments> arguments = new ArrayList<>();

        final Configuration envConfiguration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource()
                .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888")
                .put(Configuration.PROPERTY_NO_PROXY, "localhost"))
            .build();

        arguments.add(Arguments.of(envConfiguration));

        final Configuration explicitConfiguration = new ConfigurationBuilder()
            .putProperty("http.proxy.hostname", "localhost")
            .putProperty("http.proxy.port", "42")
            .putProperty("http.proxy.non-proxy-hosts", "localhost")
            .build();

        arguments.add(Arguments.of(explicitConfiguration));
        return arguments.stream();
    }

    @Test
    void testAllowedHeadersFromNetworkProperties() {
        JdkHttpClientBuilder jdkHttpClientBuilder = spy(new JdkHttpClientBuilder());
        Properties properties = new Properties();
        properties.put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade");
        when(jdkHttpClientBuilder.getNetworkProperties()).thenReturn(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders.addAll(com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testAllowedHeadersFromConfiguration() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE,
            new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade"),
            EMPTY_SOURCE)
            .build();

        JdkHttpClientBuilder jdkHttpClientBuilder = spy(
            new JdkHttpClientBuilder().configuration(configuration));

        Properties properties = new Properties();
        when(jdkHttpClientBuilder.getNetworkProperties()).thenReturn(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders.addAll(com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testAllowedHeadersFromBoth() {
        Configuration configuration = new ConfigurationBuilder(new TestConfigurationSource(),
            new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade"),
            new TestConfigurationSource())
            .build();

        JdkHttpClientBuilder jdkHttpClientBuilder = spy(
            new JdkHttpClientBuilder().configuration(configuration));

        Properties properties = new Properties();
        properties.put("jdk.httpclient.allowRestrictedHeaders", "host, connection, upgrade");
        when(jdkHttpClientBuilder.getNetworkProperties()).thenReturn(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders.addAll(com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "host", "connection", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 1);
    }

    @Test
    void testAllowedHeadersFromSystemProperties() {
        Properties properties = new Properties();
        properties.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade");

        JdkHttpClientBuilder jdkHttpClientBuilder = spy(new JdkHttpClientBuilder());
        when(jdkHttpClientBuilder.getNetworkProperties()).thenReturn(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders.addAll(com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testCaseInsensitivity() {
        Properties properties = new Properties();
        properties.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-LENGTH");

        JdkHttpClientBuilder jdkHttpClientBuilder = spy(new JdkHttpClientBuilder());
        when(jdkHttpClientBuilder.getNetworkProperties()).thenReturn(properties);

        Set<String> restrictedHeaders = jdkHttpClientBuilder.getRestrictedHeaders();
        assertTrue(restrictedHeaders.contains("Connection"), "connection header is missing");
        assertTrue(restrictedHeaders.contains("connection"), "connection header is missing");
        assertTrue(restrictedHeaders.contains("CONNECTION"), "connection header is missing");

        assertFalse(restrictedHeaders.contains("Content-Length"), "content-length not removed");
        assertFalse(restrictedHeaders.contains("content-length"), "content-length not removed");
        assertFalse(restrictedHeaders.contains("CONTENT-length"), "content-length not removed");
    }


    private static void configurationProxyTest(Configuration configuration) {
        HttpClient httpClient = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder())
            .configuration(configuration)
            .build();
        // Url of the service behind proxy
        final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
            .expectNextCount(1)
            .verifyComplete();
    }

    private void validateRestrictedHeaders(JdkHttpClientBuilder jdkHttpClientBuilder,
        Set<String> expectedRestrictedHeaders, int expectedRestrictedHeadersSize) {
        Set<String> restrictedHeaders = jdkHttpClientBuilder.getRestrictedHeaders();
        assertEquals(expectedRestrictedHeadersSize, restrictedHeaders.size());
        assertEquals(expectedRestrictedHeaders, restrictedHeaders);
    }

}
