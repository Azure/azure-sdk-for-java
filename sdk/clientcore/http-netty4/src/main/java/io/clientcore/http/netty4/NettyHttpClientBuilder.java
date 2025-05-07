// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.concurrent.ThreadFactory;

/**
 * Builder for creating instances of NettyHttpClient.
 */
public class NettyHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClientBuilder.class);

    private static final String EPOLL = "io.netty.channel.epoll.Epoll";
    private static final String EPOLL_CHANNEL = "io.netty.channel.epoll.EpollSocketChannel";
    private static final String EPOLL_EVENT_LOOP_GROUP = "io.netty.channel.epoll.EpollEventLoopGroup";
    private static final boolean IS_EPOLL_AVAILABLE;
    private static final Class<? extends Channel> EPOLL_CHANNEL_CLASS;
    private static final Class<?> EPOLL_EVENT_LOOP_GROUP_CLASS;
    private static final MethodHandle EPOLL_EVENT_LOOP_GROUP_CREATOR;

    private static final String KQUEUE = "io.netty.channel.kqueue.KQueue";
    private static final String KQUEUE_CHANNEL = "io.netty.channel.kqueue.KQueueSocketChannel";
    private static final String KQUEUE_EVENT_LOOP_GROUP = "io.netty.channel.kqueue.KQueueEventLoopGroup";
    private static final boolean IS_KQUEUE_AVAILABLE;
    private static final Class<? extends Channel> KQUEUE_CHANNEL_CLASS;
    private static final Class<?> KQUEUE_EVENT_LOOP_GROUP_CLASS;
    private static final MethodHandle KQUEUE_EVENT_LOOP_GROUP_CREATOR;

    static {
        // Inspect the class path to determine is native transports are available.
        // If they are, this will determine runtime behaviors.
        boolean isEpollAvailable;
        Class<? extends Channel> epollChannelClass;
        Class<?> epollEventLoopGroupClass;
        MethodHandle epollEventLoopGroupCreator;
        try {
            Class<?> epollClass = Class.forName(EPOLL);
            isEpollAvailable = (boolean) epollClass.getDeclaredMethod("isAvailable").invoke(null);

            epollChannelClass = getChannelClass(EPOLL_CHANNEL);
            epollEventLoopGroupClass = Class.forName(EPOLL_EVENT_LOOP_GROUP);
            epollEventLoopGroupCreator = MethodHandles.publicLookup()
                .unreflectConstructor(epollEventLoopGroupClass.getDeclaredConstructor(ThreadFactory.class));
        } catch (ReflectiveOperationException ignored) {
            LOGGER.atVerbose().log("Epoll is unavailable and won't be used.");
            isEpollAvailable = false;
            epollChannelClass = null;
            epollEventLoopGroupClass = null;
            epollEventLoopGroupCreator = null;
        }

        IS_EPOLL_AVAILABLE = isEpollAvailable;
        EPOLL_CHANNEL_CLASS = epollChannelClass;
        EPOLL_EVENT_LOOP_GROUP_CLASS = epollEventLoopGroupClass;
        EPOLL_EVENT_LOOP_GROUP_CREATOR = epollEventLoopGroupCreator;

        boolean isKqueueAvailable;
        Class<? extends Channel> kqueueChannelClass;
        Class<?> kqueueEventLoopGroupClass;
        MethodHandle kqueueEventLoopGroupCreator;
        try {
            Class<?> kqueueClass = Class.forName(KQUEUE);
            isKqueueAvailable = (boolean) kqueueClass.getDeclaredMethod("isAvailable").invoke(null);

            kqueueChannelClass = getChannelClass(KQUEUE_CHANNEL);
            kqueueEventLoopGroupClass = Class.forName(KQUEUE_EVENT_LOOP_GROUP);
            kqueueEventLoopGroupCreator = MethodHandles.publicLookup()
                .unreflectConstructor(kqueueEventLoopGroupClass.getDeclaredConstructor(ThreadFactory.class));
        } catch (ReflectiveOperationException ignored) {
            LOGGER.atVerbose().log("Epoll is unavailable and won't be used.");
            isKqueueAvailable = false;
            kqueueChannelClass = null;
            kqueueEventLoopGroupClass = null;
            kqueueEventLoopGroupCreator = null;
        }

        IS_KQUEUE_AVAILABLE = isKqueueAvailable;
        KQUEUE_CHANNEL_CLASS = kqueueChannelClass;
        KQUEUE_EVENT_LOOP_GROUP_CLASS = kqueueEventLoopGroupClass;
        KQUEUE_EVENT_LOOP_GROUP_CREATOR = kqueueEventLoopGroupCreator;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Channel> getChannelClass(String className) throws ClassNotFoundException {
        return (Class<? extends Channel>) Class.forName(className);
    }

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
     * <p>
     * By default, if no {@code eventLoopGroup} is configured and no native transports are available (Epoll KQueue)
     * {@link NioEventLoopGroup} will be used.
     * <p>
     * If native transports are available, the {@link EventLoopGroup} implementation for the native transport will be
     * chosen over {@link NioEventLoopGroup}.
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
     * <p>
     * By default, if no {@code channelClass} is configured and no native transports are available (Epoll, KQueue)
     * {@link NioSocketChannel} will be used.
     * <p>
     * If native transports are available, the {@link Channel} implementation for the native transport will be chosen
     * over {@link NioSocketChannel}.
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
        EventLoopGroup group = getEventLoopGroupToUse();
        Class<? extends Channel> channelClass = getChannelClass(group);

        // Leave breadcrumbs about the NettyHttpClient configuration, in case troubleshooting is needed.
        LOGGER.atVerbose()
            .addKeyValue("customEventLoopGroup", eventLoopGroup != null)
            .addKeyValue("eventLoopGroupClass", group.getClass())
            .addKeyValue("customChannelClass", this.channelClass != null)
            .addKeyValue("channelClass", channelClass)
            .log("NettyHttpClient was built with these configurations.");

        Bootstrap bootstrap = new Bootstrap().group(group)
            .channel(channelClass)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) getTimeoutMillis(connectTimeout, 10_000));
        // Disable auto-read as we want to control when and how data is read from the channel.
        bootstrap.option(ChannelOption.AUTO_READ, false);

        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions
            = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;

        return new NettyHttpClient(bootstrap, sslContext, new ChannelInitializationProxyHandler(buildProxyOptions),
            getTimeoutMillis(readTimeout), getTimeoutMillis(responseTimeout), getTimeoutMillis(writeTimeout));
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

    // For testing.
    ProxyOptions getProxyOptions() {
        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        return (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;
    }

    private EventLoopGroup getEventLoopGroupToUse() {
        if (this.eventLoopGroup != null) {
            return this.eventLoopGroup;
        }

        ThreadFactory threadFactory = new DefaultThreadFactory("clientcore-netty-client");
        if (IS_EPOLL_AVAILABLE) {
            try {
                return (EventLoopGroup) EPOLL_EVENT_LOOP_GROUP_CREATOR.invokeExact(threadFactory);
            } catch (Throwable ignored) {
            }
        }

        if (IS_KQUEUE_AVAILABLE) {
            try {
                return (EventLoopGroup) KQUEUE_EVENT_LOOP_GROUP_CREATOR.invokeExact(threadFactory);
            } catch (Throwable ignored) {
            }
        }

        // Fallback to NioEventLoopGroup.
        return new NioEventLoopGroup(threadFactory);
    }

    private Class<? extends Channel> getChannelClass(EventLoopGroup eventLoopGroup) {
        if (this.channelClass != null) {
            // If the Channel class was manually set, use it.
            return this.channelClass;
        } else if (IS_EPOLL_AVAILABLE && eventLoopGroup.getClass() == EPOLL_EVENT_LOOP_GROUP_CLASS) {
            // If Epoll is available and the EventLoopGroup is EpollEventLoopGroup, use EpollSocketChannel.
            return EPOLL_CHANNEL_CLASS;
        } else if (IS_KQUEUE_AVAILABLE && eventLoopGroup.getClass() == KQUEUE_EVENT_LOOP_GROUP_CLASS) {
            // If KQueue is available and the EventLoopGroup is KQueueEventLoopGroup, use KQueueSocketChannel.
            return KQUEUE_CHANNEL_CLASS;
        } else {
            // Fallback to NioSocketChannel.
            return NioSocketChannel.class;
        }
    }
}
