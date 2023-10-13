// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.httpurlconnection;

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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import javax.servlet.ServletException;
import java.net.ConnectException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link HttpUrlConnectionAsyncClientBuilder}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class HttpUrlConnectionAsyncClientBuilderTests {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static LocalTestServer server;

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
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void buildWithConfigurationNone() {
        HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        String defaultUrl = server.getHttpUri() + SERVICE_ENDPOINT;

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void buildWithHttpProxy() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
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
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
                new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTP_PROXY, "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
                    .put("java.net.useSystemProxies", "true"))
                .build();

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
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
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new ConfigurationBuilder()
                .putProperty("http.proxy.hostname", proxyEndpoint.getHost())
                .putProperty("http.proxy.port", String.valueOf(proxyEndpoint.getPort()))
                .build();

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
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
    public void buildWithNullProxyAddress() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            proxyServer.start();

            ProxyOptions mockPoxyOptions = Mockito.mock(ProxyOptions.class);
            Mockito.when(mockPoxyOptions.getType()).thenReturn(ProxyOptions.Type.HTTP);
            Mockito.when(mockPoxyOptions.getAddress()).thenReturn(null);

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
                .proxy(mockPoxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .verifyError(RuntimeException.class);
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
            new HttpUrlConnectionAsyncClientBuilder()
                .proxy(clientProxyOptions)
                .build();
        });
    }

    @Test
    public void buildWithNullProxyType() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions mockPoxyOptions = Mockito.mock(ProxyOptions.class);
            Mockito.when(mockPoxyOptions.getType()).thenReturn(null);
            Mockito.when(mockPoxyOptions.getAddress()).thenReturn(new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
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
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

            HttpClient httpClient = new HttpUrlConnectionAsyncClientBuilder()
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
