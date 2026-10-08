/*
 * Copyright (c) 2020-2026 VMware, Inc. or its affiliates, All Rights Reserved.
 * Modifications Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LogLevel;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.http.Http2SettingsSpec;
import reactor.netty.http.client.HttpResponseDecoderSpec;
import reactor.util.annotation.Nullable;

/**
 * TLS/H2 pipeline construction extracted from Reactor Netty 1.2.18 {@code HttpClientConfig}.
 * Source: https://github.com/reactor/reactor-netty/blob/c753da48febe3ea28b19bb1cf64ba3b377f1a820/reactor-netty-http/src/main/java/reactor/netty/http/client/HttpClientConfig.java
 *
 * Modifications: relocated/renamed class; settings supplied as an argument instead of a field;
 * Cosmos codec builder and the extracted traffic handler substituted at the two marked sites.
 * Keep the upstream method structure for dependency-upgrade comparisons.
 * HTTP/1.1, H2C, TLS negotiation, pooling and outbound stream initialization remain in Reactor.
 */
final class ReactorNettyHttp2Config {
    private ReactorNettyHttp2Config() {
    }

    static Http2Settings http2Settings(@Nullable Http2SettingsSpec http2Settings) {
        Http2Settings settings = Http2Settings.defaultSettings();

        if (http2Settings != null) {
            Long headerTableSize = http2Settings.headerTableSize();
            if (headerTableSize != null) {
                settings.headerTableSize(headerTableSize);
            }

            Integer initialWindowSize = http2Settings.initialWindowSize();
            if (initialWindowSize != null) {
                settings.initialWindowSize(initialWindowSize);
            }

            Long maxConcurrentStreams = http2Settings.maxConcurrentStreams();
            if (maxConcurrentStreams != null) {
                settings.maxConcurrentStreams(maxConcurrentStreams);
            }

            Integer maxFrameSize = http2Settings.maxFrameSize();
            if (maxFrameSize != null) {
                settings.maxFrameSize(maxFrameSize);
            }

            settings.maxHeaderListSize(http2Settings.maxHeaderListSize());

            Boolean pushEnabled = http2Settings.pushEnabled();
            if (pushEnabled != null) {
                settings.pushEnabled(pushEnabled);
            }
        }

        return settings;
    }

    static void configureHttp2Pipeline(ChannelPipeline p, HttpResponseDecoderSpec decoder,
        Http2Settings http2Settings, @Nullable Http2SettingsSpec http2SettingsSpec, ConnectionObserver observer) {
        // Cosmos substitution: upstream uses Http2FrameCodecBuilder.forClient().
        Http2FrameCodecBuilder http2FrameCodecBuilder =
            new CosmosHttp2FrameCodecBuilder()
                .validateHeaders(decoder.validateHeaders())
                .initialSettings(http2Settings);

        if (p.get(NettyPipeline.LoggingHandler) != null) {
            http2FrameCodecBuilder.frameLogger(new Http2FrameLogger(LogLevel.DEBUG,
                "reactor.netty.http.client.h2"));
        }

        if (http2SettingsSpec != null) {
            if (http2SettingsSpec.maxDecodedRstFramesPerWindow() != null
                && http2SettingsSpec.maxDecodedRstFramesSecondsPerWindow() != null) {
                http2FrameCodecBuilder.decoderEnforceMaxRstFramesPerWindow(http2SettingsSpec.maxDecodedRstFramesPerWindow(),
                    http2SettingsSpec.maxDecodedRstFramesSecondsPerWindow());
            }

            if (http2SettingsSpec.maxEncodedRstFramesPerWindow() != null
                && http2SettingsSpec.maxEncodedRstFramesSecondsPerWindow() != null) {
                http2FrameCodecBuilder.encoderEnforceMaxRstFramesPerWindow(http2SettingsSpec.maxEncodedRstFramesPerWindow(),
                    http2SettingsSpec.maxEncodedRstFramesSecondsPerWindow());
            }
        }

        p.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2Flush, new FlushConsolidationHandler(1024, true))
            .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpCodec, http2FrameCodecBuilder.build())
            .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2MultiplexHandler,
                new Http2MultiplexHandler(H2InboundStreamHandler.INSTANCE))
            // Cosmos substitution: upstream's package-private HttpTrafficHandler is extracted separately.
            .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpTrafficHandler,
                new ReactorNettyHttpTrafficHandler(observer));
    }

    /**
     * Handle inbound streams (server pushes).
     * This feature is not supported and disabled.
     */
    static final class H2InboundStreamHandler implements ChannelHandler {
        static final ChannelHandler INSTANCE = new H2InboundStreamHandler();

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
        }

        @Override
        @SuppressWarnings("deprecation")
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }
    }
}
