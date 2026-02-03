// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.ProxyOptions;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ChannelInitializationProxyHandler}.
 */
public class ChannelInitializationProxyHandlerTests {
    @Test
    public void socks4ProxyCanBeCreated() {
        ChannelInitializationProxyHandler handler = new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.SOCKS4, new InetSocketAddress("localhost", 1080)));

        assertInstanceOf(Socks4ProxyHandler.class, handler.createProxy(null));
    }

    @Test
    public void socks5ProxyCanBeCreated() {
        ChannelInitializationProxyHandler handler = new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.SOCKS5, new InetSocketAddress("localhost", 1080)));

        assertInstanceOf(Socks5ProxyHandler.class, handler.createProxy(null));
    }

    @Test
    public void httpProxyCanBeCreated() {
        ChannelInitializationProxyHandler handler = new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8080)));

        assertInstanceOf(HttpProxyHandler.class, handler.createProxy(null));
    }

    @Test
    public void httpProxyWithAuthenticationCanBeCreated() {
        ChannelInitializationProxyHandler handler = new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8080))
                .setCredentials("username", "password"));

        assertInstanceOf(Netty4HttpProxyHandler.class, handler.createProxy(null));
    }

    @Test
    public void nullProxyOptionsShouldNotProxy() {
        assertFalse(new ChannelInitializationProxyHandler(null).test(null));
    }

    @Test
    public void nullProxyAddressShouldNotProxy() {
        assertFalse(new ChannelInitializationProxyHandler(new ProxyOptions(null, null)).test(null));
    }

    @Test
    public void noNonProxyHostsShouldAlwaysProxy() {
        assertTrue(new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8080))).test(null));
    }

    @Test
    public void socketAddressNotInetSocketAddressShouldNotProxy() {
        assertFalse(new ChannelInitializationProxyHandler(
            new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8080))
                .setNonProxyHosts("localhost")).test(new EmbeddedChannel().localAddress()));
    }
}
