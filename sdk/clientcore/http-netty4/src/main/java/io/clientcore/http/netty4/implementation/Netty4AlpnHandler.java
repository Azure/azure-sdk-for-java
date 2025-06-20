// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpRequest;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp11Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp2Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * Handler that deals with application protocol negotiation (ALPN) and configures the {@link ChannelPipeline} to use
 * either HTTP/1.1 or HTTP/2 based on the result of negotiation.
 */
public final class Netty4AlpnHandler extends ApplicationProtocolNegotiationHandler {
    private final HttpRequest request;
    private final AtomicReference<ResponseStateInfo> responseReference;
    private final AtomicReference<Throwable> errorReference;
    private final CountDownLatch latch;

    /**
     * Creates a new instance of {@link Netty4AlpnHandler} with a fallback to using HTTP/1.1.
     *
     * @param request The request to send once ALPN negotiation completes.
     * @param errorReference An AtomicReference keeping track of errors during the request lifecycle.
     * @param latch A CountDownLatch that will be released once the request completes.
     */
    public Netty4AlpnHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.request = request;
        this.responseReference = responseReference;
        this.errorReference = errorReference;
        this.latch = latch;
    }

    @Override
    public boolean isSharable() {
        return false;
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
            pipeline.addAfter(Netty4HandlerNames.HTTP_2_CODEC, Netty4HandlerNames.HTTP_2_RESPONSE,
                new Netty4Http2ResponseHandler(request, responseReference, errorReference, latch));

            new Http2StreamChannelBootstrap(ctx.channel()).open()
                .addListener((GenericFutureListener<? extends Future<Http2StreamChannel>>) future -> {
                    Http2StreamChannel streamChannel = future.get();
                    if (!future.isSuccess()) {
                        setOrSuppressError(errorReference, future.cause());
                        streamChannel.close();
                        latch.countDown();
                        return;
                    }

                    sendHttp2Request(request, streamChannel, errorReference)
                        .addListener((ChannelFutureListener) sendListener -> {
                            if (!sendListener.isSuccess()) {
                                setOrSuppressError(errorReference, sendListener.cause());
                                sendListener.channel().close();
                                latch.countDown();
                            } else {
                                sendListener.channel().read();
                            }
                        });
                });

        } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            if (ctx.pipeline().get(Netty4HandlerNames.PROGRESS_AND_TIMEOUT) != null) {
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_1_1_RESPONSE,
                        new Netty4Http11ResponseHandler(request, responseReference, errorReference, latch));
                ctx.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_1_1_CODEC,
                        createCodec());
            } else {
                ctx.pipeline().addBefore(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_1_1_CODEC, createCodec());
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.HTTP_1_1_CODEC, Netty4HandlerNames.HTTP_1_1_RESPONSE,
                        new Netty4Http11ResponseHandler(request, responseReference, errorReference, latch));
            }

            sendHttp11Request(request, ctx.channel(), errorReference)
                .addListener((ChannelFutureListener) sendListener -> {
                    if (!sendListener.isSuccess()) {
                        setOrSuppressError(errorReference, sendListener.cause());
                        sendListener.channel().close();
                        latch.countDown();
                    } else {
                        sendListener.channel().read();
                    }
                });
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
