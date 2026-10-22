// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JdkHttpClientBuilder}.
 * <p>
 * Now that the default HttpClient, and related code, are using multi-release JARs this must be an integration test as
 * the full JAR must be available to use the multi-release code.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientBuilderIT {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";

    private static LocalTestServer server;
    private static SimpleBasicAuthHttpProxyServer proxyServer;
    private static SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
            if ("GET".equalsIgnoreCase(req.getMethod()) && SERVICE_ENDPOINT.equals(req.getServletPath())) {
                resp.setStatus(200);
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + req.getServletPath());
            }
        });

        server.start();

        proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME, PROXY_PASSWORD, SERVICE_ENDPOINT);
        proxyEndpoint = proxyServer.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }

        if (proxyServer != null) {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxy() throws IOException {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort())).setCredentials(PROXY_USERNAME,
                PROXY_PASSWORD);

        HttpClient httpClient = new JdkHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() throws IOException {
        Configuration configuration = Configuration.from(new TestConfigurationSource()
            .put(Configuration.HTTP_PROXY,
                "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
            .put(Configuration.HTTPS_PROXY, "")
            .put("java.net.useSystemProxies", "true"));

        HttpClient httpClient = new JdkHttpClientBuilder().configuration(configuration).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 123000 })
    public void configuredTimeoutsAndExplicitOverrides(long timeoutMillis) throws ReflectiveOperationException {
        String value = Long.toString(timeoutMillis);
        Configuration configuration
            = Configuration.from(new TestConfigurationSource().put(Configuration.REQUEST_CONNECT_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_READ_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_WRITE_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_RESPONSE_TIMEOUT_IN_MS, value));
        JdkHttpClientBuilder builder = new JdkHttpClientBuilder().configuration(configuration);
        assertTimeouts(builder.build(), Math.max(0, timeoutMillis));
        assertTimeouts(builder.connectionTimeout(Duration.ofMillis(456000))
            .readTimeout(Duration.ofMillis(456000))
            .writeTimeout(Duration.ofMillis(456000))
            .responseTimeout(Duration.ofMillis(456000))
            .build(), 456000);
        assertTimeouts(builder.connectionTimeout(Duration.ofNanos(-1))
            .readTimeout(Duration.ZERO)
            .writeTimeout(Duration.ofNanos(-1))
            .responseTimeout(Duration.ZERO)
            .build(), 0);
    }

    private static void assertTimeouts(HttpClient client, long expected) throws ReflectiveOperationException {
        java.lang.reflect.Field nativeClientField = client.getClass().getDeclaredField("jdkHttpClient");
        nativeClientField.setAccessible(true);
        Object nativeClient = nativeClientField.get(client);
        Object connectTimeout
            = Class.forName("java.net.http.HttpClient").getMethod("connectTimeout").invoke(nativeClient);
        assertEquals(expected == 0 ? Optional.empty() : Optional.of(Duration.ofMillis(expected)), connectTimeout);
        for (String name : new String[] { "readTimeout", "writeTimeout", "responseTimeout" }) {
            java.lang.reflect.Field field = client.getClass().getDeclaredField(name);
            field.setAccessible(true);
            assertEquals(expected == 0 && !"readTimeout".equals(name) ? null : Duration.ofMillis(expected),
                field.get(client));
        }
    }

    @Test
    public void buildWithNullProxyAddress() {
        ProxyOptions mockPoxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, null);
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder().proxy(mockPoxyOptions));
    }

    @Test
    public void buildWithNullProxyType() {
        ProxyOptions mockPoxyOptions
            = new ProxyOptions(null, new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder().proxy(mockPoxyOptions));
    }

    @Test
    public void buildWithoutProxyAuthentication() throws IOException {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

        HttpClient httpClient = new JdkHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertNotNull(response);
        }
    }
}
