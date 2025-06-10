// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;

/**
 * {@link ChannelInboundHandlerAdapter} that configures the Netty pipeline based on the negotiated application-level
 * protocol after the SSL handshake is complete.
 * <p>
 * This handler checks the negotiated protocol and configures the pipeline for either HTTP/2 or HTTP/1.1 accordingly.
 */
public final class Netty4H2OrHttp11Handler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
        if (sslHandler != null) {
            String protocol = sslHandler.applicationProtocol();
            if (protocol == null) {
                protocol = ApplicationProtocolNames.HTTP_1_1;
            }
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                FlushConsolidationHandler flushConsolidationHandler = new FlushConsolidationHandler(1024, true);
                Http2FrameCodec http2FrameCodec = Http2FrameCodecBuilder.forClient().validateHeaders(true).build();
                Http2MultiplexHandler http2MultiplexHandler = new Http2MultiplexHandler(NoOpHandler.INSTANCE);
                ChannelPipeline pipeline = ctx.channel().pipeline();
                pipeline.addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_2_FLUSH, flushConsolidationHandler);
                pipeline.addAfter(Netty4HandlerNames.HTTP_2_FLUSH, Netty4HandlerNames.HTTP_2_CODEC, http2FrameCodec);
                pipeline.addAfter(Netty4HandlerNames.HTTP_2_CODEC, Netty4HandlerNames.HTTP_2_MULTIPLEX,
                    http2MultiplexHandler);
            } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                ctx.channel()
                    .pipeline()
                    .addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_1_1_CODEC, Netty4Utility.createCodec());
            } else {
                throw new IllegalStateException("unknown protocol: " + protocol);
            }

            ctx.fireChannelActive();
            ctx.channel().pipeline().remove(this);
        } else {
            throw new IllegalStateException("Cannot determine negotiated application-level protocol.");
        }
    }

    private static final class NoOpHandler extends ChannelHandlerAdapter {
        private static final NoOpHandler INSTANCE = new NoOpHandler();

        @Override
        public boolean isSharable() {
            return true;
        }
    }
}
