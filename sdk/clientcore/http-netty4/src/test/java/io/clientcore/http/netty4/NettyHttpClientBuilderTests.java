// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer;
import io.netty.bootstrap.BootstrapConfig;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.IoHandler;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.DEFAULT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link NettyHttpClientBuilder}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientBuilderTests {

    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static final String SERVER_HTTP_URI = NettyHttpClientLocalTestServer.getServer().getUri();
    private static final String DEFAULT_URL = SERVER_HTTP_URI + DEFAULT_PATH;

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @MethodSource("buildWithProxySupplier")
    public void buildWithProxy(ProxyOptions proxyOptions) {
        NettyHttpClientBuilder builder = new NettyHttpClientBuilder().proxy(proxyOptions);

        if (proxyOptions != null) {
            assertNotNull(builder.getProxyOptions());
            assertEquals(proxyOptions.getType(), builder.getProxyOptions().getType());
            assertEquals(proxyOptions.getAddress(), builder.getProxyOptions().getAddress());
            assertEquals(proxyOptions.getUsername(), builder.getProxyOptions().getUsername());
            assertEquals(proxyOptions.getPassword(), builder.getProxyOptions().getPassword());
            assertEquals(proxyOptions.getNonProxyHosts(), builder.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(builder.getProxyOptions());
        }
    }

    private static Stream<ProxyOptions> buildWithProxySupplier() {
        InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 12345);
        List<ProxyOptions> proxyOptionsList = new ArrayList<>();

        /*
         * Simple non-authenticated proxies without non-proxy hosts configured.
         */
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.SOCKS4, proxyAddress));
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.SOCKS5, proxyAddress));
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress));

        /*
         * HTTP proxy with authentication configured.
         */
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setCredentials("1", "1"));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts(rawNonProxyHosts));

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        proxyOptionsList.add(new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts(rawNonProxyHosts)
            .setCredentials("1", "1"));

        return proxyOptionsList.stream();
    }

    @Test
    public void buildWithConfigurationNone() throws IOException {
        NettyHttpClient nettyClient
            = (NettyHttpClient) new NettyHttpClientBuilder().configuration(Configuration.none()).build();

        try (Response<BinaryData> response
            = nettyClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(DEFAULT_URL))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("buildWithEnvConfigurationProxySupplier")
    public void buildWithEnvConfigurationProxy(Configuration configuration) {
        NettyHttpClientBuilder builder = new NettyHttpClientBuilder().configuration(configuration);
        ProxyOptions expected = ProxyOptions.fromConfiguration(configuration);

        if (expected != null) {
            assertNotNull(builder.getProxyOptions());
            assertEquals(expected.getType(), builder.getProxyOptions().getType());

            InetSocketAddress actualAddress = builder.getProxyOptions().getAddress();
            if (actualAddress.isUnresolved()) {
                actualAddress = new InetSocketAddress(actualAddress.getHostName(), actualAddress.getPort());
            }
            assertEquals(expected.getAddress(), actualAddress);

            assertEquals(expected.getUsername(), builder.getProxyOptions().getUsername());
            assertEquals(expected.getPassword(), builder.getProxyOptions().getPassword());
            assertEquals(expected.getNonProxyHosts(), builder.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(builder.getProxyOptions());
        }
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithExplicitConfigurationProxy(Configuration configuration) {
        NettyHttpClientBuilder builder = new NettyHttpClientBuilder().configuration(configuration);
        ProxyOptions expected = ProxyOptions.fromConfiguration(configuration);

        if (expected != null) {
            assertNotNull(builder.getProxyOptions());
            assertEquals(expected.getType(), builder.getProxyOptions().getType());

            InetSocketAddress actualAddress = builder.getProxyOptions().getAddress();
            if (actualAddress.isUnresolved()) {
                actualAddress = new InetSocketAddress(actualAddress.getHostName(), actualAddress.getPort());
            }
            assertEquals(expected.getAddress(), actualAddress);

            assertEquals(expected.getUsername(), builder.getProxyOptions().getUsername());
            assertEquals(expected.getPassword(), builder.getProxyOptions().getPassword());
            assertEquals(expected.getNonProxyHosts(), builder.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(builder.getProxyOptions());
        }
    }

    private static Stream<Configuration> buildWithEnvConfigurationProxySupplier() {
        Supplier<TestConfigurationSource> baseJavaProxyConfigurationSupplier
            = () -> new TestConfigurationSource().put(JAVA_HTTP_PROXY_HOST, "localhost")
                .put(JAVA_HTTP_PROXY_PORT, "12345");

        List<Configuration> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Configuration.from(baseJavaProxyConfigurationSupplier.get()));

        arguments.add(
            Configuration.from(new TestConfigurationSource().put(Configuration.HTTP_PROXY, "http://localhost:12345")
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")));

        /*
         * HTTP proxy with authentication configured.
         */
        arguments.add(Configuration.from(baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1")));

        arguments.add(
            Configuration.from(new TestConfigurationSource().put(Configuration.HTTP_PROXY, "http://1:1@localhost:12345")
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        arguments.add(Configuration
            .from(baseJavaProxyConfigurationSupplier.get().put(JAVA_NON_PROXY_HOSTS, rawJavaNonProxyHosts)));
        arguments.add(
            Configuration.from(new TestConfigurationSource().put(Configuration.HTTP_PROXY, "http://localhost:12345")
                .put(Configuration.NO_PROXY, rawEnvNonProxyHosts)
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")));

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        arguments.add(Configuration.from(baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_NON_PROXY_HOSTS, rawJavaNonProxyHosts)
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1")));
        arguments.add(
            Configuration.from(new TestConfigurationSource().put(Configuration.HTTP_PROXY, "http://1:1@localhost:12345")
                .put(Configuration.NO_PROXY, rawEnvNonProxyHosts)
                .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true")));

        return arguments.stream();
    }

    private static Stream<Configuration> buildWithExplicitConfigurationProxySupplier() {
        Supplier<TestConfigurationSource> baseHttpProxy
            = () -> new TestConfigurationSource().put("http.proxy.hostname", "localhost")
                .put("http.proxy.port", "12345");

        List<Configuration> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Configuration.from(baseHttpProxy.get()));

        /*
         * HTTP proxy with authentication configured.
         */
        arguments.add(
            Configuration.from(baseHttpProxy.get().put("http.proxy.username", "1").put("http.proxy.password", "1")));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        arguments.add(Configuration.from(baseHttpProxy.get().put("http.proxy.non-proxy-hosts", rawJavaNonProxyHosts)));

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        arguments.add(Configuration.from(baseHttpProxy.get()
            .put("http.proxy.non-proxy-hosts", rawJavaNonProxyHosts)
            .put("http.proxy.username", "1")
            .put("http.proxy.password", "1")));

        return arguments.stream();
    }

    /**
     * Tests that a custom {@link io.netty.channel.EventLoopGroup} is properly applied to the Netty client to handle
     * sending and receiving requests and responses.
     */
    @Test
    public void buildEventLoopClient() {
        String expectedThreadName = "testEventLoop";
        EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(1,
            (Runnable r) -> new Thread(r, expectedThreadName), NioIoHandler.newFactory());

        try {
            NettyHttpClient nettyClient
                = (NettyHttpClient) new NettyHttpClientBuilder().eventLoopGroup(eventLoopGroup).build();
            assertSame(eventLoopGroup, nettyClient.getBootstrap().config().group());
        } finally {
            eventLoopGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    @ParameterizedTest
    @MethodSource("getTimeoutMillisSupplier")
    public void getTimeoutMillis(Duration timeout, long expected) {
        assertEquals(expected, NettyHttpClientBuilder.getTimeoutMillis(timeout));
    }

    private static Stream<Arguments> getTimeoutMillisSupplier() {
        return Stream.of(Arguments.of(null, TimeUnit.SECONDS.toMillis(60)), Arguments.of(Duration.ofSeconds(0), 0),
            Arguments.of(Duration.ofSeconds(-1), 0),
            Arguments.of(Duration.ofSeconds(120), TimeUnit.SECONDS.toMillis(120)),
            Arguments.of(Duration.ofNanos(1), TimeUnit.MILLISECONDS.toMillis(1)));
    }

    @Test
    public void preservesAllocatorAndResolverDefaults() {
        NettyHttpClient client = (NettyHttpClient) new NettyHttpClientBuilder().build();
        try {
            ByteBufAllocator expectedAllocator = System.getProperty("io.netty.allocator.type") == null
                ? PooledByteBufAllocator.DEFAULT
                : ByteBufAllocator.DEFAULT;
            assertSame(expectedAllocator, client.getBootstrap().config().options().get(ChannelOption.ALLOCATOR));
            assertSame(DefaultAddressResolverGroup.INSTANCE, client.getBootstrap().config().resolver());
        } finally {
            client.getBootstrap().config().group().shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void windowsUseNioByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertTrue(assertInstanceOf(IoEventLoopGroup.class, config.group()).isIoType(NioIoHandler.class));
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    public void macUsesKQueueByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertTrue(assertInstanceOf(IoEventLoopGroup.class, config.group()).isIoType(KQueueIoHandler.class));
        assertInstanceOf(KQueueSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    public void macUsesNioIfConfigured() {
        NettyHttpClient nettyHttpClient
            = (NettyHttpClient) new NettyHttpClientBuilder().channelClass(NioSocketChannel.class)
                .eventLoopGroup(new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory()))
                .build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertTrue(assertInstanceOf(IoEventLoopGroup.class, config.group()).isIoType(NioIoHandler.class));
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void linuxUsesEpollByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertTrue(assertInstanceOf(IoEventLoopGroup.class, config.group()).isIoType(EpollIoHandler.class));
        assertInstanceOf(EpollSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void linuxUsesNioIfConfigured() {
        NettyHttpClient nettyHttpClient
            = (NettyHttpClient) new NettyHttpClientBuilder().channelClass(NioSocketChannel.class)
                .eventLoopGroup(new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory()))
                .build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertTrue(assertInstanceOf(IoEventLoopGroup.class, config.group()).isIoType(NioIoHandler.class));
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @ParameterizedTest
    @MethodSource("getEventLoopGroupToUseSupplier")
    public void getEventLoopGroupToUse(String expectedTransport, boolean configureGroup,
        Class<? extends SocketChannel> configuredChannelClass, boolean isEpollAvailable, boolean epollFails,
        boolean isKqueueAvailable, boolean kqueueFails) {
        IoHandlerFactory epollFactory = mockIoHandlerFactory();
        IoHandlerFactory kqueueFactory = mockIoHandlerFactory();
        MethodHandle exceptionCreator
            = MethodHandles.throwException(IoHandlerFactory.class, IllegalStateException.class)
                .bindTo(new IllegalStateException("Native transport unavailable"));
        MethodHandle epollCreator
            = epollFails ? exceptionCreator : MethodHandles.constant(IoHandlerFactory.class, epollFactory);
        MethodHandle kqueueCreator
            = kqueueFails ? exceptionCreator : MethodHandles.constant(IoHandlerFactory.class, kqueueFactory);
        EventLoopGroup configuredGroup
            = configureGroup ? new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()) : null;
        EventLoopGroup eventLoopGroup = NettyHttpClientBuilder.getEventLoopGroupToUse(configuredGroup,
            configuredChannelClass, isEpollAvailable, epollCreator, isKqueueAvailable, kqueueCreator);

        try {
            assertInstanceOf(MultiThreadIoEventLoopGroup.class, eventLoopGroup);
            if (configuredGroup != null) {
                assertSame(configuredGroup, eventLoopGroup);
            }
            if ("epoll".equals(expectedTransport)) {
                verify(epollFactory, atLeastOnce()).newHandler(any());
                verifyNoInteractions(kqueueFactory);
            } else if ("kqueue".equals(expectedTransport)) {
                verify(kqueueFactory, atLeastOnce()).newHandler(any());
                verifyNoInteractions(epollFactory);
            } else {
                verifyNoInteractions(epollFactory, kqueueFactory);
            }
            assertTrue(assertInstanceOf(IoEventLoopGroup.class, eventLoopGroup).isIoType(NioIoHandler.class));
            if (configuredGroup == null) {
                assertTrue(eventLoopGroup.next()
                    .submit(() -> Thread.currentThread().isDaemon())
                    .syncUninterruptibly()
                    .getNow());
                assertTrue(eventLoopGroup.next()
                    .submit(() -> Thread.currentThread().getName().startsWith("clientcore-netty-client"))
                    .syncUninterruptibly()
                    .getNow());
            }
        } finally {
            eventLoopGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    @Test
    public void buildNettyClientWithoutConnectionPool() throws NoSuchFieldException, IllegalAccessException {
        NettyHttpClient client = (NettyHttpClient) new NettyHttpClientBuilder().connectionPoolSize(0).build();

        Field connectionPoolField = NettyHttpClient.class.getDeclaredField("connectionPool");
        connectionPoolField.setAccessible(true);
        assertNull(connectionPoolField.get(client), "Connection pool should be null when pool size is 0.");
    }

    @Test
    public void testInvalidMaxPendingAcquires() {
        NettyHttpClientBuilder builder = new NettyHttpClientBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.maxPendingAcquires(0));
        assertThrows(IllegalArgumentException.class, () -> builder.maxPendingAcquires(-1));
    }

    @Test
    public void testMaximumHttpVersion() throws NoSuchFieldException, IllegalAccessException {
        NettyHttpClientBuilder builder = new NettyHttpClientBuilder();

        NettyHttpClient clientv1 = (NettyHttpClient) builder.maximumHttpVersion(HttpProtocolVersion.HTTP_1_1).build();
        Field httpVersionField = NettyHttpClient.class.getDeclaredField("maximumHttpVersion");
        httpVersionField.setAccessible(true);
        assertEquals(HttpProtocolVersion.HTTP_1_1, httpVersionField.get(clientv1));

        NettyHttpClient clientv2 = (NettyHttpClient) builder.maximumHttpVersion(null).build();
        assertEquals(HttpProtocolVersion.HTTP_2, httpVersionField.get(clientv2));
    }

    private static Stream<Arguments> getEventLoopGroupToUseSupplier() {
        return Stream.of(Arguments.of("nio", true, null, true, true, true, true),
            Arguments.of("epoll", false, null, true, false, false, false),
            Arguments.of("epoll", false, EpollSocketChannel.class, true, false, false, false),
            Arguments.of("nio", false, null, true, true, false, false),
            Arguments.of("kqueue", false, null, false, false, true, false),
            Arguments.of("kqueue", false, KQueueSocketChannel.class, false, false, true, false),
            Arguments.of("nio", false, null, false, false, true, true),
            Arguments.of("epoll", false, null, true, false, true, false),
            Arguments.of("kqueue", false, KQueueSocketChannel.class, true, false, true, false),
            Arguments.of("kqueue", false, null, true, true, true, false),
            Arguments.of("nio", false, null, true, true, true, true),
            Arguments.of("epoll", false, EpollSocketChannel.class, true, false, true, false),
            Arguments.of("nio", false, NioSocketChannel.class, true, true, true, true),
            Arguments.of("nio", false, null, false, false, false, false));
    }

    private static IoHandlerFactory mockIoHandlerFactory() {
        // Delegate to NIO so factory selection can be tested without loading native libraries.
        IoHandlerFactory delegate = NioIoHandler.newFactory();
        IoHandlerFactory factory = mock(IoHandlerFactory.class);
        when(factory.newHandler(any())).thenAnswer(invocation -> delegate.newHandler(invocation.getArgument(0)));
        return factory;
    }

    @ParameterizedTest
    @MethodSource("getChannelClassSupplier")
    public void getChannelClass(Class<?> expected, Class<? extends SocketChannel> configuredChannelClass,
        EventLoopGroup configuredGroup, boolean isEpollAvailable, boolean isKqueueAvailable) {
        Class<? extends Channel> channelClass = NettyHttpClientBuilder.getChannelClass(configuredChannelClass,
            configuredGroup, isEpollAvailable, isKqueueAvailable);

        assertEquals(expected, channelClass);
    }

    private static Stream<Arguments> getChannelClassSupplier() {
        IoEventLoopGroup nioGroup = mockIoEventLoopGroup(NioIoHandler.class);
        IoEventLoopGroup epollGroup = mockIoEventLoopGroup(EpollIoHandler.class);
        IoEventLoopGroup kqueueGroup = mockIoEventLoopGroup(KQueueIoHandler.class);

        return Stream.of(Arguments.of(NioSocketChannel.class, NioSocketChannel.class, epollGroup, true, true),
            Arguments.of(EpollSocketChannel.class, null, epollGroup, true, false),
            Arguments.of(KQueueSocketChannel.class, null, kqueueGroup, false, true),
            Arguments.of(NioSocketChannel.class, null, nioGroup, true, false),
            Arguments.of(NioSocketChannel.class, null, nioGroup, false, true),
            Arguments.of(NioSocketChannel.class, null, nioGroup, true, true),
            Arguments.of(EpollSocketChannel.class, null, epollGroup, true, true),
            Arguments.of(KQueueSocketChannel.class, null, kqueueGroup, true, true),
            Arguments.of(NioSocketChannel.class, null, mock(EventLoopGroup.class), true, true));
    }

    private static IoEventLoopGroup mockIoEventLoopGroup(Class<? extends IoHandler> ioHandlerClass) {
        IoEventLoopGroup group = mock(IoEventLoopGroup.class);
        when(group.isIoType(ioHandlerClass)).thenReturn(true);
        return group;
    }
}
