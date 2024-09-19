// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link DefaultHttpClientBuilder}.
 */
public class DefaultHttpClientBuilderTests {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";
    private static final TestConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static LocalTestServer server;
    private static SimpleBasicAuthHttpProxyServer proxyServer;
    private static SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer((req, resp, requestBody) -> {
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

        HttpClient httpClient = new DefaultHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() throws IOException {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY,
                    "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
                .put("java.net.useSystemProxies", "true")).build();

        HttpClient httpClient = new DefaultHttpClientBuilder().configuration(configuration).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void buildWithHttpProxyFromExplicitConfiguration() throws IOException {
        Configuration configuration = new ConfigurationBuilder().putProperty("http.proxy.hostname",
            proxyEndpoint.getHost()).putProperty("http.proxy.port", String.valueOf(proxyEndpoint.getPort())).build();

        HttpClient httpClient = new DefaultHttpClientBuilder().configuration(configuration).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void buildWithNullProxyAddress() {
        ProxyOptions mockPoxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, null);

        HttpClient httpClient = new DefaultHttpClientBuilder().proxy(mockPoxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        assertThrows(IOException.class, () -> httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri)).close());
    }

    @Test
    public void buildWithInvalidProxyType() {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.SOCKS5,
            new InetSocketAddress("test.com", 8080));

        assertThrows(IllegalArgumentException.class,
            () -> new DefaultHttpClientBuilder().proxy(clientProxyOptions).build());
    }

    @Test
    public void buildWithNullProxyType() throws IOException {
        ProxyOptions mockPoxyOptions = new ProxyOptions(null,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

        HttpClient httpClient = new DefaultHttpClientBuilder().proxy(mockPoxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertNotNull(response);
        }
    }

    @Test
    public void buildWithoutProxyAuthentication() throws IOException {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

        HttpClient httpClient = new DefaultHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUri))) {
            assertNotNull(response);
        }
    }
}
