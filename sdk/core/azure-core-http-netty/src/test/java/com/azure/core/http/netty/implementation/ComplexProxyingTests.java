// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set of tests for complex proxying scenarios.
 */
public class ComplexProxyingTests {
    /**
     * Tests the scenario of an HttpClient with an authenticated proxy where the request will be sent through the proxy.
     */
    @Test
    public void authenticatedProxyWithProxiedHost() {
        shouldHaveProxyScenario(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
            .setCredentials("username", "password")
            .setNonProxyHosts("azure.com"));
    }

    /**
     * Tests the scenario of an HttpClient with an authenticated proxy where the request won't be sent through the
     * proxy.
     */
    @Test
    public void authenticatedProxyWithNonProxiedHost() {
        shouldNotHaveProxyScenario(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
            .setCredentials("username", "password")
            .setNonProxyHosts("azure.com"));
    }

    /**
     * Tests the scenario of an HttpClient without proxy authentication where the request will be sent through the
     * proxy.
     */
    @Test
    public void unauthenticatedProxyWithProxiedHost() {
        shouldHaveProxyScenario(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
            .setNonProxyHosts("azure.com"));
    }

    /**
     * Tests the scenario of an HttpClient without proxy authentication where the request won't be sent through the
     * proxy.
     */
    @Test
    public void unauthenticatedProxyWithNonProxiedHost() {
        shouldNotHaveProxyScenario(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
            .setNonProxyHosts("azure.com"));
    }

    /**
     * Tests the scenario of an HttpClient without a proxy. This is mostly a litmus test to provide further validation.
     */
    @Test
    public void noProxy() {
        shouldNotHaveProxyScenario(null);
    }

    private static void shouldHaveProxyScenario(ProxyOptions proxyOptions) {
        reactor.netty.http.client.HttpClient reactorHttpClient = reactor.netty.http.client.HttpClient.create()
            .doOnChannelInit((connectionObserver, channel, remoteAddress) -> channel.pipeline()
                .addLast(new ProxyCheckChannelHandler(address -> {
                    InetSocketAddress socketAddress = assertInstanceOf(InetSocketAddress.class, address);
                    assertTrue(socketAddress.isUnresolved(), "Expected remote address to be unresolved but it was.");
                })));

        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorHttpClient).proxy(proxyOptions).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com");
        Throwable ex = assertThrows(Throwable.class, () -> httpClient.sendSync(request, Context.NONE).close());
        while (ex != null) {
            assertFalse(ex instanceof AssertionError);
            ex = ex.getCause();
        }
    }

    private static void shouldNotHaveProxyScenario(ProxyOptions proxyOptions) {
        reactor.netty.http.client.HttpClient reactorHttpClient = reactor.netty.http.client.HttpClient.create()
            .doOnChannelInit((connectionObserver, channel, remoteAddress) -> channel.pipeline()
                .addLast(new ProxyCheckChannelHandler(address -> {
                    InetSocketAddress socketAddress = assertInstanceOf(InetSocketAddress.class, address);
                    assertFalse(socketAddress.isUnresolved(), "Expected remote address to be resolved but it wasn't.");
                })));

        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorHttpClient).proxy(proxyOptions).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://azure.com");
        try (HttpResponse response = httpClient.sendSync(request, Context.NONE)) {
            assertNotNull(response);
        } catch (Throwable ex) {
            while (ex != null) {
                assertFalse(ex instanceof AssertionError);
                ex = ex.getCause();
            }
        }
    }

    private static final class ProxyCheckChannelHandler extends ChannelOutboundHandlerAdapter {
        private final Consumer<SocketAddress> remoteAddressConsumer;

        private ProxyCheckChannelHandler(Consumer<SocketAddress> remoteAddressConsumer) {
            this.remoteAddressConsumer = remoteAddressConsumer;
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {
            remoteAddressConsumer.accept(remoteAddress);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }
    }
}
