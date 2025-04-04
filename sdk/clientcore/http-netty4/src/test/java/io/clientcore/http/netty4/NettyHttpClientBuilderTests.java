// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.DEFAULT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link NettyHttpClientBuilder}.
 */
@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class NettyHttpClientBuilderTests {

    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static final String SERVER_HTTP_URI = NettyHttpClientLocalTestServer.getServer().getHttpUri();
    private static final String DEFAULT_URL = SERVER_HTTP_URI + DEFAULT_PATH;

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @MethodSource("buildWithProxySupplier")
    public void buildWithProxy(ProxyOptions proxyOptions) {
        NettyHttpClient nettyClient = (NettyHttpClient) new NettyHttpClientBuilder().proxy(proxyOptions).build();

        if (proxyOptions != null) {
            assertNotNull(nettyClient.getProxyOptions());
            assertEquals(proxyOptions.getType(), nettyClient.getProxyOptions().getType());
            assertEquals(proxyOptions.getAddress(), nettyClient.getProxyOptions().getAddress());
            assertEquals(proxyOptions.getUsername(), nettyClient.getProxyOptions().getUsername());
            assertEquals(proxyOptions.getPassword(), nettyClient.getProxyOptions().getPassword());
            assertEquals(proxyOptions.getNonProxyHosts(), nettyClient.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(nettyClient.getProxyOptions());
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
        NettyHttpClient nettyClient
            = (NettyHttpClient) new NettyHttpClientBuilder().configuration(configuration).build();
        ProxyOptions expected = ProxyOptions.fromConfiguration(configuration);

        if (expected != null) {
            assertNotNull(nettyClient.getProxyOptions());
            assertEquals(expected.getType(), nettyClient.getProxyOptions().getType());

            InetSocketAddress actualAddress = nettyClient.getProxyOptions().getAddress();
            if (actualAddress.isUnresolved()) {
                actualAddress = new InetSocketAddress(actualAddress.getHostName(), actualAddress.getPort());
            }
            assertEquals(expected.getAddress(), actualAddress);

            assertEquals(expected.getUsername(), nettyClient.getProxyOptions().getUsername());
            assertEquals(expected.getPassword(), nettyClient.getProxyOptions().getPassword());
            assertEquals(expected.getNonProxyHosts(), nettyClient.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(nettyClient.getProxyOptions());
        }
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithExplicitConfigurationProxy(Configuration configuration) {
        NettyHttpClient nettyClient
            = (NettyHttpClient) new NettyHttpClientBuilder().configuration(configuration).build();
        ProxyOptions expected = ProxyOptions.fromConfiguration(configuration);

        if (expected != null) {
            assertNotNull(nettyClient.getProxyOptions());
            assertEquals(expected.getType(), nettyClient.getProxyOptions().getType());

            InetSocketAddress actualAddress = nettyClient.getProxyOptions().getAddress();
            if (actualAddress.isUnresolved()) {
                actualAddress = new InetSocketAddress(actualAddress.getHostName(), actualAddress.getPort());
            }
            assertEquals(expected.getAddress(), actualAddress);

            assertEquals(expected.getUsername(), nettyClient.getProxyOptions().getUsername());
            assertEquals(expected.getPassword(), nettyClient.getProxyOptions().getPassword());
            assertEquals(expected.getNonProxyHosts(), nettyClient.getProxyOptions().getNonProxyHosts());
        } else {
            assertNull(nettyClient.getProxyOptions());
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
}
