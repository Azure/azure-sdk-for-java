// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.ChannelOperations;
import reactor.netty.http.Http2SettingsSpec;

import java.io.IOException;

final class CosmosHttp2ChannelInitializer extends ChannelInboundHandlerAdapter {
    static final String HANDLER_NAME = "cosmosHttp2ChannelInitializer";
    private final ConnectionObserver observer;
    private final Http2Settings settings;
    private final Http2SettingsSpec settingsSpec;

    private CosmosHttp2ChannelInitializer(ConnectionObserver observer, Http2SettingsSpec settingsSpec) {
        this.observer = observer;
        this.settingsSpec = settingsSpec;
        this.settings = Http2Settings.defaultSettings();
        if (settingsSpec != null) {
            if (settingsSpec.headerTableSize() != null) {
                settings.headerTableSize(settingsSpec.headerTableSize());
            }
            if (settingsSpec.initialWindowSize() != null) {
                settings.initialWindowSize(settingsSpec.initialWindowSize());
            }
            if (settingsSpec.maxConcurrentStreams() != null) {
                settings.maxConcurrentStreams(settingsSpec.maxConcurrentStreams());
            }
            if (settingsSpec.maxFrameSize() != null) {
                settings.maxFrameSize(settingsSpec.maxFrameSize());
            }
            settings.maxHeaderListSize(settingsSpec.maxHeaderListSize());
            if (settingsSpec.pushEnabled() != null) {
                settings.pushEnabled(settingsSpec.pushEnabled());
            }
        }
    }

    static void install(Channel channel, ConnectionObserver observer, Http2SettingsSpec settingsSpec) {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(NettyPipeline.H2OrHttp11Codec) == null) {
            throw new IllegalStateException("Expected Reactor's deferred H2/HTTP1.1 initializer");
        }
        pipeline.addBefore(NettyPipeline.H2OrHttp11Codec, HANDLER_NAME,
            new CosmosHttp2ChannelInitializer(observer, settingsSpec));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SslHandler ssl = ctx.pipeline().get(SslHandler.class);
        if (ssl == null) {
            throw new IllegalStateException("Expected TLS before Cosmos HTTP/2 initialization");
        }
        if (ApplicationProtocolNames.HTTP_2.equals(ssl.applicationProtocol())) {
            ChannelPipeline pipeline = ctx.pipeline();
            CosmosHttp2FrameCodecBuilder builder = new CosmosHttp2FrameCodecBuilder();
            builder.initialSettings(settings);
            if (settingsSpec != null) {
                if (settingsSpec.maxDecodedRstFramesPerWindow() != null
                    && settingsSpec.maxDecodedRstFramesSecondsPerWindow() != null) {
                    builder.decoderEnforceMaxRstFramesPerWindow(settingsSpec.maxDecodedRstFramesPerWindow(),
                        settingsSpec.maxDecodedRstFramesSecondsPerWindow());
                }
                if (settingsSpec.maxEncodedRstFramesPerWindow() != null
                    && settingsSpec.maxEncodedRstFramesSecondsPerWindow() != null) {
                    builder.encoderEnforceMaxRstFramesPerWindow(settingsSpec.maxEncodedRstFramesPerWindow(),
                        settingsSpec.maxEncodedRstFramesSecondsPerWindow());
                }
            }
            if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
                builder.frameLogger(new Http2FrameLogger(LogLevel.DEBUG, "reactor.netty.http.client.h2"));
            }
            pipeline.remove(NettyPipeline.H2OrHttp11Codec);
            pipeline.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2Flush,
                new FlushConsolidationHandler(1024, true));
            pipeline.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpCodec, builder.build());
            pipeline.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2MultiplexHandler,
                new Http2MultiplexHandler(InboundStreamHandler.INSTANCE));
            pipeline.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpTrafficHandler,
                new InitialSettingsHandler(observer));
        }
        ctx.fireChannelActive();
        ctx.pipeline().remove(this);
    }

    @ChannelHandler.Sharable
    private static final class InboundStreamHandler extends ChannelInboundHandlerAdapter {
        private static final InboundStreamHandler INSTANCE = new InboundStreamHandler();
    }

    private static final class InitialSettingsHandler extends ChannelInboundHandlerAdapter {
        // Reactor uses this channel attribute when creating H2 stream operations.
        private static final AttributeKey<Long> ENABLE_CONNECT_PROTOCOL = AttributeKey.valueOf("$ENABLE_CONNECT_PROTOCOL");
        private final ConnectionObserver observer;

        private InitialSettingsHandler(ConnectionObserver observer) {
            this.observer = observer;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            notifyState(ctx.channel(), ConnectionObserver.State.CONNECTED);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof Http2SettingsFrame) {
                ctx.channel().attr(ENABLE_CONNECT_PROTOCOL).set(
                    ((Http2SettingsFrame) msg).settings().get(Http2CodecUtil.SETTINGS_ENABLE_CONNECT_PROTOCOL));
                notifyState(ctx.channel(), ConnectionObserver.State.CONFIGURED);
                ctx.pipeline().remove(NettyPipeline.ReactiveBridge);
                ctx.pipeline().remove(this);
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.fireExceptionCaught(new IOException("Connection closed before receiving initial HTTP/2 SETTINGS"));
        }

        private void notifyState(Channel channel, ConnectionObserver.State state) {
            Connection connection = Connection.from(channel);
            ChannelOperations<?, ?> operations = connection.as(ChannelOperations.class);
            observer.onStateChange(operations == null ? connection : operations, state);
        }
    }
}
