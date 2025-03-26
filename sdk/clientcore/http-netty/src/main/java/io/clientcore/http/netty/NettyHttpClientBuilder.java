// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.time.Duration;

/**
 * Builder for creating instances of NettyHttpClient.
 */
public class NettyHttpClientBuilder {
    private EventLoopGroup eventLoopGroup;
    private Class<? extends Channel> channelClass;

    private ProxyOptions proxyOptions;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration responseTimeout;
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
     * Sets the {@link Channel} type that will be used to create channels of that type.
     *
     * @param channelClass The {@link Channel} class to use.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder channelClass(Class<? extends Channel> channelClass) {
        this.channelClass = channelClass;
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
     * Sets the response timeout.
     *
     * @param responseTimeout The response timeout.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
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
        Class<? extends Channel> channelClass = this.channelClass == null ? NioSocketChannel.class : this.channelClass;
        Bootstrap bootstrap = new Bootstrap().group(group).channel(channelClass);

        if (connectTimeout != null) {
            bootstrap.option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());
        }

        return new NettyHttpClient(bootstrap, proxyOptions, getTimeoutMillis(readTimeout),
            getTimeoutMillis(responseTimeout), getTimeoutMillis(writeTimeout));
    }

    private static long getTimeoutMillis(Duration duration) {
        if (duration == null) {
            return 60_000;
        }

        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }

        return Math.max(1, duration.toMillis());
    }
}
