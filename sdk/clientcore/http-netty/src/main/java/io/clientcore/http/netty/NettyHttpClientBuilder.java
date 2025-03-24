// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Builder for creating instances of NettyHttpClient.
 */
public class NettyHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClientBuilder.class);

    ProxyOptions proxyOptions;

    private EventLoopGroup eventLoopGroup;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;

    /**
     * Creates a new instance of {@link NettyHttpClientBuilder}.
     */
    public NettyHttpClientBuilder() {
    }

    /**
     * Sets the event loop group for the Netty client.
     *
     * @param eventLoopGroup The event loop group.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder eventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout The connection timeout.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout The read timeout.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the write timeout.
     *
     * @param writeTimeout The write timeout.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the proxy options.
     *
     * @param proxyOptions The proxy options.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Builds the NettyHttpClient.
     *
     * @return A configured NettyHttpClient instance.
     */
    public HttpClient build() {
        EventLoopGroup group
            = eventLoopGroup != null ? eventLoopGroup : new NioEventLoopGroup(new DefaultThreadFactory("netty-client"));
        Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class);

        if (connectTimeout != null) {
            bootstrap.option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());
        }

        if (proxyOptions != null) {
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    if (proxyOptions.getAddress() != null) {
                        ch.pipeline()
                            .addFirst(new HttpProxyHandler(
                                new InetSocketAddress(proxyOptions.getAddress().getHostName(),
                                    proxyOptions.getAddress().getPort()),
                                proxyOptions.getUsername(), proxyOptions.getPassword()));
                    }
                }
            });
        }

        return new NettyHttpClient(); // Customize with additional bootstrap configurations if needed.
    }
}
