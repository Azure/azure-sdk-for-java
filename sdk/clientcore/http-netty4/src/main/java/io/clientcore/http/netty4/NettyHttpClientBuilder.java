// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.utils.configuration.Configuration;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.time.Duration;

/**
 * Builder for creating instances of NettyHttpClient.
 */
public class NettyHttpClientBuilder {
    private EventLoopGroup eventLoopGroup;
    private Class<? extends Channel> channelClass;
    private SslContext sslContext;

    private Configuration configuration;
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
     * Sets the {@link SslContext} that will be used to configure SSL/TLS when establishing secure connections.
     * <p>
     * If this is left unset a default {@link SslContext} will be used to establish secure connections.
     *
     * @param sslContext The {@link SslContext} for SSL/TLS.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder sslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}.
     *
     * @param configuration The configuration store.
     * @return The updated builder.
     */
    public NettyHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
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
        Bootstrap bootstrap = new Bootstrap().group(group)
            .channel(channelClass)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) getTimeoutMillis(connectTimeout, 10_000));

        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions
            = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;

        return new NettyHttpClient(bootstrap, sslContext, buildProxyOptions, getTimeoutMillis(readTimeout),
            getTimeoutMillis(responseTimeout), getTimeoutMillis(writeTimeout));
    }

    static long getTimeoutMillis(Duration duration) {
        return getTimeoutMillis(duration, 60_000);
    }

    static long getTimeoutMillis(Duration duration, long defaultTimeout) {
        if (duration == null) {
            return defaultTimeout;
        }

        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }

        return Math.max(1, duration.toMillis());
    }
}
