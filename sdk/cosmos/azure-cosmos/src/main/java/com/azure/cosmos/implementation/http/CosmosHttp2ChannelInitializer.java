// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.http.Http2SettingsSpec;
import reactor.netty.http.client.HttpResponseDecoderSpec;

/**
 * Installs the attributed Reactor TLS/H2 copy before the stock codec can send its preface.
 * Protocol selection outside TLS/H2 stays with Reactor; this is not a general-purpose initializer.
 */
final class CosmosHttp2ChannelInitializer extends ChannelInboundHandlerAdapter {
    static final String HANDLER_NAME = "cosmosHttp2ChannelInitializer";
    private final ConnectionObserver observer;
    private final Http2Settings settings;
    private final Http2SettingsSpec settingsSpec;
    private final HttpResponseDecoderSpec decoder;

    private CosmosHttp2ChannelInitializer(ConnectionObserver observer,
                                         reactor.netty.http.client.HttpClientConfig config) {
        this.observer = observer;
        this.settingsSpec = config.http2SettingsSpec();
        this.decoder = config.decoder();
        this.settings = ReactorNettyHttp2Config.http2Settings(settingsSpec);
    }

    static void install(Channel channel, ConnectionObserver observer, reactor.netty.http.client.HttpClientConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        // Reactor strips TLS/H2 for http:// URIs, including the opt-in plaintext emulator.
        if (pipeline.get(NettyPipeline.SslHandler) == null
            && pipeline.get(NettyPipeline.HttpCodec) instanceof HttpClientCodec) {
            return;
        }
        if (pipeline.get(NettyPipeline.H2OrHttp11Codec) == null) {
            throw new IllegalStateException("Expected Reactor's deferred H2/HTTP1.1 initializer");
        }
        pipeline.addBefore(NettyPipeline.H2OrHttp11Codec, HANDLER_NAME,
            new CosmosHttp2ChannelInitializer(observer, config));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SslHandler ssl = ctx.pipeline().get(SslHandler.class);
        if (ssl == null) {
            throw new IllegalStateException("Expected TLS before Cosmos HTTP/2 initialization");
        }
        if (ApplicationProtocolNames.HTTP_2.equals(ssl.applicationProtocol())) {
            ChannelPipeline pipeline = ctx.pipeline();
            ReactorNettyHttp2Config.configureHttp2Pipeline(pipeline, decoder, settings, settingsSpec, observer);
            pipeline.remove(NettyPipeline.H2OrHttp11Codec);
            // This pre-existing Cosmos filter is not part of the copied Reactor initialization.
            pipeline.addAfter(NettyPipeline.HttpCodec, Http2SettingsHandler.HANDLER_NAME, Http2SettingsHandler.INSTANCE);
        }
        super.channelActive(ctx);
        ctx.pipeline().remove(this);
    }
}
