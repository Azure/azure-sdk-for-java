// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JdkAsyncHttpClientBuilder}.
 */
public class JdkAsyncHttpClientBuilderTests {
    /**
     * Tests that an {@link JdkAsyncHttpClient} is able to be built from an existing
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

        final JdkAsyncHttpClient client = (JdkAsyncHttpClient) new JdkAsyncHttpClientBuilder(existingClientBuilder)
            .build();

        final String defaultPath = "/default";
        final WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        final String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
        assertNotNull(marker[0]);
        assertEquals(marker[0], "on_custom_executor");
    }

    /**
     * Tests that instantiating an {@link JdkAsyncHttpClientBuilder} with a {@code null} {@link JdkAsyncHttpClient}
     * will throw a {@link NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new JdkAsyncHttpClientBuilder(null));
    }

    /**
     * Tests building a client with a given {@code Executor}.
     */
    @Test
    public void buildWithExecutor() {
        final String[] marker = new String[1];
        final HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .executor(new Executor() {
                private final ExecutorService executorService = Executors.newFixedThreadPool(10);
                @Override
                public void execute(Runnable command) {
                    marker[0] = "on_custom_executor";
                    executorService.submit(command);
                }
            })
            .build();

        final String defaultPath = "/default";
        final WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        final String defaultUrl = "http://localhost:" + server.port() + defaultPath;
        try {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } finally {
            if (server.isRunning()) {
                server.shutdown();
            }
        }
        assertNotNull(marker[0]);
        assertEquals(marker[0], "on_custom_executor");
    }

    /**
     * Tests that passing a {@code null} {@code executor} to the builder will throw a
     * {@link NullPointerException}.
     */
    @Test
    public void nullExecutorThrows() {
        assertThrows(NullPointerException.class, () -> new JdkAsyncHttpClientBuilder().executor(null));
    }

    /**
     * Tests building a client with a given proxy.
     */
    @Test
    public void buildWithHttpProxy() {
        final String proxyUserName = "foo";
        final String proxyPassword = "bar";
        final String serviceEndpoint = "/default";

        final SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(proxyUserName,
            proxyPassword,
            new String[] {serviceEndpoint});
        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                .setCredentials(proxyUserName, proxyPassword);

            HttpClient httpClient = new JdkAsyncHttpClientBuilder(java.net.http.HttpClient.newBuilder())
                .proxy(clientProxyOptions)
                .build();
            // Url of the service behind proxy
            final String serviceUrl = "http://localhost:80" + serviceEndpoint;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxyFromConfiguration() {
        final String proxyUserName = "foo";
        final String proxyPassword = "bar";
        final String proxyUserInfo = proxyUserName + ":" + proxyPassword + "@";
        final String serviceEndpoint = "/default";

        final SimpleBasicAuthHttpProxyServer proxyServer = new SimpleBasicAuthHttpProxyServer(proxyUserName,
            proxyPassword,
            new String[] {serviceEndpoint});
        try {
            SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint = proxyServer.start();

            Configuration configuration = new Configuration()
                .put(Configuration.PROPERTY_HTTP_PROXY,
                    "http://" + proxyUserInfo + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort());

            HttpClient httpClient = new JdkAsyncHttpClientBuilder(java.net.http.HttpClient.newBuilder())
                .configuration(configuration)
                .build();
            // Url of the service behind proxy
            final String serviceUrl = "http://localhost:80" + serviceEndpoint;
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, serviceUrl)))
                .expectNextCount(1)
                .verifyComplete();
        } finally {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithConfigurationNone() {
        final HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        final String defaultPath = "/default";
        final WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        final String defaultUrl = "http://localhost:" + server.port() + defaultPath;
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
    public void buildWithNonProxyConfigurationProxy() {
        final Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888")
            .put(Configuration.PROPERTY_NO_PROXY, "localhost");

        final HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .configuration(configuration)
            .build();

        final String defaultPath = "/default";
        final WireMockServer server
            = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(defaultPath).willReturn(WireMock.aResponse().withStatus(200)));
        server.start();
        final String defaultUrl = "http://localhost:" + server.port() + defaultPath;
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
}
