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
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
import java.util.concurrent.ThreadFactory;
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
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1, (Runnable r) -> new Thread(r, expectedThreadName));

        NettyHttpClient nettyClient
            = (NettyHttpClient) new NettyHttpClientBuilder().eventLoopGroup(eventLoopGroup).build();

        assertSame(eventLoopGroup, nettyClient.getBootstrap().config().group());
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
    @EnabledOnOs(OS.WINDOWS)
    public void windowsUseNioByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertInstanceOf(NioEventLoopGroup.class, config.group());
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    public void macUsesKQueueByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertInstanceOf(KQueueEventLoopGroup.class, config.group());
        assertInstanceOf(KQueueSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    public void macUsesNioIfConfigured() {
        NettyHttpClient nettyHttpClient
            = (NettyHttpClient) new NettyHttpClientBuilder().channelClass(NioSocketChannel.class)
                .eventLoopGroup(new NioEventLoopGroup())
                .build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertInstanceOf(NioEventLoopGroup.class, config.group());
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void linuxUsesEpollByDefault() {
        NettyHttpClient nettyHttpClient = (NettyHttpClient) new NettyHttpClientBuilder().build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertInstanceOf(EpollEventLoopGroup.class, config.group());
        assertInstanceOf(EpollSocketChannel.class, config.channelFactory().newChannel());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void linuxUsesNioIfConfigured() {
        NettyHttpClient nettyHttpClient
            = (NettyHttpClient) new NettyHttpClientBuilder().channelClass(NioSocketChannel.class)
                .eventLoopGroup(new NioEventLoopGroup())
                .build();

        BootstrapConfig config = nettyHttpClient.getBootstrap().config();
        assertInstanceOf(NioEventLoopGroup.class, config.group());
        assertInstanceOf(NioSocketChannel.class, config.channelFactory().newChannel());
    }

    @ParameterizedTest
    @MethodSource("getEventLoopGroupToUseSupplier")
    public void getEventLoopGroupToUse(Class<?> expected, EventLoopGroup configuredGroup,
        Class<? extends SocketChannel> configuredChannelClass, boolean isEpollAvailable,
        MethodHandle epollEventLoopGroupCreator, boolean isKqueueAvailable, MethodHandle kqueueEventLoopGroupCreator) {
        EventLoopGroup eventLoopGroup
            = NettyHttpClientBuilder.getEventLoopGroupToUse(configuredGroup, configuredChannelClass, isEpollAvailable,
                epollEventLoopGroupCreator, isKqueueAvailable, kqueueEventLoopGroupCreator);

        assertInstanceOf(expected, eventLoopGroup);
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

    private static Stream<Arguments> getEventLoopGroupToUseSupplier() throws ReflectiveOperationException {
        // Doesn't matter what this is calling, just needs to throw an exception.
        // This will as it doesn't accept the arguments that it will be called with.
        MethodHandle exceptionCreator
            = MethodHandles.publicLookup().unreflectConstructor(NioEventLoopGroup.class.getDeclaredConstructor());

        // NOTE: This test doesn't use EpollEventLoopGroup or KQueueEventLoopGroup directly, but rather uses different
        // EventLoopGroup classes as the creation of those requires native libraries to be loaded.
        // This is a workaround to avoid loading the native libraries in the test, as not all OSes can support the
        // native transports.
        MethodHandle epollCreator = MethodHandles.publicLookup()
            .unreflectConstructor(MockEpollEventLoopGroup.class.getDeclaredConstructor(ThreadFactory.class));
        MethodHandle kqueueCreator = MethodHandles.publicLookup()
            .unreflectConstructor(MockKQueueEventLoopGroup.class.getDeclaredConstructor(ThreadFactory.class));

        // EventLoopGroup is configured, use it.
        Arguments configuredGroup
            = Arguments.of(NioEventLoopGroup.class, new NioEventLoopGroup(), null, false, null, false, null);

        // Epoll is available and nothing is configured, use EpollEventLoopGroup.
        Arguments epollGroup = Arguments.of(MockEpollEventLoopGroup.class, null, null, true, epollCreator, false, null);

        // Epoll is available and EpollSocketChannel is configured, use EpollEventLoopGroup.
        Arguments epollChannelGroup = Arguments.of(MockEpollEventLoopGroup.class, null, EpollSocketChannel.class, true,
            epollCreator, false, null);

        // Epoll is available but throws an exception, use NioEventLoopGroup.
        Arguments epollExceptionGroup
            = Arguments.of(NioEventLoopGroup.class, null, null, true, exceptionCreator, false, null);

        // KQueue is available and nothing is configured, use KQueueEventLoopGroup.
        Arguments kqueueGroup
            = Arguments.of(MockKQueueEventLoopGroup.class, null, null, false, null, true, kqueueCreator);

        // KQueue is available and KQueueSocketChannel is configured, use KQueueEventLoopGroup.
        Arguments kqueueChannelGroup = Arguments.of(MockKQueueEventLoopGroup.class, null, KQueueSocketChannel.class,
            false, null, true, kqueueCreator);

        // KQueue is available but throws an exception, use NioEventLoopGroup.
        Arguments kqueueExceptionGroup
            = Arguments.of(NioEventLoopGroup.class, null, null, false, null, true, exceptionCreator);

        // Both Epoll and KQueue are available, use EpollEventLoopGroup.
        Arguments epollAndKqueueGroup
            = Arguments.of(MockEpollEventLoopGroup.class, null, null, true, epollCreator, true, kqueueCreator);

        // Both Epoll and KQueue are available but channel class is set to KQueueSocketChannel, use
        // KQueueEventLoopGroup.
        Arguments epollAndKqueueChannelGroup = Arguments.of(MockKQueueEventLoopGroup.class, null,
            KQueueSocketChannel.class, true, epollCreator, true, kqueueCreator);

        // Both Epoll and KQueue are available but throws an exception, use NioEventLoopGroup.
        Arguments epollAndKqueueExceptionGroup
            = Arguments.of(NioEventLoopGroup.class, null, null, true, exceptionCreator, true, exceptionCreator);

        // Both Epoll and KQueue are available but channel class is set to EpollSocketChannel, use
        // EpollEventLoopGroup.
        Arguments epollAndKqueueChannelExceptionGroup = Arguments.of(MockEpollEventLoopGroup.class, null,
            EpollSocketChannel.class, true, epollCreator, true, kqueueCreator);

        // Both Epoll and KQueue are available but channel class is set to NioSocketChannel, use
        // NioEventLoopGroup.
        Arguments epollAndKqueueChannelNioGroup = Arguments.of(NioEventLoopGroup.class, null, NioSocketChannel.class,
            true, epollCreator, true, kqueueCreator);

        return Stream.of(configuredGroup, epollGroup, epollChannelGroup, epollExceptionGroup, kqueueGroup,
            kqueueChannelGroup, kqueueExceptionGroup, epollAndKqueueGroup, epollAndKqueueChannelGroup,
            epollAndKqueueExceptionGroup, epollAndKqueueChannelExceptionGroup, epollAndKqueueChannelNioGroup);
    }

    public static final class MockEpollEventLoopGroup extends NioEventLoopGroup {
        public MockEpollEventLoopGroup(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }

    public static final class MockKQueueEventLoopGroup extends NioEventLoopGroup {
        public MockKQueueEventLoopGroup(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }

    @ParameterizedTest
    @MethodSource("getChannelClassSupplier")
    public void getChannelClass(Class<?> expected, Class<? extends SocketChannel> configuredChannelClass,
        Class<? extends EventLoopGroup> congiguredGroupClass, boolean isEpollAvailable, boolean isKqueueAvailable) {
        Class<? extends Channel> channelClass = NettyHttpClientBuilder.getChannelClass(configuredChannelClass,
            congiguredGroupClass, isEpollAvailable, isKqueueAvailable);

        assertEquals(expected, channelClass);
    }

    private static Stream<Arguments> getChannelClassSupplier() {
        // Channel class is configured, use it.
        Arguments configuredChannel = Arguments.of(NioSocketChannel.class, NioSocketChannel.class, null, false, false);

        // Epoll is available and EventLoopGroup is EpollEventLoopGroup, use EpollSocketChannel.
        Arguments epollChannel = Arguments.of(EpollSocketChannel.class, null, EpollEventLoopGroup.class, true, false);

        // KQueue is available and EventLoopGroup is KQueueEventLoopGroup, use KQueueSocketChannel.
        Arguments kqueueChannel
            = Arguments.of(KQueueSocketChannel.class, null, KQueueEventLoopGroup.class, false, true);

        // Epoll is available and EventLoopGroup is NioEventLoopGroup, use NioSocketChannel.
        Arguments epollNioChannel = Arguments.of(NioSocketChannel.class, null, NioEventLoopGroup.class, true, false);

        // KQueue is available and EventLoopGroup is NioEventLoopGroup, use NioSocketChannel.
        Arguments kqueueNioChannel = Arguments.of(NioSocketChannel.class, null, NioEventLoopGroup.class, false, true);

        // Both Epoll and KQueue are available and EventLoopGroup is NioEventLoopGroup, use NioSocketChannel.
        Arguments epollAndKqueueNioChannel
            = Arguments.of(NioSocketChannel.class, null, NioEventLoopGroup.class, true, true);

        // Both Epoll and KQueue are available and EventLoopGroup is EpollEventLoopGroup, use EpollSocketChannel.
        Arguments epollAndKqueueEpollChannel
            = Arguments.of(EpollSocketChannel.class, null, EpollEventLoopGroup.class, true, true);

        // Both Epoll and KQueue are available and EventLoopGroup is KQueueEventLoopGroup, use KQueueSocketChannel.
        Arguments epollAndKqueueKqueueChannel
            = Arguments.of(KQueueSocketChannel.class, null, KQueueEventLoopGroup.class, true, true);

        return Stream.of(configuredChannel, epollChannel, kqueueChannel, epollNioChannel, kqueueNioChannel,
            epollAndKqueueNioChannel, epollAndKqueueEpollChannel, epollAndKqueueKqueueChannel);
    }
}
