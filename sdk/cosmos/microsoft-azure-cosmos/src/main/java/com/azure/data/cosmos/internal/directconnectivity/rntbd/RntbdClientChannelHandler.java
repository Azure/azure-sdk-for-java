// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class RntbdClientChannelHandler extends ChannelInitializer<Channel> implements ChannelPoolHandler {

    private static Logger logger = LoggerFactory.getLogger(RntbdClientChannelHandler.class);
    private final RntbdEndpoint.Config config;

    RntbdClientChannelHandler(final RntbdEndpoint.Config config) {
        checkNotNull(config, "config");
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
     *     (ReadTimeoutHandler#0 = io.netty.handler.timeout.ReadTimeoutHandler),
     *     (SslHandler#0 = io.netty.handler.ssl.SslHandler),
     *     (RntbdContextNegotiator#0 = com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdContextNegotiator),
     *     (RntbdResponseDecoder#0 = com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdResponseDecoder),
     *     (RntbdRequestEncoder#0 = com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdRequestEncoder),
     *     (WriteTimeoutHandler#0 = io.netty.handler.timeout.WriteTimeoutHandler),
     *     (RntbdRequestManager#0 = com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdRequestManager),
     * }
     * }</pre>
     *
     * @param channel a channel that was just registered with an event loop
     */
    @Override
    protected void initChannel(final Channel channel) {

        checkNotNull(channel);

        final RntbdRequestManager requestManager = new RntbdRequestManager(this.config.getMaxRequestsPerChannel());
        final long readerIdleTime = this.config.getReceiveHangDetectionTime();
        final long writerIdleTime = this.config.getSendHangDetectionTime();
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addFirst(
            new RntbdContextNegotiator(requestManager, this.config.getUserAgent()),
            new RntbdResponseDecoder(),
            new RntbdRequestEncoder(),
            new WriteTimeoutHandler(writerIdleTime, TimeUnit.NANOSECONDS),
            requestManager
        );

        if (this.config.getWireLogLevel() != null) {
            pipeline.addFirst(new LoggingHandler(this.config.getWireLogLevel()));
        }

        final SSLEngine sslEngine = this.config.getSslContext().newEngine(channel.alloc());

        pipeline.addFirst(
            new ReadTimeoutHandler(readerIdleTime, TimeUnit.NANOSECONDS),
            new SslHandler(sslEngine)
        );
    }
}
