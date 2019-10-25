// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity.rntbd;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class RntbdClientChannelHandler extends ChannelInitializer<Channel> implements ChannelPoolHandler {

    private static final AttributeKey<RntbdRequestManager> REQUEST_MANAGER = AttributeKey.newInstance("requestManager");
    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelHandler.class);
    private final ChannelHealthChecker healthChecker;
    private final RntbdEndpoint.Config config;

    RntbdClientChannelHandler(final RntbdEndpoint.Config config, final ChannelHealthChecker healthChecker) {
        checkNotNull(healthChecker, "healthChecker");
        checkNotNull(config, "config");
        this.healthChecker = healthChecker;
        this.config = config;
    }

    /**
     * Called by {@link ChannelPool#acquire} after a {@link Channel} is acquired
     * <p>
     * This method is called within the {@link EventLoop} of the {@link Channel}.
     *
     * @param channel a channel that was just acquired
     */
    @Override
    public void channelAcquired(final Channel channel) {
        logger.trace("{} CHANNEL ACQUIRED", channel);
    }

    /**
     * Called by {@link ChannelPool#release} after a {@link Channel} is created
     * <p>
     * This method is called within the {@link EventLoop} of the {@link Channel}.
     *
     * @param channel a channel that was just created
     */
    @Override
    public void channelCreated(final Channel channel) {
        logger.trace("{} CHANNEL CREATED", channel);
        this.initChannel(channel);
    }

    /**
     * Called by {@link ChannelPool#release} after a {@link Channel} is released
     * <p>
     * This method is called within the {@link EventLoop} of the {@link Channel}.
     *
     * @param channel a channel that was just released
     */
    @Override
    public void channelReleased(final Channel channel) {
        logger.trace("{} CHANNEL RELEASED", channel);
    }

    /**
     * Called by @{ChannelPipeline} initializer after the current channel is registered to an event loop.
     * <p>
     * This method constructs this pipeline:
     * <pre>{@code
     * ChannelPipeline {
     *     (SslHandler#0 = io.netty.handler.ssl.SslHandler),
     *     (IdleTimeoutHandler#0 = io.netty.handler.timeout.IdleTimeoutHandler),
     *     (LoggingHandler#0 = io.netty.handler.logging.LoggingHandler),  // iff RntbdClientChannelHandler.config.wireLogLevel != null
     *     (RntbdContextNegotiator#0 = com.azure.cosmos.internal.directconnectivity.rntbd.RntbdContextNegotiator),
     *     (RntbdResponseDecoder#0 = com.azure.cosmos.internal.directconnectivity.rntbd.RntbdResponseDecoder),
     *     (RntbdRequestEncoder#0 = com.azure.cosmos.internal.directconnectivity.rntbd.RntbdRequestEncoder),
     *     (RntbdRequestManager#0 = com.azure.cosmos.internal.directconnectivity.rntbd.RntbdRequestManager),
     * }
     * }</pre>
     *
     * @param channel a channel that was just registered with an event loop
     */
    @Override
    protected void initChannel(final Channel channel) {

        checkNotNull(channel);

        final RntbdRequestManager requestManager = new RntbdRequestManager(this.healthChecker, this.config.maxRequestsPerChannel());
        final long readerIdleTime = this.config.receiveHangDetectionTime();
        final long writerIdleTime = this.config.sendHangDetectionTime();
        final long allIdleTime = this.config.idleConnectionTimeout();
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addFirst(
            new RntbdContextNegotiator(requestManager, this.config.userAgent()),
            new RntbdResponseDecoder(),
            new RntbdRequestEncoder(),
            requestManager
        );

        if (this.config.wireLogLevel() != null) {
            pipeline.addFirst(new LoggingHandler(this.config.wireLogLevel()));
        }

        pipeline.addFirst(
            this.config.sslContext().newHandler(channel.alloc()),
            new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.NANOSECONDS)
        );

        channel.attr(REQUEST_MANAGER).set(requestManager);
    }
}
