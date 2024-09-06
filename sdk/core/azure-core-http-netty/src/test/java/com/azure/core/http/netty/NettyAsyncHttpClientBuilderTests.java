// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.NettyHttpClientLocalTestServer;
import com.azure.core.http.test.common.models.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpResponseDecoderSpec;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.core.http.netty.implementation.NettyHttpClientLocalTestServer.DEFAULT_PATH;
import static com.azure.core.http.netty.implementation.NettyHttpClientLocalTestServer.PREBUILT_CLIENT_PATH;
import static com.azure.core.implementation.util.HttpUtils.getTimeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link NettyAsyncHttpClientBuilder}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class NettyAsyncHttpClientBuilderTests {
    private static final String COOKIE_NAME = "test";
    private static final String COOKIE_VALUE = "success";

    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static final String SERVER_HTTP_URI = NettyHttpClientLocalTestServer.getServer().getHttpUri();
    private static final String DEFAULT_URL = SERVER_HTTP_URI + DEFAULT_PATH;
    private static final String PREBUILT_CLIENT_URL = SERVER_HTTP_URI + PREBUILT_CLIENT_PATH;

    private static final Exception EXPECTED_EXCEPTION = new IOException(
        "This is a local test so we " + "cannot connect to remote hosts eagerly. This is exception is expected.");

    /**
     * Tests that constructing a {@link NettyAsyncHttpClient} from a pre-configured Netty {@link HttpClient} will use
     * that as the underlying client.
     */
    @Test
    public void buildClientFromConfiguredClient() {
        HttpClient expectedClient = HttpClient.create().cookie(new DefaultCookie(COOKIE_NAME, COOKIE_VALUE));
        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(expectedClient).build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, PREBUILT_CLIENT_URL)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@code null} Netty {@link HttpClient} into the builder will throw a
     * {@link NullPointerException}.
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
        ConnectionProvider connectionProvider
            = (transportConfig, connectionObserver, supplier, addressResolverGroup) -> {
                throw new UnsupportedOperationException("Bad connection provider");
            };

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder().connectionProvider(connectionProvider).build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, DEFAULT_URL)))
            .verifyError(UnsupportedOperationException.class);
    }

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @MethodSource("buildWithProxySupplier")
    public void buildWithProxy(boolean shouldHaveProxy, ProxyOptions.Type proxyType, boolean usesAzureHttpProxyHandler,
        ProxyOptions proxyOptions, String requestUrl) {
        HttpClient validatorClient
            = nettyHttpClientWithProxyValidation(shouldHaveProxy, proxyType, usesAzureHttpProxyHandler);

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient).proxy(proxyOptions).build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .verifyErrorMatches(throwable -> throwable == EXPECTED_EXCEPTION);
    }

    private static Stream<Arguments> buildWithProxySupplier() {
        InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 12345);

        ProxyOptions socks4Proxy = new ProxyOptions(ProxyOptions.Type.SOCKS4, proxyAddress);
        ProxyOptions socks5Proxy = new ProxyOptions(ProxyOptions.Type.SOCKS5, proxyAddress);
        ProxyOptions simpleHttpProxy = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress);

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated proxies without non-proxy hosts configured.
         */
        arguments.add(Arguments.of(true, ProxyOptions.Type.SOCKS4, false, socks4Proxy, DEFAULT_URL));
        arguments.add(Arguments.of(true, ProxyOptions.Type.SOCKS5, false, socks5Proxy, DEFAULT_URL));
        arguments.add(Arguments.of(true, ProxyOptions.Type.HTTP, false, simpleHttpProxy, DEFAULT_URL));

        /*
         * HTTP proxy with authentication configured.
         */
        ProxyOptions authenticatedHttpProxy
            = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setCredentials("1", "1");

        arguments.add(Arguments.of(true, ProxyOptions.Type.HTTP, true, authenticatedHttpProxy, DEFAULT_URL));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrlsWithoutProxying = new String[] {
            "http://localhost",
            "http://127.0.0.1",
            "http://azure.microsoft.com",
            "http://careers.linkedin.com" };

        String[] requestUrlsWithProxying
            = new String[] { "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8" };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<ProxyOptions> nonProxyHostsSupplier
            = () -> new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts(rawNonProxyHosts);

        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, ProxyOptions.Type.HTTP, false, nonProxyHostsSupplier.get(), requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(Arguments.of(true, ProxyOptions.Type.HTTP, false, nonProxyHostsSupplier.get(), requestUrl));
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<ProxyOptions> authenticatedNonProxyHostsSupplier
            = () -> nonProxyHostsSupplier.get().setCredentials("1", "1");

        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, ProxyOptions.Type.HTTP, true, authenticatedNonProxyHostsSupplier.get(),
                requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(
                Arguments.of(true, ProxyOptions.Type.HTTP, true, authenticatedNonProxyHostsSupplier.get(), requestUrl));
        }

        return arguments.stream();
    }

    @Test
    public void buildWithConfigurationNone() {
        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder().configuration(Configuration.NONE).build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, DEFAULT_URL)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("buildWithEnvConfigurationProxySupplier")
    public void buildWithEnvConfigurationProxy(boolean shouldHaveProxy, boolean usesAzureHttpProxyHandler,
        Configuration configuration, String requestUrl) {
        HttpClient validatorClient
            = nettyHttpClientWithProxyValidation(shouldHaveProxy, ProxyOptions.Type.HTTP, usesAzureHttpProxyHandler);

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient).configuration(configuration)
                .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .verifyErrorMatches(throwable -> throwable == EXPECTED_EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithExplicitConfigurationProxy(boolean shouldHaveProxy, boolean usesAzureHttpProxyHandler,
        Configuration configuration, String requestUrl) {
        HttpClient validatorClient
            = nettyHttpClientWithProxyValidation(shouldHaveProxy, ProxyOptions.Type.HTTP, usesAzureHttpProxyHandler);

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient).configuration(configuration)
                .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .verifyErrorMatches(throwable -> throwable == EXPECTED_EXCEPTION);
    }

    private static Stream<Arguments> buildWithEnvConfigurationProxySupplier() {
        Supplier<TestConfigurationSource> baseJavaProxyConfigurationSupplier
            = () -> new TestConfigurationSource().put(JAVA_HTTP_PROXY_HOST, "localhost")
                .put(JAVA_HTTP_PROXY_PORT, "12345");

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Arguments.of(true, false,
            new ConfigurationBuilder(EMPTY_SOURCE, baseJavaProxyConfigurationSupplier.get(), EMPTY_SOURCE).build(),
            DEFAULT_URL));

        Configuration simpleEnvProxy = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")).build();
        arguments.add(Arguments.of(true, false, simpleEnvProxy, DEFAULT_URL));

        /*
         * HTTP proxy with authentication configured.
         */
        TestConfigurationSource javaProxyWithAuthentication = baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1");
        arguments.add(Arguments.of(true, true,
            new ConfigurationBuilder(EMPTY_SOURCE, javaProxyWithAuthentication, EMPTY_SOURCE).build(), DEFAULT_URL));

        Configuration envProxyWithAuthentication = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")).build();
        arguments.add(Arguments.of(true, true, envProxyWithAuthentication, DEFAULT_URL));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrlsWithoutProxying = new String[] {
            "http://localhost",
            "http://127.0.0.1",
            "http://azure.microsoft.com",
            "http://careers.linkedin.com" };

        String[] requestUrlsWithProxying
            = new String[] { "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8" };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<TestConfigurationSource> javaNonProxyHostsSupplier
            = () -> baseJavaProxyConfigurationSupplier.get().put(JAVA_NON_PROXY_HOSTS, rawJavaNonProxyHosts);
        Supplier<TestConfigurationSource> envNonProxyHostsSupplier
            = () -> new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
                .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");

        List<Supplier<TestConfigurationSource>> nonProxyHostsSuppliers
            = Arrays.asList(javaNonProxyHostsSupplier, envNonProxyHostsSupplier);

        for (Supplier<TestConfigurationSource> configurationSupplier : nonProxyHostsSuppliers) {
            for (String requestUrl : requestUrlsWithoutProxying) {
                arguments.add(Arguments.of(false, false,
                    new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(),
                    requestUrl));
            }

            for (String requestUrl : requestUrlsWithProxying) {
                arguments.add(Arguments.of(true, false,
                    new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(),
                    requestUrl));
            }
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<TestConfigurationSource> authenticatedJavaNonProxyHostsSupplier
            = () -> javaNonProxyHostsSupplier.get().put(JAVA_HTTP_PROXY_USER, "1").put(JAVA_HTTP_PROXY_PASSWORD, "1");
        Supplier<TestConfigurationSource> authenticatedEnvNonProxyHostsSupplier
            = () -> new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
                .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");

        List<Supplier<TestConfigurationSource>> authenticatedNonProxyHostsSuppliers
            = Arrays.asList(authenticatedJavaNonProxyHostsSupplier, authenticatedEnvNonProxyHostsSupplier);

        for (Supplier<TestConfigurationSource> configurationSupplier : authenticatedNonProxyHostsSuppliers) {
            for (String requestUrl : requestUrlsWithoutProxying) {
                arguments.add(Arguments.of(false, true,
                    new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(),
                    requestUrl));
            }

            for (String requestUrl : requestUrlsWithProxying) {
                arguments.add(Arguments.of(true, true,
                    new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(),
                    requestUrl));
            }
        }

        return arguments.stream();
    }

    private static Stream<Arguments> buildWithExplicitConfigurationProxySupplier() {
        Supplier<ConfigurationBuilder> baseHttpProxy
            = () -> new ConfigurationBuilder().putProperty("http.proxy.hostname", "localhost")
                .putProperty("http.proxy.port", "12345");

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Arguments.of(true, false, baseHttpProxy.get().build(), DEFAULT_URL));

        /*
         * HTTP proxy with authentication configured.
         */
        Configuration httpProxyWithAuthentication = baseHttpProxy.get()
            .putProperty("http.proxy.username", "1")
            .putProperty("http.proxy.password", "1")
            .build();

        arguments.add(Arguments.of(true, true, httpProxyWithAuthentication, DEFAULT_URL));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrlsWithoutProxying = new String[] {
            "http://localhost",
            "http://127.0.0.1",
            "http://azure.microsoft.com",
            "http://careers.linkedin.com" };

        String[] requestUrlsWithProxying
            = new String[] { "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8" };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<ConfigurationBuilder> javaNonProxyHostsSupplier
            = () -> baseHttpProxy.get().putProperty("http.proxy.non-proxy-hosts", rawJavaNonProxyHosts);
        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, false, javaNonProxyHostsSupplier.get().build(), requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(Arguments.of(true, false, javaNonProxyHostsSupplier.get().build(), requestUrl));
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<ConfigurationBuilder> authenticatedJavaNonProxyHostsSupplier = () -> javaNonProxyHostsSupplier.get()
            .putProperty("http.proxy.username", "1")
            .putProperty("http.proxy.password", "1");

        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, true, authenticatedJavaNonProxyHostsSupplier.get().build(), requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(Arguments.of(true, true, authenticatedJavaNonProxyHostsSupplier.get().build(), requestUrl));
        }

        return arguments.stream();
    }

    private static HttpClient nettyHttpClientWithProxyValidation(boolean shouldHaveProxy, ProxyOptions.Type proxyType,
        boolean isAuthenticated) {
        return HttpClient.create()
            .doOnChannelInit((connectionObserver, channel, socketAddress) -> channel.pipeline()
                .addFirst("TestProxyHandler", new TestProxyValidator(shouldHaveProxy, proxyType, isAuthenticated)));
    }

    private static final class TestProxyValidator extends ChannelDuplexHandler {
        private final boolean shouldHaveProxy;
        private final ProxyOptions.Type proxyType;
        private final boolean usesAzureHttpProxyHandler;

        private TestProxyValidator(boolean shouldHaveProxy, ProxyOptions.Type proxyType,
            boolean usesAzureHttpProxyHandler) {
            this.shouldHaveProxy = shouldHaveProxy;
            this.proxyType = proxyType;
            this.usesAzureHttpProxyHandler = usesAzureHttpProxyHandler;
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) {
            ProxyHandler proxyHandler = ctx.pipeline().get(ProxyHandler.class);
            Throwable throwable = EXPECTED_EXCEPTION;

            try {
                if (shouldHaveProxy) {
                    assertNotNull(proxyHandler);

                    switch (proxyType) {
                        case HTTP:
                            if (usesAzureHttpProxyHandler) {
                                assertTrue(
                                    proxyHandler instanceof com.azure.core.http.netty.implementation.HttpProxyHandler);
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

                } else {
                    assertNull(proxyHandler);
                }
            } catch (Throwable throwable1) {
                throwable = throwable1;
            }

            promise.setFailure(throwable);
            ctx.fireExceptionCaught(throwable);
        }
    }

    /**
     * Tests when {@code wiretap} is set to {@code true} the Netty pipeline will have a {@link LoggingHandler} added.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void buildWiretappedClient() {
        HttpClient validatorClient = HttpClient.create()
            .doAfterResponseSuccess(
                (response, connection) -> assertNotNull(connection.channel().pipeline().get(LoggingHandler.class)));

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient).wiretap(true).build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, DEFAULT_URL)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests when a {@code port} is set that any path-only request will be sent to the port specified.
     */
    @Test
    public void buildPortClient() {
        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder().port(12345).build();

        SocketAddress remoteAddress = nettyClient.nettyClient.configuration().remoteAddress().get();
        InetSocketAddress socketAddress = assertInstanceOf(InetSocketAddress.class, remoteAddress);
        assertEquals(12345, socketAddress.getPort());
    }

    /**
     * Tests that a custom {@link io.netty.channel.EventLoopGroup} is properly applied to the Netty client to handle
     * sending and receiving requests and responses.
     */
    @Test
    public void buildEventLoopClient() {
        String expectedThreadName = "testEventLoop";
        HttpClient validatorClient = HttpClient.create().doAfterResponseSuccess((response, connection) -> {
            // Validate that the EventLoop being used is a NioEventLoop.
            NioEventLoop eventLoop = (NioEventLoop) connection.channel().eventLoop();
            assertNotNull(eventLoop);

            assertEquals(expectedThreadName, eventLoop.threadProperties().name());
        });

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1, (Runnable r) -> new Thread(r, expectedThreadName));

        NettyAsyncHttpClient nettyClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(validatorClient).eventLoopGroup(eventLoopGroup)
                .build();

        StepVerifier.create(nettyClient.send(new HttpRequest(HttpMethod.GET, DEFAULT_URL)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getTimeoutMillisSupplier")
    public void getTimeoutMillis(Duration timeout, long expected) {
        assertEquals(expected, getTimeout(timeout, Duration.ofMillis(60000)).toMillis());
    }

    private static Stream<Arguments> getTimeoutMillisSupplier() {
        return Stream.of(Arguments.of(null, TimeUnit.SECONDS.toMillis(60)), Arguments.of(Duration.ofSeconds(0), 0),
            Arguments.of(Duration.ofSeconds(-1), 0),
            Arguments.of(Duration.ofSeconds(120), TimeUnit.SECONDS.toMillis(120)),
            Arguments.of(Duration.ofNanos(1), TimeUnit.MILLISECONDS.toMillis(1)));
    }

    @Test
    @SuppressWarnings("deprecation") // maxChunkSize is deprecated in a future version of Reactor Netty
    public void preconfiguredHttpResponseDecoderIsMaintained() {
        HttpClient nettyHttpClient = HttpClient.create()
            .httpResponseDecoder(
                httpResponseDecoderSpec -> httpResponseDecoderSpec.maxChunkSize(64 * 1024).validateHeaders(true));

        NettyAsyncHttpClient azureHttpClient
            = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder(nettyHttpClient).build();

        HttpResponseDecoderSpec spec = azureHttpClient.nettyClient.configuration().decoder();

        // Header validation is explicitly set to false.
        assertFalse(spec.validateHeaders());
        assertEquals(64 * 1024, spec.maxChunkSize());
    }
}
