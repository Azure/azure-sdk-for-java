// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * Handler that deals with application protocol negotiation (ALPN) and configures the {@link ChannelPipeline} to use
 * either HTTP/1.1 or HTTP/2 based on the result of negotiation.
 */
public final class Netty4AlpnHandler extends ApplicationProtocolNegotiationHandler {
    /**
     * Creates a new instance of {@link Netty4AlpnHandler} with a fallback to using HTTP/1.1.
     */
    public Netty4AlpnHandler() {
        super(ApplicationProtocolNames.HTTP_1_1);
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            FlushConsolidationHandler flushConsolidationHandler = new FlushConsolidationHandler(1024, true);
            Http2FrameCodec http2FrameCodec = Http2FrameCodecBuilder.forClient().validateHeaders(true).build();
            Http2MultiplexHandler http2MultiplexHandler = new Http2MultiplexHandler(NoOpHandler.INSTANCE);
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_2_FLUSH, flushConsolidationHandler);
            pipeline.addAfter(Netty4HandlerNames.HTTP_2_FLUSH, Netty4HandlerNames.HTTP_2_CODEC, http2FrameCodec);
            pipeline.addAfter(Netty4HandlerNames.HTTP_2_CODEC, Netty4HandlerNames.HTTP_2_MULTIPLEX,
                http2MultiplexHandler);

            if (channel.pipeline().get(Netty4HandlerNames.HTTP_1_1_CODEC) != null) {
                sendHttp11Request(request, channel, addProgressAndTimeoutHandler, errorReference)
                    .addListener((ChannelFutureListener) sendListener -> {
                        if (!sendListener.isSuccess()) {
                            setOrSuppressError(errorReference, sendListener.cause());
                            sendListener.channel().close();
                            latch.countDown();
                        } else {
                            sendListener.channel().read();
                        }
                    });
            } else if (channel.pipeline().get(Netty4HandlerNames.HTTP_2_CODEC) != null) {
                sendHttp2Request(request, channel, addProgressAndTimeoutHandler, errorReference)
                    .addListener((ChannelFutureListener) sendListener -> {
                        if (!sendListener.isSuccess()) {
                            setOrSuppressError(errorReference, sendListener.cause());
                            sendListener.channel().close();
                            latch.countDown();
                        } else {
                            sendListener.channel().read();
                        }
                    });
            }
        } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline().addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_1_1_CODEC, createCodec());
        } else {
            throw new IllegalStateException("unknown protocol: " + protocol);
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
