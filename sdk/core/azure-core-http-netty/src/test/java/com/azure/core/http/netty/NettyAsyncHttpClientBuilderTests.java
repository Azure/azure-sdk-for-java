// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.netty.channel.BootstrapHandlers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link NettyAsyncHttpClientBuilder}.
 */
public class NettyAsyncHttpClientBuilderTests {
    private static final String DEFAULT_PATH = "/default";
    private static final String PREBUILT_CLIENT_PATH = "/prebuiltClient";

    private static final String COOKIE_NAME = "test";
    private static final String COOKIE_VALUE = "success";

    private static WireMockServer server;
    private static String defaultUrl;
    private static String prebuiltClientUrl;

    @BeforeAll
    public static void setupWireMock() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());

        // Mocked endpoint to test building a client with a prebuilt Netty HttpClient.
        server.stubFor(WireMock.get(PREBUILT_CLIENT_PATH).withCookie(COOKIE_NAME, WireMock.matching(COOKIE_VALUE))
            .willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with a set port.
        server.stubFor(WireMock.get(DEFAULT_PATH).willReturn(WireMock.aResponse().withStatus(200)));

        server.start();

        defaultUrl = "http://localhost:" + server.port() + DEFAULT_PATH;
        prebuiltClientUrl = "http://localhost:" + server.port() + PREBUILT_CLIENT_PATH;
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

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, prebuiltClientUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@code null} Netty {@link HttpClient} into the builder will throw a {@link
     * NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new NettyAsyncHttpClientBuilder(null));
    }

    /**
     * Tests that creating a client with a {@link ConnectionProvider} will use it to create connections to a server.
     */
    @Test
    public void buildWithConnectionProvider() {
        ConnectionProvider connectionProvider = bootstrap -> {
            throw new UnsupportedOperationException("Bad connection provider");
        };

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .connectionProvider(connectionProvider)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .verifyError(UnsupportedOperationException.class);
    }

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @EnumSource(ProxyOptions.Type.class)
    public void buildWithProxy(ProxyOptions.Type proxyType) {
        HttpClient validatorClient = HttpClient.create().tcpConfiguration(tcpClient -> tcpClient
            .bootstrap(bootstrap -> BootstrapHandlers.updateConfiguration(bootstrap, "TestProxyHandler",
                (connectionObserver, channel) ->
                    channel.pipeline().addFirst("TestProxyHandler", new TestProxyValidator(proxyType)))));

        ProxyOptions proxyOptions = new ProxyOptions(proxyType, new InetSocketAddress("localhost", 12345));

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient)
            .proxy(proxyOptions)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .verifyError();
    }

    @Test
    public void buildWithAuthenticatedProxy() {
        HttpClient validatorClient = HttpClient.create().tcpConfiguration(tcpClient -> tcpClient
            .bootstrap(bootstrap -> BootstrapHandlers.updateConfiguration(bootstrap, "TestProxyHandler",
                (connectionObserver, channel) ->
                    channel.pipeline().addFirst("TestProxyHandler", new TestProxyValidator(ProxyOptions.Type.HTTP)))));

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 12345))
            .setCredentials("1", "1");

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient)
            .proxy(proxyOptions)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .verifyError();
    }

    @Test
    public void buildWithConfigurationNone() {
        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void buildWithConfigurationProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888");

        HttpClient validatorClient = HttpClient.create().tcpConfiguration(tcpClient -> tcpClient
            .bootstrap(bootstrap -> BootstrapHandlers.updateConfiguration(bootstrap, "TestProxyHandler",
                (connectionObserver, channel) ->
                    channel.pipeline().addFirst("TestProxyHandler", new TestProxyValidator(ProxyOptions.Type.HTTP)))));

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient)
            .configuration(configuration)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .verifyError();
    }

    @Test
    public void buildWithNonProxyConfigurationProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888")
            .put(Configuration.PROPERTY_NO_PROXY, "localhost");

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .configuration(configuration)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void buildWithAuthenticatedNonProxyConfigurationProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:8888")
            .put(Configuration.PROPERTY_NO_PROXY, "localhost");

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .configuration(configuration)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static final class TestProxyValidator extends ChannelDuplexHandler {
        private final ProxyOptions.Type proxyType;
        private final boolean isAuthenticated;

        private TestProxyValidator(ProxyOptions.Type proxyType) {
            this(proxyType, false);
        }

        private TestProxyValidator(ProxyOptions.Type proxyType, boolean isAuthenticated) {
            this.proxyType = proxyType;
            this.isAuthenticated = isAuthenticated;
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) {
            ProxyHandler proxyHandler = ctx.pipeline().get(ProxyHandler.class);
            assertNotNull(proxyHandler);

            switch (proxyType) {
                case HTTP:
                    if (isAuthenticated) {
                        assertTrue(proxyHandler instanceof com.azure.core.http.netty.implementation.HttpProxyHandler);
                    } else {
                        assertTrue(proxyHandler instanceof HttpProxyHandler);
                    }
                    break;

                case SOCKS5:
                    assertTrue(proxyHandler instanceof Socks5ProxyHandler);
                    break;

                case SOCKS4:
                    assertTrue(proxyHandler instanceof Socks4ProxyHandler);
                    break;

                default:
                    fail("Unknown proxy type, failing test");
                    break;
            }

            ctx.connect(remoteAddress, localAddress, promise);
        }
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

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
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

        StepVerifier.create(nettyClient.nettyClient.get().uri(DEFAULT_PATH).response())
            .assertNext(response -> assertEquals(200, response.status().code()))
            .verifyComplete();
    }

    /**
     * Tests that a custom {@link io.netty.channel.EventLoopGroup} is properly applied to the Netty client
     * to handle sending and receiving requests and responses.
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
            .eventLoopGroup(eventLoopGroup)
            .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getTimeoutMillisSupplier")
    public void getTimeoutMillis(Duration timeout, long expected) {
        assertEquals(expected, NettyAsyncHttpClientBuilder.getTimeoutMillis(timeout));
    }

    private static Stream<Arguments> getTimeoutMillisSupplier() {
        return Stream.of(
            Arguments.of(null, TimeUnit.SECONDS.toMillis(60)),
            Arguments.of(Duration.ofSeconds(0), 0),
            Arguments.of(Duration.ofSeconds(-1), 0),
            Arguments.of(Duration.ofSeconds(120), TimeUnit.SECONDS.toMillis(120)),
            Arguments.of(Duration.ofNanos(1), TimeUnit.MILLISECONDS.toMillis(1))
        );
    }
}
