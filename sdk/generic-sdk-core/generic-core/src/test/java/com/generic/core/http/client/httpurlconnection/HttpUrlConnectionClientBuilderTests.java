// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.models.Context;
import com.generic.core.util.configuration.Configuration;
import com.generic.core.util.configuration.ConfigurationBuilder;
import com.generic.core.util.configuration.ConfigurationSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.TestConfigurationSource;

import javax.servlet.ServletException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link HttpUrlConnectionClientBuilder}.
 */
public class HttpUrlConnectionClientBuilderTests {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";
    private static final TestConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

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
    public void buildWithHttpProxy() {
        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
            PROXY_PASSWORD, SERVICE_ENDPOINT);

        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                .setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
                .proxy(clientProxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE);
        } finally {
            proxyServer.shutdown();
        }
    }

//    @Test
//    public void buildWithHttpProxyFromEnvConfiguration() {
//        SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME,
//            PROXY_PASSWORD, SERVICE_ENDPOINT);
//
//        try {
//            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();
//
//            Configuration configuration = new ConfigurationBuilder((ConfigurationSource) EMPTY_SOURCE, (ConfigurationSource) EMPTY_SOURCE,
//                    (ConfigurationSource) new TestConfigurationSource()
//                        .put(Configuration.PROPERTY_HTTP_PROXY, "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
//                        .put("java.net.useSystemProxies", "true"))
//                .build();
//
//            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
//                .configuration(configuration)
//                .build();
//
//            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
//            httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE);
//        } finally {
//            proxyServer.shutdown();
//        }
//    }

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

            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
                .configuration(configuration)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE);
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

            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
                .proxy(mockPoxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            Assertions.assertThrows(RuntimeException.class,
                () -> httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE));
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

        assertThrows(IllegalArgumentException.class, () -> new HttpUrlConnectionClientBuilder()
            .proxy(clientProxyOptions)
            .build());
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

            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
                .proxy(mockPoxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            try (HttpResponse response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE)) {
                assertNotNull(response);
            };
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

            HttpClient httpClient = new HttpUrlConnectionClientBuilder()
                .proxy(clientProxyOptions)
                .build();

            final String serviceUrl = "http://localhost:80" + SERVICE_ENDPOINT;
            try(HttpResponse response = httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl, null), Context.NONE)) {
                assertNotNull(response);
            }
        } finally {
            proxyServer.shutdown();
        }
    }
}
