// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationBuilder;
import io.clientcore.core.util.configuration.ConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
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

import static io.clientcore.http.jdk.httpclient.JdkHttpClientLocalTestServer.PROXY_PASSWORD;
import static io.clientcore.http.jdk.httpclient.JdkHttpClientLocalTestServer.PROXY_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JdkHttpClientBuilder}.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientBuilderTests {
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    static final String SERVICE_ENDPOINT = "/default";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static final String SERVER_HTTP_URI = JdkHttpClientLocalTestServer.getServer().getHttpUri();
    private static final int PROXY_SERVER_HTTP_PORT = JdkHttpClientLocalTestServer.getProxyServer().getHttpPort();

    /**
     * Tests that an {@link JdkHttpClient} is able to be built from an existing
     * {@link java.net.http.HttpClient.Builder}.
     */
    @Test
    public void buildClientWithExistingClient() throws IOException {
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

        final String defaultUri = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());

            assertNotNull(marker[0]);
            assertEquals(marker[0], "on_custom_executor");
        }
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
    public void buildWithExecutor() throws IOException {
        final String[] marker = new String[1];
        final HttpClient httpClient = new JdkHttpClientBuilder().executor(new Executor() {
            private final ExecutorService executorService = Executors.newFixedThreadPool(10);

            @Override
            public void execute(Runnable command) {
                marker[0] = "on_custom_executor";
                executorService.submit(command);
            }
        }).build();

        final String defaultUri = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());

            assertNotNull(marker[0]);
            assertEquals(marker[0], "on_custom_executor");
        }
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
    public void buildWithHttpProxy() throws IOException {
        ProxyOptions clientProxyOptions
            = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", PROXY_SERVER_HTTP_PORT))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

        HttpClient httpClient
            = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder()).proxy(clientProxyOptions).build();
        // Uri of the service behind proxy
        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertNotNull(response);
        }
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() throws IOException {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource()
                .put(Configuration.PROPERTY_HTTP_PROXY,
                    "http://" + PROXY_USER_INFO + "localhost:" + PROXY_SERVER_HTTP_PORT)
                .put("java.net.useSystemProxies", "true")).build();

        configurationProxyTest(configuration);
    }

    @Test
    public void buildWithHttpProxyFromExplicitConfiguration() throws IOException {
        Configuration configuration = new ConfigurationBuilder().putProperty("http.proxy.hostname", "localhost")
            .putProperty("http.proxy.port", String.valueOf(PROXY_SERVER_HTTP_PORT))
            .build();

        configurationProxyTest(configuration);
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithNonProxyConfigurationProxy(Configuration configuration) throws IOException {
        final HttpClient httpClient = new JdkHttpClientBuilder().configuration(configuration).build();

        final String defaultUri = SERVER_HTTP_URI + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private static Stream<Arguments> buildWithExplicitConfigurationProxySupplier() {
        List<Arguments> arguments = new ArrayList<>();

        final Configuration envConfiguration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888")
                .put(Configuration.PROPERTY_NO_PROXY, "localhost")).build();

        arguments.add(Arguments.of(envConfiguration));

        final Configuration explicitConfiguration
            = new ConfigurationBuilder().putProperty("http.proxy.hostname", "localhost")
                .putProperty("http.proxy.port", "42")
                .putProperty("http.proxy.non-proxy-hosts", "localhost")
                .build();

        arguments.add(Arguments.of(explicitConfiguration));
        return arguments.stream();
    }

    @Test
    void testAllowedHeadersFromNetworkProperties() {
        Properties properties = new Properties();
        properties.put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade");

        JdkHttpClientBuilder jdkHttpClientBuilder = new JdkHttpClientBuilderOverriddenProperties(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders
            .addAll(io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testAllowedHeadersFromConfiguration() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE,
            new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade"),
            EMPTY_SOURCE).build();

        Properties properties = new Properties();

        JdkHttpClientBuilder jdkHttpClientBuilder
            = new JdkHttpClientBuilderOverriddenProperties(properties).configuration(configuration);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders
            .addAll(io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testAllowedHeadersFromBoth() {
        Configuration configuration = new ConfigurationBuilder(new TestConfigurationSource(),
            new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade"),
            new TestConfigurationSource()).build();

        Properties properties = new Properties();
        properties.put("jdk.httpclient.allowRestrictedHeaders", "host, connection, upgrade");

        JdkHttpClientBuilder jdkHttpClientBuilder
            = new JdkHttpClientBuilderOverriddenProperties(properties).configuration(configuration);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders
            .addAll(io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        Arrays.asList("content-length", "host", "connection", "upgrade").forEach(expectedRestrictedHeaders::remove);

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 1);
    }

    @Test
    void testAllowedHeadersFromSystemProperties() {
        Properties properties = new Properties();
        properties.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-length, upgrade");

        JdkHttpClientBuilder jdkHttpClientBuilder = new JdkHttpClientBuilderOverriddenProperties(properties);

        Set<String> expectedRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        expectedRestrictedHeaders
            .addAll(io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.DEFAULT_RESTRICTED_HEADERS);
        expectedRestrictedHeaders.removeAll(Arrays.asList("content-length", "upgrade"));

        validateRestrictedHeaders(jdkHttpClientBuilder, expectedRestrictedHeaders, 3);
    }

    @Test
    void testCaseInsensitivity() {
        Properties properties = new Properties();
        properties.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-LENGTH");

        JdkHttpClientBuilder jdkHttpClientBuilder = new JdkHttpClientBuilderOverriddenProperties(properties);

        Set<String> restrictedHeaders = jdkHttpClientBuilder.getRestrictedHeaders();
        assertTrue(restrictedHeaders.contains("connection"), "connection header is missing");

        assertFalse(restrictedHeaders.contains("content-length"), "content-length not removed");
    }

    private static void configurationProxyTest(Configuration configuration) throws IOException {
        HttpClient httpClient
            = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder()).configuration(configuration).build();
        // Uri of the service behind proxy
        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;
        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertNotNull(response);
        }
    }

    private void validateRestrictedHeaders(JdkHttpClientBuilder jdkHttpClientBuilder,
        Set<String> expectedRestrictedHeaders, int expectedRestrictedHeadersSize) {
        Set<String> restrictedHeaders = jdkHttpClientBuilder.getRestrictedHeaders();
        assertEquals(expectedRestrictedHeadersSize, restrictedHeaders.size());
        assertEquals(expectedRestrictedHeaders, restrictedHeaders);
    }

    private static final class JdkHttpClientBuilderOverriddenProperties extends JdkHttpClientBuilder {
        private final Properties properties;

        JdkHttpClientBuilderOverriddenProperties(Properties properties) {
            this.properties = properties;
        }

        @Override
        Properties getNetworkProperties() {
            return properties;
        }
    };

}
