// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link NettyAsyncHttpClientBuilder}.
 */
public class NettyAsyncHttpClientBuilderTests {
    private static final String PREBUILT_CLIENT_PATH = "/prebuiltClient";
    private static final String PORT_CONFIGURED_PATH = "/portConfiguredClient";
    private static final String WIRETAP_PATH = "/wiretap";
    private static final String EVENT_LOOP_PATH = "/eventLoopPath";

    private static final String COOKIE_NAME = "test";
    private static final String COOKIE_VALUE = "success";

    private static WireMockServer server;

    @BeforeAll
    public static void setupWireMock() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());

        // Mocked endpoint to test building a client with a prebuilt Netty HttpClient.
        server.stubFor(WireMock.get(PREBUILT_CLIENT_PATH).withCookie(COOKIE_NAME, WireMock.matching(COOKIE_VALUE))
            .willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with a set port.
        server.stubFor(WireMock.get(PORT_CONFIGURED_PATH).willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with wiretapping.
        server.stubFor(WireMock.get(WIRETAP_PATH).willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with a custom EventLoopGroup.
        server.stubFor(WireMock.get(EVENT_LOOP_PATH).willReturn(WireMock.aResponse().withStatus(200)));

        server.start();
    }

    @AfterAll
    public static void shutdownWireMock() {
        if (server.isRunning()) {
            server.shutdown();
        }
    }

    /**
     * Tests that constructing a {@link NettyAsyncHttpClient} from a pre-configured Netty {@link HttpClient} will use
     * that as the underlying client.
     */
    @Test
    public void buildClientFromConfiguredClient() {
        HttpClient expectedClient = HttpClient.create().cookie(new DefaultCookie(COOKIE_NAME, COOKIE_VALUE));
        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(expectedClient)
            .build();

        String url = "http://localhost:" + server.port() + PREBUILT_CLIENT_PATH;
        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, url)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@code null} Netty {@link HttpClient} into the builder will throw a {@link
     * NullPointerException}.
     */
    @Test
    public void startWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new NettyAsyncHttpClientBuilder(null));
    }

    /**
     * Tests when {@code wiretap} is set to {@code true} the Netty pipeline will have a {@link LoggingHandler} added.
     */
    @Test
    public void buildWiretappedClient() {
        HttpClient validatorClient = HttpClient.create().doAfterResponse((response, connection) ->
            assertNotNull(connection.channel().pipeline().get(LoggingHandler.class)));

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient)
            .wiretap(true)
            .build();

        String url = "http://localhost:" + server.port() + WIRETAP_PATH;
        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, url)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests when a {@code port} is set that any path-only request will be sent to the port specified.
     */
    @Test
    public void buildPortClient() {
        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .port(server.port())
            .build();

        StepVerifier.create(nettyClient.nettyClient.get().uri(PORT_CONFIGURED_PATH).response())
            .assertNext(response -> assertEquals(200, response.status().code()))
            .verifyComplete();
    }

    /**
     * Tests that a custom {@link NioEventLoopGroup} is properly applied to the Netty client to handle sending and
     * receiving requests and responses.
     */
    @Test
    public void buildEventLoopClient() {
        String expectedThreadName = "testEventLoop";
        HttpClient validatorClient = HttpClient.create().doAfterResponse((response, connection) -> {
            // Validate that the EventLoop being used is a NioEventLoop.
            NioEventLoop eventLoop = (NioEventLoop) connection.channel().eventLoop();
            assertNotNull(eventLoop);

            assertEquals(expectedThreadName, eventLoop.threadProperties().name());
        });

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1, (Runnable r) -> new Thread(r, expectedThreadName));

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient)
            .nioEventLoopGroup(eventLoopGroup)
            .build();

        String url = "http://localhost:" + server.port() + WIRETAP_PATH;
        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, url)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }
}
